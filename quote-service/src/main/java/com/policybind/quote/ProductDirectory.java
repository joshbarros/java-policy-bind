package com.policybind.quote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_directory")
public class ProductDirectory {
    @Id
    private String code;
    @Column(name = "rate_cents")
    private int rateCents;
    @Column(name = "term_days")
    private int termDays;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getRateCents() { return rateCents; }
    public void setRateCents(int rateCents) { this.rateCents = rateCents; }
    public int getTermDays() { return termDays; }
    public void setTermDays(int termDays) { this.termDays = termDays; }
}
