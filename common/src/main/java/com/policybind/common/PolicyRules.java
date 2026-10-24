package com.policybind.common;

import java.util.Locale;

public final class PolicyRules {
    private PolicyRules() {
    }

    public static boolean overAuthority(int premiumCents) {
        return premiumCents > 1_000_000;
    }

    public static boolean denied(String approverToken) {
        return approverToken != null && approverToken.toLowerCase(Locale.ROOT).contains("denied");
    }

    public static boolean authorityDecline(String approverToken) {
        return approverToken != null && approverToken.toLowerCase(Locale.ROOT).contains("decline");
    }

    public static boolean sanctioned(String name) {
        return name != null && name.toLowerCase(Locale.ROOT).contains("sanction");
    }

    public static boolean brokenForm(String formToken) {
        return formToken != null && formToken.toLowerCase(Locale.ROOT).contains("broken");
    }

    public static boolean premiumDeclined(String premiumToken) {
        return premiumToken != null && premiumToken.endsWith("0000");
    }

    public static int proRataReturn(int premiumCents, int termDays, int elapsedDays) {
        if (termDays <= 0 || elapsedDays < 0 || elapsedDays > termDays) {
            throw new IllegalArgumentException("term");
        }
        long unused = (long) termDays - elapsedDays;
        return (int) ((long) premiumCents * unused / termDays);
    }

    public static int commission(int premiumCents) {
        return (int) ((long) premiumCents * 10 / 100);
    }

    public static boolean releaseCapacity(String status) {
        return switch (status) {
            case "HELD", "CLEARED", "AUTHORIZED", "BOUND", "ISSUED", "POSTED" -> true;
            default -> false;
        };
    }

    public static boolean cancelBind(String status) {
        return switch (status) {
            case "BOUND", "ISSUED", "POSTED" -> true;
            default -> false;
        };
    }

    public static boolean voidDocument(String status) {
        return switch (status) {
            case "ISSUED", "POSTED" -> true;
            default -> false;
        };
    }

    public static boolean voidPremium(String status) {
        return "POSTED".equals(status);
    }
}
