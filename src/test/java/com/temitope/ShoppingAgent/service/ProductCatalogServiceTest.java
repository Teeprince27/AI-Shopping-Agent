package com.temitope.ShoppingAgent.service;

import com.temitope.ShoppingAgent.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProductCatalogServiceTest {

    @Autowired
    private ProductCatalogService catalogService;

    @Test
    @DisplayName("Catalog is initialized with 10 products on startup")
    void catalog_initializedWith10Products() {
        assertThat(catalogService.getInventorySnapshot()).hasSize(10);
    }

    @Test
    @DisplayName("searchProducts finds toys within ₦20,000 budget")
    void search_toysUnder20k() {
        List<Product> results = catalogService.searchProducts("toys kids", new BigDecimal("20000"));
        assertThat(results).isNotEmpty();
        results.forEach(p ->
                assertThat(p.getPrice()).isLessThanOrEqualTo(new BigDecimal("20000"))
        );
    }

    @Test
    @DisplayName("searchProducts excludes out-of-budget products")
    void search_excludesExpensiveProducts() {
        List<Product> results = catalogService.searchProducts("toys", new BigDecimal("10000"));
        results.forEach(p ->
                assertThat(p.getPrice()).isLessThanOrEqualTo(new BigDecimal("10000"))
        );
    }

    @Test
    @DisplayName("searchProducts returns results sorted by rating descending")
    void search_sortedByRating() {
        List<Product> results = catalogService.searchProducts("kids", null);
        for (int i = 0; i < results.size() - 1; i++) {
            assertThat(results.get(i).getRating())
                    .isGreaterThanOrEqualTo(results.get(i + 1).getRating());
        }
    }

    @Test
    @DisplayName("getProductById returns correct product for P001")
    void getProductById_found() {
        Optional<Product> product = catalogService.getProductById("P001");
        assertThat(product).isPresent();
        assertThat(product.get().getName()).isEqualTo("LEGO Classic Brick Box");
    }

    @Test
    @DisplayName("getProductById returns empty for unknown ID")
    void getProductById_notFound() {
        Optional<Product> product = catalogService.getProductById("PXXX");
        assertThat(product).isEmpty();
    }

    @Test
    @DisplayName("P010 (Puzzle Set) is correctly marked out of stock")
    void outOfStock_puzzle() {
        Optional<Product> product = catalogService.getProductById("P010");
        assertThat(product).isPresent();
        assertThat(product.get().isInStock()).isFalse();
    }
}
