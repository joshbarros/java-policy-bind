package com.policybind.common;

public record CapacityCommitCommand(String quoteId, String treatyCode, int amountCents) {
}
