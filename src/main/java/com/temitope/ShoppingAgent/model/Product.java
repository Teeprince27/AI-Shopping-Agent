package com.temitope.ShoppingAgent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String id;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private String currency;
    private int stockQuantity;
    private List<String> tags;          // e.g., ["kids", "toys", "age-5"]
    private double rating;
    private int reviewCount;
    private String imageUrl;

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public String getFormattedPrice() {
        return "₦" + price.toPlainString();
    }
}
