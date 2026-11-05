-- =====================================================================
-- V1: Tao cau truc bang cho Online Shop System (SQL Server)
-- Khop voi cac @Entity trong com.swd392.onlineshop.entity
-- (Hibernate chay o che do ddl-auto=validate de kiem tra)
-- =====================================================================

-- ---------- settings (User Role, Post/Product Category, Admin Menu) ----------
CREATE TABLE settings (
    id            BIGINT IDENTITY NOT NULL,
    type          NVARCHAR(30)  NOT NULL
        CONSTRAINT ck_settings_type CHECK (type IN ('USER_ROLE','POST_CATEGORY','PRODUCT_CATEGORY','ADMIN_MENU')),
    name          NVARCHAR(100) NOT NULL,
    value         NVARCHAR(100) NOT NULL,
    display_order INT,
    description   NVARCHAR(500),
    active        BIT           NOT NULL,
    created_at    DATETIME2(7)  NOT NULL,
    updated_at    DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_settings PRIMARY KEY (id),
    CONSTRAINT uk_settings_type_value UNIQUE (type, value)
);

-- ---------- users (tai khoan dang nhap) ----------
CREATE TABLE users (
    id         BIGINT IDENTITY NOT NULL,
    full_name  NVARCHAR(100) NOT NULL,
    email      NVARCHAR(100) NOT NULL,
    password   NVARCHAR(100) NOT NULL,
    gender     NVARCHAR(10)
        CONSTRAINT ck_users_gender CHECK (gender IN ('MALE','FEMALE','OTHER')),
    mobile     NVARCHAR(15),
    address    NVARCHAR(255),
    avatar     NVARCHAR(255),
    role_id    BIGINT        NOT NULL,
    status     NVARCHAR(20)  NOT NULL
        CONSTRAINT ck_users_status CHECK (status IN ('NOT_VERIFIED','ACTIVE','INACTIVE')),
    created_at DATETIME2(7)  NOT NULL,
    updated_at DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES settings (id)
);

