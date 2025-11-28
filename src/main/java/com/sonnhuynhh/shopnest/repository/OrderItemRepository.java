// src/main/java/com/sonnhuynhh/shopnest/repository/OrderItemRepository.java
package com.sonnhuynhh.shopnest.repository;

import com.sonnhuynhh.shopnest.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}