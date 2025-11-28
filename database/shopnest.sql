-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               10.4.32-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             12.8.0.6908
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for shopnest
DROP DATABASE IF EXISTS `shopnest`;
CREATE DATABASE IF NOT EXISTS `shopnest` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `shopnest`;

-- Dumping structure for table shopnest.addresses
DROP TABLE IF EXISTS `addresses`;
CREATE TABLE IF NOT EXISTS `addresses` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `recipient_name` varchar(255) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `province` varchar(100) NOT NULL,
  `district` varchar(100) NOT NULL,
  `ward` varchar(100) NOT NULL,
  `street` varchar(500) NOT NULL,
  `is_default` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `addresses_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.addresses: ~0 rows (approximately)

-- Dumping structure for table shopnest.brands
DROP TABLE IF EXISTS `brands`;
CREATE TABLE IF NOT EXISTS `brands` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL,
  `logo_url` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.brands: ~4 rows (approximately)
INSERT INTO `brands` (`id`, `name`, `slug`, `logo_url`, `created_at`) VALUES
	(1, 'Apple', 'apple', NULL, '2025-11-28 15:36:33'),
	(2, 'Samsung', 'samsung', NULL, '2025-11-28 15:36:33'),
	(3, 'Xiaomi', 'xiaomi', NULL, '2025-11-28 15:36:33'),
	(4, 'Oppo', 'oppo', NULL, '2025-11-28 15:36:33'),
	(5, 'Sony', 'sony', NULL, '2025-11-28 15:36:33');

-- Dumping structure for table shopnest.carts
DROP TABLE IF EXISTS `carts`;
CREATE TABLE IF NOT EXISTS `carts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `carts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.carts: ~2 rows (approximately)
INSERT INTO `carts` (`id`, `user_id`, `created_at`, `updated_at`) VALUES
	(2, 1, '2025-11-28 03:14:51', '2025-11-28 03:14:51'),
	(3, 2, '2025-11-28 03:18:19', '2025-11-28 03:18:19');

-- Dumping structure for table shopnest.cart_items
DROP TABLE IF EXISTS `cart_items`;
CREATE TABLE IF NOT EXISTS `cart_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cart_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_cart_product` (`cart_id`,`product_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `cart_items_ibfk_1` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `cart_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.cart_items: ~1 rows (approximately)
INSERT INTO `cart_items` (`id`, `cart_id`, `product_id`, `quantity`, `created_at`, `updated_at`) VALUES
	(1, 2, 4, 1, '2025-11-28 03:14:51', '2025-11-28 03:14:51');

-- Dumping structure for table shopnest.categories
DROP TABLE IF EXISTS `categories`;
CREATE TABLE IF NOT EXISTS `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.categories: ~5 rows (approximately)
INSERT INTO `categories` (`id`, `name`, `slug`, `description`, `created_at`, `updated_at`) VALUES
	(1, 'Điện thoại', 'dien-thoai', NULL, '2025-11-28 15:36:33', '2025-11-28 15:36:33'),
	(2, 'Laptop', 'laptop', NULL, '2025-11-28 15:36:33', '2025-11-28 15:36:33'),
	(3, 'Tai nghe', 'tai-nghe', NULL, '2025-11-28 15:36:33', '2025-11-28 15:36:33'),
	(4, 'Đồng hồ thông minh', 'dong-ho-thong-minh', NULL, '2025-11-28 15:36:33', '2025-11-28 15:36:33'),
	(5, 'Máy tính bảng', 'may-tinh-bang', NULL, '2025-11-28 15:36:33', '2025-11-28 15:36:33');

