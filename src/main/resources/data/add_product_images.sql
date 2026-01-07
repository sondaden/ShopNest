-- =====================================================
-- SQL Script: Thêm ảnh online cho tất cả sản phẩm
-- Database: shopnest
-- Ngày tạo: 2024-12-01
-- =====================================================

-- Xóa tất cả ảnh cũ (nếu có) để tránh trùng lặp
-- DELETE FROM product_images;

-- =====================================================
-- CÁCH 1: Thêm ảnh dựa trên product_id cụ thể
-- (Sử dụng nếu biết rõ product_id trong database)
-- =====================================================

-- Giả sử có các sản phẩm với id từ 1-20, mỗi sản phẩm có 3-4 ảnh
-- Sử dụng ảnh từ các nguồn: Unsplash, Picsum, Placeholder

-- Product 1 - Điện thoại
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(1, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600', 1, true, NOW()),
(1, 'https://images.unsplash.com/photo-1565849904461-04a58ad377e0?w=600', 2, false, NOW()),
(1, 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=600', 3, false, NOW());

-- Product 2 - Laptop
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(2, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=600', 1, true, NOW()),
(2, 'https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?w=600', 2, false, NOW()),
(2, 'https://images.unsplash.com/photo-1484788984921-03950022c9ef?w=600', 3, false, NOW());

-- Product 3 - Tai nghe
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(3, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600', 1, true, NOW()),
(3, 'https://images.unsplash.com/photo-1484704849700-f032a568e944?w=600', 2, false, NOW()),
(3, 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600', 3, false, NOW());

-- Product 4 - Đồng hồ thông minh
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(4, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600', 1, true, NOW()),
(4, 'https://images.unsplash.com/photo-1434493789847-2f02dc6ca35d?w=600', 2, false, NOW()),
(4, 'https://images.unsplash.com/photo-1617043786394-f977fa12eddf?w=600', 3, false, NOW());

-- Product 5 - Camera
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(5, 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600', 1, true, NOW()),
(5, 'https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=600', 2, false, NOW()),
(5, 'https://images.unsplash.com/photo-1617005082133-548c4dd27f35?w=600', 3, false, NOW());

-- Product 6 - Bàn phím cơ
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(6, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600', 1, true, NOW()),
(6, 'https://images.unsplash.com/photo-1595225476474-87563907a212?w=600', 2, false, NOW()),
(6, 'https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=600', 3, false, NOW());

-- Product 7 - Chuột gaming
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(7, 'https://images.unsplash.com/photo-1527814050087-3793815479db?w=600', 1, true, NOW()),
(7, 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=600', 2, false, NOW()),
(7, 'https://images.unsplash.com/photo-1623820919239-0d0ff10797a1?w=600', 3, false, NOW());

-- Product 8 - Màn hình
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(8, 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600', 1, true, NOW()),
(8, 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600', 2, false, NOW()),
(8, 'https://images.unsplash.com/photo-1593640495253-23196b27a87f?w=600', 3, false, NOW());

-- Product 9 - Loa Bluetooth
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(9, 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600', 1, true, NOW()),
(9, 'https://images.unsplash.com/photo-1545454675-3531b543be5d?w=600', 2, false, NOW()),
(9, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600', 3, false, NOW());

-- Product 10 - Sạc dự phòng
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(10, 'https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?w=600', 1, true, NOW()),
(10, 'https://images.unsplash.com/photo-1585338107529-13afc5f02586?w=600', 2, false, NOW()),
(10, 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=600', 3, false, NOW());

-- Product 11 - Ốp lưng điện thoại
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(11, 'https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=600', 1, true, NOW()),
(11, 'https://images.unsplash.com/photo-1592899677977-9c10ca588bbd?w=600', 2, false, NOW()),
(11, 'https://images.unsplash.com/photo-1609081219090-a6d81d3085bf?w=600', 3, false, NOW());

-- Product 12 - Túi xách
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(12, 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600', 1, true, NOW()),
(12, 'https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=600', 2, false, NOW()),
(12, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600', 3, false, NOW());

-- Product 13 - Giày thể thao
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(13, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600', 1, true, NOW()),
(13, 'https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=600', 2, false, NOW()),
(13, 'https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=600', 3, false, NOW());

-- Product 14 - Áo thun
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(14, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600', 1, true, NOW()),
(14, 'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=600', 2, false, NOW()),
(14, 'https://images.unsplash.com/photo-1562157873-818bc0726f68?w=600', 3, false, NOW());

-- Product 15 - Quần jeans
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(15, 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=600', 1, true, NOW()),
(15, 'https://images.unsplash.com/photo-1475178626620-a4d074967452?w=600', 2, false, NOW()),
(15, 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=600', 3, false, NOW());

-- Product 16 - Kính mát
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(16, 'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600', 1, true, NOW()),
(16, 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=600', 2, false, NOW()),
(16, 'https://images.unsplash.com/photo-1508296695146-257a814070b4?w=600', 3, false, NOW());

-- Product 17 - Nước hoa
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(17, 'https://images.unsplash.com/photo-1541643600914-78b084683601?w=600', 1, true, NOW()),
(17, 'https://images.unsplash.com/photo-1523293182086-7651a899d37f?w=600', 2, false, NOW()),
(17, 'https://images.unsplash.com/photo-1595425970377-c9703cf48b6d?w=600', 3, false, NOW());

-- Product 18 - Balo
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(18, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600', 1, true, NOW()),
(18, 'https://images.unsplash.com/photo-1581605405669-fcdf81165afa?w=600', 2, false, NOW()),
(18, 'https://images.unsplash.com/photo-1622560480605-d83c853bc5c3?w=600', 3, false, NOW());

-- Product 19 - Ví da
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(19, 'https://images.unsplash.com/photo-1627123424574-724758594e93?w=600', 1, true, NOW()),
(19, 'https://images.unsplash.com/photo-1606503153255-59d8b8b82176?w=600', 2, false, NOW()),
(19, 'https://images.unsplash.com/photo-1624996379697-f01d168b1a52?w=600', 3, false, NOW());

-- Product 20 - Đèn bàn
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
(20, 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=600', 1, true, NOW()),
(20, 'https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=600', 2, false, NOW()),
(20, 'https://images.unsplash.com/photo-1534073828943-f801091bb18c?w=600', 3, false, NOW());

-- =====================================================
-- CÁCH 2: Thêm ảnh cho TẤT CẢ sản phẩm tự động
-- (Sử dụng nếu không biết product_id cụ thể)
-- =====================================================

-- Stored Procedure để thêm ảnh random cho tất cả sản phẩm chưa có ảnh
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS AddProductImagesForAll()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE p_id BIGINT;
    DECLARE p_name VARCHAR(255);
    DECLARE img_count INT;
    
    -- Danh sách các URL ảnh mẫu
    DECLARE img_urls TEXT DEFAULT 
        'https://picsum.photos/seed/prod1/600/600,' ||
        'https://picsum.photos/seed/prod2/600/600,' ||
        'https://picsum.photos/seed/prod3/600/600,' ||
        'https://picsum.photos/seed/prod4/600/600,' ||
        'https://picsum.photos/seed/prod5/600/600';
    
    -- Cursor để duyệt qua tất cả sản phẩm
    DECLARE product_cursor CURSOR FOR 
        SELECT id, name FROM products;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN product_cursor;
    
    read_loop: LOOP
        FETCH product_cursor INTO p_id, p_name;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Kiểm tra sản phẩm đã có ảnh chưa
        SELECT COUNT(*) INTO img_count FROM product_images WHERE product_id = p_id;
        
        -- Nếu chưa có ảnh, thêm 3 ảnh
        IF img_count = 0 THEN
            INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at) VALUES
            (p_id, CONCAT('https://picsum.photos/seed/', p_id, 'a/600/600'), 1, true, NOW()),
            (p_id, CONCAT('https://picsum.photos/seed/', p_id, 'b/600/600'), 2, false, NOW()),
            (p_id, CONCAT('https://picsum.photos/seed/', p_id, 'c/600/600'), 3, false, NOW());
        END IF;
    END LOOP;
    
    CLOSE product_cursor;
END //
DELIMITER ;

-- Gọi procedure
-- CALL AddProductImagesForAll();

-- =====================================================
-- CÁCH 3: INSERT đơn giản cho tất cả sản phẩm (không cần procedure)
-- Sử dụng Picsum Photos với seed = product_id để có ảnh khác nhau
-- =====================================================

-- Thêm ảnh chính (primary) cho tất cả sản phẩm chưa có ảnh
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT 
    p.id,
    CONCAT('https://picsum.photos/seed/', p.id, '/600/600'),
    1,
    true,
    NOW()
FROM products p
LEFT JOIN product_images pi ON p.id = pi.product_id
WHERE pi.id IS NULL;

-- Thêm ảnh thứ 2 cho tất cả sản phẩm
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT 
    p.id,
    CONCAT('https://picsum.photos/seed/', p.id, 'b/600/600'),
    2,
    false,
    NOW()
FROM products p
WHERE p.id NOT IN (
    SELECT DISTINCT product_id FROM product_images WHERE display_order = 2
);

-- Thêm ảnh thứ 3 cho tất cả sản phẩm
INSERT INTO product_images (product_id, image_url, display_order, is_primary, created_at)
SELECT 
    p.id,
    CONCAT('https://picsum.photos/seed/', p.id, 'c/600/600'),
    3,
    false,
    NOW()
FROM products p
WHERE p.id NOT IN (
    SELECT DISTINCT product_id FROM product_images WHERE display_order = 3
);

-- =====================================================
-- VERIFY: Kiểm tra kết quả
-- =====================================================
SELECT 
    p.id AS product_id,
    p.name AS product_name,
    COUNT(pi.id) AS image_count
FROM products p
LEFT JOIN product_images pi ON p.id = pi.product_id
GROUP BY p.id, p.name
ORDER BY p.id;

-- Xem chi tiết ảnh của từng sản phẩm
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
