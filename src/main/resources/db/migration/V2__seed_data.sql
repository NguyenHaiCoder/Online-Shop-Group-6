-- =====================================================================
-- V2: Du lieu ban dau
-- Mat khau mac dinh cua tat ca tai khoan: 123456 (BCrypt)
-- =====================================================================

-- ---------- Settings: User Roles ----------
INSERT INTO settings (type, name, value, display_order, description, active, created_at, updated_at) VALUES
(N'USER_ROLE', N'Administrator', N'ADMIN',        1, N'Quản trị hệ thống',     1, SYSDATETIME(), SYSDATETIME()),
(N'USER_ROLE', N'Marketing',     N'MARKETING',    2, N'Nhân viên marketing',   1, SYSDATETIME(), SYSDATETIME()),
(N'USER_ROLE', N'Sale Manager',  N'SALE_MANAGER', 3, N'Quản lý bán hàng',      1, SYSDATETIME(), SYSDATETIME()),
(N'USER_ROLE', N'Sale',          N'SALE',         4, N'Nhân viên bán hàng',    1, SYSDATETIME(), SYSDATETIME()),
(N'USER_ROLE', N'Customer',      N'CUSTOMER',     5, N'Khách hàng đã đăng ký', 1, SYSDATETIME(), SYSDATETIME());

-- ---------- Settings: Post Categories ----------
INSERT INTO settings (type, name, value, display_order, description, active, created_at, updated_at) VALUES
(N'POST_CATEGORY', N'Tin tức',         N'NEWS',      1, NULL, 1, SYSDATETIME(), SYSDATETIME()),
(N'POST_CATEGORY', N'Khuyến mãi',      N'PROMOTION', 2, NULL, 1, SYSDATETIME(), SYSDATETIME()),
(N'POST_CATEGORY', N'Hướng dẫn',       N'GUIDE',     3, NULL, 1, SYSDATETIME(), SYSDATETIME());

-- ---------- Settings: Product Categories ----------
INSERT INTO settings (type, name, value, display_order, description, active, created_at, updated_at) VALUES
(N'PRODUCT_CATEGORY', N'Điện thoại', N'PHONE',     1, NULL, 1, SYSDATETIME(), SYSDATETIME()),
(N'PRODUCT_CATEGORY', N'Laptop',     N'LAPTOP',    2, NULL, 1, SYSDATETIME(), SYSDATETIME()),
(N'PRODUCT_CATEGORY', N'Phụ kiện',   N'ACCESSORY', 3, NULL, 1, SYSDATETIME(), SYSDATETIME());

-- ---------- Settings: Admin Menu ----------
INSERT INTO settings (type, name, value, display_order, description, active, created_at, updated_at) VALUES
(N'ADMIN_MENU', N'Dashboard', N'/admin/dashboard', 1, NULL, 1, SYSDATETIME(), SYSDATETIME()),
(N'ADMIN_MENU', N'Users',     N'/admin/users',     2, NULL, 1, SYSDATETIME(), SYSDATETIME()),
(N'ADMIN_MENU', N'Settings',  N'/admin/settings',  3, NULL, 1, SYSDATETIME(), SYSDATETIME());

-- ---------- Users (password = 123456) ----------
INSERT INTO users (full_name, email, password, gender, mobile, address, avatar, role_id, status, created_at, updated_at) VALUES
(N'Admin',          N'admin@onlineshop.com',       N'$2a$10$6HtD.HNaUfSUWPFmDXlRWeXbawKcHD8x7KptXheagV2AjfkfV5Z1e', N'MALE',   N'0900000001', N'Hà Nội', NULL,
    (SELECT id FROM settings WHERE type = N'USER_ROLE' AND value = N'ADMIN'),        N'ACTIVE', SYSDATETIME(), SYSDATETIME()),
(N'Marketing User', N'marketing@onlineshop.com',   N'$2a$10$6HtD.HNaUfSUWPFmDXlRWeXbawKcHD8x7KptXheagV2AjfkfV5Z1e', N'FEMALE', N'0900000002', N'Hà Nội', NULL,
    (SELECT id FROM settings WHERE type = N'USER_ROLE' AND value = N'MARKETING'),    N'ACTIVE', SYSDATETIME(), SYSDATETIME()),
