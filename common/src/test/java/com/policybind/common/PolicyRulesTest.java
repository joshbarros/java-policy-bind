package com.policybind.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PolicyRulesTest {

    @Test
    void authorityNameFormAndPremiumTokenAreExact() {
        assertFalse(PolicyRules.overAuthority(1_000_000));
        assertTrue(PolicyRules.overAuthority(1_000_001));
        assertTrue(PolicyRules.denied("lead-denied"));
        assertFalse(PolicyRules.denied("lead-ok"));
        assertTrue(PolicyRules.authorityDecline("lead-decline"));
        assertFalse(PolicyRules.authorityDecline("lead-denied"));
        assertTrue(PolicyRules.sanctioned("North Sanction Ltd"));
        assertFalse(PolicyRules.sanctioned("Ada Marine"));
        assertTrue(PolicyRules.brokenForm("FORM-BROKEN"));
        assertFalse(PolicyRules.brokenForm("FORM-1"));
        assertTrue(PolicyRules.premiumDeclined("PREM0000"));
        assertFalse(PolicyRules.premiumDeclined("PREM0001"));
    }

    @Test
    void compensationFollowsTheStepsThatAlreadyCommitted() {
        assertFalse(PolicyRules.releaseCapacity("RATED"));
        assertTrue(PolicyRules.releaseCapacity("HELD"));
        assertFalse(PolicyRules.cancelBind("AUTHORIZED"));
        assertTrue(PolicyRules.cancelBind("BOUND"));
        assertFalse(PolicyRules.voidDocument("BOUND"));
        assertTrue(PolicyRules.voidDocument("ISSUED"));
        assertFalse(PolicyRules.voidPremium("ISSUED"));
        assertTrue(PolicyRules.voidPremium("POSTED"));
        assertTrue(PolicyRules.releaseCapacity("POSTED"));
        assertTrue(PolicyRules.cancelBind("POSTED"));
        assertTrue(PolicyRules.voidDocument("POSTED"));
        assertFalse(PolicyRules.releaseCapacity("IN_FORCE"));
    }

    @Test
    void proRataReturnAndCommissionUseIntegerDays() {
        assertEquals(10_000, PolicyRules.proRataReturn(10_000, 10, 0));
        assertEquals(6_000, PolicyRules.proRataReturn(10_000, 10, 4));
        assertEquals(0, PolicyRules.proRataReturn(10_000, 10, 10));
        assertEquals(364, PolicyRules.proRataReturn(365, 365, 1));
        assertEquals(500, PolicyRules.commission(5_000));
        assertEquals(1_000, PolicyRules.commission(10_000));
        assertEquals(100_000, PolicyRules.commission(1_000_001));
        assertThrows(IllegalArgumentException.class, () -> PolicyRules.proRataReturn(10_000, 10, 11));
        assertThrows(IllegalArgumentException.class, () -> PolicyRules.proRataReturn(10_000, 10, -1));
    }
}
