package com.policybind.common;

public record CapacityReturnCommand(String quoteId, String treatyCode, int amountCents) {
}
