package com.sonnhuynhh.shopnest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Fix OrderStatus mismatch trong database
 * Đổi 'SHIPPED' → 'SHIPPING' và 'DELIVERED' → 'COMPLETED'
 */
@Component
@Order(1) // Chạy đầu tiên
public class OrderStatusFixRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Mở rộng column status trước (nếu là ENUM cũ)
            try {
                jdbcTemplate.execute("ALTER TABLE orders MODIFY COLUMN status VARCHAR(20)");
                System.out.println("✅ Modified orders.status column to VARCHAR(20)");
            } catch (Exception e) {
                // Column có thể đã đúng format
            }
            
            // Fix SHIPPED → SHIPPING
            int shippedCount = jdbcTemplate.update(
                "UPDATE orders SET status = 'SHIPPING' WHERE status = 'SHIPPED'"
            );
            if (shippedCount > 0) {
                System.out.println("✅ Fixed " + shippedCount + " orders: SHIPPED → SHIPPING");
            }

            // Fix DELIVERED → COMPLETED
            int deliveredCount = jdbcTemplate.update(
                "UPDATE orders SET status = 'COMPLETED' WHERE status = 'DELIVERED'"
            );
            if (deliveredCount > 0) {
                System.out.println("✅ Fixed " + deliveredCount + " orders: DELIVERED → COMPLETED");
            }

            if (shippedCount == 0 && deliveredCount == 0) {
                System.out.println("✅ OrderStatus: All values are valid");
            }
        } catch (Exception e) {
            System.out.println("ℹ️ OrderStatusFixRunner: " + e.getMessage());
        }
    }
}
