-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 05, 2025 at 04:19 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `storvendb`
--

-- --------------------------------------------------------

--
-- Table structure for table `accounts`
--

CREATE TABLE `accounts` (
  `account_id` int(15) NOT NULL,
  `fname` varchar(250) NOT NULL,
  `lname` varchar(250) NOT NULL,
  `username` varchar(250) NOT NULL,
  `password` varchar(250) NOT NULL,
  `role` varchar(250) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `accounts`
--

INSERT INTO `accounts` (`account_id`, `fname`, `lname`, `username`, `password`, `role`) VALUES
(15, '1', '1', '1', '1', 'Admin'),
(16, 'Mark Jeshmer', 'Jaromahum', 'MarkStorm', 'Bloody123', 'Admin'),
(17, 'Delyn', 'Delamata', 'Lyn', '123456789', 'User'),
(18, 'Fenny Anne', 'Cervantez', 'Ceannix', 'Cervantezfen', 'User'),
(19, 'Delyn', 'Delamata', 'Daylaine', 'Delamats', 'User');

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `category_id` int(11) NOT NULL,
  `category_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`category_id`, `category_name`) VALUES
(5, 'Beverages'),
(4, 'Canned & Instant Foods'),
(7, 'Cooking Essentials'),
(2, 'Frozen Foods'),
(9, 'Laundry & Cleaning Supplies'),
(8, 'Personal Care'),
(6, 'Powdered Drinks'),
(10, 'School & Office Supplies'),
(3, 'Snacks');

-- --------------------------------------------------------

--
-- Table structure for table `inventory`
--

CREATE TABLE `inventory` (
  `product_id` int(100) NOT NULL,
  `product_name` varchar(250) NOT NULL,
  `category` varchar(250) NOT NULL,
  `stock_level` int(11) NOT NULL DEFAULT 0,
  `reorder_level` int(11) NOT NULL DEFAULT 0,
  `price` decimal(10,2) UNSIGNED NOT NULL DEFAULT 0.00,
  `expiration_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `inventory`
--

INSERT INTO `inventory` (`product_id`, `product_name`, `category`, `stock_level`, `reorder_level`, `price`, `expiration_date`) VALUES
(2, 'Lucky Me Pancit Canton(Chilimansi)', 'Canned & Instant Foods', 38, 5, 15.00, '2025-05-10'),
(4, 'Magic Sarap', 'Cooking Essentials', 58, 5, 5.00, '2026-10-20'),
(5, 'Pride Bar', 'Laundry & Cleaning Supplies', 18, 10, 24.00, '2027-05-01'),
(6, 'Lucky Me Noodles (Beef)', 'Canned & Instant Foods', 77, 5, 10.00, '2025-11-03'),
(7, 'Holiday Beef Loaf', 'Canned & Instant Foods', 43, 5, 26.00, '2026-04-23'),
(8, 'Cracklings', 'Snacks', 40, 5, 10.00, '2025-08-19'),
(9, 'Coca-Cola 190mL', 'Beverages', 24, 5, 15.00, '2025-09-02'),
(10, 'Coca-Cola 1L', 'Beverages', 98, 5, 45.00, '2025-10-01'),
(11, 'Chuckie', 'Beverages', 25, 5, 18.00, '2025-07-15'),
(12, 'Wings Solve Powder', 'Laundry & Cleaning Supplies', 95, 10, 8.00, '2026-06-04'),
(13, 'Surf Powder (Rose Fresh)', 'Laundry & Cleaning Supplies', 99, 10, 8.00, '2026-03-29'),
(14, 'Tang Orange', 'Powdered Drinks', 79, 10, 22.00, '2025-10-15'),
(15, 'Tawas', 'Personal Care', 10, 5, 11.00, '2028-11-17'),
(16, '1/4 Pad Paper (80 Leaves)', 'School & Office Supplies', 19, 10, 24.00, NULL),
(17, '1/2 Pad Paper Lengthwise (80 Leaves)', 'School & Office Supplies', 40, 10, 21.00, NULL),
(18, '1/2 Pad  Paper Crosswise (80 Leaves)', 'School & Office Supplies', 55, 10, 21.00, NULL),
(19, '1/2 Pad Paper Lengthwise (50 Leaves)', 'School & Office Supplies', 19, 10, 19.00, NULL),
(20, '1/2 Pad Paper Crosswise (50 Leaves)', 'School & Office Supplies', 19, 10, 19.00, NULL),
(21, 'Tang Grapes', 'Powdered Drinks', 49, 10, 22.00, '2025-10-30'),
(22, 'Mega Sardines Spicy', 'Canned & Instant Foods', 19, 10, 26.00, '2028-05-15'),
(23, 'Mega Sardines', 'Canned & Instant Foods', 19, 10, 26.00, '2025-05-06'),
(24, 'Yellow Pad', 'School & Office Supplies', 19, 10, 36.00, NULL),
(25, 'Cream Silk', 'Personal Care', 89, 10, 8.00, '2025-11-18'),
(26, 'Sun Silk', 'Personal Care', 80, 10, 10.00, '2025-10-08'),
(27, 'Safe Guard Pink', 'Personal Care', 29, 10, 30.00, '2026-06-01'),
(28, 'Choco Knots', 'Snacks', 49, 10, 10.00, '2025-07-16'),
(29, 'Crispy Patata', 'Snacks', 39, 10, 10.00, '2025-08-25'),
(30, 'Chippy', 'Snacks', 39, 10, 10.00, '2025-09-01'),
(31, 'Mongol Pencil #2 Medium Yellow  12/Box', 'School & Office Supplies', 29, 10, 119.00, NULL),
(32, 'Mongol Pencil #2 Medium (1pc)', 'School & Office Supplies', 39, 10, 12.00, NULL),
(33, 'HBOffice ULTRA Eraser E-07', 'School & Office Supplies', 49, 10, 20.00, NULL),
(34, 'Faber Castell 1423 Ballpoint Pen Black', 'School & Office Supplies', 49, 10, 16.00, NULL),
(35, 'Rover Sprint Ballpoint Pen Stick 0.5mm', 'School & Office Supplies', 49, 10, 8.00, NULL),
(36, '1kg Oil', 'Cooking Essentials', 19, 10, 90.00, '2025-10-09'),
(37, '1/2 Oil', 'Cooking Essentials', 19, 10, 45.00, '2025-12-24'),
(38, 'Salt', 'Cooking Essentials', 19, 10, 10.00, '2028-01-01'),
(39, 'Safe Guard Yellow', 'Personal Care', 19, 10, 30.00, '2026-06-01'),
(40, 'Safe Guard Blue', 'Personal Care', 24, 10, 30.00, '2026-06-01'),
(41, 'Nestea Iced Tea Lemon Blend 20g', 'Powdered Drinks', 29, 10, 22.00, '2025-11-01'),
(42, 'Nestea Iced Tea Apple 20g', 'Powdered Drinks', 19, 10, 22.00, '2025-08-06'),
(43, 'Onion Rings', 'Snacks', 18, 10, 10.00, '2025-08-31'),
(44, 'Bond Paper (1pc)', 'School & Office Supplies', 99, 10, 1.25, NULL),
(45, 'Downey Black Perfume', 'Laundry & Cleaning Supplies', 19, 10, 12.00, '2026-07-09'),
(46, 'Downey Sunrise', 'Laundry & Cleaning Supplies', 29, 10, 12.00, '2026-07-09'),
(47, 'Clover Cheese Flavor', 'Snacks', 19, 10, 10.00, '2025-08-19'),
(48, 'Mang Juan Chicharon', 'Snacks', 35, 5, 10.00, '2026-01-06');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `notification_id` int(11) NOT NULL,
  `message` varchar(500) NOT NULL,
  `type` varchar(50) NOT NULL,
  `product_name` varchar(250) DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`notification_id`, `message`, `type`, `product_name`, `is_read`, `created_at`) VALUES
