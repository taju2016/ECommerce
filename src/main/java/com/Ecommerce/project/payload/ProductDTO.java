package com.Ecommerce.project.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long productId;

    @NotNull
    @Size(min = 5, message = "Name of Product should be at least 5 characters long")
    private String productName;

    private String description;

    private String image;

    private Integer quantity;

    private double price;

    private double discount;

    private double specialPrice;

}
