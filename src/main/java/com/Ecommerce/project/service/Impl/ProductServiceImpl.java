package com.Ecommerce.project.service.Impl;

import com.Ecommerce.project.exceptions.APIException;
import com.Ecommerce.project.exceptions.ResourceNotFoundException;
import com.Ecommerce.project.model.Cart;
import com.Ecommerce.project.model.Category;
import com.Ecommerce.project.model.Product;
import com.Ecommerce.project.payload.CartDTO;
import com.Ecommerce.project.payload.CartResponse;
import com.Ecommerce.project.payload.ProductDTO;
import com.Ecommerce.project.payload.ProductResponse;
import com.Ecommerce.project.repositories.CartRepository;
import com.Ecommerce.project.repositories.CategoryRepository;
import com.Ecommerce.project.repositories.ProductRepository;
import com.Ecommerce.project.service.CartService;
import com.Ecommerce.project.service.FileService;
import com.Ecommerce.project.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${product.image}")
    private String path;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
        Optional<Product> optionalProduct = productRepository.findByProductName(productDTO.getProductName());
        if(!optionalProduct.isEmpty()) throw new APIException(String.format("Product with name : %s already exists", productDTO.getProductName()));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        Product requestProduct = modelMapper.map(productDTO, Product.class);
        requestProduct.setCategory(category);
        double specialPrice = requestProduct.getPrice() - ((requestProduct.getDiscount()/100)*requestProduct.getPrice());
        requestProduct.setSpecialPrice(specialPrice);
        Product savedProduct = productRepository.save(requestProduct);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageDetails);

        List<Product> products = productPage.getContent();
        if(products == null || products.size() == 0) throw new APIException("There are no Products present");
        List<ProductDTO> productDTOS =  products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        ProductResponse response = new ProductResponse();
        response.setContent(productDTOS);
        response.setPageNumber(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalElements(productPage.getTotalElements());
        response.setIsLastPage(productPage.isLast());
        return response;
    }

    @Override
    public ProductResponse getProductById(Long productId) {

        Optional<Product> products = productRepository.findById(productId);
        Product product = products
                .orElseThrow(() -> new APIException("There are no Products present"));
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
        List<ProductDTO> productDTOList = new ArrayList<>();
        ProductResponse response = new ProductResponse();
        response.setContent(productDTOList);
        return response;

    }


    @Override
    public ProductResponse getProductsByCategoryId(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
        Page<Product> productPage = productRepository.findByCategory(category, pageDetails);
        List<Product> products = productPage.getContent();
        if(products == null || products.size() == 0) throw new APIException("There are no Products present");
        List<ProductDTO> productDTOS =  products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        ProductResponse response = new ProductResponse();
        response.setContent(productDTOS);
        response.setPageNumber(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalElements(productPage.getTotalElements());
        response.setIsLastPage(productPage.isLast());
        return response;
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findByProductNameContaining(keyword, pageDetails);
        List<Product> products = productPage.getContent();
        if(products == null || products.size() == 0) throw new APIException(String.format("There are no Products present with Keyword %s", keyword));
        List<ProductDTO> productDTOS =  products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();
        ProductResponse response = new ProductResponse();
        response.setContent(productDTOS);
        response.setPageNumber(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalElements(productPage.getTotalElements());
        response.setIsLastPage(productPage.isLast());
        return response;
    }

    @Override
    public ProductDTO updateProductDTO(Long productId, ProductDTO productDTO) {

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        existingProduct.setProductName(productDTO.getProductName());
        existingProduct.setImage(productDTO.getImage());
        existingProduct.setDiscount(productDTO.getDiscount());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setSpecialPrice(productDTO.getSpecialPrice());
        existingProduct.setDescription(productDTO.getDescription());
        Product savedProduct = productRepository.save(existingProduct);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProductById(Long productId) {

        Optional<Product> products = productRepository.findById(productId);
        Product deleteProduct = products
                .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
        productRepository.delete(deleteProduct);
        ProductDTO deletedProductDTO = modelMapper.map(products.get(), ProductDTO.class);
        return deletedProductDTO;
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {

        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        String fileName = fileService.uploadImage(path, image);

        productFromDB.setImage(fileName);
        productRepository.save(productFromDB);

        return modelMapper.map(productFromDB, ProductDTO.class);
    }


}
