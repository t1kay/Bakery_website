package com.tiembanhngot.tiem_banh_online.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Import Model
import org.springframework.web.bind.annotation.GetMapping;

import com.tiembanhngot.tiem_banh_online.entity.Product;
import com.tiembanhngot.tiem_banh_online.service.ProductService;

@Controller
public class HomeController {
    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String home(Model model) { // Thêm Model
        List<Product> featuredProducts = productService.getFeaturedProducts();
        model.addAttribute("currentPage", "home");
        model.addAttribute("featuredProducts", featuredProducts);
        return "index";
    }
}