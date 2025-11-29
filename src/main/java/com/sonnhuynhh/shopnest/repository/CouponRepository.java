package com.sonnhuynhh.shopnest.repository;

import com.sonnhuynhh.shopnest.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    Optional<Coupon> findByCode(String code);
    
    Optional<Coupon> findByCodeIgnoreCase(String code);
    
    boolean existsByCode(String code);
    
    @Query("SELECT c FROM Coupon c WHERE c.active = true " +
           "AND (c.startDate IS NULL OR c.startDate <= :now) " +
           "AND (c.endDate IS NULL OR c.endDate >= :now) " +
           "AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    List<Coupon> findAllValidCoupons(LocalDateTime now);
    
    List<Coupon> findByActiveTrue();
    
    @Query("SELECT c FROM Coupon c WHERE c.endDate < :now AND c.active = true")
    List<Coupon> findExpiredCoupons(LocalDateTime now);
}
