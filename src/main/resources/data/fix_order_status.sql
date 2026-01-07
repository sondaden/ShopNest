-- =====================================================
-- SQL Script: Fix OrderStatus enum mismatch
-- Lỗi: Database chứa giá trị 'SHIPPED' và 'DELIVERED'
-- nhưng code Java đã đổi thành 'SHIPPING' và 'COMPLETED'
-- =====================================================

-- Kiểm tra các giá trị status hiện tại
SELECT DISTINCT status FROM orders;

-- UPDATE: Đổi 'SHIPPED' → 'SHIPPING'
UPDATE orders SET status = 'SHIPPING' WHERE status = 'SHIPPED';

-- UPDATE: Đổi 'DELIVERED' → 'COMPLETED'
UPDATE orders SET status = 'COMPLETED' WHERE status = 'DELIVERED';

-- Verify: Kiểm tra lại
SELECT DISTINCT status FROM orders;

-- Kiểm tra số lượng đơn hàng theo status mới
SELECT status, COUNT(*) as count FROM orders GROUP BY status;
