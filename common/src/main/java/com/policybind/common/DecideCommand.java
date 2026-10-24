package com.policybind.common;

public record DecideCommand(String quoteId, int premiumCents, String approverToken) {
}