(N'Sale Manager',   N'salemanager@onlineshop.com', N'$2a$10$6HtD.HNaUfSUWPFmDXlRWeXbawKcHD8x7KptXheagV2AjfkfV5Z1e', N'MALE',   N'0900000003', N'Hà Nội', NULL,
    (SELECT id FROM settings WHERE type = N'USER_ROLE' AND value = N'SALE_MANAGER'), N'ACTIVE', SYSDATETIME(), SYSDATETIME()),
(N'Sale User',      N'sale@onlineshop.com',        N'$2a$10$6HtD.HNaUfSUWPFmDXlRWeXbawKcHD8x7KptXheagV2AjfkfV5Z1e', N'FEMALE', N'0900000004', N'Hà Nội', NULL,
    (SELECT id FROM settings WHERE type = N'USER_ROLE' AND value = N'SALE'),         N'ACTIVE', SYSDATETIME(), SYSDATETIME()),
(N'Sale User 2',    N'sale2@onlineshop.com',       N'$2a$10$6HtD.HNaUfSUWPFmDXlRWeXbawKcHD8x7KptXheagV2AjfkfV5Z1e', N'MALE',   N'0900000006', N'Hà Nội', NULL,
    (SELECT id FROM settings WHERE type = N'USER_ROLE' AND value = N'SALE'),         N'ACTIVE', SYSDATETIME(), SYSDATETIME()),
(N'Nguyễn Văn A',   N'customer@onlineshop.com',    N'$2a$10$6HtD.HNaUfSUWPFmDXlRWeXbawKcHD8x7KptXheagV2AjfkfV5Z1e', N'MALE',   N'0900000005', N'Đà Nẵng', NULL,
    (SELECT id FROM settings WHERE type = N'USER_ROLE' AND value = N'CUSTOMER'),     N'ACTIVE', DATEADD(DAY, -10, SYSDATETIME()), DATEADD(DAY, -10, SYSDATETIME()));

-- ---------- Customers / contacts ----------
-- A: co tai khoan + co don SUCCESS -> CUSTOMER
-- B: chi gui feedback              -> CONTACT
-- C: Guest dat don chua SUCCESS    -> POTENTIAL
INSERT INTO customers (full_name, gender, email, mobile, address, status, user_id, created_at, updated_at) VALUES
(N'Nguyễn Văn A', N'MALE',   N'customer@onlineshop.com', N'0900000005', N'Đà Nẵng',     N'CUSTOMER',
    (SELECT id FROM users WHERE email = N'customer@onlineshop.com'), DATEADD(DAY, -10, SYSDATETIME()), DATEADD(DAY, -5, SYSDATETIME())),
(N'Trần Thị B',   N'FEMALE', N'tranthib@gmail.com',      N'0911111111', NULL,           N'CONTACT',   NULL,
    DATEADD(DAY, -4, SYSDATETIME()), DATEADD(DAY, -4, SYSDATETIME())),
(N'Lê Văn C',     N'MALE',   N'levanc@gmail.com',        N'0922222222', N'Hồ Chí Minh', N'POTENTIAL', NULL,
    SYSDATETIME(), SYSDATETIME());

-- ---------- Customer histories (moi dong = thong tin sau khi thay doi) ----------
INSERT INTO customer_histories (customer_id, email, full_name, gender, mobile, address, updated_by, created_at, updated_at) VALUES
((SELECT id FROM customers WHERE email = N'customer@onlineshop.com'),
    N'customer@onlineshop.com', N'Nguyễn Văn A', N'MALE', N'0900000005', N'Huế', NULL,
    DATEADD(DAY, -10, SYSDATETIME()), DATEADD(DAY, -10, SYSDATETIME())),
((SELECT id FROM customers WHERE email = N'customer@onlineshop.com'),
    N'customer@onlineshop.com', N'Nguyễn Văn A', N'MALE', N'0900000005', N'Đà Nẵng',
    (SELECT id FROM users WHERE email = N'marketing@onlineshop.com'),
    DATEADD(DAY, -5, SYSDATETIME()), DATEADD(DAY, -5, SYSDATETIME())),
((SELECT id FROM customers WHERE email = N'tranthib@gmail.com'),
    N'tranthib@gmail.com', N'Trần Thị B', N'FEMALE', N'0911111111', NULL, NULL,
    DATEADD(DAY, -4, SYSDATETIME()), DATEADD(DAY, -4, SYSDATETIME())),
