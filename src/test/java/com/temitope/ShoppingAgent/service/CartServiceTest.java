package com.temitope.ShoppingAgent.service;

import com.temitope.ShoppingAgent.model.Cart;
import com.temitope.ShoppingAgent.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CartServiceTest {

    @Autowired
    private CartService cartService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id("P001").name("LEGO Classic Brick Box")
                .price(new BigDecimal("12500"))
                .currency("NGN").stockQuantity(15)
                .tags(List.of("kids", "toys"))
                .build();
    }

    @Test
    @DisplayName("Cart subtotal is correct after adding one item")
    void addToCart_calculatesSubtotal() {
        Cart cart = cartService.addToCart("CART-A", "U001", sampleProduct, 2);
        assertThat(cart.getSubtotal()).isEqualByComparingTo("25000");
        assertThat(cart.getTotal()).isEqualByComparingTo("25000");
    }

    @Test
    @DisplayName("KIDS20 applies 20% discount correctly")
    void applyDiscount_kids20() {
        cartService.addToCart("CART-B", "U001", sampleProduct, 1);  // ₦12,500
        String result = cartService.applyDiscount("CART-B", "KIDS20");

        Cart cart = cartService.getOrCreateCart("CART-B", "U001");
        assertThat(cart.getDiscountAmount()).isEqualByComparingTo("2500.00");
        assertThat(cart.getTotal()).isEqualByComparingTo("10000.00");
        assertThat(result).contains("20%");
    }

    @Test
    @DisplayName("SAVE10 applies 10% discount correctly")
    void applyDiscount_save10() {
        cartService.addToCart("CART-C", "U001", sampleProduct, 1);  // ₦12,500
        cartService.applyDiscount("CART-C", "SAVE10");

        Cart cart = cartService.getOrCreateCart("CART-C", "U001");
        assertThat(cart.getDiscountAmount()).isEqualByComparingTo("1250.00");
        assertThat(cart.getTotal()).isEqualByComparingTo("11250.00");
    }

    @Test
    @DisplayName("Invalid discount code returns error message")
    void applyDiscount_invalid_returnsError() {
        cartService.getOrCreateCart("CART-D", "U001");
        String result = cartService.applyDiscount("CART-D", "FAKE99");
        assertThat(result).containsIgnoringCase("Invalid");
    }

    @Test
    @DisplayName("Applying discount to missing cart returns error message")
    void applyDiscount_missingCart_returnsError() {
        String result = cartService.applyDiscount("NO-SUCH-CART", "SAVE10");
        assertThat(result).containsIgnoringCase("not found");
    }

    @Test
    @DisplayName("getValidDiscountCodes returns all 4 codes")
    void getValidDiscountCodes() {
        List<String> codes = cartService.getValidDiscountCodes();
        assertThat(codes).containsExactlyInAnyOrder("SAVE10", "KIDS20", "WELCOME15", "FLASH5");
    }
}
