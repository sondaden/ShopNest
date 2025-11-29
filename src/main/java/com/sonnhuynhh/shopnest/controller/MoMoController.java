package com.sonnhuynhh.shopnest.controller;

import com.sonnhuynhh.shopnest.dto.MoMoIPNRequest;
import com.sonnhuynhh.shopnest.model.Order;
import com.sonnhuynhh.shopnest.model.PaymentStatus;
import com.sonnhuynhh.shopnest.repository.OrderRepository;
import com.sonnhuynhh.shopnest.service.MoMoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý thanh toán MoMo
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MoMoController {
    
    private final MoMoService moMoService;
    private final OrderRepository orderRepository;
    
    /**
     * API endpoint nhận IPN callback từ MoMo
     * MoMo gọi đến endpoint này khi thanh toán hoàn tất
     */
    @PostMapping("/api/payment/momo/ipn")
    @ResponseBody
    public ResponseEntity<?> handleIPN(@RequestBody MoMoIPNRequest ipnRequest) {
        log.info("Received MoMo IPN: orderId={}, resultCode={}", 
                ipnRequest.getOrderId(), ipnRequest.getResultCode());
        
        try {
            boolean success = moMoService.processIPN(ipnRequest);
            
            if (success) {
                // MoMo yêu cầu trả về status 204 nếu xử lý thành công
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to process IPN"
                ));
            }
        } catch (Exception e) {
            log.error("Error processing MoMo IPN: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint redirect từ MoMo sau khi thanh toán
     * User được redirect về đây sau khi thanh toán trên MoMo
     */
    @GetMapping("/payment/momo/return")
    public String handleReturn(
            @RequestParam(required = false) String partnerCode,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) Long amount,
            @RequestParam(required = false) String orderInfo,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) Long transId,
            @RequestParam(required = false) Integer resultCode,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String payType,
            @RequestParam(required = false) Long responseTime,
            @RequestParam(required = false) String extraData,
            @RequestParam(required = false) String signature,
            Model model) {
        
        log.info("MoMo Return: orderId={}, resultCode={}, message={}", orderId, resultCode, message);
        
        // Xử lý kết quả
        String orderCode = moMoService.processRedirect(orderId, resultCode, message);
        
        // Lấy thông tin order
        Order order = orderRepository.findAll().stream()
                .filter(o -> o.getOrderCode().equals(orderCode))
                .findFirst()
                .orElse(null);
        
        if (order == null) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Không tìm thấy đơn hàng");
            return "payment-result";
        }
        
        // Kiểm tra kết quả thanh toán
        boolean isSuccess = resultCode != null && resultCode == 0;
        
        if (isSuccess) {
            // Cập nhật trạng thái thanh toán (nếu chưa được cập nhật bởi IPN)
            if (order.getPaymentStatus() != PaymentStatus.PAID) {
                order.setPaymentStatus(PaymentStatus.PAID);
                orderRepository.save(order);
            }
            
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
            model.addAttribute("orderCode", orderCode);
            model.addAttribute("transId", transId);
            model.addAttribute("amount", amount);
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message", message != null ? message : "Thanh toán thất bại");
            model.addAttribute("orderCode", orderCode);
        }
        
        model.addAttribute("order", order);
        
        return "payment-result";
    }
    
    /**
     * API kiểm tra trạng thái thanh toán của đơn hàng
     */
    @GetMapping("/api/payment/momo/status/{orderCode}")
    @ResponseBody
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String orderCode) {
        Order order = orderRepository.findAll().stream()
                .filter(o -> o.getOrderCode().equals(orderCode))
                .findFirst()
                .orElse(null);
        
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Map.of(
            "orderCode", orderCode,
            "paymentStatus", order.getPaymentStatus().name(),
            "isPaid", order.getPaymentStatus() == PaymentStatus.PAID
        ));
    }
}
