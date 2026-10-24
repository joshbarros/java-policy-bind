package com.policybind.premium;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "premiums")
public class PremiumEntity {
    @Id
    @Column(name = "quote_id")
    private String quoteId;
    @Column(name = "amount_cents")
    private int amountCents;
    @Column(name = "return_cents")
    private int returnCents;
    private String status;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public int getAmountCents() { return amountCents; }
    public void setAmountCents(int amountCents) { this.amountCents = amountCents; }
    public int getReturnCents() { return returnCents; }
    public void setReturnCents(int returnCents) { this.returnCents = returnCents; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
