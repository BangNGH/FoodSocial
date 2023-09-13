package com.example.foodsocialproject.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class Admin_HomeController {
    @GetMapping()
    public String index(){
        return "admin/home/index";
    }
}
