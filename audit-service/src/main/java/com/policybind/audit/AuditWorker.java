package com.policybind.audit;

import java.time.Instant;

import com.policybind.common.BindEvent;
import com.policybind.common.CapacityEvent;
import com.policybind.common.Channels;
import com.policybind.common.CommissionEvent;
import com.policybind.common.DocumentEvent;
import com.policybind.common.EndorsementEvent;
import com.policybind.common.PremiumEvent;
import com.policybind.common.QuoteFact;
import com.policybind.common.RatingEvent;
import com.policybind.common.ReferralEvent;
import com.policybind.common.SanctionEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuditWorker {

    private final AuditRepository entries;

    public AuditWorker(AuditRepository entries) {
        this.entries = entries;
    }

    @KafkaListener(topics = Channels.QUOTES, groupId = "audit-quotes")
    @Transactional
    public void onQuote(QuoteFact event) {
        add("QuoteFact", event.quoteId(), event.status());
    }

    @KafkaListener(topics = Channels.RATINGS, groupId = "audit-ratings")
    @Transactional
    public void onRating(RatingEvent event) {
        add("RatingEvent", event.quoteId(), event.status());
    }

    @KafkaListener(topics = Channels.CAPACITY, groupId = "audit-capacity")
    @Transactional
    public void onCapacity(CapacityEvent event) {
        add("CapacityEvent", event.quoteId(), event.status());
    }

    @KafkaListener(topics = Channels.SANCTIONS, groupId = "audit-sanctions")
    @Transactional
    public void onSanction(SanctionEvent event) {
        add("SanctionEvent", event.quoteId(), event.status());
    }

    @KafkaListener(topics = Channels.REFERRALS, groupId = "audit-referrals")
    @Transactional
    public void onReferral(ReferralEvent event) {
        add("ReferralEvent", event.quoteId(), event.status());
    }

    @KafkaListener(topics = Channels.BINDS, groupId = "audit-binds")
    @Transactional
    public void onBind(BindEvent event) {
        add("BindEvent", event.quoteId(), event.status());
    }

    @KafkaListener(topics = Channels.DOCUMENTS, groupId = "audit-documents")
    @Transactional
    public void onDocument(DocumentEvent event) {
        add("DocumentEvent", event.quoteId(), event.status());
    }

    @KafkaListener(topics = Channels.PREMIUMS, groupId = "audit-premiums")
    @Transactional
    public void onPremium(PremiumEvent event) {
        add("PremiumEvent", event.quoteId(), event.status());
    }

    @KafkaListener(topics = Channels.COMMISSIONS, groupId = "audit-commissions")
    @Transactional
    public void onCommission(CommissionEvent event) {
        add("CommissionEvent", event.quoteId(), event.status());
    }

    @KafkaListener(topics = Channels.ENDORSEMENTS, groupId = "audit-endorsements")
    @Transactional
    public void onEndorsement(EndorsementEvent event) {
        add("EndorsementEvent", event.quoteId(), event.status());
    }

    private void add(String kind, String quoteId, String status) {
        String eventId = kind + ":" + quoteId + ":" + status;
        if (entries.existsById(eventId)) {
            return;
        }
        AuditEntry row = new AuditEntry();
        row.setEventId(eventId);
        row.setQuoteId(quoteId);
        row.setKind(kind);
        row.setStatus(status);
        row.setRecordedAt(Instant.now());
        entries.save(row);
    }
}
