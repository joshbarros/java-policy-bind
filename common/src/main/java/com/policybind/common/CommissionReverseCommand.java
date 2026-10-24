package com.policybind.common;

public record CommissionReverseCommand(String quoteId, int clawbackCents) {
}
