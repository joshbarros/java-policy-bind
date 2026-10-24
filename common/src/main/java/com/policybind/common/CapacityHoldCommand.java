package com.policybind.common;

public record CapacityHoldCommand(String quoteId, String treatyCode, int amountCents) {
}
