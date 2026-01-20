package com.tiembanhngot.tiem_banh_online.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemRequest {
    private Long productId;
    private int quantity;
    private String selectedSize;

}
