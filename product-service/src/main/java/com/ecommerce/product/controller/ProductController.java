package com.ecommerce.product.controller;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController @RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }
    @GetMapping public List<ProductResponse> getAll() { return service.getAll(); }
}
