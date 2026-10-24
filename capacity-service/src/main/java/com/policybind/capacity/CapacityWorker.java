package com.policybind.capacity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.CapacityCommitCommand;
import com.policybind.common.CapacityEvent;
import com.policybind.common.CapacityHoldCommand;
import com.policybind.common.CapacityReleaseCommand;
import com.policybind.common.CapacityReturnCommand;
import com.policybind.common.Channels;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CapacityWorker {

    private final TreatyRepository treaties;
    private final HoldRepository holds;
    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public CapacityWorker(TreatyRepository treaties, HoldRepository holds, OutboxRepository outbox,
            ObjectMapper mapper, EntityManager em) {
        this.treaties = treaties;
        this.holds = holds;
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @RabbitListener(queues = Channels.HOLD, concurrency = "4")
    @Transactional
    public void onHold(CapacityHoldCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":hold")) {
            return;
        }
        if (command.amountCents() < 0 || !treaties.existsById(command.treatyCode())
                || treaties.hold(command.treatyCode(), command.amountCents()) != 1) {
            publish(command.quoteId(), "DECLINED");
            return;
        }
        HoldEntity hold = new HoldEntity();
        hold.setQuoteId(command.quoteId());
        hold.setTreatyCode(command.treatyCode());
        hold.setAmountCents(command.amountCents());
        hold.setState("HELD");
        holds.save(hold);
        publish(command.quoteId(), "HELD");
    }

    @RabbitListener(queues = Channels.RELEASE, concurrency = "4")
    @Transactional
    public void onRelease(CapacityReleaseCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":capacity-release")) {
            return;
        }
        int deleted = em.createNativeQuery("DELETE FROM capacity_holds WHERE quote_id = :id AND state = 'HELD'")
                .setParameter("id", command.quoteId())
                .executeUpdate();
        if (deleted != 1 || treaties.release(command.treatyCode(), command.amountCents()) != 1) {
            throw new IllegalStateException("capacity release missed " + command.quoteId());
        }
        publish(command.quoteId(), "RELEASED");
    }

    @RabbitListener(queues = Channels.COMMIT, concurrency = "4")
    @Transactional
    public void onCommit(CapacityCommitCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":capacity-commit")) {
            return;
        }
        int updated = em.createNativeQuery("UPDATE capacity_holds SET state = 'COMMITTED' WHERE quote_id = :id AND state = 'HELD'")
                .setParameter("id", command.quoteId())
                .executeUpdate();
        if (updated != 1 || treaties.commitHold(command.treatyCode(), command.amountCents()) != 1) {
            throw new IllegalStateException("capacity commit missed " + command.quoteId());
        }
        publish(command.quoteId(), "COMMITTED");
    }

    @RabbitListener(queues = Channels.RETURN_CAPACITY)
    @Transactional
    public void onReturn(CapacityReturnCommand command) {
        if (!InboxClaims.claim(em, command.quoteId() + ":capacity-return")) {
            return;
        }
        if (treaties.giveBack(command.treatyCode(), command.amountCents()) != 1) {
            throw new IllegalStateException("capacity return missed " + command.quoteId());
        }
        publish(command.quoteId(), "RETURNED");
    }

    private void publish(String quoteId, String status) {
        OutboxWriter.kafka(outbox, mapper, Channels.CAPACITY, quoteId, new CapacityEvent(quoteId, status));
    }
}
