#!/usr/bin/env python3
"""Drive policy bind through referral, compensation, pro-rata cancel, and overlap."""

import base64
import json
import sys
import time
import urllib.error
import urllib.request
import uuid
from concurrent.futures import ThreadPoolExecutor

PORTS = {
    "account": 8201,
    "product": 8202,
    "quote": 8203,
    "rating": 8204,
    "capacity": 8205,
    "referral": 8206,
    "sanction": 8207,
    "bind": 8208,
    "document": 8209,
    "premium": 8210,
    "commission": 8211,
    "endorsement": 8212,
    "audit": 8213,
}
MGMT = "http://127.0.0.1:15683"
AUTH = "platform:platform"
failures = []


def check(name, ok, detail=""):
    if ok:
        print(f"PASS  {name}")
    else:
        print(f"FAIL  {name} {detail}")
        failures.append(f"{name}: {detail}")


def call(method, url, body=None, auth=None, timeout=20):
    data = None if body is None else json.dumps(body).encode()
    request = urllib.request.Request(url, data=data, method=method)
    if data is not None:
        request.add_header("content-type", "application/json")
    if auth:
        request.add_header("Authorization", "Basic " + base64.b64encode(auth.encode()).decode())
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            raw = response.read().decode()
            return response.status, json.loads(raw) if raw else None
    except urllib.error.HTTPError as ex:
        raw = ex.read().decode()
        try:
            parsed = json.loads(raw) if raw else None
        except json.JSONDecodeError:
            parsed = raw
        return ex.code, parsed
    except (urllib.error.URLError, TimeoutError, json.JSONDecodeError):
        return 0, None


def wait_ok(url, seconds=300, auth=None):
    deadline = time.time() + seconds
    while time.time() < deadline:
        if call("GET", url, auth=auth)[0] == 200:
            return True
        time.sleep(1)
    return False


def until(label, fn, seconds=90):
    deadline = time.time() + seconds
    last = None
    while time.time() < deadline:
        last = fn()
        if last is not None:
            return last
        time.sleep(0.4)
    check(label, False, f"timed out last={last!r}")
    return None


def depth(name):
    status, body = call("GET", f"{MGMT}/api/queues/%2F/{name}", auth=AUTH)
    if status != 200 or not isinstance(body, dict):
        return None
    return body.get("messages", 0)


def grew(name, before, amount=1):
    def ready():
        now = depth(name)
        if now is not None and now >= before + amount:
            return now
        return None
    return until(f"dlq {name}", ready, 30)


def base(name):
    return f"http://127.0.0.1:{PORTS[name]}"


def account(name, email):
    status, body = call("POST", f"{base('account')}/accounts", {"name": name, "email": email})
    check(f"account {email}", status == 201, body)
    if status != 201:
        return None
    account_id = body["id"]

    def directory():
        code, row = call("GET", f"{base('quote')}/directory/{account_id}")
        if code == 200 and isinstance(row, dict) and row.get("name") == name:
            return row
        return None

    check(f"directory {email}", until(email, directory, 40) is not None)
    return account_id


def product(code, rate, term):
    status, body = call("POST", f"{base('product')}/products", {
        "code": code, "rateCents": rate, "termDays": term,
    })
    check(f"product {code}", status == 201, body)
    if status != 201:
        return None

    def seen():
        coded, row = call("GET", f"{base('quote')}/products/{code}")
        if coded == 200 and isinstance(row, dict) and row.get("rateCents") == rate:
            return row
        return None

    check(f"product directory {code}", until(code, seen, 40) is not None)
    return code


def treaty(limit):
    code = "T-" + uuid.uuid4().hex[:10]
    status, body = call("POST", f"{base('capacity')}/treaties", {"code": code, "limitCents": limit})
    check(f"treaty {code}", status == 201, body)
    if status != 201:
        return None

    def seen():
        coded, row = call("GET", f"{base('quote')}/treaties/{code}")
        return row if coded == 200 else None

    check(f"treaty directory {code}", until(code, seen, 40) is not None)
    return code


def place(account_id, product_code, treaty_code, units, approver="lead-ok", form="FORM-1",
          token="PREM0001", request_id=None):
    return call("POST", f"{base('quote')}/quotes", {
        "requestId": request_id or str(uuid.uuid4()),
        "accountId": account_id,
        "productCode": product_code,
        "treatyCode": treaty_code,
        "units": units,
        "approverToken": approver,
        "formToken": form,
        "premiumToken": token,
    })


