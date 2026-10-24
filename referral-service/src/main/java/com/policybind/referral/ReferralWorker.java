package com.policybind.referral;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.Channels;
import com.policybind.common.DecideCommand;
import com.policybind.common.PolicyRules;
import com.policybind.common.ReferralEvent;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class ReferralWorker {

    private final DecisionRepository decisions;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;
    private final TransactionTemplate tx;

    public ReferralWorker(DecisionRepository decisions, OutboxRepository outbox, ObjectMapper mapper,
            EntityManager em, PlatformTransactionManager manager) {
        this.decisions = decisions;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
        this.tx = new TransactionTemplate(manager);
    }

    @RabbitListener(queues = Channels.DECIDE)
    public void onDecide(DecideCommand command) {
        Boolean dead = tx.execute(status -> {
            if (!InboxClaims.claim(em, command.quoteId() + ":decide")) {
                return false;
            }
            boolean denied = PolicyRules.denied(command.approverToken());
            boolean over = PolicyRules.overAuthority(command.premiumCents());
            String outcome;
            boolean referred = false;
            if (denied) {
                outcome = "DENIED";
            } else if (over && PolicyRules.authorityDecline(command.approverToken())) {
                outcome = "REJECTED";
            } else {
                outcome = "APPROVED";
                referred = over;
            }
            if (!denied) {
                DecisionEntity row = new DecisionEntity();
                row.setQuoteId(command.quoteId());
                row.setStatus(outcome);
                row.setReferred(referred);
                decisions.save(row);
            }
            OutboxWriter.kafka(outbox, mapper, Channels.REFERRALS, command.quoteId(),
                    new ReferralEvent(command.quoteId(), outcome, referred));
            return denied;
        });
        if (Boolean.TRUE.equals(dead)) {
            throw new AmqpRejectAndDontRequeueException("approver denied");
        }
    }
}
