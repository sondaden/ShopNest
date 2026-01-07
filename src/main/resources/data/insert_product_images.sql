-- =====================================================
-- SQL Script: Thêm ảnh cho sản phẩm từ link online
-- Database: shopnest
-- Ngày tạo: 2024-12-01
-- =====================================================

-- Trước tiên, tìm product_id của sản phẩm iPhone 16 Pro Max
-- SELECT id, name FROM products WHERE name LIKE '%iPhone%' OR name LIKE '%iphone%';

-- =====================================================
-- PRODUCT 1: iPhone 16 Pro Max
-- =====================================================
-- Xóa ảnh cũ (nếu có) - thay @PRODUCT_ID bằng id thực tế
-- DELETE FROM product_images WHERE product_id = @PRODUCT_ID;

-- Cách 1: Nếu biết product_id (ví dụ: id = 1)
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(1, 'https://cdsassets.apple.com/live/7WUAS350/images/tech-specs/121032-iphone-16-pro-max.png', 1, true, NOW()),
(1, 'https://images.openai.com/static-rsc-1/5iDb3HuiU1QyypCCry6O-iyVKkHPhQhI_DAFVpnl2SvXet98xrZG5eyP_5bGRIrbsdmpwzgELB9_aqE8pkcLf_vw1XVGSYJJcyyxcE996qGp01_JJdljsKQ4yrDjRKwqAN0o1E7o9bKyiY1xjvWuyg', 2, false, NOW()),
(1, 'https://www.apple.com/newsroom/images/2024/09/apple-debuts-iphone-16-pro-and-iphone-16-pro-max/article/Apple-iPhone-16-Pro-hero-geo-240909_inline.jpg.large.jpg', 3, false, NOW());

-- Cách 2: Nếu không biết product_id, dùng subquery theo tên
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT id, 'https://cdsassets.apple.com/live/7WUAS350/images/tech-specs/121032-iphone-16-pro-max.png', 1, true, NOW()
FROM products WHERE name LIKE '%iPhone 16 Pro Max%' LIMIT 1;

INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT id, 'https://images.openai.com/static-rsc-1/5iDb3HuiU1QyypCCry6O-iyVKkHPhQhI_DAFVpnl2SvXet98xrZG5eyP_5bGRIrbsdmpwzgELB9_aqE8pkcLf_vw1XVGSYJJcyyxcE996qGp01_JJdljsKQ4yrDjRKwqAN0o1E7o9bKyiY1xjvWuyg', 2, false, NOW()
FROM products WHERE name LIKE '%iPhone 16 Pro Max%' LIMIT 1;

INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT id, 'https://www.apple.com/newsroom/images/2024/09/apple-debuts-iphone-16-pro-and-iphone-16-pro-max/article/Apple-iPhone-16-Pro-hero-geo-240909_inline.jpg.large.jpg', 3, false, NOW()
FROM products WHERE name LIKE '%iPhone 16 Pro Max%' LIMIT 1;

-- =====================================================
-- PRODUCT 2: iPhone 16
-- =====================================================
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT id, 'https://cdsassets.apple.com/live/7WUAS350/images/tech-specs/iphone-16.png', 1, true, NOW()
FROM products WHERE name LIKE '%iPhone 16%' AND name NOT LIKE '%Pro%' LIMIT 1;

INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT id, 'https://m.media-amazon.com/images/I/61Ml-IP%2B73L.jpg', 2, false, NOW()
FROM products WHERE name LIKE '%iPhone 16%' AND name NOT LIKE '%Pro%' LIMIT 1;

INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT id, 'https://store.storeimages.cdn-apple.com/1/as-images.apple.com/is/iphone-16-finish-select-202409-6-1inch_GEO_EMEA_FMT_WHH?.v=UXp1U3VDY3IyR1hNdHZwdFdOLzg1V0tFK1lhSCtYSGRqMUdhR284NTN4K0VvSjFQM0pLN0VsK2pmbVJmK1hUZDhiZjRKRUJ6ZU96N3VHVCtXdS9WdVUzdWN4ZENIZEJCc01VOW1QK3BzTGVNdlIyKy9FMURXQmRzdk1KZVhnSDh1WjFlQndWT3ZmeW5zc3dRUHliS2dB&fmt=p-jpg&hei=492&qlt=80&traceId=1&wid=1280', 3, false, NOW()
FROM products WHERE name LIKE '%iPhone 16%' AND name NOT LIKE '%Pro%' LIMIT 1;

-- =====================================================
-- PRODUCT 3: Samsung Galaxy S25 Ultra
-- =====================================================
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT id, 'https://tse4.mm.bing.net/th/id/OIP.SJx7s5sdyzwq9Fpdb4OVLAHaHa?w=474&h=379&c=7&p=0', 1, true, NOW()
FROM products WHERE name LIKE '%Samsung Galaxy S25 Ultra%' OR name LIKE '%S25 Ultra%' LIMIT 1;

INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT id, 'https://tse3.mm.bing.net/th/id/OIP.ytj57SokkD1t8t1yRCWVdQHaEK?w=474&h=379&c=7&p=0', 2, false, NOW()
FROM products WHERE name LIKE '%Samsung Galaxy S25 Ultra%' OR name LIKE '%S25 Ultra%' LIMIT 1;

INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT id, 'https://tse1.mm.bing.net/th/id/OIP.HBozQEkHM58MCy-UZxNE6QHaEK?w=474&h=379&c=7&p=0', 3, false, NOW()
FROM products WHERE name LIKE '%Samsung Galaxy S25 Ultra%' OR name LIKE '%S25 Ultra%' LIMIT 1;

-- =====================================================
-- Thêm sản phẩm tiếp theo ở đây...
-- =====================================================



-- =====================================================
-- VERIFY: Kiểm tra kết quả
-- =====================================================
SELECT 
    pi.id,
    pi.product_id,
    p.name AS product_name,
    pi.image_url,
    pi.display_order,
    pi.is_primary
FROM product_images pi
JOIN products p ON pi.product_id = p.id
ORDER BY pi.product_id, pi.display_order;
