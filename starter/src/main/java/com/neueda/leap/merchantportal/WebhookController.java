package com.neueda.leap.merchantportal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebhookController {

    @Autowired
    private PayoutRepository payoutRepository;

    @Autowired
    private PayoutStatusUpdater payoutStatusUpdater;

    // OWASP A08: verify the event's merchant matches the payout's merchant before trusting it, to stop a webhook for one merchant settling another merchant's payout
    @PostMapping("/api/webhooks/payment-status")
    public void handlePaymentStatusWebhook(@RequestBody PaymentStatusEvent event) {
        PayoutRequest payout = payoutRepository.findById(event.getPayoutId())
                .orElseThrow(() -> new RuntimeException("Payout not found"));

        if (!payout.getMerchantId().equals(event.getMerchantId())) {
            throw new RuntimeException("Merchant does not match payout");
        }

        payoutStatusUpdater.markSettled(event.getPayoutId(), event.getStatus());
    }
}
