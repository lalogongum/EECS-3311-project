package com.consulthub.entity;

import jakarta.persistence.*;

/**
 * Stores all admin-configurable system policies as key-value pairs.
 * Keys: cancellationHours, priceMin, priceMax, notificationsEnabled, refundPolicy
 */
@Entity
@Table(name = "system_policies")
public class SystemPolicyEntity {

    @Id
    @Column(nullable = false, unique = true)
    private String policyKey;

    @Column(nullable = false)
    private String policyValue;

    public SystemPolicyEntity() {}

    public SystemPolicyEntity(String policyKey, String policyValue) {
        this.policyKey   = policyKey;
        this.policyValue = policyValue;
    }

    public String getPolicyKey()   { return policyKey; }
    public String getPolicyValue() { return policyValue; }
    public void   setPolicyValue(String v) { this.policyValue = v; }
}