((SELECT id FROM customers WHERE email = N'levanc@gmail.com'),
    N'levanc@gmail.com', N'Lê Văn C', N'MALE', N'0922222222', N'Hồ Chí Minh', NULL,
    SYSDATETIME(), SYSDATETIME());

-- ---------- Products ----------
INSERT INTO products (name, thumbnail, brief_info, description, category_id, original_price, sale_price, stock_quantity, featured, visible, created_at, updated_at) VALUES
(N'iPhone 15 128GB',        N'/images/products/iphone-15.jpg',     N'Chip A16, camera 48MP',          N'iPhone 15 với Dynamic Island, camera chính 48MP, cổng USB-C.',
    (SELECT id FROM settings WHERE type = N'PRODUCT_CATEGORY' AND value = N'PHONE'),     22990000, 19990000, 50,  1, 1, SYSDATETIME(), SYSDATETIME()),
(N'Samsung Galaxy S24',     N'/images/products/galaxy-s24.jpg',    N'Galaxy AI, màn hình 6.2 inch',   N'Samsung Galaxy S24 tích hợp Galaxy AI, Snapdragon 8 Gen 3.',
    (SELECT id FROM settings WHERE type = N'PRODUCT_CATEGORY' AND value = N'PHONE'),     20990000, NULL,     40,  0, 1, SYSDATETIME(), SYSDATETIME()),
(N'MacBook Air M3 13 inch', N'/images/products/macbook-air-m3.jpg', N'Chip M3, RAM 8GB, SSD 256GB',    N'MacBook Air M3 mỏng nhẹ, pin đến 18 giờ.',
    (SELECT id FROM settings WHERE type = N'PRODUCT_CATEGORY' AND value = N'LAPTOP'),    27990000, 25990000, 20,  1, 1, SYSDATETIME(), SYSDATETIME()),
(N'Dell XPS 13',            N'/images/products/dell-xps-13.jpg',   N'Intel Core Ultra 7, RAM 16GB',   N'Dell XPS 13 thiết kế cao cấp, màn hình InfinityEdge.',
    (SELECT id FROM settings WHERE type = N'PRODUCT_CATEGORY' AND value = N'LAPTOP'),    32990000, NULL,     15,  0, 1, SYSDATETIME(), SYSDATETIME()),
(N'AirPods Pro 2',          N'/images/products/airpods-pro-2.jpg', N'Chống ồn chủ động, USB-C',       N'AirPods Pro thế hệ 2 với chip H2 và chống ồn chủ động.',
    (SELECT id FROM settings WHERE type = N'PRODUCT_CATEGORY' AND value = N'ACCESSORY'),  6190000,  5490000, 100, 1, 1, SYSDATETIME(), SYSDATETIME()),
(N'Sạc nhanh Anker 65W',    N'/images/products/anker-65w.jpg',     N'GaN, 3 cổng sạc',                N'Củ sạc Anker 65W công nghệ GaN nhỏ gọn.',
    (SELECT id FROM settings WHERE type = N'PRODUCT_CATEGORY' AND value = N'ACCESSORY'),   990000, NULL,     200, 0, 1, SYSDATETIME(), SYSDATETIME());

-- ---------- Product images (attached images) ----------
INSERT INTO product_images (product_id, image_url, display_order, created_at, updated_at) VALUES
((SELECT id FROM products WHERE name = N'iPhone 15 128GB'),        N'/images/products/iphone-15-1.jpg',      1, SYSDATETIME(), SYSDATETIME()),
((SELECT id FROM products WHERE name = N'iPhone 15 128GB'),        N'/images/products/iphone-15-2.jpg',      2, SYSDATETIME(), SYSDATETIME()),
((SELECT id FROM products WHERE name = N'Samsung Galaxy S24'),     N'/images/products/galaxy-s24-1.jpg',     1, SYSDATETIME(), SYSDATETIME()),
((SELECT id FROM products WHERE name = N'MacBook Air M3 13 inch'), N'/images/products/macbook-air-m3-1.jpg', 1, SYSDATETIME(), SYSDATETIME()),
((SELECT id FROM products WHERE name = N'MacBook Air M3 13 inch'), N'/images/products/macbook-air-m3-2.jpg', 2, SYSDATETIME(), SYSDATETIME()),
((SELECT id FROM products WHERE name = N'AirPods Pro 2'),          N'/images/products/airpods-pro-2-1.jpg',  1, SYSDATETIME(), SYSDATETIME());

