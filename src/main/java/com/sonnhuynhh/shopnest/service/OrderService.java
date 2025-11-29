// src/main/java/com/sonnhuynhh/shopnest/service/OrderService.java
package com.sonnhuynhh.shopnest.service;

import com.sonnhuynhh.shopnest.dto.*;
import com.sonnhuynhh.shopnest.model.*;
import com.sonnhuynhh.shopnest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();
        // Thử tìm bằng username trước, sau đó bằng email (cho OAuth2 users)
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new RuntimeException("User not found: " + identifier));
    }

    // USER: Đặt hàng từ giỏ hàng
    public OrderResponse checkout(CheckoutRequest request) {
        Order order = createOrder(request);
        
        // XÓA GIỎ HÀNG SAU KHI ĐẶT THÀNH CÔNG
        cartService.clearCart();

        return mapToResponse(order);
    }
    
    // Tạo order mà không xóa giỏ hàng (dùng cho MoMo payment)
    public Order createOrder(CheckoutRequest request) {
        User user = getCurrentUser();
        CartResponse cart = cartService.getCart();

        if (cart.items().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống!");
        }

        // Tạo đơn hàng
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setUser(user);
        order.setTotalAmount(cart.totalPrice());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.paymentMethod());
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setShippingAddress(request.shippingAddress());
        order.setCustomerName(request.customerName());
        order.setPhone(request.phone());
        order.setNote(request.note());

        // Thêm items từ giỏ hàng
        for (CartItemResponse item : cart.items()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            Product product = new Product();
            product.setId(item.productId());
            orderItem.setProduct(product);
            orderItem.setProductName(item.productName());
            orderItem.setProductImage(item.imageUrl());
            orderItem.setPrice(item.price());
            orderItem.setQuantity(item.quantity());
            orderItem.setSubtotal(item.subtotal());
            order.getItems().add(orderItem);
        }

        return orderRepository.save(order);
    }
    
    // Xóa giỏ hàng (public method cho MoMo callback)
    public void clearCartAfterPayment() {
        cartService.clearCart();
    }
    
    // Tìm order theo orderCode
    public Order findByOrderCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderCode));
    }

    // USER: Xem lịch sử đơn hàng
    public List<OrderResponse> getMyOrders() {
        User user = getCurrentUser();
        return orderRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ADMIN: Xem tất cả đơn hàng
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ADMIN: Cập nhật trạng thái đơn hàng
    public OrderResponse updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        order.setStatus(status);
        if (status == OrderStatus.COMPLETED) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        return mapToResponse(orderRepository.save(order));
    }
    
    // ADMIN: Cập nhật trạng thái thanh toán
    public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        order.setPaymentStatus(paymentStatus);
        return mapToResponse(orderRepository.save(order));
    }
    
    // ADMIN: Xem chi tiết đơn hàng
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        return mapToResponse(order);
    }
    
    // ADMIN: Thống kê đơn hàng
    public OrderStatistics getOrderStatistics() {
        List<Order> allOrders = orderRepository.findAll();
        
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
        long confirmedOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count();
        long shippingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPING).count();
        long completedOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        long cancelledOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        
        java.math.BigDecimal totalRevenue = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        return new OrderStatistics(totalOrders, pendingOrders, confirmedOrders, shippingOrders, 
                                   completedOrders, cancelledOrders, totalRevenue);
    }
    
    // Statistics DTO
    public record OrderStatistics(
        long totalOrders,
        long pendingOrders,
        long confirmedOrders,
        long shippingOrders,
        long completedOrders,
        long cancelledOrders,
        java.math.BigDecimal totalRevenue
    ) {}

    private String generateOrderCode() {
        return "SHOP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(oi -> new OrderResponse.OrderItemResponse(
                        oi.getProduct().getId(),
                        oi.getProductName(),
                        oi.getProductImage(),
                        oi.getPrice(),
                        oi.getQuantity(),
                        oi.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderCode(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getShippingAddress(),
                order.getCustomerName(),
                order.getPhone(),
                order.getNote(),
                order.getCreatedAt(),
                items
        );
    }
}