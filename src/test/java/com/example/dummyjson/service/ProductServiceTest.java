package com.example.dummyjson.service;

import com.example.dummyjson.dto.Product;
import com.example.dummyjson.dto.ProductsResponse;
import com.example.dummyjson.exception.ProductNotFoundException;
import com.example.dummyjson.exception.ServiceUnavailableException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class ProductServiceIntegrationTest {

    private static MockWebServer mockWebServer;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private ProductService productService;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getAllProducts_ShouldReturnProducts_WhenResponseIsSuccessful() {
        String baseUrl = mockWebServer.url("/").toString();
        WebClient webClient = WebClient.create(baseUrl);
        ProductService service = new ProductService(webClient);

        String responseBody = """
        {
            "products": [
                {
                    "id": 1,
                    "title": "iPhone 9",
                    "price": 549
                },
                {
                    "id": 2,
                    "title": "iPhone X",
                    "price": 899
                }
            ],
            "total": 2,
            "skip": 0,
            "limit": 2
        }
        """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        Flux<ProductsResponse> result = service.getAllProducts();

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getProducts().size() == 2 &&
                                response.getProducts().get(0).getTitle().equals("iPhone 9") &&
                                response.getProducts().get(1).getPrice() == 899
                )
                .verifyComplete();
    }

    @Test
    void getAllProducts_ShouldThrowServiceUnavailableException_WhenConnectionFails() {
        WebClient webClient = WebClient.create("http://localhost:9999");
        ProductService service = new ProductService(webClient);

        Flux<ProductsResponse> result = service.getAllProducts();

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ServiceUnavailableException &&
                                throwable.getMessage().equals("Falha ao recuperar os produtos")
                )
                .verify();
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() {
        // Arrange
        Long productId = 1L;
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setTitle("iPhone 9");
        mockProduct.setPrice(549.0);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/products/{id}", productId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Product.class)).thenReturn(Mono.just(mockProduct));

        // Act & Assert
        StepVerifier.create(productService.getProductById(productId))
                .expectNextMatches(product ->
                        product.getId().equals(productId) &&
                                product.getTitle().equals("iPhone 9") &&
                                product.getPrice() == 549
                )
                .verifyComplete();
    }

    @Test
    void getProductById_ShouldThrowProductNotFoundException_WhenProductDoesNotExist() {
        // Arrange
        Long productId = 999L;

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/products/{id}", productId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Product.class))
                .thenReturn(Mono.error(new ProductNotFoundException("Produto não encontrado com o ID: " + productId)));

        // Act & Assert
        StepVerifier.create(productService.getProductById(productId))
                .expectErrorMatches(throwable ->
                        throwable instanceof ProductNotFoundException &&
                                throwable.getMessage().equals("Produto não encontrado com o ID: " + productId)
                );
    }

    @Test
    void getProductById_ShouldThrowServiceUnavailableException_WhenServerErrorOccurs() {
        // Arrange
        Long productId = 1L;

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/products/{id}", productId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Product.class))
                .thenReturn(Mono.error(new ServiceUnavailableException("Serviço DummyJSON indisponível")));

        // Act & Assert
        StepVerifier.create(productService.getProductById(productId))
                .expectErrorMatches(throwable ->
                        throwable instanceof ServiceUnavailableException &&
                                throwable.getMessage().equals("Serviço DummyJSON indisponível")
                );
    }

    @Test
    void getProductById_ShouldThrowServiceUnavailableException_WhenGenericErrorOccurs() {
        // Arrange
        Long productId = 1L;

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/products/{id}", productId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Product.class))
                .thenReturn(Mono.error(new RuntimeException("Timeout")));

        // Act & Assert
        StepVerifier.create(productService.getProductById(productId))
                .expectErrorMatches(throwable ->
                        throwable instanceof ServiceUnavailableException &&
                                throwable.getMessage().equals("Falha ao recuperar o produto")
                )
                .verify();
    }

    @Test
    void getProductById_ShouldThrowServiceUnavailableException_WhenInvalidResponse() {
        // Arrange
        Long productId = 1L;

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/products/{id}", productId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Product.class))
                .thenReturn(Mono.error(new IllegalStateException("Invalid JSON response")));

        // Act & Assert
        StepVerifier.create(productService.getProductById(productId))
                .expectErrorMatches(throwable ->
                        throwable instanceof ServiceUnavailableException &&
                                throwable.getMessage().equals("Falha ao recuperar o produto")
                )
                .verify();
    }
}