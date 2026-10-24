package com.policybind.common;

public record CapacityReleaseCommand(String quoteId, String treatyCode, int amountCents) {
}
