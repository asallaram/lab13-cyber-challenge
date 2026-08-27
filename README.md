# Lab 13 — Cyber Challenge: OWASP Fixes

This repo started from a deliberately vulnerable Spring Boot "merchant portal" service
(`starter/`). Each fix below was developed on its own branch and merged into `main`.

## Branches & Fixes

### 1. `fix/PayoutApprovalService` — OWASP A06: Insecure Design (Separation of Duties)

**File:** `PayoutApprovalService.java`

**Issue:** `approve()` let any `approvingUserId` approve a payout, including the same
user who originally requested it. There was also no check on the payout's current
status, so an already-approved/rejected payout could be re-approved.

**Fix:**
- Reject the approval if `approvingUserId` matches `payout.getRequestedByUserId()`
  (requester can no longer approve their own payout).
- Reject the approval unless the payout is currently `PENDING`.

**Why it maps to A06:** Insecure Design covers missing business/security controls
in the design itself but the lack of a maker-checker (separation of duties)
control was a design flaw, not just a coding bug.

---

### 2. `fix/MerchantController` — OWASP A01: Broken Access Control (IDOR)

**File:** `MerchantController.java`

**Issue:** `GET /api/payouts/{payoutId}` returned any payout by ID with no check
that the caller was associated with that payout's merchant.

**Fix:**
- Changed the route to `GET /api/merchants/{merchantId}/payouts/{payoutId}`.
- Added a check that the requested payout's `merchantId` matches the `merchantId`
  in the path before returning it; otherwise the request is rejected.

**Why it maps to A01:** A01 is about missing enforcement of what an authenticated
caller is actually allowed to access. Returning another merchant's payout data
with zero ownership check is the textbook IDOR case under this category.

---

### 3. `feature/batchpayoutjob` — OWASP A10: Mishandling of Exceptional Conditions

**File:** `BatchPayoutJob.java`

**Issue:** `runNightlyBatch()` marked every payout as `"PAID"` regardless of whether
the bank transfer actually succeeded and even a caught `BankTransferException`
resulted in `"PAID"`.

**Fix:**
- On success, the payout is marked `"APPROVED"` (not `"PAID"` because the actual
  settlement should be confirmed separately).
- On a failed transfer (`BankTransferException`), the payout is marked
  `"REJECTED"` instead of `"PAID"`, and the failure is logged accurately.

**Why this matters:** Treating a failed financial transaction as successful is a
serious integrity/logic flaw — it can cause merchants to be told they were paid
when no money moved.

---

### 4. `feature/webhook` — OWASP A08: Software and Data Integrity Failures

**File:** `WebhookController.java` (with supporting changes in `PaymentStatusEvent.java`)

**Issue:** `POST /api/webhooks/payment-status` trusted the incoming
`PaymentStatusEvent` completely and immediately marked the referenced payout as
settled and with no check that the event actually belonged to the merchant that
owns that payout. Any caller could forge an event for someone else's payout.

**Fix:**
- Added a `merchantId` field to `PaymentStatusEvent`.
- The webhook handler now looks up the payout by ID and verifies
  `payout.getMerchantId()` matches `event.getMerchantId()` before calling
  `payoutStatusUpdater.markSettled(...)`. A mismatch throws and the update is
  rejected.

**Why it maps to A08:** A08 covers failures to verify the integrity/origin of
data before trusting and acting on it and settling a payout based on an
unverified webhook payload is exactly that failure.

---

## CI

A `Jenkinsfile` was added at the repo root, which builds, tests, and packages the
Maven project under `starter/` (`mvn clean compile`, `mvn test`, `mvn package`).
