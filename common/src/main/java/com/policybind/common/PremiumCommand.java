package com.policybind.common;

public record PremiumCommand(String quoteId, int amountCents, String premiumToken) {
}
