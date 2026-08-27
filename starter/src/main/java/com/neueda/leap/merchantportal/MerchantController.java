package com.neueda.leap.merchantportal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MerchantController {

    @Autowired
    private PayoutRepository payoutRepository;

    /*This an OWASP A01: Broken Access Control
    because there is no check to ensure that the
    merchant requesting data is the owner of the 
    payout. Fixed by adding a check to ensure
    the merchant is indeed owner of payout.
    */
    @GetMapping("/api/merchants/{merchantId}/payouts/{payoutId}")
    public PayoutRequest getPayout(@PathVariable Long merchantId, @PathVariable Long payoutId) {
        PayoutRequest payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new RuntimeException("Payout not found"));

        if (!payout.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("Not authorized to view this payout");
        }

        return payout;
    }
}