-- Dumping structure for table shopnest.orders
DROP TABLE IF EXISTS `orders`;
CREATE TABLE IF NOT EXISTS `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_code` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `total_amount` decimal(38,2) NOT NULL,
  `status` enum('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED') DEFAULT 'PENDING',
  `payment_method` enum('COD','BANK_TRANSFER','MOMO','VNPAY') DEFAULT 'COD',
  `payment_status` enum('UNPAID','PAID','REFUNDED') DEFAULT 'UNPAID',
  `customer_name` varchar(255) NOT NULL,
  `phone` varchar(255) NOT NULL,
  `shipping_address` varchar(255) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_code` (`order_code`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.orders: ~0 rows (approximately)
INSERT INTO `orders` (`id`, `order_code`, `user_id`, `total_amount`, `status`, `payment_method`, `payment_status`, `customer_name`, `phone`, `shipping_address`, `note`, `created_at`, `updated_at`) VALUES
	(1, 'SHOP20251128150755', 2, 79980000.00, 'PENDING', 'COD', 'UNPAID', 'Nguyễn Văn A', '0901234567', '123 Đường Láng, Hà Nội', 'Giao buổi chiều giúp em', '2025-11-28 08:07:55', '2025-11-28 08:07:55');

-- Dumping structure for table shopnest.order_items
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE IF NOT EXISTS `order_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `product_image` varchar(255) DEFAULT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `quantity` int(11) NOT NULL,
  `subtotal` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.order_items: ~1 rows (approximately)
INSERT INTO `order_items` (`id`, `order_id`, `product_id`, `product_name`, `product_image`, `price`, `quantity`, `subtotal`) VALUES
	(1, 1, 4, 'iPhone 16 Pro Max', 'https://example.com/iphone16.jpg', 39990000.00, 2, 79980000.00);

