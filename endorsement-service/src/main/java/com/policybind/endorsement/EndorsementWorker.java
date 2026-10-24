package com.policybind.endorsement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.Channels;
import com.policybind.common.EndorseCommand;
import com.policybind.common.EndorsementEvent;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EndorsementWorker {

    private final EndorsementRepository endorsements;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public EndorsementWorker(EndorsementRepository endorsements, OutboxRepository outbox, ObjectMapper mapper,
            EntityManager em) {
        this.endorsements = endorsements;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.ENDORSE)
    @Transactional
    public void onEndorse(EndorseCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":endorse")) {
            return;
        }
        EndorsementEntity row = new EndorsementEntity();
        row.setQuoteId(command.quoteId());
        row.setPremiumCents(command.premiumCents());
        row.setReturnCents(command.returnCents());
        row.setElapsedDays(command.elapsedDays());
        row.setStatus("RECORDED");
        endorsements.save(row);
        OutboxWriter.kafka(outbox, mapper, Channels.ENDORSEMENTS, command.quoteId(),
                new EndorsementEvent(command.quoteId(), command.returnCents(), "RECORDED"));
    }
}
