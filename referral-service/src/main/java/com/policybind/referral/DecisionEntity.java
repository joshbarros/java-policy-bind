package com.policybind.referral;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "decisions")
public class DecisionEntity {
    @Id
    @Column(name = "quote_id")
    private String quoteId;
    private String status;
    private boolean referred;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isReferred() { return referred; }
    public void setReferred(boolean referred) { this.referred = referred; }
}
