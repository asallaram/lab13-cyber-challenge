package com.neueda.leap.merchantportal;

public class PaymentStatusEvent {
    private Long payoutId;
    private String status;
    private Long merchantId;

    public Long getPayoutId() { return payoutId; }
    public String getStatus() { return status; }
    public Long getMerchantId() { return merchantId; }
}
