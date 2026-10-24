package com.policybind.capacity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "treaties")
public class TreatyEntity {
    @Id
    private String code;
    @Column(name = "limit_cents")
    private int limitCents;
    @Column(name = "held_cents")
    private int heldCents;
    @Column(name = "bound_cents")
    private int boundCents;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public int getLimitCents() { return limitCents; }
    public void setLimitCents(int limitCents) { this.limitCents = limitCents; }
    public int getHeldCents() { return heldCents; }
    public void setHeldCents(int heldCents) { this.heldCents = heldCents; }
    public int getBoundCents() { return boundCents; }
    public void setBoundCents(int boundCents) { this.boundCents = boundCents; }
}
