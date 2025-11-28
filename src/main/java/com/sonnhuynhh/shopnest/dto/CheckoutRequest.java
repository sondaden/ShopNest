// src/main/java/com/sonnhuynhh/shopnest/dto/CheckoutRequest.java
package com.sonnhuynhh.shopnest.dto;

import com.sonnhuynhh.shopnest.model.PaymentMethod;

public record CheckoutRequest(
        String shippingAddress,
        String phone,
        String customerName,
        String note,
        PaymentMethod paymentMethod
) {}