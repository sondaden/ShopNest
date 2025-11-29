package com.sonnhuynhh.shopnest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho MoMo IPN Callback (Instant Payment Notification)
 * MoMo gọi đến server khi thanh toán hoàn tất
 */
@Data
public class MoMoIPNRequest {
    
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("orderInfo")
    private String orderInfo;
    
    @JsonProperty("orderType")
    private String orderType;
    
    @JsonProperty("transId")
    private Long transId;
    
    @JsonProperty("resultCode")
    private Integer resultCode;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("payType")
    private String payType;
    
    @JsonProperty("responseTime")
    private Long responseTime;
    
    @JsonProperty("extraData")
    private String extraData;
    
    @JsonProperty("signature")
    private String signature;
    
    /**
     * Kiểm tra thanh toán thành công
     * resultCode = 0 nghĩa là thành công
     */
    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }
}
