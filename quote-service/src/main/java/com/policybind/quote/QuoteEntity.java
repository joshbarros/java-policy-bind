package com.policybind.quote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quotes")
public class QuoteEntity {
    @Id
    private String id;
    @Column(name = "request_id", unique = true)
    private String requestId;
    @Column(name = "account_id")
    private String accountId;
    private String name;
    private String email;
    @Column(name = "product_code")
    private String productCode;
    @Column(name = "treaty_code")
    private String treatyCode;
    private int units;
    @Column(name = "rate_cents")
    private int rateCents;
    @Column(name = "term_days")
    private int termDays;
    @Column(name = "approver_token")
    private String approverToken;
    @Column(name = "form_token")
    private String formToken;
    @Column(name = "premium_token")
    private String premiumToken;
    @Column(name = "premium_cents")
    private int premiumCents;
    private boolean referred;
    @Column(name = "policy_number")
    private String policyNumber;
    @Column(name = "commission_cents")
    private int commissionCents;
    @Column(name = "return_cents")
    private int returnCents;
    @Column(name = "elapsed_days")
    private int elapsedDays;
    private String status;
    @Column(name = "capacity_status")
    private String capacityStatus;
    private String reason;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getTreatyCode() { return treatyCode; }
    public void setTreatyCode(String treatyCode) { this.treatyCode = treatyCode; }
    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }
    public int getRateCents() { return rateCents; }
    public void setRateCents(int rateCents) { this.rateCents = rateCents; }
    public int getTermDays() { return termDays; }
    public void setTermDays(int termDays) { this.termDays = termDays; }
    public String getApproverToken() { return approverToken; }
    public void setApproverToken(String approverToken) { this.approverToken = approverToken; }
    public String getFormToken() { return formToken; }
    public void setFormToken(String formToken) { this.formToken = formToken; }
    public String getPremiumToken() { return premiumToken; }
    public void setPremiumToken(String premiumToken) { this.premiumToken = premiumToken; }
    public int getPremiumCents() { return premiumCents; }
    public void setPremiumCents(int premiumCents) { this.premiumCents = premiumCents; }
    public boolean isReferred() { return referred; }
    public void setReferred(boolean referred) { this.referred = referred; }
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    public int getCommissionCents() { return commissionCents; }
    public void setCommissionCents(int commissionCents) { this.commissionCents = commissionCents; }
    public int getReturnCents() { return returnCents; }
    public void setReturnCents(int returnCents) { this.returnCents = returnCents; }
    public int getElapsedDays() { return elapsedDays; }
    public void setElapsedDays(int elapsedDays) { this.elapsedDays = elapsedDays; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCapacityStatus() { return capacityStatus; }
    public void setCapacityStatus(String capacityStatus) { this.capacityStatus = capacityStatus; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
