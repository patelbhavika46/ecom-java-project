package com.divacode.ecom_proj.controller;

import com.divacode.ecom_proj.model.CartItem;
import com.divacode.ecom_proj.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public String addToCart(@RequestBody CartItem newItem, Principal principal) {
        if (principal == null) {
            // Handle unauthenticated users (e.g., return an error or redirect to login).
            return "Please log in to add items to your cart.";
        }

        String userId = principal.getName(); // Or some other way to get the unique user ID.
        cartService.addItemToCart(userId, newItem);

        return "Added '" + newItem.getName() + "' to your cart.";
    }

    @GetMapping("/view")
    public Map<String, CartItem> viewCart(Principal principal) {
        if (principal == null) {
            return new HashMap<>(); // Return an empty cart for unauthenticated users.
        }

        String userId = principal.getName();
        return cartService.getCartByUserId(userId);
    }
}
