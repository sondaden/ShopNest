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
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // USER: Đặt hàng từ giỏ hàng
    public OrderResponse checkout(CheckoutRequest request) {
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

        orderRepository.save(order);

        // XÓA GIỎ HÀNG SAU KHI ĐẶT THÀNH CÔNG
        cartService.clearCart();

        return mapToResponse(order);
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
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        if (status == OrderStatus.DELIVERED) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        return mapToResponse(order);
    }

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