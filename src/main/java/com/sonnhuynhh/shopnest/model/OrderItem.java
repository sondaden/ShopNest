// src/main/java/com/sonnhuynhh/shopnest/model/OrderItem.java
package com.sonnhuynhh.shopnest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "order_items")
public class OrderItem {
    // getters & setters...
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Setter
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Setter
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Setter
    @Column(name = "product_image")
    private String productImage;

    @Setter
    private BigDecimal price;
    @Setter
    private int quantity;
    @Setter
    private BigDecimal subtotal;
}