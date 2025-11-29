package com.sonnhuynhh.shopnest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * DTO cho MoMo Payment Request
 * API: /v2/gateway/api/create
 */
@Data
@Builder
public class MoMoPaymentRequest {
    
    @JsonProperty("partnerCode")
    private String partnerCode;
    
    @JsonProperty("partnerName")
    private String partnerName;
    
    @JsonProperty("storeId")
    private String storeId;
    
    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("amount")
    private Long amount;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("orderInfo")
    private String orderInfo;
    
    @JsonProperty("redirectUrl")
    private String redirectUrl;
    
    @JsonProperty("ipnUrl")
    private String ipnUrl;
    
    @JsonProperty("lang")
    private String lang;
    
    @JsonProperty("extraData")
    private String extraData;
    
    @JsonProperty("requestType")
    private String requestType;
    
    @JsonProperty("signature")
    private String signature;
}
