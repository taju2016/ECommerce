package com.Ecommerce.project.repositories;

import com.Ecommerce.project.model.Category;
import com.Ecommerce.project.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageDetails);

    Page<Product> findByProductNameContaining(String keyword, Pageable pageDetails);

    Optional<Product> findByProductName(String productName);
}