-- ---------- Posts ----------
INSERT INTO posts (title, thumbnail, brief_info, content, category_id, author_id, featured, visible, created_at, updated_at) VALUES
(N'Khai trương Online Shop', N'/images/posts/grand-opening.jpg', N'Chào mừng bạn đến với Online Shop!',
    N'Online Shop chính thức khai trương với hàng ngàn sản phẩm công nghệ chính hãng.',
    (SELECT id FROM settings WHERE type = N'POST_CATEGORY' AND value = N'NEWS'),
    (SELECT id FROM users WHERE email = N'marketing@onlineshop.com'), 1, 1, SYSDATETIME(), SYSDATETIME()),
(N'Giảm giá đến 20% cho iPhone 15', N'/images/posts/iphone-sale.jpg', N'Ưu đãi có hạn trong tháng này.',
    N'Nhân dịp khai trương, iPhone 15 được giảm giá đến 20%. Số lượng có hạn!',
    (SELECT id FROM settings WHERE type = N'POST_CATEGORY' AND value = N'PROMOTION'),
    (SELECT id FROM users WHERE email = N'marketing@onlineshop.com'), 1, 1, SYSDATETIME(), SYSDATETIME()),
(N'Hướng dẫn đặt hàng online', N'/images/posts/how-to-order.jpg', N'Các bước đặt hàng đơn giản.',
    N'Bước 1: Chọn sản phẩm. Bước 2: Thêm vào giỏ hàng. Bước 3: Nhập thông tin người nhận. Bước 4: Xác nhận đơn hàng.',
    (SELECT id FROM settings WHERE type = N'POST_CATEGORY' AND value = N'GUIDE'),
    (SELECT id FROM users WHERE email = N'marketing@onlineshop.com'), 0, 1, SYSDATETIME(), SYSDATETIME());

-- ---------- Sliders ----------
INSERT INTO sliders (title, image, backlink, notes, display_order, author_id, visible, created_at, updated_at) VALUES
(N'Khai trương - Ưu đãi lớn', N'/images/sliders/slider-1.jpg', N'/blogs/1',    NULL, 1,
    (SELECT id FROM users WHERE email = N'marketing@onlineshop.com'), 1, SYSDATETIME(), SYSDATETIME()),
(N'iPhone 15 giảm 20%',       N'/images/sliders/slider-2.jpg', N'/products/1', NULL, 2,
    (SELECT id FROM users WHERE email = N'marketing@onlineshop.com'), 1, SYSDATETIME(), SYSDATETIME()),
(N'MacBook Air M3',           N'/images/sliders/slider-3.jpg', N'/products/3', NULL, 3,
    (SELECT id FROM users WHERE email = N'marketing@onlineshop.com'), 1, SYSDATETIME(), SYSDATETIME());

-- ---------- Orders ----------
-- Don 1: Customer A, SUCCESS, Sale User (5 ngay truoc)
INSERT INTO orders (customer_id, receiver_name, receiver_gender, receiver_email, receiver_mobile, receiver_address, note,
                    total_amount, status, assigned_sale_id, sale_note, created_at, updated_at) VALUES
((SELECT id FROM customers WHERE email = N'customer@onlineshop.com'),
    N'Nguyễn Văn A', N'MALE', N'customer@onlineshop.com', N'0900000005', N'Đà Nẵng', N'Giao giờ hành chính',
    25480000, N'SUCCESS',
    (SELECT id FROM users WHERE email = N'sale@onlineshop.com'), N'Đã giao, khách hài lòng',
    DATEADD(DAY, -5, SYSDATETIME()), DATEADD(DAY, -3, SYSDATETIME()));

INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, created_at, updated_at) VALUES
((SELECT MAX(id) FROM orders), (SELECT id FROM products WHERE name = N'iPhone 15 128GB'), N'iPhone 15 128GB', 19990000, 1, DATEADD(DAY, -5, SYSDATETIME()), DATEADD(DAY, -5, SYSDATETIME())),
((SELECT MAX(id) FROM orders), (SELECT id FROM products WHERE name = N'AirPods Pro 2'),   N'AirPods Pro 2',    5490000, 1, DATEADD(DAY, -5, SYSDATETIME()), DATEADD(DAY, -5, SYSDATETIME()));

