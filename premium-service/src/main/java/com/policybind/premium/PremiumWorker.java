package com.policybind.premium;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.Channels;
import com.policybind.common.PolicyRules;
import com.policybind.common.PremiumCommand;
import com.policybind.common.PremiumEvent;
import com.policybind.common.PremiumReturnCommand;
import com.policybind.common.PremiumVoidCommand;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class PremiumWorker {

    private final PremiumRepository premiums;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;
    private final TransactionTemplate tx;

    public PremiumWorker(PremiumRepository premiums, OutboxRepository outbox, ObjectMapper mapper, EntityManager em,
            PlatformTransactionManager manager) {
        this.premiums = premiums;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
        this.tx = new TransactionTemplate(manager);
    }

    @RabbitListener(queues = Channels.POST)
    public void onPost(PremiumCommand command) {
        Boolean dead = tx.execute(status -> {
            if (!InboxClaims.claim(em, command.quoteId() + ":post")) {
                return false;
            }
            if (PolicyRules.premiumDeclined(command.premiumToken())) {
                publish(command.quoteId(), command.amountCents(), "FAILED");
                return true;
            }
            PremiumEntity row = new PremiumEntity();
            row.setQuoteId(command.quoteId());
            row.setAmountCents(command.amountCents());
            row.setReturnCents(0);
            row.setStatus("POSTED");
            premiums.save(row);
            publish(command.quoteId(), command.amountCents(), "POSTED");
            return false;
        });
        if (Boolean.TRUE.equals(dead)) {
            throw new AmqpRejectAndDontRequeueException("premium declined");
        }
    }

    @RabbitListener(queues = Channels.PREMIUM_RETURN)
    @Transactional
    public void onReturn(PremiumReturnCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":premium-return")) {
            return;
        }
        int updated = em.createNativeQuery("UPDATE premiums SET return_cents = :ret, status = 'RETURNED' WHERE quote_id = :id AND status = 'POSTED'")
                .setParameter("ret", command.returnCents())
                .setParameter("id", command.quoteId())
                .executeUpdate();
        if (updated != 1) {
            throw new IllegalStateException("premium return missed " + command.quoteId());
        }
        publish(command.quoteId(), command.returnCents(), "RETURNED");
    }

    @RabbitListener(queues = Channels.PREMIUM_VOID)
    @Transactional
    public void onVoid(PremiumVoidCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":premium-void")) {
            return;
        }
        int updated = em.createNativeQuery("UPDATE premiums SET status = 'VOID' WHERE quote_id = :id AND status = 'POSTED'")
                .setParameter("id", command.quoteId())
                .executeUpdate();
        if (updated != 1) {
            throw new IllegalStateException("premium void missed " + command.quoteId());
        }
        publish(command.quoteId(), 0, "VOID");
    }

    private void publish(String quoteId, int amountCents, String status) {
        OutboxWriter.kafka(outbox, mapper, Channels.PREMIUMS, quoteId, new PremiumEvent(quoteId, amountCents, status));
    }
}
