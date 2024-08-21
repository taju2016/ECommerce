package com.Ecommerce.project.repositories;

import com.Ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("SELECT ci from CartItem ci WHERE ci.cart.id = ?2 AND ci.product.id = ?1")
    CartItem findByCartIdProductId(Long productId, Long cartId);

    @Modifying
    @Query("DELETE from CartItem ci WHERE ci.cart.id = ?2 AND ci.product.id = ?1")
    void deleteCartItemByProductIdAndCartId(Long productId, Long cartId);
}
