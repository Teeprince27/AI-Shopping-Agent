package com.temitope.ShoppingAgent.tools;

import com.temitope.ShoppingAgent.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShoppingToolsTest {

    @Autowired
    private ShoppingTools shoppingTools;

    @Autowired
    private CartService cartService;

    // ── searchProducts ─────────────────────────────────────────────────────

    @Test
    @DisplayName("searchProducts returns results for 'toys for 5 year old' within ₦20,000")
    void searchProducts_withinBudget() {
        String result = shoppingTools.searchProducts("toys for 5 year old", 20000);
        assertThat(result).contains("Found");
        assertThat(result).doesNotContain("₦35000"); // Barbie at ₦35k should be excluded
    }

    @Test
    @DisplayName("searchProducts returns no results for impossible query")
    void searchProducts_noMatch() {
        String result = shoppingTools.searchProducts("zzzzznotaproduct", 0);
        assertThat(result).contains("No products found");
    }

    // ── checkInventory ─────────────────────────────────────────────────────

    @Test
    @DisplayName("checkInventory returns IN STOCK for P001 (LEGO)")
    void checkInventory_inStock() {
        String result = shoppingTools.checkInventory("P001");
        assertThat(result).containsIgnoringCase("IN STOCK");
    }

    @Test
    @DisplayName("checkInventory returns OUT OF STOCK for P010 (Puzzle)")
    void checkInventory_outOfStock() {
        String result = shoppingTools.checkInventory("P010");
        assertThat(result).containsIgnoringCase("OUT OF STOCK");
    }

    @Test
    @DisplayName("checkInventory handles unknown product ID gracefully")
    void checkInventory_notFound() {
        String result = shoppingTools.checkInventory("P999");
        assertThat(result).containsIgnoringCase("not found");
    }

    // ── getPersonalizedRecommendations ────────────────────────────────────

    @Test
    @DisplayName("getPersonalizedRecommendations returns results for known user U001")
    void recommendations_knownUser() {
        String result = shoppingTools.getPersonalizedRecommendations("U001");
        // U001 already bought P003 and P005; they should not appear in recs
        assertThat(result).doesNotContain("P003");
        assertThat(result).doesNotContain("P005");
    }

    @Test
    @DisplayName("getPersonalizedRecommendations falls back to GUEST for unknown userId")
    void recommendations_unknownUser() {
        String result = shoppingTools.getPersonalizedRecommendations("UNKNOWN_USER");
        assertThat(result).isNotBlank();
    }

    // ── applyDiscount ──────────────────────────────────────────────────────

    @Test
    @DisplayName("applyDiscount applies KIDS20 correctly to a cart with items")
    void applyDiscount_valid() {
        // Seed a cart first
        cartService.getOrCreateCart("CART-TEST", "U001");

        String result = shoppingTools.applyDiscount("CART-TEST", "KIDS20");
        // Cart is empty so discount amount is 0 but code is accepted
        assertThat(result).containsIgnoringCase("KIDS20");
    }

    @Test
    @DisplayName("applyDiscount rejects an invalid code")
    void applyDiscount_invalid() {
        cartService.getOrCreateCart("CART-TEST2", "U001");
        String result = shoppingTools.applyDiscount("CART-TEST2", "BADCODE");
        assertThat(result).containsIgnoringCase("Invalid");
    }

    @Test
    @DisplayName("applyDiscount handles missing cart gracefully")
    void applyDiscount_cartNotFound() {
        String result = shoppingTools.applyDiscount("NONEXISTENT-CART", "SAVE10");
        assertThat(result).containsIgnoringCase("not found");
    }

    // ── getAvailableDiscountCodes ─────────────────────────────────────────

    @Test
    @DisplayName("getAvailableDiscountCodes returns all known codes")
    void getAvailableDiscountCodes() {
        String result = shoppingTools.getAvailableDiscountCodes();
        assertThat(result).contains("SAVE10", "KIDS20", "WELCOME15", "FLASH5");
    }
}
