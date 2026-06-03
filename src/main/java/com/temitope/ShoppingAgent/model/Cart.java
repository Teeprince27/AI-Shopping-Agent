package com.temitope.ShoppingAgent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    private String cartId;
    private String userId;
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String appliedDiscountCode;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
