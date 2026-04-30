package com.ecommerce.product.service.impl;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import java.util.List;
import org.springframework.stereotype.Service;
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    public ProductServiceImpl(ProductRepository repository) { this.repository = repository; }
    public List<ProductResponse> getAll() {
        return repository.findAll().stream().map(p -> new ProductResponse(p.getId(), p.getName(), p.getPrice())).toList();
    }
}
