package com.policybind.capacity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "capacity_holds")
public class HoldEntity {
    @Id
    @Column(name = "quote_id")
    private String quoteId;
    @Column(name = "treaty_code")
    private String treatyCode;
    @Column(name = "amount_cents")
    private int amountCents;
    private String state;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public String getTreatyCode() { return treatyCode; }
    public void setTreatyCode(String treatyCode) { this.treatyCode = treatyCode; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
