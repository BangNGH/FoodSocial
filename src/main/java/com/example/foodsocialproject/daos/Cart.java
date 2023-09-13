package com.example.foodsocialproject.daos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class Cart {
    private List<Item> cartItems = new ArrayList<>();
    public void addItems(Item item) {
        boolean isExist = cartItems.stream()
                .filter(i -> Objects.equals(i.getProductId(),
                        item.getProductId()))
                .findFirst()
                .map(i -> {
                    i.setQuantity(i.getQuantity() +
                            item.getQuantity());
                    return true;
                })
                .orElse(false);
        if (!isExist) {
            cartItems.add(item);
        }
    }
    public void removeItems(Long productId) {
        cartItems.removeIf(item -> Objects.equals(item.getProductId(),
                productId));
    }
    public void updateItems(Long productId,int quantity) {
        cartItems.stream()
                .filter(item -> Objects.equals(item
                        .getProductId(), productId))
                .forEach(item -> item.setQuantity(quantity));
    }
}
