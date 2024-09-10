package com.Ecommerce.project.service;

import com.Ecommerce.project.payload.OrderDTO;
import jakarta.transaction.Transactional;

import java.util.concurrent.CompletableFuture;

public interface OrderService {

    @Transactional
    CompletableFuture<OrderDTO> placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage);
}
