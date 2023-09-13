package com.example.foodsocialproject.services;

import com.example.foodsocialproject.daos.Cart;
import com.example.foodsocialproject.daos.Item;
import com.example.foodsocialproject.entity.OrderDetails;
import com.example.foodsocialproject.entity.Orders;
import com.example.foodsocialproject.entity.Users;
import com.example.foodsocialproject.repository.OderDetailsRepository;
import com.example.foodsocialproject.repository.OrderRepository;
import com.example.foodsocialproject.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
public class CartService {
    private static final String CART_SESSION_KEY = "cart";

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OderDetailsRepository oderDetailsRepository;

    public Cart getCart(@NotNull HttpSession session) {
        return Optional.ofNullable((Cart)
                        session.getAttribute(CART_SESSION_KEY))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    session.setAttribute(CART_SESSION_KEY, cart);
                    return cart;
                });
    }
    public void updateCart(@NotNull HttpSession session, Cart cart) {
        session.setAttribute(CART_SESSION_KEY, cart);
    }
    public void removeCart(@NotNull HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
    public int getSumQuantity(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToInt(Item::getQuantity)
                .sum();
    }
    public int getQuantity(@NotNull HttpSession session){
        return getCart(session).getCartItems().size();
    }
    public double getSumPrice(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToDouble(item -> item.getPrice() *
                        item.getQuantity())
                .sum();
    }
    public void saveCart(@NotNull HttpSession session, String note, String address, Users user, String payment, Boolean is_paid) {
        var cart = getCart(session);
        if (cart.getCartItems().isEmpty()) return;
        var order = new Orders();
        order.setDate_purchase(new Date(new Date().getTime()));
        order.setTotal_price(getSumPrice(session));
        order.setNote(note);
        order.setAddress(address);
        order.setUser(user);
        order.setIs_paid(is_paid);
//        order.setStatus_order(statusOrderRepository.findById(1L).orElseThrow());
        if(payment == "cash")
            order.setPayment(false);
        if(payment == "paypal")
            order.setPayment(true);
        orderRepository.save(order);

        cart.getCartItems().forEach(item -> {
            var items = new OrderDetails();
            items.setOrders(order);
            items.setQuantity(item.getQuantity());
            items.setTotal_price(item.getPrice()*item.getQuantity());
            items.setProduct(productRepository.findById(item.getProductId()).orElseThrow());
            oderDetailsRepository.save(items);
        });
        session.setAttribute("orderID", order.getId());
        session.setAttribute("sumPrice", getSumPrice(session));
        removeCart(session);
    }
}
