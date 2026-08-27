package com.neueda.leap.merchantportal;

public class PayoutApprovalService {

    private PayoutRepository payoutRepository;

    public PayoutApprovalService(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    /*
    This is an A06: Separation of Concerns OWASP issue.
    A user requesting a payment could also approve the payment.
    This is a conflict of interest and major security risk.
    Approval and request process needs to be separated.
    */
    public void approve(Long payoutId, Long approvingUserId) {
        PayoutRequest payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new RuntimeException("Payout not found"));

        // Separation of duties: requester cannot approve their own payout
        if (payout.getRequestedByUserId().equals(approvingUserId)) {
            throw new IllegalStateException("Requester cannot approve their own payout");
        }

        // Only allow approval from a valid pending state
        if (!"PENDING".equals(payout.getApprovalStatus())) {
            throw new IllegalStateException(
                    "Payout " + payoutId + " is not pending approval (status: " + payout.getApprovalStatus() + ")");
        }

        payout.setApprovalStatus("APPROVED");
        payout.setApprovedByUserId(approvingUserId);
        payoutRepository.save(payout);
    }
}
