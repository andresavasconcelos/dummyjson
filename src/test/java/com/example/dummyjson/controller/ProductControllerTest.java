package com.example.dummyjson.controller;

import com.example.dummyjson.dto.Product;
import com.example.dummyjson.dto.ProductsResponse;
import com.example.dummyjson.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductService productService;

    @Test
    void getAllProducts_shouldReturn200WithProducts() {
        ProductsResponse response = new ProductsResponse();
        List<ProductsResponse> products = List.of(response);

        when(productService.getAllProducts()).thenReturn(Flux.fromIterable(products));

        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ProductsResponse.class)
                .hasSize(1);
    }

    @Test
    void getAllProducts_shouldReturn204WhenNoProducts() {
        when(productService.getAllProducts()).thenReturn(Flux.fromIterable(Collections.emptyList()));

        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getProductById_shouldReturn200WhenProductExists() {
        Long productId = 1L;
        Product product = new Product();

        when(productService.getProductById(productId)).thenReturn(Mono.just(product));

        webTestClient.get()
                .uri("/api/products/{id}", productId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Product.class)
                .value(returnedProduct -> {
                    assertEquals(product.getId(), returnedProduct.getId());
                    assertEquals(product.getTitle(), returnedProduct.getTitle());
                });
    }

    @Test
    void getProductById_shouldReturn404WhenProductNotFound() {
        Long productId = 999L;

        when(productService.getProductById(productId)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/products/{id}", productId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getProductById_shouldReturn400WhenIdIsInvalid() {
        webTestClient.get()
                .uri("/api/products/invalid")
                .exchange()
                .expectStatus().isBadRequest();
    }
}