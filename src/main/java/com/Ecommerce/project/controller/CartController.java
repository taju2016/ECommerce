package com.Ecommerce.project.controller;

import com.Ecommerce.project.configuration.AppConstants;
import com.Ecommerce.project.model.Cart;
import com.Ecommerce.project.payload.CartDTO;
import com.Ecommerce.project.payload.CartResponse;
import com.Ecommerce.project.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    CartService cartService;

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId,
                                                    @PathVariable int quantity) {
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);

    }

    @GetMapping("/carts")
    public ResponseEntity<CartResponse> getAllCarts(@RequestParam(name="pageNumber", defaultValue = AppConstants.CARTS_PAGE_NUMBER, required = false) Integer pageNumber,
                                                    @RequestParam(name="pageSize", defaultValue = AppConstants.CARTS_PAGE_SIZE, required = false) Integer pageSize,
                                                    @RequestParam(name="sortBy", defaultValue = AppConstants.CARTS_SORT_BY, required = false) String sortBy,
                                                    @RequestParam(name="sortDir", defaultValue = AppConstants.CARTS_SORT_DIR, required = false) String sortDir) {
        CartResponse cartResponse = cartService.getAllCarts(pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<CartResponse>(cartResponse, HttpStatus.OK);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getCartById() {
        CartDTO cartDTO = cartService.getCart();
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/cart/products/{productId}/quantity/{operation}/{quantity}")
    public ResponseEntity<CartDTO> updateCartProductQuantity(@PathVariable Long productId,
                                                             @PathVariable String operation,
                                                             @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId, operation, quantity);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long productId, @PathVariable Long cartId) {
        String status = cartService.deleteProductFromCart(cartId, productId);
        return new ResponseEntity<String>(status, HttpStatus.OK);
    }
}
