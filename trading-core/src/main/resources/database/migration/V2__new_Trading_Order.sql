ALTER TABLE app_order
    ALTER COLUMN ibkr_order_id DROP NOT NULL;

ALTER INDEX idx_oder_ibkr_order_id
    RENAME TO idx_app_order_ibkr_order_id;