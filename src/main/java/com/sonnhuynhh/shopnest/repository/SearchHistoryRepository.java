// src/main/java/com/sonnhuynhh/shopnest/repository/SearchHistoryRepository.java
package com.sonnhuynhh.shopnest.repository;

import com.sonnhuynhh.shopnest.model.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    Optional<SearchHistory> findByUserIdAndKeyword(Long userId, String keyword);
    Optional<SearchHistory> findByUserIdIsNullAndKeyword(String keyword);

    List<SearchHistory> findTop10ByKeywordStartingWithIgnoreCaseOrderBySearchCountDesc(String prefix);

    List<SearchHistory> findTop20ByOrderBySearchCountDescLastSearchedAtDesc();
}