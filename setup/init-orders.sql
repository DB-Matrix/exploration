-- Initialize Orders Database Schema

-- Create ORDERS table
CREATE TABLE ORDERS (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create ORDER_ITEMS table
CREATE TABLE ORDER_ITEMS (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES ORDERS(id) ON DELETE CASCADE
);

-- Create ORDER_STATUS_HISTORY table
CREATE TABLE ORDER_STATUS_HISTORY (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES ORDERS(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_orders_user_id ON ORDERS(user_id);
CREATE INDEX idx_orders_status ON ORDERS(order_status);
CREATE INDEX idx_order_items_order_id ON ORDER_ITEMS(order_id);
CREATE INDEX idx_order_items_product_id ON ORDER_ITEMS(product_id);
CREATE INDEX idx_order_status_history_order_id ON ORDER_STATUS_HISTORY(order_id);

