package com.policybind.common;

public record RatingEvent(String quoteId, int premiumCents, int termDays, String status) {
}
