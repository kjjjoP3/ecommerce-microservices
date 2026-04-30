package com.ecommerce.product.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name = "PRODUCT")
public class Product {
    @Id @Column(name = "ID") private Long id;
    @Column(name = "NAME") private String name;
    @Column(name = "PRICE") private BigDecimal price;
    public Product() {}
    public Product(Long id, String name, BigDecimal price) { this.id = id; this.name = name; this.price = price; }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal price) { this.price = price; }
}
