package com.policybind.common;

public record CommissionEvent(String quoteId, int amountCents, String status) {
}
