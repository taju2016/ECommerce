package com.Ecommerce.project.service.Impl;

import com.Ecommerce.project.exceptions.APIException;
import com.Ecommerce.project.exceptions.ResourceNotFoundException;
import com.Ecommerce.project.model.Cart;
import com.Ecommerce.project.model.CartItem;
import com.Ecommerce.project.model.Product;
import com.Ecommerce.project.payload.*;
import com.Ecommerce.project.repositories.CartItemRepository;
import com.Ecommerce.project.repositories.CartRepository;
import com.Ecommerce.project.repositories.ProductRepository;
import com.Ecommerce.project.service.CartService;
import com.Ecommerce.project.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Override
    public CartDTO addProductToCart(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Cart cart = createCart();

        CartItem cartItem = cartItemRepository.findByCartIdProductId(productId, cart.getCartId());
        if(cartItem != null) throw new APIException("Product " + product.getProductName() + " already exists in the cart");

        if(product.getQuantity() == 0) throw new APIException(product.getProductName() + " is not available");

        if(product.getQuantity() < quantity)
            throw new APIException("Quantity of "
                + product.getProductName() + " has only "
                    + product.getQuantity() + " items left in stock");
        cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setQuantity(quantity);
        cartItem.setDiscount(product.getDiscount());

        cartItemRepository.save(cartItem);
        product.setQuantity(product.getQuantity() - quantity);
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice()*quantity));
        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItems.stream()
                .map(item -> {
                    ProductDTO productDTO = modelMapper.map(item, ProductDTO.class);
                    productDTO.setQuantity(product.getQuantity());
                    return productDTO;
                });
        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }

    @Override
    public CartResponse getAllCarts(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Cart> cartPage = cartRepository.findAll(pageDetails);

        List<Cart> carts = cartPage.getContent();
        if(carts == null || carts.size() == 0) throw new APIException("There are no Carts present");
        List<CartDTO> cartDTOList =  carts.stream()
                .map(cart -> {
                    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
                    cart.getCartItems().forEach(c ->
                            c.getProduct().setQuantity(c.getQuantity()));
                    //Internal Products also needs to be mapped
                    List<ProductDTO> products = cart.getCartItems().stream()
                            .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                            .toList();

                    cartDTO.setProducts(products);
                    return cartDTO;
                })
                .toList();

        CartResponse response = new CartResponse();
        response.setContent(cartDTOList);
        response.setPageNumber(cartPage.getNumber());
        response.setPageSize(cartPage.getSize());
        response.setTotalPages(cartPage.getTotalPages());
        response.setTotalElements(cartPage.getTotalElements());
        response.setIsLastPage(cartPage.isLast());
        return response;
    }

    @Override
    public CartDTO getCart() {
        String email = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByUserEmail(email);
        if(cart == null) throw new ResourceNotFoundException("Cart", "cartId", email);
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cart.getCartItems().forEach(c ->
                c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> products = cart.getCartItems().stream()
                .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                .toList();
        cartDTO.setProducts(products);
        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, String operation, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        int oppInt = 0;
        if(operation.equalsIgnoreCase("delete")) oppInt = -1;
        else oppInt = 1;
        if(oppInt == -1 &&product.getQuantity() < quantity) {
            throw new APIException(String.format("Stock not available, please try reducing quantity to %s", product.getQuantity()));
        }
        CartDTO cartDTO = getCart();
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        cartDTO.getProducts().stream()
                .forEach(productDTO -> {
                    if(productDTO.getProductId() == productId) {
                        productDTO.setQuantity(quantity);
                    }
                });

        CartItem cartItem = cartItemRepository.findByCartIdProductId(productId, cartDTO.getCartId());
        if(quantity == 0) {
            deleteProductFromCart(cartDTO.getCartId(), productId);
        } else {
            cartItem.setQuantity(quantity);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());
            cartItemRepository.save(cartItem);
        }
        return cartDTO;

    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findByCartIdProductId(cartId, productId);
        if(cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice()*cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByProductIdAndCartId(productId, cartId);

        return "Product " + cartItem.getProduct().getProductId() + " has been removed from the cart";
    }

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByUserEmail(authUtil.loggedInEmail());
        if(userCart != null) return userCart;
        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart = cartRepository.save(cart);
        return newCart;
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findByCartIdProductId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice()
                - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice
                + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepository.save(cartItem);
    }
}
