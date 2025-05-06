package com.example.dummyjson.dto;

import java.util.List;

public class ProductsResponse {

    private List<Product> products;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
