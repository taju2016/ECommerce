package com.Ecommerce.project.service;

import com.Ecommerce.project.payload.ProductDTO;
import com.Ecommerce.project.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {

    ProductDTO addProduct(ProductDTO productDTO, Long categoryId);

    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    ProductResponse getProductsByCategoryId(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    ProductResponse getProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDir);

    ProductDTO updateProductDTO(Long productId, ProductDTO productDTO);

    ProductDTO deleteProductById(Long productId);

    ProductResponse getProductById(Long productId);

    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;
}
