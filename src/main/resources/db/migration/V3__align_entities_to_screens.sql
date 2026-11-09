-- Cot con thieu so voi man hinh da ve.
-- Khong dung GO: JDBC gui ca file mot batch, nen cau lenh cham cot moi phai nam trong EXEC.

ALTER TABLE products ADD code NVARCHAR(50) NULL;
ALTER TABLE products ADD specifications NVARCHAR(MAX) NULL;
EXEC('UPDATE products SET code = N''SP'' + RIGHT(N''000000'' + CAST(id AS NVARCHAR(20)), 6) WHERE code IS NULL');
EXEC('ALTER TABLE products ALTER COLUMN code NVARCHAR(50) NOT NULL');
EXEC('CREATE UNIQUE INDEX uk_products_code ON products (code)');

ALTER TABLE customers ADD note NVARCHAR(500) NULL;
ALTER TABLE customers ADD visible BIT NOT NULL CONSTRAINT df_customers_visible DEFAULT 1;

ALTER TABLE orders ADD code NVARCHAR(20) NULL;
ALTER TABLE orders ADD shipping_fee NUMERIC(15,2) NOT NULL CONSTRAINT df_orders_shipping_fee DEFAULT 0;
ALTER TABLE orders ADD payment_method NVARCHAR(30) NOT NULL CONSTRAINT df_orders_payment_method DEFAULT N'COD';
EXEC('UPDATE orders SET code = N''OD'' + RIGHT(N''000000'' + CAST(id AS NVARCHAR(20)), 6) WHERE code IS NULL');
EXEC('ALTER TABLE orders ALTER COLUMN code NVARCHAR(20) NOT NULL');
EXEC('CREATE UNIQUE INDEX uk_orders_code ON orders (code)');

ALTER TABLE order_items ADD product_code NVARCHAR(50) NULL;
EXEC('UPDATE oi SET product_code = p.code FROM order_items oi INNER JOIN products p ON p.id = oi.product_id WHERE oi.product_code IS NULL');

CREATE TABLE order_histories (
    id         BIGINT IDENTITY NOT NULL,
    order_id   BIGINT        NOT NULL,
    content    NVARCHAR(500) NOT NULL,
    actor_id   BIGINT,
    created_at DATETIME2(7)  NOT NULL,
    updated_at DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_order_histories PRIMARY KEY (id),
    CONSTRAINT fk_order_histories_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_histories_actor FOREIGN KEY (actor_id) REFERENCES users (id)
);

EXEC('INSERT INTO order_histories (order_id, content, actor_id, created_at, updated_at)
      SELECT id, N''Don duoc tao'', assigned_sale_id, created_at, created_at FROM orders');

-- Email Service nam ngoai pham vi, khong con entity UserToken.
IF OBJECT_ID(N'user_tokens', N'U') IS NOT NULL DROP TABLE user_tokens;
