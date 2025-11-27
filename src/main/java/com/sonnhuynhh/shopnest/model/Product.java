package com.sonnhuynhh.shopnest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")  // Chỉ định tên bảng rõ ràng
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Name cannot be null")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Min(value = 0, message = "Price must be non-negative")
    private BigDecimal price;

    @Min(value = 0, message = "Stock must be non-negative")
    private int stock;

    @Column(length = 1000)
    private String description;

    private LocalDate expirationDate;  // Hạn sử dụng

    private LocalDate importDate;  // Ngày nhập

    @Column(length = 255)
    private String imageUrl;  // URL hình ảnh

    @Size(max = 100)
    private String category;

    @Size(max = 100)
    private String brand;

    private Double rating;  // Đánh giá trung bình

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();  // Ngày tạo

    private LocalDateTime updatedAt = LocalDateTime.now();  // Ngày cập nhật

    // Constructors
    public Product() {}

    public Product(String name, BigDecimal price, int stock, String description, LocalDate expirationDate,
                   LocalDate importDate, String imageUrl, String category, String brand, Double rating) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.expirationDate = expirationDate;
        this.importDate = importDate;
        this.imageUrl = imageUrl;
        this.category = category;
        this.brand = brand;
        this.rating = rating;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public LocalDate getImportDate() { return importDate; }
    public void setImportDate(LocalDate importDate) { this.importDate = importDate; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}