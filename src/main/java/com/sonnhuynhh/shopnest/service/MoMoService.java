package com.sonnhuynhh.shopnest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonnhuynhh.shopnest.config.MoMoConfig;
import com.sonnhuynhh.shopnest.dto.MoMoIPNRequest;
import com.sonnhuynhh.shopnest.dto.MoMoPaymentRequest;
import com.sonnhuynhh.shopnest.dto.MoMoPaymentResponse;
import com.sonnhuynhh.shopnest.model.Order;
import com.sonnhuynhh.shopnest.model.PaymentStatus;
import com.sonnhuynhh.shopnest.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Service xử lý thanh toán MoMo
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MoMoService {
    
    private final MoMoConfig moMoConfig;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Tạo payment request gửi đến MoMo
     * @param order Đơn hàng cần thanh toán
     * @return MoMoPaymentResponse chứa payUrl để redirect
     */
    public MoMoPaymentResponse createPayment(Order order) {
        if (!moMoConfig.isConfigured()) {
            throw new RuntimeException("MoMo payment is not configured!");
        }
        
        String requestId = UUID.randomUUID().toString();
        String orderId = order.getOrderCode() + "_" + System.currentTimeMillis();
        Long amount = order.getTotalAmount().longValue();
        String orderInfo = "Thanh toán đơn hàng " + order.getOrderCode() + " tại ShopNest";
        String extraData = order.getOrderCode(); // Lưu orderCode để verify sau
        
        // Tạo raw signature theo format của MoMo
        String rawSignature = String.format(
            "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
            moMoConfig.getAccessKey(),
            amount,
            extraData,
            moMoConfig.getIpnUrl(),
            orderId,
            orderInfo,
            moMoConfig.getPartnerCode(),
            moMoConfig.getRedirectUrl(),
            requestId,
            moMoConfig.getRequestType()
        );
        
        log.debug("MoMo Raw Signature: {}", rawSignature);
        
        // Tạo chữ ký HMAC SHA256
        String signature = hmacSHA256(rawSignature, moMoConfig.getSecretKey());
        
        // Build request
        MoMoPaymentRequest paymentRequest = MoMoPaymentRequest.builder()
                .partnerCode(moMoConfig.getPartnerCode())
                .partnerName("ShopNest")
                .storeId(moMoConfig.getPartnerCode())
                .requestId(requestId)
                .amount(amount)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(moMoConfig.getRedirectUrl())
                .ipnUrl(moMoConfig.getIpnUrl())
                .lang("vi")
                .extraData(extraData)
                .requestType(moMoConfig.getRequestType())
                .signature(signature)
                .build();
        
        // Gửi request đến MoMo
        try {
            String requestBody = objectMapper.writeValueAsString(paymentRequest);
            log.info("MoMo Payment Request: {}", requestBody);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<MoMoPaymentResponse> response = restTemplate.exchange(
                    moMoConfig.getEndpoint() + "/create",
                    HttpMethod.POST,
                    entity,
                    MoMoPaymentResponse.class
            );
            
            MoMoPaymentResponse moMoResponse = response.getBody();
            log.info("MoMo Payment Response: resultCode={}, message={}, payUrl={}", 
                    moMoResponse != null ? moMoResponse.getResultCode() : null,
                    moMoResponse != null ? moMoResponse.getMessage() : null,
                    moMoResponse != null ? moMoResponse.getPayUrl() : null);
            
            return moMoResponse;
            
        } catch (Exception e) {
            log.error("Error creating MoMo payment: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo thanh toán MoMo: " + e.getMessage());
        }
    }
    
    /**
     * Xử lý IPN callback từ MoMo
     * @param ipnRequest Request từ MoMo
     * @return true nếu xử lý thành công
     */
    @Transactional
    public boolean processIPN(MoMoIPNRequest ipnRequest) {
        log.info("Processing MoMo IPN: orderId={}, resultCode={}, transId={}", 
                ipnRequest.getOrderId(), ipnRequest.getResultCode(), ipnRequest.getTransId());
        
        // Verify signature
        if (!verifySignature(ipnRequest)) {
            log.error("Invalid MoMo signature for orderId: {}", ipnRequest.getOrderId());
            return false;
        }
        
        // Lấy orderCode từ extraData hoặc từ orderId
        String orderCode = ipnRequest.getExtraData();
        if (orderCode == null || orderCode.isEmpty()) {
            // Lấy từ orderId (format: ORDERCODE_timestamp)
            String orderId = ipnRequest.getOrderId();
            if (orderId.contains("_")) {
                orderCode = orderId.substring(0, orderId.lastIndexOf("_"));
            } else {
                orderCode = orderId;
            }
        }
        
        // Tìm order trong database
        final String finalOrderCode = orderCode;
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElse(null);
        
        if (order == null) {
            log.error("Order not found: {}", finalOrderCode);
            return false;
        }
        
        // Cập nhật trạng thái thanh toán
        if (ipnRequest.isSuccess()) {
            order.setPaymentStatus(PaymentStatus.PAID);
            log.info("Order {} marked as PAID via MoMo, transId: {}", orderCode, ipnRequest.getTransId());
        } else {
            log.warn("MoMo payment failed for order {}: {}", orderCode, ipnRequest.getMessage());
        }
        
        orderRepository.save(order);
        return true;
    }
    
    /**
     * Xử lý redirect callback từ MoMo
     * @param orderId Order ID từ MoMo
     * @param resultCode Kết quả thanh toán
     * @return Order code nếu hợp lệ
     */
    public String processRedirect(String orderId, Integer resultCode, String message) {
        log.info("MoMo Redirect: orderId={}, resultCode={}, message={}", orderId, resultCode, message);
        
        // Lấy orderCode từ orderId
        String orderCode;
        if (orderId.contains("_")) {
            orderCode = orderId.substring(0, orderId.lastIndexOf("_"));
        } else {
            orderCode = orderId;
        }
        
        return orderCode;
    }
    
    /**
     * Verify chữ ký từ MoMo IPN
     */
    private boolean verifySignature(MoMoIPNRequest request) {
        String rawSignature = String.format(
            "accessKey=%s&amount=%d&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%d&resultCode=%d&transId=%d",
            moMoConfig.getAccessKey(),
            request.getAmount(),
            request.getExtraData() != null ? request.getExtraData() : "",
            request.getMessage(),
            request.getOrderId(),
            request.getOrderInfo(),
            request.getOrderType() != null ? request.getOrderType() : "",
            request.getPartnerCode(),
            request.getPayType() != null ? request.getPayType() : "",
            request.getRequestId(),
            request.getResponseTime(),
            request.getResultCode(),
            request.getTransId()
        );
        
        String expectedSignature = hmacSHA256(rawSignature, moMoConfig.getSecretKey());
        boolean valid = expectedSignature.equals(request.getSignature());
        
        if (!valid) {
            log.debug("Signature mismatch. Expected: {}, Received: {}", expectedSignature, request.getSignature());
        }
        
        return valid;
    }
    
    /**
     * Tạo chữ ký HMAC SHA256
     */
    private String hmacSHA256(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), 
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            log.error("Error creating HMAC SHA256: {}", e.getMessage());
            throw new RuntimeException("Error creating signature", e);
        }
    }
    
    /**
     * Kiểm tra MoMo đã được cấu hình chưa
     */
    public boolean isConfigured() {
        return moMoConfig.isConfigured();
    }
}
