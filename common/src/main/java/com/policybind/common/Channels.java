package com.policybind.common;

public final class Channels {
    public static final String ACCOUNTS = "policy.accounts";
    public static final String PRODUCTS = "policy.products";
    public static final String TREATIES = "policy.treaties";
    public static final String QUOTES = "policy.quotes";
    public static final String RATINGS = "policy.ratings";
    public static final String CAPACITY = "policy.capacity";
    public static final String REFERRALS = "policy.referrals";
    public static final String SANCTIONS = "policy.sanctions";
    public static final String BINDS = "policy.binds";
    public static final String DOCUMENTS = "policy.documents";
    public static final String PREMIUMS = "policy.premiums";
    public static final String COMMISSIONS = "policy.commissions";
    public static final String ENDORSEMENTS = "policy.endorsements";

    public static final String RATE = "policy.rating.rate";
    public static final String RATE_DLQ = "policy.rating.rate.dlq";
    public static final String HOLD = "policy.capacity.hold";
    public static final String HOLD_DLQ = "policy.capacity.hold.dlq";
    public static final String RELEASE = "policy.capacity.release";
    public static final String RELEASE_DLQ = "policy.capacity.release.dlq";
    public static final String COMMIT = "policy.capacity.commit";
    public static final String COMMIT_DLQ = "policy.capacity.commit.dlq";
    public static final String RETURN_CAPACITY = "policy.capacity.return";
    public static final String RETURN_CAPACITY_DLQ = "policy.capacity.return.dlq";
    public static final String SCREEN = "policy.sanction.screen";
    public static final String SCREEN_DLQ = "policy.sanction.screen.dlq";
    public static final String DECIDE = "policy.referral.decide";
    public static final String DECIDE_DLQ = "policy.referral.decide.dlq";
    public static final String ISSUE = "policy.bind.issue";
    public static final String ISSUE_DLQ = "policy.bind.issue.dlq";
    public static final String BIND_CANCEL = "policy.bind.cancel";
    public static final String BIND_CANCEL_DLQ = "policy.bind.cancel.dlq";
    public static final String DOCUMENT = "policy.document.issue";
    public static final String DOCUMENT_DLQ = "policy.document.issue.dlq";
    public static final String DOCUMENT_VOID = "policy.document.void";
    public static final String DOCUMENT_VOID_DLQ = "policy.document.void.dlq";
    public static final String POST = "policy.premium.post";
    public static final String POST_DLQ = "policy.premium.post.dlq";
    public static final String PREMIUM_RETURN = "policy.premium.return";
    public static final String PREMIUM_RETURN_DLQ = "policy.premium.return.dlq";
    public static final String PREMIUM_VOID = "policy.premium.void";
    public static final String PREMIUM_VOID_DLQ = "policy.premium.void.dlq";
    public static final String ACCRUE = "policy.commission.accrue";
    public static final String ACCRUE_DLQ = "policy.commission.accrue.dlq";
    public static final String REVERSE = "policy.commission.reverse";
    public static final String REVERSE_DLQ = "policy.commission.reverse.dlq";
    public static final String ENDORSE = "policy.endorsement.record";
    public static final String ENDORSE_DLQ = "policy.endorsement.record.dlq";

    private Channels() {
    }
}
