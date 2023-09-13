package com.example.foodsocialproject.controller.admin;

import com.example.foodsocialproject.entity.Category;
import com.example.foodsocialproject.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/category")
public class Admin_CategoryController {
    @Autowired
    private CategoryService categoryService;
    @GetMapping()
    public String getList(Model model){
        model.addAttribute("categories", categoryService.getList());
        if(model.containsAttribute("message")){
            model.addAttribute("message", model.getAttribute("message"));
        }
        return "admin/category/index";
    }
    @GetMapping("/add")
    public String addNew(Model model){
        model.addAttribute("category", new Category());
        return "admin/category/add";
    }
    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("category") Category category,
                      BindingResult bindingResult,
                      RedirectAttributes redirectAttributes, Model model){
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                model.addAttribute(error.getField() + "_error",
                        error.getDefaultMessage());
            }
            return "admin/category/add";
        }
        categoryService.addCategory(category);
        redirectAttributes.addFlashAttribute("message", "Save successfully!");
        return "redirect:/admin/category";
    }
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model){
        Optional editCategory = categoryService.get(id);
        if (editCategory.isPresent())
            model.addAttribute("category", editCategory);

        return "admin/category/edit";
    }

    @PostMapping("/edit")
    public String edit(@Valid @ModelAttribute("category") Category updateCategory,
                       BindingResult bindingResult ,Model model,
                       RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                model.addAttribute(error.getField() + "_error",
                        error.getDefaultMessage());
            }
            model.addAttribute("category", updateCategory);
            return "admin/category/edit";
        }
        categoryService.updateCategory(updateCategory);
        redirectAttributes.addFlashAttribute("message", "Save successfully!");
        return "redirect:/admin/category";
    }
}
