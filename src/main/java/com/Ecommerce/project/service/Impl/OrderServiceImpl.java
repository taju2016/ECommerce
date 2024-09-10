package com.Ecommerce.project.service.Impl;

import com.Ecommerce.project.exceptions.APIException;
import com.Ecommerce.project.exceptions.ResourceNotFoundException;
import com.Ecommerce.project.model.*;
import com.Ecommerce.project.payload.OrderDTO;
import com.Ecommerce.project.payload.OrderItemDTO;
import com.Ecommerce.project.repositories.*;
import com.Ecommerce.project.service.CartService;
import com.Ecommerce.project.service.OrderService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    CartService cartService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ProductRepository productRepository;

//    @Override
//    @Transactional
//    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
//        Cart cart = cartRepository.findCartByUserEmail(emailId);
//        if (cart == null) {
//            throw new ResourceNotFoundException("Cart", "email", emailId);
//        }
//
//        Address address = addressRepository.findById(addressId)
//                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
//
//        Order order = new Order();
//        order.setUserEmail(emailId);
//        order.setOrderDate(LocalDate.now());
//        order.setTotalAmount(cart.getTotalPrice());
//        order.setOrderStatus("Order Accepted !");
//        order.setAddress(address);
//
//        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
//        payment.setOrder(order);
//        payment = paymentRepository.save(payment);
//        order.setPayment(payment);
//
//        Order savedOrder = orderRepository.save(order);
//
//        List<CartItem> cartItems = cart.getCartItems();
//        if (cartItems.isEmpty()) {
//            throw new APIException("Cart is empty");
//        }
//
//        List<OrderItem> orderItems = new ArrayList<>();
//        for (CartItem cartItem : cartItems) {
//            OrderItem orderItem = new OrderItem();
//            orderItem.setProduct(cartItem.getProduct());
//            orderItem.setQuantity(cartItem.getQuantity());
//            orderItem.setDiscount(cartItem.getDiscount());
//            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
//            orderItem.setMainOrder(savedOrder);
//            orderItems.add(orderItem);
//        }
//
//        orderItems = orderItemRepository.saveAll(orderItems);
//
//        cart.getCartItems().forEach(item -> {
//            int quantity = item.getQuantity();
//            Product product = item.getProduct();
//
//            // Reduce stock quantity
//            product.setQuantity(product.getQuantity() - quantity);
//
//            // Save product back to the database
//            productRepository.save(product);
//
//            // Remove items from cart
//            cartService.deleteProductFromCart(cart.getCartId(), item.getProduct().getProductId());
//        });
//
//        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
//        orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));
//
//        orderDTO.setAddressId(addressId);
//
//        return orderDTO;
//    }

    @Override
    @Transactional
    @Async
    public CompletableFuture<OrderDTO> placeOrder(String emailId, Long addressId, String paymentMethod, String pgName,
                                                  String pgPaymentId, String pgStatus, String pgResponseMessage) {
        return findCart(emailId)
                .thenCompose(cart -> findAddress(addressId)
                        .thenCompose(address -> createOrder(emailId, cart, address, paymentMethod, pgName, pgPaymentId, pgStatus, pgResponseMessage))
                        .thenCompose(savedOrder -> processCartItems(savedOrder, cart))
                        .thenApply(savedOrder -> mapToOrderDTO(savedOrder))
                );
    }

    private CompletableFuture<Cart> findCart(String emailId) {
        return CompletableFuture.supplyAsync(() -> {
            Cart cart = cartRepository.findCartByUserEmail(emailId);
            if (cart == null) {
                throw new ResourceNotFoundException("Cart", "email", emailId);
            }
            return cart;
        });
    }

    private CompletableFuture<Address> findAddress(Long addressId) {
        return CompletableFuture.supplyAsync(() -> addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId)));
    }

    private CompletableFuture<Order> createOrder(String emailId, Cart cart, Address address, String paymentMethod,
                                                 String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        Order order = new Order();
        order.setUserEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
        payment.setOrder(order);

        return CompletableFuture.supplyAsync(() -> paymentRepository.save(payment))
                .thenApply(savedPayment -> {
                    order.setPayment(savedPayment);
                    return orderRepository.save(order);
                });
    }

    private CompletableFuture<Order> processCartItems(Order savedOrder, Cart cart) {
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        List<CompletableFuture<Void>> orderItemFutures = cartItems.stream()
                .map(cartItem -> CompletableFuture.runAsync(() -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setDiscount(cartItem.getDiscount());
                    orderItem.setOrderedProductPrice(cartItem.getProductPrice());
                    orderItem.setMainOrder(savedOrder);
                    orderItemRepository.save(orderItem);
                }))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(orderItemFutures.toArray(new CompletableFuture[0]))
                .thenCompose(v -> updateProductsAndCart(cartItems, cart))
                .thenApply(v -> savedOrder);
    }

    private CompletableFuture<Void> updateProductsAndCart(List<CartItem> cartItems, Cart cart) {
        List<CompletableFuture<Void>> productUpdateFutures = cartItems.stream()
                .map(cartItem -> CompletableFuture.runAsync(() -> {
                    int quantity = cartItem.getQuantity();
                    Product product = cartItem.getProduct();
                    product.setQuantity(product.getQuantity() - quantity);
                    productRepository.save(product);
                    cartService.deleteProductFromCart(cart.getCartId(), product.getProductId());
                }))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(productUpdateFutures.toArray(new CompletableFuture[0]));
    }

    private OrderDTO mapToOrderDTO(Order savedOrder) {
        List<OrderItem> orderItems = orderItemRepository.findByMainOrder(savedOrder.getOrderId());
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderDTO.setOrderItems(orderItems.stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .collect(Collectors.toList()));
        return orderDTO;
    }
}
