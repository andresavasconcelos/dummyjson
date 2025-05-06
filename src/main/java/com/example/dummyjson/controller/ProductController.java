package com.example.dummyjson.controller;

import com.example.dummyjson.dto.Product;
import com.example.dummyjson.dto.ProductsResponse;
import com.example.dummyjson.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(
        name = "Produtos",
        description = "API para consulta de produtos"
)
@RestController
@Validated
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(
            summary = "Listar todos os produtos",
            description = "Retorna uma lista paginada de todos os produtos disponiveis"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produtos recuperados com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Product.class, type = "array")
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "Nenhum produto encontrado",
                    content = @Content
            )
    })
    @GetMapping
    public Mono<ResponseEntity<Flux<ProductsResponse>>> getAllProducts() {
        return productService.getAllProducts()
                .collectList()
                .flatMap(products -> {
                    if (products.isEmpty()) {
                        return Mono.just(ResponseEntity.noContent().build());
                    }
                    return Mono.just(ResponseEntity.ok(Flux.fromIterable(products)));
                });
    }

    @Operation(
            summary = "Buscar produto por ID",
            description = "Recupera um produto específico pelo seu identificador único"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Produto encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Product.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Produto não encontrado",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"error\": \"Produto com ID 1 não encontrado\"}"
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Product>> getProductById(
            @Parameter(
                    description = "ID do produto a ser buscado",
                    required = true,
                    example = "1"
            )
            @PathVariable @NotNull Long id
    ) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
