package com.policybind.common;

public record EndorseCommand(String quoteId, int premiumCents, int returnCents, int elapsedDays) {
}
