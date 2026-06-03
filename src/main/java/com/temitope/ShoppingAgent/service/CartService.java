package com.temitope.ShoppingAgent.service;

import com.temitope.ShoppingAgent.model.Cart;
import com.temitope.ShoppingAgent.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CartService {

    // In-memory carts: cartId -> Cart
    private final Map<String, Cart> carts = new HashMap<>();

    // Valid discount codes: code -> percentage off
    private final Map<String, Integer> discountCodes = new HashMap<>();

    @PostConstruct
    public void initDiscountCodes() {
        discountCodes.put("SAVE10", 10);
        discountCodes.put("KIDS20", 20);
        discountCodes.put("WELCOME15", 15);
        discountCodes.put("FLASH5", 5);
    }

    public Cart getOrCreateCart(String cartId, String userId) {
        return carts.computeIfAbsent(cartId, id ->
                Cart.builder().cartId(id).userId(userId).build()
        );
    }

    public Cart addToCart(String cartId, String userId, Product product, int quantity) {
        Cart cart = getOrCreateCart(cartId, userId);
        BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));

        cart.getItems().add(Cart.CartItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .lineTotal(lineTotal)
                .build());

        recalculate(cart);
        return cart;
    }

    /**
     * Applies a discount code to a cart.
     * Returns a result message indicating success or failure.
     */
    public String applyDiscount(String cartId, String code) {
        Cart cart = carts.get(cartId);
        if (cart == null) {
            return "Cart not found: " + cartId;
        }

        String upperCode = code.toUpperCase();
        if (!discountCodes.containsKey(upperCode)) {
            return "Invalid discount code: " + code + ". Valid codes are: SAVE10, KIDS20, WELCOME15, FLASH5.";
        }

        cart.setAppliedDiscountCode(upperCode);
        recalculate(cart);

        int percentage = discountCodes.get(upperCode);
        return String.format(
                "Discount code '%s' applied! You save %d%% (₦%s). New total: ₦%s",
                upperCode, percentage,
                cart.getDiscountAmount().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                cart.getTotal().setScale(2, RoundingMode.HALF_UP).toPlainString()
        );
    }

    public List<String> getValidDiscountCodes() {
        return new ArrayList<>(discountCodes.keySet());
    }

    private void recalculate(Cart cart) {
        BigDecimal subtotal = cart.getItems().stream()
                .map(Cart.CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setSubtotal(subtotal);

        if (cart.getAppliedDiscountCode() != null) {
            int pct = discountCodes.getOrDefault(cart.getAppliedDiscountCode(), 0);
            BigDecimal discount = subtotal.multiply(BigDecimal.valueOf(pct)).divide(BigDecimal.valueOf(100));
            cart.setDiscountAmount(discount);
            cart.setTotal(subtotal.subtract(discount));
        } else {
            cart.setDiscountAmount(BigDecimal.ZERO);
            cart.setTotal(subtotal);
        }
    }
}
