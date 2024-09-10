package com.Ecommerce.project.controller;

import com.Ecommerce.project.payload.OrderDTO;
import com.Ecommerce.project.payload.OrderRequestDTO;
import com.Ecommerce.project.service.OrderService;
import com.Ecommerce.project.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    public CompletableFuture<ResponseEntity<OrderDTO>> orderProducts(
            @PathVariable String paymentMethod,
            @RequestBody OrderRequestDTO orderRequestDTO) {

        String emailId = authUtil.loggedInEmail();

        // Call the asynchronous method
        return orderService.placeOrder(
                        emailId,
                        orderRequestDTO.getAddressId(),
                        paymentMethod,
                        orderRequestDTO.getPgName(),
                        orderRequestDTO.getPgPaymentId(),
                        orderRequestDTO.getPgStatus(),
                        orderRequestDTO.getPgResponseMessage()
                ).thenApply(order -> new ResponseEntity<>(order, HttpStatus.CREATED))
                .exceptionally(ex -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
