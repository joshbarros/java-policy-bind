package com.policybind.bind;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "policies")
public class PolicyEntity {
    @Id
    @Column(name = "quote_id")
    private String quoteId;
    @Column(name = "risk_key")
    private String riskKey;
    @Column(name = "policy_number")
    private String policyNumber;
    private String status;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public String getRiskKey() { return riskKey; }
    public void setRiskKey(String riskKey) { this.riskKey = riskKey; }
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
