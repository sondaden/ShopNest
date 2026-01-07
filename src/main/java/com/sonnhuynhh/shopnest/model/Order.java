// src/main/java/com/sonnhuynhh/shopnest/model/Order.java
package com.sonnhuynhh.shopnest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    // getters & setters (hoặc dùng Lombok @Getter @Setter)
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @Column(name = "order_code", unique = true, nullable = false)
    private String orderCode;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @Getter
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Setter
    @Getter
    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Getter
    @Setter
    private String note;

    @Setter
    @Getter
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Setter
    @Getter
    @Column(name = "phone", nullable = false)
    private String phone;

    @Getter
    private LocalDateTime createdAt;

    @Getter
    private LocalDateTime updatedAt;

    @Setter
    @Getter
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for templates
    public String getCode() {
        return orderCode;
    }

    public String getStatusLabel() {
        return switch (status) {
            case PENDING -> "Chờ xử lý";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
        };
    }

    public String getPaymentStatusLabel() {
        return switch (paymentStatus) {
            case UNPAID -> "Chưa thanh toán";
            case PAID -> "Đã thanh toán";
            case REFUNDED -> "Đã hoàn tiền";
        };
    }

    public String getPaymentMethodLabel() {
        return switch (paymentMethod) {
            case COD -> "Thanh toán khi nhận hàng";
            case BANK_TRANSFER -> "Chuyển khoản";
            case MOMO -> "Ví MoMo";
        };
    }

    // Additional helper methods
    public String getCustomerPhone() {
        return phone;
    }

    public int getItemsCount() {
        return items != null ? items.size() : 0;
    }

    public boolean getIsPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }
}