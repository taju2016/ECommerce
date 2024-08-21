package com.Ecommerce.project.controller;

import com.Ecommerce.project.configuration.AppConstants;
import com.Ecommerce.project.payload.ProductDTO;
import com.Ecommerce.project.payload.ProductResponse;
import com.Ecommerce.project.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts(@RequestParam(name="pageNumber", defaultValue = AppConstants.PRODUCTS_PAGE_NUMBER, required = false) Integer pageNumber,
                                                          @RequestParam(name="pageSize", defaultValue = AppConstants.PRODUCTS_PAGE_SIZE, required = false) Integer pageSize,
                                                          @RequestParam(name="sortBy", defaultValue = AppConstants.PRODUCTS_SORT_BY, required = false) String sortBy,
                                                          @RequestParam(name="sortDir", defaultValue = AppConstants.PRODUCTS_SORT_DIR, required = false) String sortDir) {
        ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/product/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        ProductResponse productResponse = productService.getProductById(productId);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategoryId(@PathVariable Long categoryId,
                                                          @RequestParam(name="pageNumber", defaultValue = AppConstants.PRODUCTS_PAGE_NUMBER, required = false) Integer pageNumber,
                                                          @RequestParam(name="pageSize", defaultValue = AppConstants.PRODUCTS_PAGE_SIZE, required = false) Integer pageSize,
                                                          @RequestParam(name="sortBy", defaultValue = AppConstants.PRODUCTS_SORT_BY, required = false) String sortBy,
                                                          @RequestParam(name="sortDir", defaultValue = AppConstants.PRODUCTS_SORT_DIR, required = false) String sortDir) {
        ProductResponse productResponse = productService.getProductsByCategoryId(categoryId, pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword(@PathVariable String keyword,
                                                                   @RequestParam(name="pageNumber", defaultValue = AppConstants.PRODUCTS_PAGE_NUMBER, required = false) Integer pageNumber,
                                                                   @RequestParam(name="pageSize", defaultValue = AppConstants.PRODUCTS_PAGE_SIZE, required = false) Integer pageSize,
                                                                   @RequestParam(name="sortBy", defaultValue = AppConstants.PRODUCTS_SORT_BY, required = false) String sortBy,
                                                                   @RequestParam(name="sortDir", defaultValue = AppConstants.PRODUCTS_SORT_DIR, required = false) String sortDir) {
        ProductResponse productResponse = productService.getProductsByKeyword(keyword, pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @PostMapping("/admin/categories/{categoryId}/addProduct")
    public ResponseEntity<ProductDTO> addProduct(@RequestBody ProductDTO productDTO,
                                                 @PathVariable Long categoryId) {
        ProductDTO addedProductDTO = productService.addProduct(productDTO, categoryId);
        return new ResponseEntity<>(addedProductDTO, HttpStatus.CREATED);
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long productId,
                                                 @RequestBody ProductDTO productDTO) {
        ProductDTO savedProductDTO = productService.updateProductDTO(productId, productDTO);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/product/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId) {
        ProductDTO deletedProductDTO = productService.deleteProductById(productId);
        return new ResponseEntity<>(deletedProductDTO, HttpStatus.OK);
    }

    @PutMapping(value = "public/{productId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId, @RequestParam(name = "image")MultipartFile image) throws IOException {
        ProductDTO updatedProduct = productService.updateProductImage(productId, image);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

}
