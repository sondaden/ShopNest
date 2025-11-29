// src/main/java/com/sonnhuynhh/shopnest/model/OrderStatus.java
package com.sonnhuynhh.shopnest.model;

public enum OrderStatus {
    PENDING,     // Chờ xử lý
    CONFIRMED,   // Đã xác nhận
    SHIPPING,    // Đang giao hàng
    COMPLETED,   // Đã giao thành công
    CANCELLED    // Đã hủy
}