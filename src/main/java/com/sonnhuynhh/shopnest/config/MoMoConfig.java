package com.sonnhuynhh.shopnest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for MoMo Payment Gateway
 * Sandbox endpoint: https://test-payment.momo.vn/v2/gateway/api
 * Production endpoint: https://payment.momo.vn/v2/gateway/api
 */
@Configuration
@ConfigurationProperties(prefix = "momo")
@Getter
@Setter
public class MoMoConfig {
    
    /**
     * MoMo Partner Code (từ MoMo Business Dashboard)
     */
    private String partnerCode;
    
    /**
     * MoMo Access Key
     */
    private String accessKey;
    
    /**
     * MoMo Secret Key (dùng để tạo chữ ký HMAC SHA256)
     */
    private String secretKey;
    
    /**
     * MoMo API Endpoint
     * - Sandbox: https://test-payment.momo.vn/v2/gateway/api
     * - Production: https://payment.momo.vn/v2/gateway/api
     */
    private String endpoint = "https://test-payment.momo.vn/v2/gateway/api";
    
    /**
     * URL callback IPN (MoMo gọi server để thông báo kết quả)
     */
    private String ipnUrl;
    
    /**
     * URL redirect về sau khi thanh toán
     */
    private String redirectUrl;
    
    /**
     * Request Type: captureWallet (thanh toán bằng ví MoMo)
     */
    private String requestType = "captureWallet";
    
    /**
     * Kiểm tra cấu hình có hợp lệ không
     */
    public boolean isConfigured() {
        return partnerCode != null && !partnerCode.isEmpty() 
                && accessKey != null && !accessKey.isEmpty()
                && secretKey != null && !secretKey.isEmpty();
    }
}
