package com.policybind.bind;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "risk_slots")
public class RiskSlotEntity {
    @Id
    @Column(name = "risk_key")
    private String riskKey;
    @Column(name = "quote_id", unique = true)
    private String quoteId;

    public String getRiskKey() { return riskKey; }
    public void setRiskKey(String riskKey) { this.riskKey = riskKey; }
    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
}
