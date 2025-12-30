-- Initialize Products Database Schema

-- Create CATEGORIES table
CREATE TABLE CATEGORIES (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES CATEGORIES(id) ON DELETE SET NULL
);

-- Create PRODUCTS table
CREATE TABLE PRODUCTS (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES CATEGORIES(id) ON DELETE RESTRICT
);

-- Create PRODUCT_IMAGES table
CREATE TABLE PRODUCT_IMAGES (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(id) ON DELETE CASCADE
);

-- Create PRODUCT_ATTRIBUTES table
CREATE TABLE PRODUCT_ATTRIBUTES (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    attr_key VARCHAR(100) NOT NULL,
    attr_value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_attributes_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_categories_parent_id ON CATEGORIES(parent_id);
CREATE INDEX idx_products_category_id ON PRODUCTS(category_id);
CREATE INDEX idx_product_images_product_id ON PRODUCT_IMAGES(product_id);
CREATE INDEX idx_product_attributes_product_id ON PRODUCT_ATTRIBUTES(product_id);
CREATE INDEX idx_product_attributes_key ON PRODUCT_ATTRIBUTES(attr_key);

