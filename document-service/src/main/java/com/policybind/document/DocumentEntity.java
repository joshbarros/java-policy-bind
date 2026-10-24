package com.policybind.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "documents")
public class DocumentEntity {
    @Id
    @Column(name = "quote_id")
    private String quoteId;
    @Column(name = "form_token")
    private String formToken;
    private String status;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }
    public String getFormToken() { return formToken; }
    public void setFormToken(String formToken) { this.formToken = formToken; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
