package com.petshop.controller;

import com.petshop.models.dto.response.ResponseObject;
import com.petshop.models.entities.CartItem;
import com.petshop.services.imp.ShoppingCartServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/cart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartServiceImp shoppingCartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseObject> getCart(@PathVariable Long userId) {
        return shoppingCartService.getCart(userId);
    }

    @PostMapping("/add/{user_id}")
    public ResponseEntity<ResponseObject> addToCart(@RequestBody CartItem cartItem,@PathVariable Long user_id) {
       return shoppingCartService.addToCart(user_id, cartItem);
    }

    @DeleteMapping("/remove/{userId}/{productId}")
    public void removeItemFromCart(@PathVariable Long userId, @PathVariable Long productId) {
        shoppingCartService.RemoveItemFromCart(userId, productId);
    }

    @DeleteMapping("/remove/{userId}")
    public void removeCart(@PathVariable Long userId) {
        shoppingCartService.RemoveAddCart(userId);
    }
}