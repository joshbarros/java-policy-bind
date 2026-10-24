package com.policybind.commission;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.Channels;
import com.policybind.common.CommissionCommand;
import com.policybind.common.CommissionEvent;
import com.policybind.common.CommissionReverseCommand;
import com.policybind.common.PolicyRules;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CommissionWorker {

    private final CommissionRepository commissions;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public CommissionWorker(CommissionRepository commissions, OutboxRepository outbox, ObjectMapper mapper,
            EntityManager em) {
        this.commissions = commissions;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.ACCRUE)
    @Transactional
    public void onAccrue(CommissionCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":accrue")) {
            return;
        }
        int amount = PolicyRules.commission(command.premiumCents());
        CommissionEntity row = new CommissionEntity();
        row.setQuoteId(command.quoteId());
        row.setAmountCents(amount);
        row.setClawbackCents(0);
        row.setStatus("ACCRUED");
        commissions.save(row);
        publish(command.quoteId(), amount, "ACCRUED");
    }

    @RabbitListener(queues = Channels.REVERSE)
    @Transactional
    public void onReverse(CommissionReverseCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":reverse")) {
            return;
        }
        int updated = em.createNativeQuery("UPDATE commissions SET clawback_cents = :amt, status = 'REVERSED' WHERE quote_id = :id AND status = 'ACCRUED'")
                .setParameter("amt", command.clawbackCents())
                .setParameter("id", command.quoteId())
                .executeUpdate();
        if (updated != 1) {
            throw new IllegalStateException("commission reverse missed " + command.quoteId());
        }
        publish(command.quoteId(), command.clawbackCents(), "REVERSED");
    }

    private void publish(String quoteId, int amountCents, String status) {
        OutboxWriter.kafka(outbox, mapper, Channels.COMMISSIONS, quoteId, new CommissionEvent(quoteId, amountCents, status));
    }
}
