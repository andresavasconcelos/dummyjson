//package com.example.dummyjson.controller;
//
//import com.example.dummyjson.dto.Product;
//import com.example.dummyjson.service.ProductService;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.when;
//
//@RunWith(MockitoJUnitRunner.class)
//public class ProductControllerTest {
//
//    @InjectMocks
//    private ProductController productController;
//
//    @Mock
//    private ProductService productService;
//
//    @Test
//    public void testGetAllProducts() {
//        Product product1 = new Product();
//        product1.setId(1L);
//        product1.setTitle("Product 1");
//
//        Product product2 = new Product();
//        product2.setId(2L);
//        product2.setTitle("Product 2");
//
//        List<Product> products = Arrays.asList(product1, product2);
//        when(productService.getAllProducts()).thenReturn(products);
//
//        List<Product> result = productController.getAllProducts();
//        assertEquals(2, result.size());
//        assertEquals("Product 1", result.get(0).getTitle());
//    }
//
//    @Test
//    public void testGetProductById() {
//        Product product = new Product();
//        product.setId(1L);
//        product.setTitle("Product 1");
//
//        when(productService.getProductById(1L)).thenReturn(product);
//
//        Product result = productController.getProductById(1L);
//        assertEquals("Product 1", result.getTitle());
//    }
//}
