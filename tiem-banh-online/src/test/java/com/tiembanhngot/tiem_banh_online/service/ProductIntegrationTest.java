package com.tiembanhngot.tiem_banh_online.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.tiembanhngot.tiem_banh_online.entity.Product;
import com.tiembanhngot.tiem_banh_online.repository.ProductRepository;

@SpringBootTest(classes = com.tiembanhngot.tiem_banh_online.TiemBanhOnlineApplication.class)

@Transactional
public class ProductIntegrationTest {
    @Autowired
    ProductService productService;
    @Autowired
    ProductRepository productRepository;

    @Test
    void testSearchAvailableProducts() {
        Product p = new Product();
        p.setName("Bánh kem dâu");
        p.setPrice(BigDecimal.valueOf(50000));
        p.setIsAvailable(true);
        productRepository.save(p);

        List<Product> results = productService.searchAvailableProducts("kem");

        assertEquals(1, results.size());
        assertEquals("Bánh kem dâu", results.get(0).getName());
    }
}
