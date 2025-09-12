package com.divacode.ecom_proj.service;

import com.divacode.ecom_proj.model.CartItem;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {

    // A map to store individual user carts. Key is userId, value is a map of cart items.
    private final Map<String, Map<String, CartItem>> userCarts = new ConcurrentHashMap<>();

    /**
     * Adds an item to a user's cart.
     * @param userId The unique ID of the user.
     * @param newItem The item to add.
     */
    public void addItemToCart(String userId, CartItem newItem) {
        // Get the user's cart. If it doesn't exist, create a new one.
        Map<String, CartItem> userCart = userCarts.computeIfAbsent(userId, k -> new HashMap<>());

        // Check if the item already exists in the user's cart.
        CartItem existingItem = userCart.get(newItem.getItemId());

        if (existingItem != null) {
            // If it exists, update the quantity.
            existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
        } else {
            // If it doesn't exist, add the new item to the cart.
            userCart.put(newItem.getItemId(), newItem);
        }
    }

    /**
     * Retrieves the cart for a specific user.
     * @param userId The unique ID of the user.
     * @return A map representing the user's cart.
     */
    public Map<String, CartItem> getCartByUserId(String userId) {
        return userCarts.getOrDefault(userId, new HashMap<>());
    }
}
