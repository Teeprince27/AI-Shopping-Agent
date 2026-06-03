package com.temitope.ShoppingAgent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.temitope.ShoppingAgent.model.Product;
import com.temitope.ShoppingAgent.model.UserProfile;
import com.temitope.ShoppingAgent.service.CartService;
import com.temitope.ShoppingAgent.service.ProductCatalogService;
import com.temitope.ShoppingAgent.service.UserProfileService;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shopping tool implementations that AgentScope's ReAct agent calls autonomously.
 *
 * Each method is registered as an AgentTool and described so the LLM knows
 * when and how to invoke it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShoppingTools {

    private final ProductCatalogService catalogService;
    private final UserProfileService userProfileService;
    private final CartService cartService;
    private final ObjectMapper objectMapper;


    /**
     * Searches the product catalog using a natural language query.
     * Optionally filters by a maximum price (in NGN).
     *
     * @param query    Keywords to search (e.g., "toys for 5-year-old girl")
     * @param maxPrice Maximum price in NGN. Pass null or 0 if no budget limit.
     * @return Formatted list of matching products with prices and availability.
     */

    @Tool(name = "searchProducts",
            description = "Search the product catalog by keywords. Optionally filter by maximum price in NGN. " +
                    "Use this when the customer describes what they are looking for or mentions a budget.")
    public String searchProducts(
            @ToolParam(name = "query",    description = "Search keywords, e.g. 'toys for 5-year-old'") String query,
            @ToolParam(name = "maxPrice", description = "Maximum price in NGN. Use 0 for no limit.")    double maxPrice) {


        log.info("[TOOL] searchProducts called — query='{}', maxPrice={}", query, maxPrice);

        BigDecimal priceFilter = (maxPrice > 0) ? BigDecimal.valueOf(maxPrice) : null;
        List<Product> results = catalogService.searchProducts(query, priceFilter);

        if (results.isEmpty()) {
            return "No products found matching '" + query + "'"
                    + (priceFilter != null ? " within ₦" + priceFilter.toPlainString() : "") + ".";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(results.size()).append(" product(s):\n\n");
        for (Product p : results) {
            sb.append("• [").append(p.getId()).append("] ").append(p.getName()).append("\n");
            sb.append("  Category: ").append(p.getCategory()).append("\n");
            sb.append("  Price: ").append(p.getFormattedPrice()).append("\n");
            sb.append("  Stock: ").append(p.isInStock() ? "In Stock (" + p.getStockQuantity() + " units)" : "OUT OF STOCK").append("\n");
            sb.append("  Rating: ").append(p.getRating()).append("/5 (").append(p.getReviewCount()).append(" reviews)\n");
            sb.append("  Description: ").append(p.getDescription()).append("\n\n");
        }
        return sb.toString();
    }


    /**
     * Checks the inventory/stock status of a specific product by its ID.
     *
     * @param productId The product ID (e.g., "P001")
     * @return Stock status and quantity available.
     */

    @Tool(name = "checkInventory",
            description = "Check the stock availability of a specific product by its product ID. " +
                    "Always call this before recommending a product to confirm it is in stock.")
    public String checkInventory(
            @ToolParam(name = "productId", description = "The product ID, e.g. 'P001'") String productId) {
        log.info("[TOOL] checkInventory called — productId='{}'", productId);

        Optional<Product> productOpt = catalogService.getProductById(productId.trim().toUpperCase());

        if (productOpt.isEmpty()) {
            return "Product with ID '" + productId + "' was not found in the catalog.";
        }

        Product p = productOpt.get();
        if (p.isInStock()) {
            return String.format(" %s (ID: %s) is IN STOCK. %d unit(s) available at %s.",
                    p.getName(), p.getId(), p.getStockQuantity(), p.getFormattedPrice());
        } else {
            return String.format(" %s (ID: %s) is currently OUT OF STOCK. Please check back later.",
                    p.getName(), p.getId());
        }
    }


    /**
     * Fetches personalized product recommendations for a user based on their
     * purchase history, browsing history, and preferred categories.
     *
     * @param userId The user ID (e.g., "U001"). Use "GUEST" for anonymous users.
     * @return A curated list of recommended products.
     */

    @Tool(name = "getPersonalizedRecommendations",
            description = "Get personalized product recommendations based on a user's purchase history. " +
                    "Use userId='GUEST' for anonymous users.")
    public String getPersonalizedRecommendations(
            @ToolParam(name = "userId", description = "The customer's user ID, e.g. 'U001'") String userId) {

        log.info("[TOOL] getPersonalizedRecommendations called — userId='{}'", userId);

        Optional<UserProfile> profileOpt = userProfileService.getUserProfile(userId);
        if (profileOpt.isEmpty()) {
            return "No user profile found for userId: " + userId;
        }

        UserProfile profile = profileOpt.get();

        // Build a query from preferred categories and browsing history
        String query = String.join(" ", profile.getPreferredCategories());

        List<Product> allResults = catalogService.searchProducts(query, null);

        // Exclude already purchased items
        List<Product> recommendations = allResults.stream()
                .filter(p -> !profile.getPurchaseHistory().contains(p.getId()))
                .filter(Product::isInStock)
                .limit(5)
                .collect(Collectors.toList());

        if (recommendations.isEmpty()) {
            return "No new recommendations available for user " + profile.getName() + " at this time.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Personalized recommendations for ").append(profile.getName()).append(":\n\n");
        for (Product p : recommendations) {
            sb.append("⭐ [").append(p.getId()).append("] ").append(p.getName())
                    .append(" — ").append(p.getFormattedPrice())
                    .append(" | Rating: ").append(p.getRating()).append("/5\n");
            sb.append("   ").append(p.getDescription()).append("\n\n");
        }
        return sb.toString();
    }


    /**
     * Applies a promotional discount code to a customer's cart.
     * Valid codes: SAVE10 (10% off), KIDS20 (20% off), WELCOME15 (15% off), FLASH5 (5% off).
     *
     * @param cartId The cart ID to apply the discount to.
     * @param code   The discount/promo code string.
     * @return Confirmation message with discount amount and updated total.
     */
    @Tool(name = "applyDiscount",
            description = "Apply a promotional discount code to a customer's cart. " +
                    "Only call this when the customer explicitly asks to apply a discount or promo code.")
    public String applyDiscount(
            @ToolParam(name = "cartId", description = "The cart ID, e.g. 'CART-001'") String cartId,
            @ToolParam(name = "code",   description = "The promo code string, e.g. 'KIDS20'")  String code) {

        log.info("[TOOL] applyDiscount called — cartId='{}', code='{}'", cartId, code);
        return cartService.applyDiscount(cartId, code);
    }


    /**
     * Returns a list of all currently valid discount codes.
     *
     * @return Comma-separated list of available promo codes.
     */
    @Tool(name = "getAvailableDiscountCodes",
            description = "Returns all currently valid discount/promo codes. " +
                    "Call this when a customer asks about available promotions.")
    public String getAvailableDiscountCodes() {
        log.info("[TOOL] getAvailableDiscountCodes called");
        List<String> codes = cartService.getValidDiscountCodes();
        return "Available discount codes: " + String.join(", ", codes) +
                ". Tip: KIDS20 gives 20% off — great for children's products!";
    }
}
