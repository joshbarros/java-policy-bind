package com.policybind.rating;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ratings")
public class RatingEntity {
    @Id
    @Column(name = "quote_id")
    private String quoteId;
    @Column(name = "premium_cents")
    private int premiumCents;
    @Column(name = "term_days")
    private int termDays;
    private String status;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public int getPremiumCents() { return premiumCents; }
    public void setPremiumCents(int premiumCents) { this.premiumCents = premiumCents; }
    public int getTermDays() { return termDays; }
    public void setTermDays(int termDays) { this.termDays = termDays; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
