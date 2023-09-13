package com.example.foodsocialproject.controller.client;

import com.example.foodsocialproject.entity.*;
import com.example.foodsocialproject.services.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/shop")
public class ShopController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CartService cartService;
    @Autowired
    private UserServices userServices;
    @Autowired
    private OrderService orderService;

    @GetMapping()
    public String index(Model model, HttpSession session) {

        return findPaginated(1, model);
    }

    @GetMapping("/page/{pageNo}")
    public String findPaginated(@PathVariable(value = "pageNo") int pageNo, Model model) {
        int pageSize = 12;
        Page<Product> page = productService.findPaginated(pageNo, pageSize);
        List<Product> listProduct = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());

        model.addAttribute("categories", categoryService.getList());
        model.addAttribute("products", listProduct);
        return "client/shop/index";
    }

    @GetMapping("/category/{id}")
    public String productsByCategory(@PathVariable("id") Long id, Model model, HttpSession session) {
        model.addAttribute("products", productService.getALlProductsByCategoryId(id));
        model.addAttribute("categories", categoryService.getList());
        return "client/shop/category";
    }

    @GetMapping("/detail/{id}")
    public String productDetails(@PathVariable("id") Long id, Model model, HttpSession session) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("product_related", productService.getALlProductsByCategoryId(product.getCategory().getId()));
        model.addAttribute("categories", categoryService.getList());
        return "client/shop/detail";
    }

    //show recipe

    @GetMapping("/show-recipe")
    public String showUserRecipe(Model model, Principal principal) {
        return findPageRecipe(1, model, "date_purchase", "desc", principal);
    }

    @GetMapping("/recipe-page/{pageNo}")
    public String findPageRecipe(@PathVariable(value = "pageNo") int pageNo, Model model, @RequestParam("sortField") String sortField, @RequestParam("sortDir") String sortDir, Principal principal) {
        int pageSize = 12;
        String email = principal.getName();
        Users user = userServices.findbyEmail(email).get();
        Page<Orders> ordersPage = orderService.findUserOrdersPage(user.getId(), pageNo, pageSize, sortField, sortDir);
        List<Orders> ordersList = ordersPage.getContent();

        for (Orders orders : ordersList
             ) {
            System.out.println("ORDER " + orders.getId() + " " + orders.getUser().getFullName());
        }

        if (ordersList.isEmpty()) {
            model.addAttribute("notFound", true);
        } else model.addAttribute("notFound", false);

        model.addAttribute("ordersList", ordersList);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("totalItems", ordersPage.getTotalElements());

        model.addAttribute("sortDir", sortDir);
        model.addAttribute("sortField", sortField);
        model.addAttribute("reserseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        return "client/shop/show_recipe";
    }

    @GetMapping("/order-details/{id}")
    public String getOrderDetails(@PathVariable("id") Long id, Model model){
        Orders order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "client/shop/order_details";
    }

}
