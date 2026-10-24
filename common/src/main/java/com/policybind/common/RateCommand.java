package com.policybind.common;

public record RateCommand(String quoteId, int rateCents, int units, int termDays) {
}