-- Dumping structure for table shopnest.products
DROP TABLE IF EXISTS `products`;
CREATE TABLE IF NOT EXISTS `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `slug` varchar(255) DEFAULT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `short_description` varchar(1000) DEFAULT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `discount_price` decimal(15,2) DEFAULT NULL,
  `stock` int(11) DEFAULT NULL,
  `sku` varchar(100) DEFAULT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  `brand_id` bigint(20) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `is_featured` tinyint(1) DEFAULT 0,
  `view_count` int(11) DEFAULT 0,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `expiration_date` date DEFAULT NULL,
  `import_date` date DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `category` varchar(100) DEFAULT NULL,
  `brand` varchar(100) DEFAULT NULL,
  `rating` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug_2` (`slug`),
  UNIQUE KEY `sku` (`sku`),
  KEY `category_id` (`category_id`),
  KEY `brand_id` (`brand_id`),
  FULLTEXT KEY `ft_name_description` (`name`,`description`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `products_ibfk_2` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.products: ~20 rows (approximately)
INSERT INTO `products` (`id`, `name`, `slug`, `description`, `short_description`, `price`, `discount_price`, `stock`, `sku`, `category_id`, `brand_id`, `is_active`, `is_featured`, `view_count`, `created_at`, `updated_at`, `expiration_date`, `import_date`, `image_url`, `category`, `brand`, `rating`) VALUES
	(1, 'iPhone 16 Pro Max', 'iphone-16-pro-max', 'Flagship Apple 2025', NULL, 39990000.00, NULL, 85, NULL, 1, 1, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/42/305658/iphone-16-pro-max-den.jpg', NULL, NULL, 4.9),
	(2, 'iPhone 16', 'iphone-16', 'Bản tiêu chuẩn', NULL, 25990000.00, NULL, 120, NULL, 1, 1, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/42/305657/iphone-16-hong.jpg', NULL, NULL, 4.7),
	(3, 'Samsung Galaxy S25 Ultra', 'samsung-galaxy-s25-ultra', 'Camera 200MP', NULL, 35990000.00, NULL, 60, NULL, 1, 2, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/42/303761/s25-ultra-xam.jpg', NULL, NULL, 4.8),
	(4, 'Samsung Galaxy A55', 'samsung-galaxy-a55', 'Giá rẻ pin trâu', NULL, 9990000.00, NULL, 300, NULL, 1, 2, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/42/308166/galaxy-a55-xanh.jpg', NULL, NULL, 4.5),
	(5, 'Xiaomi 14T Pro', 'xiaomi-14t-pro', 'Sạc nhanh 120W', NULL, 14990000.00, NULL, 200, NULL, 1, 3, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/42/307998/xiaomi-14t-pro-den.jpg', NULL, NULL, 4.6),
	(6, 'Oppo Reno12 Pro', 'oppo-reno12-pro', 'Camera selfie 50MP', NULL, 13990000.00, NULL, 150, NULL, 1, 4, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/42/308167/reno12-pro-bac.jpg', NULL, NULL, 4.4),
	(7, 'MacBook Pro 14 M4 Pro', 'macbook-pro-14-m4-pro', 'Chip M4 Pro', NULL, 57990000.00, NULL, 25, NULL, 2, 1, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/44/318947/macbook-pro-14-m4-pro.jpg', NULL, NULL, 5),
	(8, 'MacBook Air M3', 'macbook-air-m3', 'Mỏng nhẹ', NULL, 27990000.00, NULL, 80, NULL, 2, 1, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/44/302234/macbook-air-m3-vang.jpg', NULL, NULL, 4.9),
	(9, 'Samsung Galaxy Book4 Pro', 'samsung-galaxy-book4-pro', 'Màn AMOLED', NULL, 33990000.00, NULL, 40, NULL, 2, 2, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/44/310000/book4-pro-bac.jpg', NULL, NULL, 4.7),
	(10, 'Dell XPS 14 2025', 'dell-xps-14-2025', 'Viền mỏng', NULL, 45990000.00, NULL, 30, NULL, 2, NULL, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://example.com/dell-xps14.jpg', NULL, NULL, 4.8),
	(11, 'AirPods Pro 2', 'airpods-pro-2', 'Chống ồn chủ động', NULL, 5990000.00, NULL, 400, NULL, 3, 1, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/54/251920/airpods-pro-2.jpg', NULL, NULL, 4.8),
	(12, 'Sony WF-1000XM5', 'sony-wf-1000xm5', 'True wireless đỉnh', NULL, 6990000.00, NULL, 180, NULL, 3, 5, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/54/309099/wf-1000xm5-den.jpg', NULL, NULL, 4.9),
	(13, 'Samsung Galaxy Buds3 Pro', 'samsung-galaxy-buds3-pro', 'Âm thanh AKG', NULL, 4990000.00, NULL, 250, NULL, 3, 2, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/54/315000/buds3-pro-bac.jpg', NULL, NULL, 4.6),
	(14, 'Apple Watch Series 10', 'apple-watch-series-10', 'Màn hình lớn hơn', NULL, 12990000.00, NULL, 100, NULL, 4, 1, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/7077/318000/watch-s10-den.jpg', NULL, NULL, 4.7),
	(15, 'Samsung Galaxy Watch7', 'samsung-galaxy-watch7', 'Wear OS', NULL, 8990000.00, NULL, 120, NULL, 4, 2, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/7077/310000/watch7-xanh.jpg', NULL, NULL, 4.6),
	(16, 'iPad Air 11 M2', 'ipad-air-11-m2', 'Chip M2', NULL, 18990000.00, NULL, 70, NULL, 5, 1, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/522/302235/ipad-air-11-m2-xam.jpg', NULL, NULL, 4.9),
	(17, 'Samsung Galaxy Tab S9 FE', 'samsung-galaxy-tab-s9-fe', 'Kèm bút', NULL, 11990000.00, NULL, 90, NULL, 5, 2, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/522/308000/tab-s9-fe-xanh.jpg', NULL, NULL, 4.5),
	(18, 'Xiaomi Pad 7 Pro', 'xiaomi-pad-7-pro', 'Màn 144Hz', NULL, 10990000.00, NULL, 150, NULL, 5, 3, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/522/315000/pad7-pro-den.jpg', NULL, NULL, 4.6),
	(19, 'Sony WH-1000XM5', 'sony-wh-1000xm5', 'Vua tai nghe over-ear', NULL, 8490000.00, NULL, 110, NULL, 3, 5, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/54/269000/wh-1000xm5-bac.jpg', NULL, NULL, 4.9),
	(20, 'iPhone 15 Pro cũ', 'iphone-15-pro-cu', 'Hàng cũ 99%', NULL, 21990000.00, NULL, 50, NULL, 1, 1, 1, 0, 0, '2025-11-28 22:36:33', '2025-11-28 22:36:33', NULL, NULL, 'https://cdn.tgdd.vn/Products/Images/42/281579/iphone-15-pro-cu.jpg', NULL, NULL, 4.8);

-- Dumping structure for table shopnest.product_images
DROP TABLE IF EXISTS `product_images`;
CREATE TABLE IF NOT EXISTS `product_images` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `image_url` varchar(1000) NOT NULL,
  `is_main` tinyint(1) DEFAULT 0,
  `sort_order` int(11) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `product_images_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.product_images: ~0 rows (approximately)

-- Dumping structure for table shopnest.product_reviews
DROP TABLE IF EXISTS `product_reviews`;
CREATE TABLE IF NOT EXISTS `product_reviews` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `rating` tinyint(4) DEFAULT NULL CHECK (`rating` between 1 and 5),
  `comment` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `product_id` (`product_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `product_reviews_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  CONSTRAINT `product_reviews_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.product_reviews: ~0 rows (approximately)

-- Dumping structure for table shopnest.product_views
DROP TABLE IF EXISTS `product_views`;
CREATE TABLE IF NOT EXISTS `product_views` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) NOT NULL,
  `view_count` int(11) DEFAULT 1,
  `last_viewed_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_product` (`user_id`,`product_id`),
  KEY `product_id` (`product_id`),
  KEY `idx_last_viewed` (`last_viewed_at`),
  CONSTRAINT `product_views_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `product_views_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.product_views: ~0 rows (approximately)

-- Dumping structure for table shopnest.search_history
DROP TABLE IF EXISTS `search_history`;
CREATE TABLE IF NOT EXISTS `search_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `keyword` varchar(255) NOT NULL,
  `search_count` int(11) DEFAULT 1,
  `last_searched_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_keyword` (`user_id`,`keyword`),
  KEY `idx_keyword` (`keyword`),
  KEY `idx_count` (`search_count`),
  KEY `idx_last_searched` (`last_searched_at`),
  KEY `idx_user_keyword` (`user_id`,`keyword`),
  CONSTRAINT `fk_search_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table shopnest.search_history: ~8 rows (approximately)
INSERT INTO `search_history` (`id`, `user_id`, `keyword`, `search_count`, `last_searched_at`) VALUES
	(1, 2, 'iphone', 1, '2025-11-28 16:02:49'),
	(2, 2, 'samsung', 1, '2025-11-28 16:02:50'),
	(3, 2, 'macbook', 1, '2025-11-28 16:02:50'),
	(4, 2, 'tai nghe', 1, '2025-11-28 16:02:50'),
	(5, 2, 'điện thoại', 1, '2025-11-28 16:02:50'),
	(6, 2, 'laptop', 1, '2025-11-28 16:02:50'),
	(7, 2, 'airpods', 1, '2025-11-28 16:02:50'),
	(8, 2, 'xiaomi', 1, '2025-11-28 16:02:50');

-- Dumping structure for table shopnest.users
DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `role` enum('USER','ADMIN') DEFAULT 'USER',
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table shopnest.users: ~2 rows (approximately)
INSERT INTO `users` (`id`, `username`, `email`, `password`, `full_name`, `phone`, `avatar_url`, `role`, `is_active`, `created_at`, `updated_at`) VALUES
	(1, 'admin', 'admin@shopnest.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrator', NULL, NULL, 'ADMIN', 1, '2025-11-27 17:02:22', '2025-11-27 17:02:22'),
	(2, 'khachhang1', 'user@gmail.com', '$2a$10$Uy1LNXrJdGoakHdiRKM0wur1RYJw3IENa4o7haCEc0LZ/PoAjSwXi', 'Nguyễn Văn A', '0901234567', NULL, 'USER', 1, '2025-11-27 17:04:11', '2025-11-27 17:04:11');

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