def quote(qid):
    code, body = call("GET", f"{base('quote')}/quotes/{qid}")
    return body if code == 200 and isinstance(body, dict) else None


def book(code):
    status, body = call("GET", f"{base('capacity')}/treaties/{code}")
    if status != 200 or not isinstance(body, dict):
        return None
    return body.get("heldCents"), body.get("boundCents")


def audit_has(qid, status):
    code, body = call("GET", f"{base('audit')}/audits/{qid}")
    if code != 200 or not isinstance(body, list):
        return False
    return any(item.get("kind") == "QuoteFact" and item.get("status") == status for item in body)


def main():
    for name in PORTS:
        check(f"{name} health", wait_ok(f"{base(name)}/actuator/health"))
    check("rabbit health", wait_ok(f"{MGMT}/api/overview", auth=AUTH))

    status, _ = call("POST", f"{base('capacity')}/treaties", {"code": "NEG", "limitCents": -1})
    check("negative limit is 400", status == 400, status)
    status, _ = call("POST", f"{base('quote')}/quotes", {
        "requestId": " ", "accountId": " ", "productCode": " ", "treatyCode": " ",
        "units": 0, "approverToken": " ", "formToken": " ", "premiumToken": " ",
    })
    check("blank quote is 400", status == 400, status)
    missing = str(uuid.uuid4())
    buyer = account("Ada Marine", "ada@shop.test")
    hull = product("HULL-" + uuid.uuid4().hex[:8], 2500, 365)
    home = treaty(100000)
    status, _ = place(missing, hull, home, 1)
    check("unknown account is 404", status == 404, status)
    dup = "DUP-" + uuid.uuid4().hex[:8]
    status, _ = call("POST", f"{base('product')}/products", {"code": dup, "rateCents": 2500, "termDays": 30})
    status, _ = call("POST", f"{base('product')}/products", {"code": dup, "rateCents": 2500, "termDays": 30})
    check("duplicate product is 409", status == 409, status)
    status, _ = place(buyer, "NO-SUCH", home, 1)
    check("unknown product is 404", status == 404, status)
    status, _ = place(buyer, hull, "NO-TREATY", 1)
    check("unknown treaty is 404", status == 404, status)

    request_id = str(uuid.uuid4())
    status, created = place(buyer, hull, home, 2, request_id=request_id)
    check("quote accepted", status == 201 and isinstance(created, dict), created)
    qid = created["id"] if isinstance(created, dict) else ""

    def in_force(expected_premium, expected_referred, treaty_code):
        def ready():
            body = quote(qid_box[0])
            money = book(treaty_code)
            if body and body.get("status") == "IN_FORCE" and body.get("premiumCents") == expected_premium and body.get("referred") is expected_referred and body.get("capacityStatus") == "COMMITTED" and money == (0, expected_premium):
                return body
            return None
        return ready

    qid_box = [qid]

    def happy_ready():
        body = quote(qid)
        money = book(home)
        if body and body.get("status") == "IN_FORCE" and body.get("premiumCents") == 5000 and body.get("referred") is False and body.get("capacityStatus") == "COMMITTED" and body.get("commissionCents") == 500 and money == (0, 5000):
            return body
        return None

    bound = until("happy path", happy_ready, 90)
    check("happy path binds", bound is not None, quote(qid))
    policy_code, policy = call("GET", f"{base('bind')}/policies/{qid}")
    check("policy number issued", policy_code == 200 and isinstance(policy, dict) and policy.get("status") == "BOUND" and policy.get("policyNumber") == "POL-" + qid, policy)
    document_code, document = call("GET", f"{base('document')}/documents/{qid}")
    check("binder issued", document_code == 200 and isinstance(document, dict) and document.get("status") == "ISSUED")
    premium_code, premium = call("GET", f"{base('premium')}/premiums/{qid}")
    check("premium posted", premium_code == 200 and isinstance(premium, dict) and premium.get("status") == "POSTED" and premium.get("amountCents") == 5000 and premium.get("returnCents") == 0)
    commission_code, commission = call("GET", f"{base('commission')}/commissions/{qid}")
    check("commission accrued", commission_code == 200 and isinstance(commission, dict) and commission.get("status") == "ACCRUED" and commission.get("amountCents") == 500)
    endorse_code, _ = call("GET", f"{base('endorsement')}/endorsements/{qid}")
    check("in force has no endorsement", endorse_code == 404, endorse_code)
    decision_code, decision = call("GET", f"{base('referral')}/decisions/{qid}")
    check("authority approved without referral", decision_code == 200 and isinstance(decision, dict) and decision.get("status") == "APPROVED" and decision.get("referred") is False)
    check("audit shows in force", until("audit", lambda: True if audit_has(qid, "IN_FORCE") else None, 30) is not None)
    replay_code, replay = place(buyer, hull, home, 2, request_id=request_id)
    check("same request returns the same quote", replay_code == 200 and isinstance(replay, dict) and replay.get("id") == qid, replay)
    check("replay does not bind more", book(home) == (0, 5000), book(home))

    huge = product("BIG-" + uuid.uuid4().hex[:8], 2147483647, 30)
    huge_treaty = treaty(100000)
    status, price_row = place(buyer, huge, huge_treaty, 2)
    price_id = price_row["id"] if status == 201 and isinstance(price_row, dict) else ""

    def price_ready():
        body = quote(price_id)
        if body and body.get("status") == "CANCELLED" and body.get("reason") == "PRICE" and book(huge_treaty) == (0, 0):
            return body
        return None

    check("overflow cancels before the hold", until("price", price_ready, 60) is not None)
    rating_code, _ = call("GET", f"{base('rating')}/ratings/{price_id}")
    check("overflow stores no rating", rating_code == 404, rating_code)

    screened = account("North Sanction Ltd", "sanction@shop.test")
    sanction_treaty = treaty(100000)
    sanction_product = product("SAN-" + uuid.uuid4().hex[:8], 2500, 30)
    screen_dlq = depth("policy.sanction.screen.dlq") or 0
    status, sanction_row = place(screened, sanction_product, sanction_treaty, 1)
    sanction_id = sanction_row["id"] if status == 201 and isinstance(sanction_row, dict) else ""

    def sanction_ready():
        body = quote(sanction_id)
        if body and body.get("status") == "CANCELLED" and body.get("reason") == "SANCTION" and book(sanction_treaty) == (0, 0):
            return body
        return None

    check("sanction releases the hold", until("sanction", sanction_ready, 70) is not None)
    screen_code, _ = call("GET", f"{base('sanction')}/screens/{sanction_id}")
    check("sanction stores no screen", screen_code == 404, screen_code)
    check("sanction dead-letters the screen", grew("policy.sanction.screen.dlq", screen_dlq) == screen_dlq + 1)

    referred_product = product("REF-" + uuid.uuid4().hex[:8], 1000001, 30)
    referred_treaty = treaty(2000000)
    status, referred_row = place(buyer, referred_product, referred_treaty, 1, approver="lead-ok")
    referred_id = referred_row["id"] if status == 201 and isinstance(referred_row, dict) else ""
    qid_box[0] = referred_id
    check("referred premium still binds", until("referral", in_force(1000001, True, referred_treaty), 90) is not None)
    decision_code, decision = call("GET", f"{base('referral')}/decisions/{referred_id}")
    check("referral is approved", decision_code == 200 and isinstance(decision, dict) and decision.get("referred") is True)

    edge_product = product("EDGE-" + uuid.uuid4().hex[:8], 1000000, 30)
    edge_treaty = treaty(2000000)
    status, edge_row = place(buyer, edge_product, edge_treaty, 1)
    edge_id = edge_row["id"] if status == 201 and isinstance(edge_row, dict) else ""
    qid_box[0] = edge_id
    check("threshold premium binds without referral", until("threshold", in_force(1000000, False, edge_treaty), 90) is not None)

    decline_product = product("DEC-" + uuid.uuid4().hex[:8], 1000001, 30)
    decline_treaty = treaty(2000000)
    status, decline_row = place(buyer, decline_product, decline_treaty, 1, approver="lead-decline")
    decline_id = decline_row["id"] if status == 201 and isinstance(decline_row, dict) else ""

    def decline_ready():
        body = quote(decline_id)
        decision_code, decision = call("GET", f"{base('referral')}/decisions/{decline_id}")
        if body and body.get("status") == "CANCELLED" and body.get("reason") == "AUTHORITY" and book(decline_treaty) == (0, 0) and decision_code == 200 and isinstance(decision, dict) and decision.get("status") == "REJECTED":
            return body
        return None

    check("decline above authority compensates", until("decline", decline_ready, 70) is not None)

    denied_product = product("DEN-" + uuid.uuid4().hex[:8], 2500, 30)
    denied_treaty = treaty(100000)
    decide_dlq = depth("policy.referral.decide.dlq") or 0
    status, denied_row = place(buyer, denied_product, denied_treaty, 1, approver="lead-denied")
    denied_id = denied_row["id"] if status == 201 and isinstance(denied_row, dict) else ""

    def denied_ready():
        body = quote(denied_id)
        if body and body.get("status") == "CANCELLED" and body.get("reason") == "AUTHORITY" and book(denied_treaty) == (0, 0):
            return body
        return None

    check("denied approver compensates", until("denied", denied_ready, 70) is not None)
    decision_code, _ = call("GET", f"{base('referral')}/decisions/{denied_id}")
    check("denied approver stores no decision", decision_code == 404, decision_code)
    check("denied approver dead-letters the decision", grew("policy.referral.decide.dlq", decide_dlq) == decide_dlq + 1)

    broken_product = product("BRK-" + uuid.uuid4().hex[:8], 2500, 30)
    broken_treaty = treaty(100000)
    document_dlq = depth("policy.document.issue.dlq") or 0
    status, broken_row = place(buyer, broken_product, broken_treaty, 1, form="FORM-BROKEN")
    broken_id = broken_row["id"] if status == 201 and isinstance(broken_row, dict) else ""

    def broken_ready():
        body = quote(broken_id)
        policy_code, policy = call("GET", f"{base('bind')}/policies/{broken_id}")
        if body and body.get("status") == "CANCELLED" and body.get("reason") == "DOCUMENT" and book(broken_treaty) == (0, 0) and policy_code == 200 and isinstance(policy, dict) and policy.get("status") == "CANCELLED":
            return body
        return None

    check("broken form compensates", until("document", broken_ready, 80) is not None)
    document_code, _ = call("GET", f"{base('document')}/documents/{broken_id}")
    check("broken form stores no binder", document_code == 404, document_code)
    check("broken form dead-letters the binder", grew("policy.document.issue.dlq", document_dlq) == document_dlq + 1)

    pay_product = product("PAY-" + uuid.uuid4().hex[:8], 2500, 30)
    pay_treaty = treaty(100000)
    pay_dlq = depth("policy.premium.post.dlq") or 0
    status, pay_row = place(buyer, pay_product, pay_treaty, 1, token="PREM0000")
    pay_id = pay_row["id"] if status == 201 and isinstance(pay_row, dict) else ""

    def pay_ready():
        body = quote(pay_id)
        document_code, document = call("GET", f"{base('document')}/documents/{pay_id}")
        policy_code, policy = call("GET", f"{base('bind')}/policies/{pay_id}")
        if body and body.get("status") == "CANCELLED" and body.get("reason") == "PREMIUM" and book(pay_treaty) == (0, 0) and document_code == 200 and isinstance(document, dict) and document.get("status") == "VOID" and policy_code == 200 and isinstance(policy, dict) and policy.get("status") == "CANCELLED":
            return body
        return None

    check("declined premium compensates", until("premium", pay_ready, 80) is not None)
    premium_code, _ = call("GET", f"{base('premium')}/premiums/{pay_id}")
    check("declined premium stores no receivable", premium_code == 404, premium_code)
    check("declined premium dead-letters the post", grew("policy.premium.post.dlq", pay_dlq) == pay_dlq + 1)

    term_product = product("TERM-" + uuid.uuid4().hex[:8], 1000, 10)
    term_treaty = treaty(100000)
    status, term_row = place(buyer, term_product, term_treaty, 10)
    term_id = term_row["id"] if status == 201 and isinstance(term_row, dict) else ""
    qid_box[0] = term_id
    check("term policy binds", until("term", in_force(10000, False, term_treaty), 90) is not None)
    status, _ = call("POST", f"{base('quote')}/quotes/{term_id}/cancel", {"elapsedDays": 11})
    check("elapsed past the term is 400", status == 400, status)
    status, _ = call("POST", f"{base('quote')}/quotes/{term_id}/cancel", {"elapsedDays": -1})
    check("negative elapsed is 400", status == 400, status)
    status, cancelled = call("POST", f"{base('quote')}/quotes/{term_id}/cancel", {"elapsedDays": 4})
    check("cancel accepted", status == 200 and isinstance(cancelled, dict) and cancelled.get("returnCents") == 6000, cancelled)

    def return_ready():
        body = quote(term_id)
        premium_code, premium = call("GET", f"{base('premium')}/premiums/{term_id}")
        commission_code, commission = call("GET", f"{base('commission')}/commissions/{term_id}")
        endorse_code, endorse = call("GET", f"{base('endorsement')}/endorsements/{term_id}")
        if body and body.get("status") == "CANCELLED" and body.get("reason") == "CANCEL" and body.get("returnCents") == 6000 and book(term_treaty) == (0, 4000) and premium_code == 200 and isinstance(premium, dict) and premium.get("status") == "RETURNED" and premium.get("returnCents") == 6000 and commission_code == 200 and isinstance(commission, dict) and commission.get("status") == "REVERSED" and commission.get("amountCents") == 1000 and commission.get("clawbackCents") == 600 and endorse_code == 200 and isinstance(endorse, dict) and endorse.get("returnCents") == 6000 and endorse.get("elapsedDays") == 4:
            return body
        return None

    check("pro-rata return releases unused premium", until("return", return_ready, 70) is not None)

    tight = treaty(7500)
    tight_product = product("CAP-" + uuid.uuid4().hex[:8], 2500, 30)
    racers = [account(f"Racer {i}", f"racer{i}-{uuid.uuid4().hex[:6]}@shop.test") for i in range(4)]

    def one_capacity(account_id):
        return place(account_id, tight_product, tight, 1)

    with ThreadPoolExecutor(max_workers=4) as pool:
        raced = list(pool.map(one_capacity, racers))
    ids = [body["id"] for code, body in raced if code == 201 and isinstance(body, dict)]
    check("four capacity quotes accepted", len(ids) == 4, [code for code, _body in raced])

    def capacity_settled():
        states = []
        for oid in ids:
            body = quote(oid)
            if not body or body.get("status") not in ("IN_FORCE", "CANCELLED"):
                return None
            if body.get("status") == "IN_FORCE" and body.get("capacityStatus") != "COMMITTED":
                return None
            states.append(body)
        paid = [row for row in states if row.get("status") == "IN_FORCE"]
        cancelled = [row for row in states if row.get("reason") == "CAPACITY"]
        if len(paid) != 3 or len(cancelled) != 1 or book(tight) != (0, 7500):
            return None
        return states

    check("three of four quotes fit the treaty", until("capacity race", capacity_settled, 110) is not None)

    shared = account("Shared Risk", f"shared-{uuid.uuid4().hex[:6]}@shop.test")
    shelf = treaty(1000000)
    shelf_product = product("ONE-" + uuid.uuid4().hex[:8], 2500, 30)

    def one_bind(_):
        return place(shared, shelf_product, shelf, 1)

    with ThreadPoolExecutor(max_workers=2) as pool:
        raced = list(pool.map(one_bind, range(2)))
    pair = [body["id"] for code, body in raced if code == 201 and isinstance(body, dict)]
    check("two risk quotes accepted", len(pair) == 2, [code for code, _body in raced])

    def risk_settled():
        states = []
        for oid in pair:
            body = quote(oid)
            if not body or body.get("status") not in ("IN_FORCE", "CANCELLED"):
                return None
            if body.get("status") == "IN_FORCE" and body.get("capacityStatus") != "COMMITTED":
                return None
            states.append(body)
        winners = [row for row in states if row.get("status") == "IN_FORCE"]
        losers = [row for row in states if row.get("reason") == "DUPLICATE"]
        if len(winners) != 1 or len(losers) != 1 or book(shelf) != (0, 2500):
            return None
        winner = winners[0]["id"]
        loser = losers[0]["id"]
        winner_code, winner_policy = call("GET", f"{base('bind')}/policies/{winner}")
        loser_code, _ = call("GET", f"{base('bind')}/policies/{loser}")
        if winner_code != 200 or not isinstance(winner_policy, dict) or winner_policy.get("policyNumber") != "POL-" + winner or loser_code != 404:
            return None
        return states

    check("one risk receives one policy", until("risk race", risk_settled, 110) is not None)

    print("\n== summary ==")
    if failures:
        print(f"{len(failures)} failed")
        for item in failures:
            print(f" - {item}")
        sys.exit(1)
    print("policy bind smoke passed")


if __name__ == "__main__":
    main()
