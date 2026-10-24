package com.policybind.rating;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.Channels;
import com.policybind.common.RateCommand;
import com.policybind.common.RatingEvent;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RatingWorker {

    private final RatingRepository ratings;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public RatingWorker(RatingRepository ratings, OutboxRepository outbox, ObjectMapper mapper, EntityManager em) {
        this.ratings = ratings;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.RATE)
    @Transactional
    public void onRate(RateCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":rate")) {
            return;
        }
        int premium;
        try {
            premium = Math.multiplyExact(command.rateCents(), command.units());
        } catch (ArithmeticException ex) {
            publish(command.quoteId(), 0, command.termDays(), "REJECTED");
            return;
        }
        if (command.units() <= 0 || command.rateCents() <= 0 || command.termDays() <= 0) {
            publish(command.quoteId(), 0, command.termDays(), "REJECTED");
            return;
        }
        RatingEntity row = new RatingEntity();
        row.setQuoteId(command.quoteId());
        row.setPremiumCents(premium);
        row.setTermDays(command.termDays());
        row.setStatus("RATED");
        ratings.save(row);
        publish(command.quoteId(), premium, command.termDays(), "RATED");
    }

    private void publish(String quoteId, int premiumCents, int termDays, String status) {
        OutboxWriter.kafka(outbox, mapper, Channels.RATINGS, quoteId,
                new RatingEvent(quoteId, premiumCents, termDays, status));
    }
}
