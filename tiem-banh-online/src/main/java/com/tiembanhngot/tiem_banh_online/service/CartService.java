package com.tiembanhngot.tiem_banh_online.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tiembanhngot.tiem_banh_online.dto.CartDTO;
import com.tiembanhngot.tiem_banh_online.dto.CartItemDTO;
import com.tiembanhngot.tiem_banh_online.entity.Product;
import com.tiembanhngot.tiem_banh_online.exception.ProductNotFoundException;
import com.tiembanhngot.tiem_banh_online.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private static final String CART_SESSION_KEY = "shoppingCart";
    @Autowired
    private ProductRepository productRepository;

    public CartDTO getCart(HttpSession session) {
        CartDTO cart = (CartDTO) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new CartDTO();
            cart.setItems(new LinkedHashMap<>());
            cart.setTotalAmount(BigDecimal.ZERO);
            cart.setTotalItems(0);
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    @Transactional(readOnly = true)
    public CartDTO addToCart(Long productId, int quantity, String selectedSize, HttpSession session) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be > 0");

        CartDTO cart = getCart(session);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy sản phẩm ID: " + productId));

        if (!Boolean.TRUE.equals(product.getIsAvailable()))
            throw new IllegalArgumentException("Product '" + product.getName() + "' hiện không khả dụng");

        BigDecimal priceToUse = product.getPrice();
        String sizeIdentifier = selectedSize;

        // Xử lý size
        if (StringUtils.hasText(selectedSize) && product.getSizeOptions() != null) {
            if (product.getSizeOptions().containsKey(selectedSize)) {
                priceToUse = product.getSizeOptions().get(selectedSize);
            } else {
                sizeIdentifier = null; // fallback
            }
        } else {
            sizeIdentifier = null;
        }

        String itemKey = productId + (sizeIdentifier != null ? "_" + sizeIdentifier : "");

        CartItemDTO existingItem = cart.getItems().get(itemKey);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItemDTO newItem = new CartItemDTO();
            newItem.setProductId(product.getProductId());
            newItem.setName(product.getName());
            newItem.setImageUrl(product.getImageUrl());
            newItem.setPrice(priceToUse);
            newItem.setQuantity(quantity);
            newItem.setSelectedSize(sizeIdentifier);
            cart.getItems().put(itemKey, newItem);
        }

        updateCartTotals(cart);

        // **Bắt buộc set lại session để chắc chắn**
        session.setAttribute(CART_SESSION_KEY, cart);

        return cart; // trả về luôn để AJAX render
    }

    public void updateQuantity(Long productId, int quantity, String selectedSize, HttpSession session) {
        CartDTO cart = getCart(session);

        String itemKeyToFind = productId + (StringUtils.hasText(selectedSize) ? "_" + selectedSize : "");

        log.debug(
                "Attempting to update quantity for item with key {} (Product ID: {}, Size: {}) to {} in cart [Session: {}].",
                itemKeyToFind, productId, (selectedSize != null ? selectedSize : "N/A"), quantity, session.getId());

        CartItemDTO item = cart.getItems().get(itemKeyToFind);

        if (item != null) {
            if (quantity > 0) {
                log.debug("Updating quantity for item with key {} to {}.", itemKeyToFind, quantity);
                item.setQuantity(quantity);
            } else {
                log.debug("Quantity for item with key {} is {} (<= 0). Removing item from cart.", itemKeyToFind,
                        quantity);
                cart.getItems().remove(itemKeyToFind);
            }
            updateCartTotals(cart);
            session.setAttribute(CART_SESSION_KEY, cart);
            log.info("Cart updated after quantity change for item with key {}. Total items: {}, Total amount: {}",
                    itemKeyToFind, cart.getTotalItems(), cart.getTotalAmount());
        } else {
            log.warn(
                    "Attempted to update quantity for item with key {} (Product ID {}, Size: {}), but item was not found in cart [Session: {}].",
                    itemKeyToFind, productId, (selectedSize != null ? selectedSize : "N/A"), session.getId());
        }
    }

    public void removeItemById(Long productId, HttpSession session) {
        CartDTO cart = getCart(session);
        String itemKeyToRemove = productId.toString();
        CartItemDTO removedItem = cart.getItems().remove(itemKeyToRemove);

        if (removedItem != null) {
            log.debug("Successfully removed product ID {} from cart.", productId);
            updateCartTotals(cart); // xoa sp khoi cart va tinh lai tien
            session.setAttribute(CART_SESSION_KEY, cart);

        } else {
            throw new IllegalArgumentException("Sản phẩm không tồn tại trong giỏ hàng để xóa.");
        }
    }

    public void clearCart(HttpSession session) {
        log.info("Attempting to clear cart for session ID: {}", session.getId());
        if (session.getAttribute(CART_SESSION_KEY) != null) {
            session.removeAttribute(CART_SESSION_KEY);
            log.info("Cart attribute '{}' removed successfully for session ID: {}.", CART_SESSION_KEY, session.getId());

            if (session.getAttribute(CART_SESSION_KEY) == null) {
                log.debug("Confirmed: Cart attribute '{}' is null after removal.", CART_SESSION_KEY);
            } else {
                log.error("CRITICAL: Cart attribute '{}' STILL EXISTS after attempting removal for session ID: {}!",
                        CART_SESSION_KEY, session.getId());
            }
        } else {
            log.warn("No cart attribute '{}' found in session ID {} to clear.", CART_SESSION_KEY, session.getId());
        }
    }

    public int getCartItemCount(HttpSession session) {
        return getCart(session).getTotalItems();
    }

    public BigDecimal getCartTotal(HttpSession session) {
        return getCart(session).getTotalAmount();
    }

    public void updateCartTotals(CartDTO cart) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItemDTO item : cart.getItems().values()) {
            BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setLineTotal(lineTotal);
            totalAmount = totalAmount.add(lineTotal);
            totalItems += item.getQuantity();
        }

        cart.setTotalAmount(totalAmount);
        cart.setTotalItems(totalItems);
    }
}