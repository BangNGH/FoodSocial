package com.example.foodsocialproject.rest_controller;

import com.example.foodsocialproject.daos.Item;
import com.example.foodsocialproject.dto.ProductDTO;
import com.example.foodsocialproject.entity.Orders;
import com.example.foodsocialproject.entity.Product;
import com.example.foodsocialproject.services.CartService;
import com.example.foodsocialproject.services.OrderService;
import com.example.foodsocialproject.services.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class CartRestController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService;

    @GetMapping("/add-to-cart/{id}/{quantity}")
    public void addToCart(@PathVariable("id") Long id, HttpSession session,
                          @PathVariable("quantity") int quantity) {
        Product product = productService.getProductById(id);
        var cart = cartService.getCart(session);
        cart.addItems(new Item(id, product.getName(), product.getPrice(), quantity, product.getImagesPath()));
        cartService.updateCart(session, cart);
    }

    @GetMapping("/getProduct/{id}")
    public ProductDTO quickView(@PathVariable("id") Long id) {
        Product product = productService.getProductById(id);
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setPrice(product.getPrice());
        productDTO.setImage(product.getImagesPath());
        productDTO.setQuantity(product.getQuantity_in_stock());
        return productDTO;
    }
    @GetMapping("/getCart")
    public List<Item> getCart(HttpSession session){
        return cartService.getCart(session).getCartItems();
    }
    @GetMapping("/removeItem/{id}")
    public void removeFromCart(HttpSession session, @PathVariable Long id) {
        var cart = cartService.getCart(session);
        cart.removeItems(id);
    }
    @GetMapping("/getTotalPrice")
    public double getTotalPrice(HttpSession session){
        return cartService.getSumPrice(session);
    }
    @GetMapping("/updateCart/{id}/{quantity}")
    public void updateCart(HttpSession session, @PathVariable Long id, @PathVariable int quantity) {
        var cart = cartService.getCart(session);
        cart.updateItems(id, quantity);
    }
}
