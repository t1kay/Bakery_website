package com.tiembanhngot.tiem_banh_online.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tiembanhngot.tiem_banh_online.dto.CartDTO;
import com.tiembanhngot.tiem_banh_online.entity.CartItemRequest;
import com.tiembanhngot.tiem_banh_online.service.CartService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/cart")
@SessionAttributes("shoppingCart")
@Slf4j
public class CartController {
    @Autowired
    private CartService cartService;

    @ModelAttribute("shoppingCart")
    public CartDTO getShoppingCart(HttpSession session) {
        return cartService.getCart(session);
    }

    @GetMapping
    public String viewCart(@ModelAttribute("shoppingCart") CartDTO cart, Model model) {
        model.addAttribute("currentPage", "cart");
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/update")
    public String updateCartItem(@RequestParam("productId") Long productId, @RequestParam("quantity") int quantity,
            String selectedSize,
            HttpSession session, RedirectAttributes redirectAttributes) {
        try {

            cartService.updateQuantity(productId, quantity, selectedSize, session); // <<< CHANGED CALL
            redirectAttributes.addFlashAttribute("cartMessageSuccess", "Product quantity updated.");
        } catch (Exception e) {
            log.error("Error updating cart for product {} (size: {}): {}", productId, selectedSize, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("cartMessageError", "Error updating cart.");
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove/{productId}")
    public String removeCartItem(@PathVariable("productId") Long productId, HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            cartService.removeItemById(productId, session);
            redirectAttributes.addFlashAttribute("cartMessageSuccess", "Product removed from cart.");
        } catch (Exception e) {
            log.error("Error removing product {} from cart.", productId, e);
            redirectAttributes.addFlashAttribute("cartMessageError", "Lỗi khi xóa sản phẩm.");
        }
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes, SessionStatus sessionStatus) {
        cartService.clearCart(session);
        sessionStatus.setComplete();
        redirectAttributes.addFlashAttribute("cartMessageSuccess", "Giỏ hàng đã được xóa thành công.");
        return "redirect:/cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Object> addToCartAjax(@RequestBody CartItemRequest req, HttpSession session) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return new ResponseEntity<>("Vui lòng đăng nhập để thêm vào giỏ hàng", HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> result = new HashMap<>();
        try {
            cartService.addToCart(req.getProductId(), req.getQuantity(), req.getSelectedSize(), session);
            int totalItems = cartService.getCartItemCount(session);

            result.put("success", true);
            result.put("message", "Đã thêm vào giỏ hàng!");
            result.put("totalItems", totalItems);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(result);
        }
    }

}
