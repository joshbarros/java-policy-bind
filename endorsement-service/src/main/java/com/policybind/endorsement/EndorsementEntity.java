package com.policybind.endorsement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "endorsements")
public class EndorsementEntity {
    @Id
    @Column(name = "quote_id")
    private String quoteId;
    @Column(name = "premium_cents")
    private int premiumCents;
    @Column(name = "return_cents")
    private int returnCents;
    @Column(name = "elapsed_days")
    private int elapsedDays;
    private String status;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public int getPremiumCents() { return premiumCents; }
    public void setPremiumCents(int premiumCents) { this.premiumCents = premiumCents; }
    public int getReturnCents() { return returnCents; }
    public void setReturnCents(int returnCents) { this.returnCents = returnCents; }
    public int getElapsedDays() { return elapsedDays; }
    public void setElapsedDays(int elapsedDays) { this.elapsedDays = elapsedDays; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
