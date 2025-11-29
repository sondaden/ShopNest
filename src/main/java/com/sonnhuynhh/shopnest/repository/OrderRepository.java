// src/main/java/com/sonnhuynhh/shopnest/repository/OrderRepository.java
package com.sonnhuynhh.shopnest.repository;

import com.sonnhuynhh.shopnest.model.Order;
import com.sonnhuynhh.shopnest.model.OrderStatus;
import com.sonnhuynhh.shopnest.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findAllByOrderByCreatedAtDesc();
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    // Count by status
    long countByStatus(OrderStatus status);
    
    // Today's revenue - sum of paid orders today
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = :paymentStatus AND o.createdAt >= :startOfDay")
    BigDecimal sumTotalAmountByPaymentStatusAndCreatedAtAfter(@Param("paymentStatus") PaymentStatus paymentStatus, @Param("startOfDay") LocalDateTime startOfDay);
    
    // Monthly revenue
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = :paymentStatus AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal sumTotalAmountByPaymentStatusAndCreatedAtBetween(@Param("paymentStatus") PaymentStatus paymentStatus, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Count today's orders
    long countByCreatedAtAfter(LocalDateTime startOfDay);
    
    // Recent orders
    List<Order> findTop10ByOrderByCreatedAtDesc();
}
