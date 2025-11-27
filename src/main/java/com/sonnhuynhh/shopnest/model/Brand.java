// src/main/java/com/sonnhuynhh/shopnest/model/Brand.java
package com.sonnhuynhh.shopnest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@Table(name = "brands")
public class Brand {

    // Getters & Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Setter
    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Setter
    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "brand")
    @JsonIgnore
    private List<Product> products;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}