-- ---------- user_tokens (link verify email / reset password) ----------
CREATE TABLE user_tokens (
    id         BIGINT IDENTITY NOT NULL,
    user_id    BIGINT        NOT NULL,
    token      NVARCHAR(100) NOT NULL,
    type       NVARCHAR(20)  NOT NULL
        CONSTRAINT ck_user_tokens_type CHECK (type IN ('VERIFY_EMAIL','RESET_PASSWORD')),
    expires_at DATETIME2(7)  NOT NULL,
    used_at    DATETIME2(7),
    created_at DATETIME2(7)  NOT NULL,
    updated_at DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_user_tokens PRIMARY KEY (id),
    CONSTRAINT uk_user_tokens_token UNIQUE (token),
    CONSTRAINT fk_user_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- ---------- customers (customer / contact, dinh danh bang email) ----------
CREATE TABLE customers (
    id         BIGINT IDENTITY NOT NULL,
    full_name  NVARCHAR(100) NOT NULL,
    gender     NVARCHAR(10)
        CONSTRAINT ck_customers_gender CHECK (gender IN ('MALE','FEMALE','OTHER')),
    email      NVARCHAR(100) NOT NULL,
    mobile     NVARCHAR(15),
    address    NVARCHAR(255),
    status     NVARCHAR(20)  NOT NULL
        CONSTRAINT ck_customers_status CHECK (status IN ('CONTACT','POTENTIAL','CUSTOMER')),
    user_id    BIGINT,
    created_at DATETIME2(7)  NOT NULL,
    updated_at DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_customers PRIMARY KEY (id),
    CONSTRAINT uk_customers_email UNIQUE (email),
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users (id)
);
-- 1 User chi lien ket 1 Customer. Dung filtered index vi UNIQUE thuong
-- cua SQL Server chi cho phep 1 gia tri NULL.
CREATE UNIQUE INDEX ux_customers_user_id ON customers (user_id) WHERE user_id IS NOT NULL;

CREATE TABLE customer_histories (
    id          BIGINT IDENTITY NOT NULL,
    customer_id BIGINT        NOT NULL,
    email       NVARCHAR(100) NOT NULL,
    full_name   NVARCHAR(100) NOT NULL,
    gender      NVARCHAR(10)
        CONSTRAINT ck_customer_histories_gender CHECK (gender IN ('MALE','FEMALE','OTHER')),
    mobile      NVARCHAR(15),
    address     NVARCHAR(255),
    updated_by  BIGINT,
    created_at  DATETIME2(7)  NOT NULL,
    updated_at  DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_customer_histories PRIMARY KEY (id),
    CONSTRAINT fk_customer_histories_customer   FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_customer_histories_updated_by FOREIGN KEY (updated_by)  REFERENCES users (id)
);

-- ---------- posts (Blog) ----------
CREATE TABLE posts (
    id          BIGINT IDENTITY NOT NULL,
    title       NVARCHAR(255) NOT NULL,
    thumbnail   NVARCHAR(255),
    brief_info  NVARCHAR(500),
    content     NVARCHAR(MAX),
    category_id BIGINT,
    author_id   BIGINT,
    featured    BIT           NOT NULL,
    visible     BIT           NOT NULL,
    created_at  DATETIME2(7)  NOT NULL,
    updated_at  DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_posts PRIMARY KEY (id),
    CONSTRAINT fk_posts_category FOREIGN KEY (category_id) REFERENCES settings (id),
    CONSTRAINT fk_posts_author   FOREIGN KEY (author_id)   REFERENCES users (id)
);

-- ---------- sliders ----------
CREATE TABLE sliders (
    id            BIGINT IDENTITY NOT NULL,
    title         NVARCHAR(255) NOT NULL,
    image         NVARCHAR(255) NOT NULL,
    backlink      NVARCHAR(255),
    notes         NVARCHAR(500),
    display_order INT,
    author_id     BIGINT,
    visible       BIT           NOT NULL,
    created_at    DATETIME2(7)  NOT NULL,
    updated_at    DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_sliders PRIMARY KEY (id),
    CONSTRAINT fk_sliders_author FOREIGN KEY (author_id) REFERENCES users (id)
);

-- ---------- products ----------
CREATE TABLE products (
    id             BIGINT IDENTITY NOT NULL,
    name           NVARCHAR(255)  NOT NULL,
    thumbnail      NVARCHAR(255),
    brief_info     NVARCHAR(500),
    description    NVARCHAR(MAX),
    category_id    BIGINT,
    original_price NUMERIC(15,2)  NOT NULL,
    sale_price     NUMERIC(15,2),
    stock_quantity INT            NOT NULL,
    featured       BIT            NOT NULL,
    visible        BIT            NOT NULL,
    created_at     DATETIME2(7)   NOT NULL,
    updated_at     DATETIME2(7)   NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES settings (id),
    CONSTRAINT ck_products_stock CHECK (stock_quantity >= 0)
);

CREATE TABLE product_images (
    id            BIGINT IDENTITY NOT NULL,
    product_id    BIGINT        NOT NULL,
    image_url     NVARCHAR(255) NOT NULL,
    display_order INT,
    created_at    DATETIME2(7)  NOT NULL,
    updated_at    DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_product_images PRIMARY KEY (id),
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- ---------- carts (chi user da dang nhap; gio cua Guest nam trong HttpSession) ----------
CREATE TABLE carts (
    id         BIGINT IDENTITY NOT NULL,
    user_id    BIGINT       NOT NULL,
    created_at DATETIME2(7) NOT NULL,
    updated_at DATETIME2(7) NOT NULL,
    CONSTRAINT pk_carts PRIMARY KEY (id),
    CONSTRAINT uk_carts_user UNIQUE (user_id),
    CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE cart_items (
    id         BIGINT IDENTITY NOT NULL,
    cart_id    BIGINT       NOT NULL,
    product_id BIGINT       NOT NULL,
    quantity   INT          NOT NULL,
    created_at DATETIME2(7) NOT NULL,
    updated_at DATETIME2(7) NOT NULL,
    CONSTRAINT pk_cart_items PRIMARY KEY (id),
    CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_id),
    CONSTRAINT fk_cart_items_cart    FOREIGN KEY (cart_id)    REFERENCES carts (id),
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_cart_items_quantity CHECK (quantity > 0)
);

-- ---------- orders ----------
CREATE TABLE orders (
    id               BIGINT IDENTITY NOT NULL,
    customer_id      BIGINT        NOT NULL,
    receiver_name    NVARCHAR(100) NOT NULL,
    receiver_gender  NVARCHAR(10)
        CONSTRAINT ck_orders_receiver_gender CHECK (receiver_gender IN ('MALE','FEMALE','OTHER')),
    receiver_email   NVARCHAR(100) NOT NULL,
    receiver_mobile  NVARCHAR(15)  NOT NULL,
    receiver_address NVARCHAR(255) NOT NULL,
    note             NVARCHAR(500),
    total_amount     NUMERIC(15,2) NOT NULL,
    status           NVARCHAR(20)  NOT NULL
        CONSTRAINT ck_orders_status CHECK (status IN ('SUBMITTED','CONFIRMED','SHIPPING','SUCCESS','CANCELLED')),
    assigned_sale_id BIGINT,
    sale_note        NVARCHAR(500),
    created_at       DATETIME2(7)  NOT NULL,
    updated_at       DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_customer      FOREIGN KEY (customer_id)      REFERENCES customers (id),
    CONSTRAINT fk_orders_assigned_sale FOREIGN KEY (assigned_sale_id) REFERENCES users (id)
);

CREATE TABLE order_items (
    id           BIGINT IDENTITY NOT NULL,
    order_id     BIGINT        NOT NULL,
    product_id   BIGINT,
    product_name NVARCHAR(255) NOT NULL,
    unit_price   NUMERIC(15,2) NOT NULL,
    quantity     INT           NOT NULL,
    created_at   DATETIME2(7)  NOT NULL,
    updated_at   DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_order_items_quantity CHECK (quantity > 0)
);

-- ---------- feedbacks ----------
CREATE TABLE feedbacks (
    id          BIGINT IDENTITY NOT NULL,
    customer_id BIGINT         NOT NULL,
    product_id  BIGINT,
    full_name   NVARCHAR(100)  NOT NULL,
    gender      NVARCHAR(10)
        CONSTRAINT ck_feedbacks_gender CHECK (gender IN ('MALE','FEMALE','OTHER')),
    email       NVARCHAR(100)  NOT NULL,
    mobile      NVARCHAR(15),
    rating      INT            NOT NULL
        CONSTRAINT ck_feedbacks_rating CHECK (rating BETWEEN 1 AND 5),
    content     NVARCHAR(2000) NOT NULL,
    visible     BIT            NOT NULL,
    created_at  DATETIME2(7)   NOT NULL,
    updated_at  DATETIME2(7)   NOT NULL,
    CONSTRAINT pk_feedbacks PRIMARY KEY (id),
    CONSTRAINT fk_feedbacks_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT fk_feedbacks_product  FOREIGN KEY (product_id)  REFERENCES products (id)
);

CREATE TABLE feedback_images (
    id          BIGINT IDENTITY NOT NULL,
    feedback_id BIGINT        NOT NULL,
    image_url   NVARCHAR(255) NOT NULL,
    created_at  DATETIME2(7)  NOT NULL,
    updated_at  DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_feedback_images PRIMARY KEY (id),
    CONSTRAINT fk_feedback_images_feedback FOREIGN KEY (feedback_id) REFERENCES feedbacks (id)
);
