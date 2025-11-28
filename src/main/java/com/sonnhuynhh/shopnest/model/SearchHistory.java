// src/main/java/com/sonnhuynhh/shopnest/model/SearchHistory.java
package com.sonnhuynhh.shopnest.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SearchHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 255)
    private String keyword;

    private int searchCount = 1;

    @Column(name = "last_searched_at")
    private LocalDateTime lastSearchedAt;

    @PrePersist @PreUpdate
    private void updateTimestamp() {
        this.lastSearchedAt = LocalDateTime.now();
    }
}