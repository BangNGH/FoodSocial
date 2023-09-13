package com.example.foodsocialproject.controller.client;

import com.example.foodsocialproject.entity.Product;
import com.example.foodsocialproject.services.CategoryService;
import com.example.foodsocialproject.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @PostMapping("/search/product")
    public String searchProduct(@RequestParam("keyword") String keyword, Model model){
        List<Product> products = productService.searchProductByName(keyword);
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getList());
        return "client/search";
    }
}
