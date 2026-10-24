package com.policybind.bind;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.BindCancelCommand;
import com.policybind.common.BindCommand;
import com.policybind.common.BindEvent;
import com.policybind.common.Channels;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BindWorker {

    private final PolicyRepository policies;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public BindWorker(PolicyRepository policies, OutboxRepository outbox, ObjectMapper mapper, EntityManager em) {
        this.policies = policies;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.ISSUE, concurrency = "4")
    @Transactional
    public void onIssue(BindCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":bind")) {
            return;
        }
        String riskKey = command.accountId() + ":" + command.productCode();
        int inserted = em.createNativeQuery("INSERT INTO risk_slots (risk_key, quote_id) VALUES (:risk, :id) ON CONFLICT DO NOTHING")
                .setParameter("risk", riskKey)
                .setParameter("id", command.quoteId())
                .executeUpdate();
        if (inserted != 1) {
            publish(command.quoteId(), "", "DUPLICATE");
            return;
        }
        PolicyEntity row = new PolicyEntity();
        row.setQuoteId(command.quoteId());
        row.setRiskKey(riskKey);
        row.setPolicyNumber("POL-" + command.quoteId());
        row.setStatus("BOUND");
        policies.save(row);
        publish(command.quoteId(), row.getPolicyNumber(), "BOUND");
    }

    @RabbitListener(queues = Channels.BIND_CANCEL)
    @Transactional
    public void onCancel(BindCancelCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":bind-cancel")) {
            return;
        }
        int updated = em.createNativeQuery("UPDATE policies SET status = 'CANCELLED' WHERE quote_id = :id AND status = 'BOUND'")
                .setParameter("id", command.quoteId())
                .executeUpdate();
        if (updated != 1) {
            throw new IllegalStateException("bind cancel missed " + command.quoteId());
        }
        publish(command.quoteId(), "", "CANCELLED");
    }

    private void publish(String quoteId, String policyNumber, String status) {
        OutboxWriter.kafka(outbox, mapper, Channels.BINDS, quoteId, new BindEvent(quoteId, policyNumber, status));
    }
}
