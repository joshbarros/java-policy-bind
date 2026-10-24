package com.policybind.sanction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.Channels;
import com.policybind.common.PolicyRules;
import com.policybind.common.SanctionEvent;
import com.policybind.common.ScreenCommand;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class SanctionWorker {

    private final ScreenRepository screens;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;
    private final TransactionTemplate tx;

    public SanctionWorker(ScreenRepository screens, OutboxRepository outbox, ObjectMapper mapper, EntityManager em,
            PlatformTransactionManager manager) {
        this.screens = screens;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
        this.tx = new TransactionTemplate(manager);
    }

    @RabbitListener(queues = Channels.SCREEN)
    public void onScreen(ScreenCommand command) {
        Boolean dead = tx.execute(status -> {
            if (!InboxClaims.claim(em, command.quoteId() + ":screen")) {
                return false;
            }
            if (PolicyRules.sanctioned(command.name())) {
                publish(command.quoteId(), "FAILED");
                return true;
            }
            ScreenEntity row = new ScreenEntity();
            row.setQuoteId(command.quoteId());
            row.setStatus("CLEARED");
            screens.save(row);
            publish(command.quoteId(), "CLEARED");
            return false;
        });
        if (Boolean.TRUE.equals(dead)) {
            throw new AmqpRejectAndDontRequeueException("name sanctioned");
        }
    }

    private void publish(String quoteId, String status) {
        OutboxWriter.kafka(outbox, mapper, Channels.SANCTIONS, quoteId, new SanctionEvent(quoteId, status));
    }
}
