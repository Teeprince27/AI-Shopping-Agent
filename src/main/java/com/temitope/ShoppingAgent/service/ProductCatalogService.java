package com.temitope.ShoppingAgent.service;

import com.temitope.ShoppingAgent.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simulates a product catalog. In production, this would connect to a real DB or external API.
 */
@Service
public class ProductCatalogService {

    private final Map<String, Product> catalog = new HashMap<>();

    @PostConstruct
    public void initCatalog() {
        List<Product> products = List.of(
                Product.builder()
                        .id("P001").name("LEGO Classic Brick Box")
                        .category("Toys").description("Creative LEGO set for kids aged 4-8")
                        .price(new BigDecimal("12500")).currency("NGN").stockQuantity(15)
                        .tags(List.of("kids", "toys", "age-5", "educational", "lego"))
                        .rating(4.8).reviewCount(320).build(),

                Product.builder()
                        .id("P002").name("Fisher-Price Musical Giraffe")
                        .category("Toys").description("Soft plush musical toy for young children")
                        .price(new BigDecimal("8000")).currency("NGN").stockQuantity(30)
                        .tags(List.of("kids", "toys", "age-5", "musical", "plush"))
                        .rating(4.5).reviewCount(210).build(),

                Product.builder()
                        .id("P003").name("Crayola 64-Piece Crayon Set")
                        .category("Art & Craft").description("Premium coloring crayons for children")
                        .price(new BigDecimal("4500")).currency("NGN").stockQuantity(50)
                        .tags(List.of("kids", "art", "age-5", "educational", "drawing"))
                        .rating(4.7).reviewCount(180).build(),

                Product.builder()
                        .id("P004").name("Magnetic Drawing Board")
                        .category("Art & Craft").description("Mess-free drawing tablet with magnetic pen")
                        .price(new BigDecimal("6800")).currency("NGN").stockQuantity(22)
                        .tags(List.of("kids", "art", "age-5", "educational"))
                        .rating(4.6).reviewCount(145).build(),

                Product.builder()
                        .id("P005").name("Children's Storybook Bundle (5 books)")
                        .category("Books").description("Collection of 5 classic illustrated storybooks")
                        .price(new BigDecimal("9500")).currency("NGN").stockQuantity(18)
                        .tags(List.of("kids", "books", "age-5", "educational", "reading"))
                        .rating(4.9).reviewCount(412).build(),

                Product.builder()
                        .id("P006").name("Hot Wheels 20-Car Gift Pack")
                        .category("Toys").description("Pack of 20 die-cast miniature toy cars")
                        .price(new BigDecimal("15000")).currency("NGN").stockQuantity(10)
                        .tags(List.of("kids", "toys", "age-5", "cars", "boys"))
                        .rating(4.7).reviewCount(290).build(),

                Product.builder()
                        .id("P007").name("Barbie Dreamhouse Playset")
                        .category("Toys").description("Large Barbie house with furniture and accessories")
                        .price(new BigDecimal("35000")).currency("NGN").stockQuantity(5)
                        .tags(List.of("kids", "toys", "girls", "dolls"))
                        .rating(4.4).reviewCount(175).build(),

                Product.builder()
                        .id("P008").name("LeapFrog Learning Tablet")
                        .category("Electronics").description("Kid-friendly learning tablet with games and lessons")
                        .price(new BigDecimal("18500")).currency("NGN").stockQuantity(8)
                        .tags(List.of("kids", "electronics", "age-5", "educational"))
                        .rating(4.3).reviewCount(98).build(),

                Product.builder()
                        .id("P009").name("Kinetic Sand Play Set")
                        .category("Toys").description("Moldable sand with molds for creative play")
                        .price(new BigDecimal("11000")).currency("NGN").stockQuantity(25)
                        .tags(List.of("kids", "toys", "age-5", "creative", "sensory"))
                        .rating(4.6).reviewCount(160).build(),

                Product.builder()
                        .id("P010").name("Puzzle Set (4 in 1, 12-48 pieces)")
                        .category("Toys").description("Age-appropriate jigsaw puzzles bundle")
                        .price(new BigDecimal("7200")).currency("NGN").stockQuantity(0) // OUT OF STOCK
                        .tags(List.of("kids", "toys", "age-5", "educational", "puzzle"))
                        .rating(4.5).reviewCount(130).build()
        );

        products.forEach(p -> catalog.put(p.getId(), p));
    }

    /**
     * Full-text search across name, category, description, and tags.
     */
    public List<Product> searchProducts(String query, BigDecimal maxPrice) {
        String lowerQuery = query.toLowerCase();
        String[] keywords = lowerQuery.split("\\s+");

        return catalog.values().stream()
                .filter(p -> {
                    String searchable = (p.getName() + " " + p.getCategory() + " " +
                            p.getDescription() + " " + String.join(" ", p.getTags())).toLowerCase();
                    return Arrays.stream(keywords).anyMatch(searchable::contains);
                })
                .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                .sorted(Comparator.comparingDouble(Product::getRating).reversed())
                .collect(Collectors.toList());
    }

    public Optional<Product> getProductById(String productId) {
        return Optional.ofNullable(catalog.get(productId));
    }

    public Map<String, Integer> getInventorySnapshot() {
        Map<String, Integer> snapshot = new LinkedHashMap<>();
        catalog.forEach((id, p) -> snapshot.put(id + " - " + p.getName(), p.getStockQuantity()));
        return snapshot;
    }
}
