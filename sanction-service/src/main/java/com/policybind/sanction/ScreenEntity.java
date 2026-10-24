package com.policybind.sanction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "screens")
public class ScreenEntity {
    @Id
    @Column(name = "quote_id")
    private String quoteId;
    private String status;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
