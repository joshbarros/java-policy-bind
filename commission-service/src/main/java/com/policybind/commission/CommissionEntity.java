package com.policybind.commission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "commissions")
public class CommissionEntity {
    @Id
    @Column(name = "quote_id")
    private String quoteId;
    @Column(name = "amount_cents")
    private int amountCents;
    @Column(name = "clawback_cents")
    private int clawbackCents;
    private String status;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
    public int getClawbackCents() { return clawbackCents; }
    public void setClawbackCents(int clawbackCents) { this.clawbackCents = clawbackCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
