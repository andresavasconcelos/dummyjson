package com.example.dummyjson.service;

import com.example.dummyjson.dto.Product;
import com.example.dummyjson.exception.ProductNotFoundException;
import com.example.dummyjson.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

    private final WebClient webClient;
    private final String baseUrl;

    public ProductService(
            WebClient.Builder webClientBuilder,
            @Value("${dummyjson.api.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.build();
        this.baseUrl = baseUrl;
    }

    public Flux<Product> getAllProducts() {
        return webClient.get()
                .uri(baseUrl + "/products")
                .retrieve()
                .bodyToFlux(Product.class)
                .onErrorResume(e -> Flux.error(new ServiceUnavailableException("Falha ao recuperar os produtos")));
    }

    public Mono<Product> getProductById(Long id) {
        return webClient.get()
                .uri(baseUrl + "/products/{id}", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new ProductNotFoundException("Produto não encontrado com o ID: " + id)))
                .onStatus(status -> status.is5xxServerError(),
                        response -> Mono.error(new ServiceUnavailableException("Serviço DummyJSON indisponivel")))
                .bodyToMono(Product.class);
    }
}