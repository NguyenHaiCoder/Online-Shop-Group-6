-- Ma va field ma man hinh dang hien nhung schema chua co.

ALTER TABLE users ADD code NVARCHAR(20) NULL;
ALTER TABLE users ADD visible BIT NOT NULL CONSTRAINT df_users_visible DEFAULT 1;
ALTER TABLE users ADD email_verified BIT NOT NULL CONSTRAINT df_users_email_verified DEFAULT 0;
EXEC('UPDATE users SET code = N''ND'' + RIGHT(N''000000'' + CAST(id AS NVARCHAR(20)), 6) WHERE code IS NULL');
EXEC('UPDATE users SET email_verified = 1 WHERE status = N''ACTIVE''');
EXEC('ALTER TABLE users ALTER COLUMN code NVARCHAR(20) NOT NULL');
EXEC('CREATE UNIQUE INDEX uk_users_code ON users (code)');

ALTER TABLE customers ADD code NVARCHAR(20) NULL;
EXEC('UPDATE customers SET code = N''KH'' + RIGHT(N''000000'' + CAST(id AS NVARCHAR(20)), 6) WHERE code IS NULL');
EXEC('ALTER TABLE customers ALTER COLUMN code NVARCHAR(20) NOT NULL');
EXEC('CREATE UNIQUE INDEX uk_customers_code ON customers (code)');

ALTER TABLE posts ADD code NVARCHAR(20) NULL;
EXEC('UPDATE posts SET code = N''BV'' + RIGHT(N''000000'' + CAST(id AS NVARCHAR(20)), 6) WHERE code IS NULL');
EXEC('ALTER TABLE posts ALTER COLUMN code NVARCHAR(20) NOT NULL');
EXEC('CREATE UNIQUE INDEX uk_posts_code ON posts (code)');

ALTER TABLE sliders ADD button_label NVARCHAR(100) NOT NULL CONSTRAINT df_sliders_button_label DEFAULT N'Xem chi tiết';

ALTER TABLE settings ADD code NVARCHAR(100) NULL;
ALTER TABLE settings ADD group_name NVARCHAR(100) NULL;
EXEC('UPDATE settings SET code = value WHERE code IS NULL');
EXEC('UPDATE settings SET group_name = type WHERE group_name IS NULL');
EXEC('ALTER TABLE settings ALTER COLUMN value NVARCHAR(1000) NOT NULL');
EXEC('ALTER TABLE settings ALTER COLUMN code NVARCHAR(100) NOT NULL');
EXEC('CREATE UNIQUE INDEX uk_settings_type_code ON settings (type, code)');

ALTER TABLE feedbacks ADD code NVARCHAR(20) NULL;
ALTER TABLE feedbacks ADD subject NVARCHAR(255) NULL;
ALTER TABLE feedbacks ADD source NVARCHAR(50) NOT NULL CONSTRAINT df_feedbacks_source DEFAULT N'Website';
ALTER TABLE feedbacks ADD updated_by BIGINT NULL;
ALTER TABLE feedbacks ALTER COLUMN rating INT NULL;
EXEC('UPDATE feedbacks SET code = N''PH'' + RIGHT(N''000000'' + CAST(id AS NVARCHAR(20)), 6) WHERE code IS NULL');
EXEC('UPDATE feedbacks SET subject = LEFT(content, 255) WHERE subject IS NULL');
EXEC('ALTER TABLE feedbacks ALTER COLUMN code NVARCHAR(20) NOT NULL');
EXEC('ALTER TABLE feedbacks ALTER COLUMN subject NVARCHAR(255) NOT NULL');
EXEC('CREATE UNIQUE INDEX uk_feedbacks_code ON feedbacks (code)');
ALTER TABLE feedbacks ADD CONSTRAINT fk_feedbacks_updated_by FOREIGN KEY (updated_by) REFERENCES users (id);

IF COL_LENGTH('products', 'specifications') IS NOT NULL
    ALTER TABLE products DROP COLUMN specifications;

CREATE TABLE product_specifications (
    id            BIGINT IDENTITY NOT NULL,
    product_id    BIGINT        NOT NULL,
    name          NVARCHAR(100) NOT NULL,
    value         NVARCHAR(255) NOT NULL,
    display_order INT,
    created_at    DATETIME2(7)  NOT NULL,
    updated_at    DATETIME2(7)  NOT NULL,
    CONSTRAINT pk_product_specifications PRIMARY KEY (id),
    CONSTRAINT fk_product_specifications_product FOREIGN KEY (product_id) REFERENCES products (id)
);
