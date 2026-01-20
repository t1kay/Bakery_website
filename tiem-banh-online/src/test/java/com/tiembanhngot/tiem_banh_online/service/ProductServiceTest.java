package com.tiembanhngot.tiem_banh_online.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tiembanhngot.tiem_banh_online.entity.Product;
import com.tiembanhngot.tiem_banh_online.repository.ProductRepository;

public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchAvailableProducts_ShouldReturnEmptyList_WhenQueryIsBlank() {
        List<Product> result1 = productService.searchAvailableProducts("");
        List<Product> result2 = productService.searchAvailableProducts("   ");

        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        verify(productRepository, never()).searchAvailableProducts(anyString());
    }

    @Test
    void searchAvailableProducts_ShouldCallRepositoryAndReturnResult() {
        // Arrange
        Product mockProduct = new Product();
        mockProduct.setName("Bánh kem");
        when(productRepository.searchAvailableProducts("banh"))
                .thenReturn(List.of(mockProduct));

        // Act
        List<Product> result = productService.searchAvailableProducts("  banh ");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Bánh kem", result.get(0).getName());
        verify(productRepository, times(1)).searchAvailableProducts("banh");
    }

}
