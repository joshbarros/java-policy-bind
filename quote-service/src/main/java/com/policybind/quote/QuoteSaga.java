package com.policybind.quote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.policybind.common.BindCancelCommand;
import com.policybind.common.BindCommand;
import com.policybind.common.BindEvent;
import com.policybind.common.CapacityCommitCommand;
import com.policybind.common.CapacityEvent;
import com.policybind.common.CapacityHoldCommand;
import com.policybind.common.CapacityReleaseCommand;
import com.policybind.common.Channels;
import com.policybind.common.CommissionCommand;
import com.policybind.common.CommissionEvent;
import com.policybind.common.DecideCommand;
import com.policybind.common.DocumentCommand;
import com.policybind.common.DocumentEvent;
import com.policybind.common.DocumentVoidCommand;
import com.policybind.common.PolicyRules;
import com.policybind.common.PremiumCommand;
import com.policybind.common.PremiumEvent;
import com.policybind.common.PremiumVoidCommand;
import com.policybind.common.QuoteFact;
import com.policybind.common.RatingEvent;
import com.policybind.common.ReferralEvent;
import com.policybind.common.SanctionEvent;
import com.policybind.common.ScreenCommand;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class QuoteSaga {

    private final OutboxRepository outbox;
    private final ObjectMapper mapper;
    private final EntityManager em;

    public QuoteSaga(OutboxRepository outbox, ObjectMapper mapper, EntityManager em) {
        this.outbox = outbox;
        this.mapper = mapper;
        this.em = em;
    }

    @KafkaListener(topics = Channels.RATINGS, groupId = "quote-ratings")
    @Transactional
    public void onRating(RatingEvent event) {
        if (!"RATED".equals(event.status()) && !"REJECTED".equals(event.status())) {
            return;
        }
        QuoteEntity row = lock(event.quoteId());
        if (row == null || !"DRAFT".equals(row.getStatus())) {
            return;
        }
        if (!"RATED".equals(event.status())) {
            cancel(row, "PRICE");
            return;
        }
        row.setPremiumCents(event.premiumCents());
        row.setTermDays(event.termDays());
        row.setStatus("RATED");
        fact(row);
        OutboxWriter.rabbit(outbox, mapper, Channels.HOLD, row.getId(),
                new CapacityHoldCommand(row.getId(), row.getTreatyCode(), row.getPremiumCents()));
    }

    @KafkaListener(topics = Channels.CAPACITY, groupId = "quote-capacity")
    @Transactional
    public void onCapacity(CapacityEvent event) {
        if ("COMMITTED".equals(event.status())) {
            QuoteEntity row = lock(event.quoteId());
            if (row != null && "IN_FORCE".equals(row.getStatus()) && "NONE".equals(row.getCapacityStatus())) {
                row.setCapacityStatus("COMMITTED");
            }
            return;
        }
        if (!"HELD".equals(event.status()) && !"DECLINED".equals(event.status())) {
            return;
        }
        QuoteEntity row = lock(event.quoteId());
        if (row == null || !"RATED".equals(row.getStatus())) {
            return;
        }
        if (!"HELD".equals(event.status())) {
            cancel(row, "CAPACITY");
            return;
        }
        row.setStatus("HELD");
        fact(row);
        OutboxWriter.rabbit(outbox, mapper, Channels.SCREEN, row.getId(), new ScreenCommand(row.getId(), row.getName()));
    }

    @KafkaListener(topics = Channels.SANCTIONS, groupId = "quote-sanctions")
    @Transactional
    public void onSanction(SanctionEvent event) {
        if (!"CLEARED".equals(event.status()) && !"FAILED".equals(event.status())) {
            return;
        }
        QuoteEntity row = lock(event.quoteId());
        if (row == null || !"HELD".equals(row.getStatus())) {
            return;
        }
        if (!"CLEARED".equals(event.status())) {
            cancel(row, "SANCTION");
            return;
        }
        row.setStatus("CLEARED");
        fact(row);
        OutboxWriter.rabbit(outbox, mapper, Channels.DECIDE, row.getId(),
                new DecideCommand(row.getId(), row.getPremiumCents(), row.getApproverToken()));
    }

    @KafkaListener(topics = Channels.REFERRALS, groupId = "quote-referrals")
    @Transactional
    public void onReferral(ReferralEvent event) {
        if (!"APPROVED".equals(event.status()) && !"REJECTED".equals(event.status()) && !"DENIED".equals(event.status())) {
            return;
        }
        QuoteEntity row = lock(event.quoteId());
        if (row == null || !"CLEARED".equals(row.getStatus())) {
            return;
        }
        if (!"APPROVED".equals(event.status())) {
            cancel(row, "AUTHORITY");
            return;
        }
        row.setReferred(event.referred());
        row.setStatus("AUTHORIZED");
        fact(row);
        OutboxWriter.rabbit(outbox, mapper, Channels.ISSUE, row.getId(),
                new BindCommand(row.getId(), row.getAccountId(), row.getProductCode()));
    }

    @KafkaListener(topics = Channels.BINDS, groupId = "quote-binds")
    @Transactional
    public void onBind(BindEvent event) {
        if (!"BOUND".equals(event.status()) && !"DUPLICATE".equals(event.status())) {
            return;
        }
        QuoteEntity row = lock(event.quoteId());
        if (row == null || !"AUTHORIZED".equals(row.getStatus())) {
            return;
        }
        if (!"BOUND".equals(event.status())) {
            cancel(row, "DUPLICATE");
            return;
        }
        row.setPolicyNumber(event.policyNumber());
        row.setStatus("BOUND");
        fact(row);
        OutboxWriter.rabbit(outbox, mapper, Channels.DOCUMENT, row.getId(),
                new DocumentCommand(row.getId(), row.getFormToken()));
    }

    @KafkaListener(topics = Channels.DOCUMENTS, groupId = "quote-documents")
    @Transactional
    public void onDocument(DocumentEvent event) {
        if (!"ISSUED".equals(event.status()) && !"FAILED".equals(event.status())) {
            return;
        }
        QuoteEntity row = lock(event.quoteId());
        if (row == null || !"BOUND".equals(row.getStatus())) {
            return;
        }
        if (!"ISSUED".equals(event.status())) {
            cancel(row, "DOCUMENT");
            return;
        }
        row.setStatus("ISSUED");
        fact(row);
        OutboxWriter.rabbit(outbox, mapper, Channels.POST, row.getId(),
                new PremiumCommand(row.getId(), row.getPremiumCents(), row.getPremiumToken()));
    }

    @KafkaListener(topics = Channels.PREMIUMS, groupId = "quote-premiums")
    @Transactional
    public void onPremium(PremiumEvent event) {
        if (!"POSTED".equals(event.status()) && !"FAILED".equals(event.status())) {
            return;
        }
        QuoteEntity row = lock(event.quoteId());
        if (row == null || !"ISSUED".equals(row.getStatus())) {
            return;
        }
        if (!"POSTED".equals(event.status())) {
            cancel(row, "PREMIUM");
            return;
        }
        row.setStatus("POSTED");
        fact(row);
        OutboxWriter.rabbit(outbox, mapper, Channels.ACCRUE, row.getId(),
                new CommissionCommand(row.getId(), row.getPremiumCents()));
    }

    @KafkaListener(topics = Channels.COMMISSIONS, groupId = "quote-commissions")
    @Transactional
    public void onCommission(CommissionEvent event) {
        if (!"ACCRUED".equals(event.status())) {
            return;
        }
        QuoteEntity row = lock(event.quoteId());
        if (row == null || !"POSTED".equals(row.getStatus())) {
            return;
        }
        row.setCommissionCents(event.amountCents());
        row.setStatus("IN_FORCE");
        fact(row);
        OutboxWriter.rabbit(outbox, mapper, Channels.COMMIT, row.getId(),
                new CapacityCommitCommand(row.getId(), row.getTreatyCode(), row.getPremiumCents()));
    }

    private QuoteEntity lock(String id) {
        return em.find(QuoteEntity.class, id, LockModeType.PESSIMISTIC_WRITE);
    }

    private void fact(QuoteEntity row) {
        OutboxWriter.kafka(outbox, mapper, Channels.QUOTES, row.getId(),
                new QuoteFact(row.getId(), row.getStatus(), row.getReason()));
    }

    private void cancel(QuoteEntity row, String reason) {
        String status = row.getStatus();
        row.setStatus("CANCELLED");
        row.setReason(reason);
        fact(row);
        if (PolicyRules.releaseCapacity(status)) {
            OutboxWriter.rabbit(outbox, mapper, Channels.RELEASE, row.getId(),
                    new CapacityReleaseCommand(row.getId(), row.getTreatyCode(), row.getPremiumCents()));
        }
        if (PolicyRules.cancelBind(status)) {
            OutboxWriter.rabbit(outbox, mapper, Channels.BIND_CANCEL, row.getId(), new BindCancelCommand(row.getId()));
        }
        if (PolicyRules.voidDocument(status)) {
            OutboxWriter.rabbit(outbox, mapper, Channels.DOCUMENT_VOID, row.getId(), new DocumentVoidCommand(row.getId()));
        }
        if (PolicyRules.voidPremium(status)) {
            OutboxWriter.rabbit(outbox, mapper, Channels.PREMIUM_VOID, row.getId(), new PremiumVoidCommand(row.getId()));
        }
    }
}
