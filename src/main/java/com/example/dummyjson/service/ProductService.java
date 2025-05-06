package com.example.dummyjson.service;

import com.example.dummyjson.dto.Product;
import com.example.dummyjson.dto.ProductsResponse;
import com.example.dummyjson.exception.ProductNotFoundException;
import com.example.dummyjson.exception.ServiceUnavailableException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

    private final WebClient webClient;

    public ProductService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<ProductsResponse> getAllProducts() {
        return webClient.get()
                .uri("/products")
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new ServiceUnavailableException("Requisição inválida")))
                .onStatus(status -> status.is5xxServerError(),
                        response -> Mono.error(new ServiceUnavailableException("Serviço DummyJSON indisponível")))
                .bodyToFlux(ProductsResponse.class)
                .onErrorResume(e -> Flux.error(new ServiceUnavailableException("Falha ao recuperar os produtos")));
    }

    public Mono<Product> getProductById(Long id) {
        return webClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new ProductNotFoundException("Produto não encontrado com o ID: " + id)))
                .onStatus(status -> status.is5xxServerError(),
                        response -> Mono.error(new ServiceUnavailableException("Serviço DummyJSON indisponível")))
                .bodyToMono(Product.class)
                .onErrorResume(e -> Mono.error(new ServiceUnavailableException("Falha ao recuperar o produto")));
    }
}