-- Don 2: Customer A, SUBMITTED, Sale User 2 (1 ngay truoc) - giao xoay vong
INSERT INTO orders (customer_id, receiver_name, receiver_gender, receiver_email, receiver_mobile, receiver_address, note,
                    total_amount, status, assigned_sale_id, sale_note, created_at, updated_at) VALUES
((SELECT id FROM customers WHERE email = N'customer@onlineshop.com'),
    N'Nguyễn Văn A', N'MALE', N'customer@onlineshop.com', N'0900000005', N'Đà Nẵng', NULL,
    25990000, N'SUBMITTED',
    (SELECT id FROM users WHERE email = N'sale2@onlineshop.com'), NULL,
    DATEADD(DAY, -1, SYSDATETIME()), DATEADD(DAY, -1, SYSDATETIME()));

INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, created_at, updated_at) VALUES
((SELECT MAX(id) FROM orders), (SELECT id FROM products WHERE name = N'MacBook Air M3 13 inch'), N'MacBook Air M3 13 inch', 25990000, 1, DATEADD(DAY, -1, SYSDATETIME()), DATEADD(DAY, -1, SYSDATETIME()));

-- Don 3: Guest C, SUBMITTED, Sale User (hom nay)
INSERT INTO orders (customer_id, receiver_name, receiver_gender, receiver_email, receiver_mobile, receiver_address, note,
                    total_amount, status, assigned_sale_id, sale_note, created_at, updated_at) VALUES
((SELECT id FROM customers WHERE email = N'levanc@gmail.com'),
    N'Lê Văn C', N'MALE', N'levanc@gmail.com', N'0922222222', N'Hồ Chí Minh', N'Gọi trước khi giao',
    1980000, N'SUBMITTED',
    (SELECT id FROM users WHERE email = N'sale@onlineshop.com'), NULL,
    SYSDATETIME(), SYSDATETIME());

INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, created_at, updated_at) VALUES
((SELECT MAX(id) FROM orders), (SELECT id FROM products WHERE name = N'Sạc nhanh Anker 65W'), N'Sạc nhanh Anker 65W', 990000, 2, SYSDATETIME(), SYSDATETIME());

-- ---------- Cart cua Customer A (giu qua cac phien) ----------
INSERT INTO carts (user_id, created_at, updated_at) VALUES
((SELECT id FROM users WHERE email = N'customer@onlineshop.com'), SYSDATETIME(), SYSDATETIME());

INSERT INTO cart_items (cart_id, product_id, quantity, created_at, updated_at) VALUES
((SELECT id FROM carts WHERE user_id = (SELECT id FROM users WHERE email = N'customer@onlineshop.com')),
    (SELECT id FROM products WHERE name = N'Samsung Galaxy S24'), 1, SYSDATETIME(), SYSDATETIME());

-- ---------- Feedbacks ----------
INSERT INTO feedbacks (customer_id, product_id, full_name, gender, email, mobile, rating, content, visible, created_at, updated_at) VALUES
((SELECT id FROM customers WHERE email = N'customer@onlineshop.com'), (SELECT id FROM products WHERE name = N'iPhone 15 128GB'),
    N'Nguyễn Văn A', N'MALE', N'customer@onlineshop.com', N'0900000005', 5, N'Sản phẩm chính hãng, giao hàng nhanh.', 1,
    DATEADD(DAY, -2, SYSDATETIME()), DATEADD(DAY, -2, SYSDATETIME())),
((SELECT id FROM customers WHERE email = N'tranthib@gmail.com'), NULL,
    N'Trần Thị B', N'FEMALE', N'tranthib@gmail.com', N'0911111111', 4, N'Website dễ sử dụng, nhân viên tư vấn nhiệt tình.', 1,
    DATEADD(DAY, -4, SYSDATETIME()), DATEADD(DAY, -4, SYSDATETIME()));

INSERT INTO feedback_images (feedback_id, image_url, created_at, updated_at) VALUES
((SELECT id FROM feedbacks WHERE email = N'customer@onlineshop.com'), N'/images/feedbacks/feedback-1.jpg', SYSDATETIME(), SYSDATETIME());