(20, 'Lucky Me Pancit Canton(Chilimansi) is about to expire on 2025-05-10', 'EXPIRING', 'Lucky Me Pancit Canton(Chilimansi)', 0, '2025-05-05 22:17:41'),
(21, 'Mega Sardines is about to expire on 2025-05-06', 'EXPIRING', 'Mega Sardines', 0, '2025-05-05 22:17:41'),
(23, 'Coca-Cola 190mL had no sales in the past 14 days.', 'SLOW_MOVING', 'Coca-Cola 190mL', 0, '2025-05-05 22:17:41'),
(25, 'Chuckie had no sales in the past 14 days.', 'SLOW_MOVING', 'Chuckie', 0, '2025-05-05 22:17:41'),
(52, 'Mang Juan Chicharon had no sales in the past 14 days.', 'SLOW_MOVING', 'Mang Juan Chicharon', 0, '2025-05-05 22:17:41'),
(221, 'Pride Bar sold 21 units in the last 7 days.', 'BEST_SELLER', 'Pride Bar', 0, '2025-05-05 22:17:41');

-- --------------------------------------------------------

--
-- Table structure for table `purchases`
--

CREATE TABLE `purchases` (
  `purchase_id` int(11) NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `category` varchar(50) NOT NULL,
  `quantity` int(11) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `purchase_date` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `purchases`
--

INSERT INTO `purchases` (`purchase_id`, `product_name`, `category`, `quantity`, `unit_price`, `purchase_date`) VALUES
(2, 'Cracklings', 'Snacks', 20, 8.00, '2025-05-02 09:29:54'),
(3, 'Safe Guard Blue', 'Personal Care', 20, 25.00, '2025-05-02 09:34:01'),
(4, 'Mang Juan Chicharon', 'Snacks', 20, 8.00, '2025-05-02 10:03:53'),
(5, 'Mang Juan Chicharon', 'Snacks', 15, 8.00, '2025-05-02 10:22:20'),
(6, '1/2 Pad  Paper Crosswise (80 Leaves)', 'School & Office Supplies', 43, 19.00, '2025-05-02 18:02:32'),
(7, '1/2 Pad Paper Lengthwise (80 Leaves)', 'School & Office Supplies', 23, 18.00, '2025-05-03 06:20:57'),
(8, 'Pride Bar', 'Laundry & Cleaning Supplies', 10, 22.00, '2025-05-04 03:47:59');

-- --------------------------------------------------------

--
-- Table structure for table `salerecords`
--

CREATE TABLE `salerecords` (
  `sales_id` int(255) NOT NULL,
  `product_id` int(110) NOT NULL,
  `product_name` varchar(250) NOT NULL,
  `category` varchar(250) NOT NULL,
  `quantity_sold` int(250) NOT NULL,
  `sale_amount` double NOT NULL,
  `sale_datetime` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `salerecords`
--

INSERT INTO `salerecords` (`sales_id`, `product_id`, `product_name`, `category`, `quantity_sold`, `sale_amount`, `sale_datetime`) VALUES
(10, 4, 'Magic Sarap', 'Cooking Essentials', 1, 5, '2025-04-30 20:07:29'),
(11, 8, 'Cracklings', 'Snacks', 2, 20, '2025-04-30 20:13:09'),
(12, 8, 'Cracklings', 'Snacks', 1, 10, '2025-05-01 00:49:41'),
(13, 6, 'Lucky Me Noodles (Beef)', 'Canned & Instant Foods', 2, 20, '2025-05-01 00:49:41'),
(14, 7, 'Holiday Beef Loaf', 'Canned & Instant Foods', 3, 78, '2025-05-01 00:49:41'),
(15, 43, 'Onion Rings', 'Snacks', 2, 20, '2025-05-01 00:49:41'),
(16, 8, 'Cracklings', 'Snacks', 5, 50, '2025-05-01 01:00:46'),
(17, 8, 'Cracklings', 'Snacks', 1, 10, '2025-05-01 02:38:16'),
(18, 7, 'Holiday Beef Loaf', 'Canned & Instant Foods', 1, 26, '2025-05-01 14:36:13'),
(19, 5, 'Pride Bar', 'Laundry & Cleaning Supplies', 1, 24, '2025-05-01 17:03:13'),
(20, 15, 'Tawas', 'Personal Care', 10, 110, '2025-05-01 23:46:22'),
(21, 15, 'Tawas', 'Personal Care', 1, 11, '2025-05-01 23:48:16'),
(22, 47, 'Clover Cheese Flavor', 'Snacks', 1, 10, '2025-05-02 01:14:17'),
(23, 46, 'Downey Sunrise', 'Laundry & Cleaning Supplies', 1, 12, '2025-05-02 01:14:17'),
(24, 45, 'Downey Black Perfume', 'Laundry & Cleaning Supplies', 1, 12, '2025-05-02 01:14:17'),
(25, 44, 'Bond Paper (1pc)', 'School & Office Supplies', 1, 1.25, '2025-05-02 01:14:17'),
(26, 41, 'Nestea Iced Tea Lemon Blend 20g', 'Powdered Drinks', 1, 22, '2025-05-02 01:14:17'),
(27, 42, 'Nestea Iced Tea Apple 20g', 'Powdered Drinks', 1, 22, '2025-05-02 01:14:17'),
(28, 23, 'Mega Sardines', 'Canned & Instant Foods', 1, 26, '2025-05-02 01:14:17'),
(29, 22, 'Mega Sardines Spicy', 'Canned & Instant Foods', 1, 26, '2025-05-02 01:14:17'),
(30, 2, 'Lucky Me Pancit Canton(Chilimansi)', 'Canned & Instant Foods', 1, 15, '2025-05-02 01:14:17'),
(31, 5, 'Pride Bar', 'Laundry & Cleaning Supplies', 1, 24, '2025-05-02 01:14:17'),
(32, 39, 'Safe Guard Yellow', 'Laundry & Cleaning Supplies', 1, 30, '2025-05-02 01:18:15'),
(33, 38, 'Salt', 'Cooking Essentials', 1, 10, '2025-05-02 01:18:15'),
(34, 37, '1/2 Oil', 'Cooking Essentials', 1, 45, '2025-05-02 01:18:15'),
(35, 36, '1kg Oil', 'Cooking Essentials', 1, 90, '2025-05-02 01:18:15'),
(36, 35, 'Rover Sprint Ballpoint Pen Stick 0.5mm', 'School & Office Supplies', 1, 8, '2025-05-02 01:18:15'),
(37, 34, 'Faber Castell 1423 Ballpoint Pen Black', 'School & Office Supplies', 1, 16, '2025-05-02 01:18:15'),
(38, 33, 'HBOffice ULTRA Eraser E-07', 'School & Office Supplies', 1, 20, '2025-05-02 01:18:15'),
(39, 31, 'Mongol Pencil #2 Medium Yellow  12/Box', 'School & Office Supplies', 1, 119, '2025-05-02 01:18:15'),
(40, 32, 'Mongol Pencil #2 Medium (1pc)', 'School & Office Supplies', 1, 12, '2025-05-02 01:18:15'),
(41, 30, 'Chippy', 'Snacks', 1, 10, '2025-05-02 01:18:15'),
(42, 29, 'Crispy Patata', 'Snacks', 1, 10, '2025-05-02 01:18:15'),
(43, 28, 'Choco Knots', 'Snacks', 1, 10, '2025-05-02 01:18:15'),
(44, 27, 'Safe Guard Pink', 'Laundry & Cleaning Supplies', 1, 30, '2025-05-02 01:18:15'),
(45, 26, 'Sun Silk', 'Laundry & Cleaning Supplies', 1, 10, '2025-05-02 01:18:33'),
(46, 25, 'Cream Silk', 'Laundry & Cleaning Supplies', 1, 8, '2025-05-02 01:19:57'),
(47, 24, 'Yellow Pad', 'School & Office Supplies', 1, 36, '2025-05-02 01:19:57'),
(48, 21, 'Tang Grapes', 'Powdered Drinks', 1, 22, '2025-05-02 01:19:57'),
(49, 17, '1/2 Pad Paper Lengthwise (80 Leaves)', 'School & Office Supplies', 1, 21, '2025-05-02 01:19:57'),
(50, 18, '1/2 Pad  Paper Crosswise (80 Leaves)', 'School & Office Supplies', 1, 21, '2025-05-02 01:19:57'),
(51, 17, '1/2 Pad Paper Lengthwise (80 Leaves)', 'School & Office Supplies', 1, 21, '2025-05-02 01:27:42'),
(52, 18, '1/2 Pad  Paper Crosswise (80 Leaves)', 'School & Office Supplies', 1, 21, '2025-05-02 01:27:42'),
(53, 19, '1/2 Pad Paper Lengthwise (50 Leaves)', 'School & Office Supplies', 1, 19, '2025-05-02 01:29:27'),
(54, 20, '1/2 Pad Paper Crosswise (50 Leaves)', 'School & Office Supplies', 1, 19, '2025-05-02 01:29:27'),
(55, 16, '1/4 Pad Paper (80 Leaves)', 'School & Office Supplies', 1, 24, '2025-05-02 01:29:27'),
(56, 14, 'Tang Orange', 'Powdered Drinks', 1, 22, '2025-05-02 01:29:27'),
(57, 18, '1/2 Pad  Paper Crosswise (80 Leaves)', 'School & Office Supplies', 6, 126, '2025-05-02 10:30:14'),
(58, 17, '1/2 Pad Paper Lengthwise (80 Leaves)', 'School & Office Supplies', 1, 21, '2025-05-02 10:30:14'),
(59, 8, 'Cracklings', 'Snacks', 1, 10, '2025-05-02 11:04:44'),
(60, 8, 'Cracklings', 'Snacks', 1, 10, '2025-05-02 11:04:50'),
(61, 13, 'Surf Powder (Rose Fresh)', 'Laundry & Cleaning Supplies', 1, 8, '2025-05-02 11:21:51'),
(62, 4, 'Magic Sarap', 'Cooking Essentials', 2, 10, '2025-05-02 11:25:15'),
(63, 6, 'Lucky Me Noodles (Beef)', 'Canned & Instant Foods', 5, 50, '2025-05-02 11:25:15'),
(64, 15, 'Tawas', 'Personal Care', 1, 11, '2025-05-02 13:33:26'),
(65, 15, 'Tawas', 'Personal Care', 1, 11, '2025-05-02 14:26:24'),
(66, 40, 'Safe Guard Blue', 'Personal Care', 1, 30, '2025-05-02 14:59:42'),
(67, 40, 'Safe Guard Blue', 'Personal Care', 1, 30, '2025-05-02 15:12:26'),
(68, 40, 'Safe Guard Blue', 'Personal Care', 1, 30, '2025-05-02 15:30:19'),
(69, 40, 'Safe Guard Blue', 'Personal Care', 1, 30, '2025-05-02 15:56:39'),
(70, 40, 'Safe Guard Blue', 'Personal Care', 1, 30, '2025-05-02 16:02:57'),
(71, 40, 'Safe Guard Blue', 'Personal Care', 1, 30, '2025-05-02 16:03:56'),
(72, 26, 'Sun Silk', 'Personal Care', 9, 90, '2025-05-02 18:26:02'),
(73, 5, 'Pride Bar', 'Laundry & Cleaning Supplies', 17, 408, '2025-05-03 17:15:26'),
(74, 2, 'Lucky Me Pancit Canton(Chilimansi)', 'Canned & Instant Foods', 5, 75, '2025-05-03 17:36:46'),
(75, 6, 'Lucky Me Noodles (Beef)', 'Canned & Instant Foods', 3, 30, '2025-05-03 17:36:46'),
(76, 2, 'Lucky Me Pancit Canton(Chilimansi)', 'Canned & Instant Foods', 4, 60, '2025-05-04 14:15:53'),
(77, 7, 'Holiday Beef Loaf', 'Canned & Instant Foods', 1, 26, '2025-05-04 14:15:53'),
(78, 10, 'Coca-Cola 1L', 'Beverages', 1, 45, '2025-05-04 14:15:53'),
(79, 5, 'Pride Bar', 'Laundry & Cleaning Supplies', 2, 48, '2025-05-04 14:23:39'),
(80, 12, 'Wings Solve Powder', 'Laundry & Cleaning Supplies', 5, 40, '2025-05-04 14:23:39');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `accounts`
--
ALTER TABLE `accounts`
  ADD PRIMARY KEY (`account_id`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`category_id`),
  ADD UNIQUE KEY `category_name` (`category_name`);

--
-- Indexes for table `inventory`
--
ALTER TABLE `inventory`
  ADD PRIMARY KEY (`product_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`notification_id`);

--
-- Indexes for table `purchases`
--
ALTER TABLE `purchases`
  ADD PRIMARY KEY (`purchase_id`);

--
-- Indexes for table `salerecords`
--
ALTER TABLE `salerecords`
  ADD PRIMARY KEY (`sales_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `accounts`
--
ALTER TABLE `accounts`
  MODIFY `account_id` int(15) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `inventory`
--
ALTER TABLE `inventory`
  MODIFY `product_id` int(100) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=49;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `notification_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=222;

--
-- AUTO_INCREMENT for table `purchases`
--
ALTER TABLE `purchases`
  MODIFY `purchase_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `salerecords`
--
ALTER TABLE `salerecords`
  MODIFY `sales_id` int(255) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=81;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
