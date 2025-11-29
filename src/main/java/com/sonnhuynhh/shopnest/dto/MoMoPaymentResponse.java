package com.sonnhuynhh.shopnest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO cho MoMo Payment Response
 * Phản hồi từ API /v2/gateway/api/create
 */
@Data
public class MoMoPaymentResponse {
    
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("responseTime")
    private Long responseTime;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("resultCode")
    private Integer resultCode;
    
    @JsonProperty("payUrl")
    private String payUrl;
    
    @JsonProperty("deeplink")
    private String deeplink;
    
    @JsonProperty("qrCodeUrl")
    private String qrCodeUrl;
    
    /**
     * Kiểm tra thanh toán thành công
     */
    public boolean isSuccess() {
        return resultCode != null && resultCode == 0;
    }
}
