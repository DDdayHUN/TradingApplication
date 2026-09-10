ALTER TABLE app_order
    ALTER COLUMN ibkr_order_id DROP NOT NULL;

ALTER TABLE app_order
    ADD COLUMN security_isin VARCHAR(20),
    ADD COLUMN security_ticker VARCHAR(20),
    ADD COLUMN security_currency VARCHAR(10);

UPDATE app_order o
SET
    security_isin = t.security_isin,
    security_ticker = t.security_ticker,
    security_currency = t.security_currency
    FROM app_trader t
WHERE o.trader_id = t.id;

ALTER TABLE app_order
    ALTER COLUMN security_isin SET NOT NULL,
    ALTER COLUMN security_ticker SET NOT NULL,
    ALTER COLUMN security_currency SET NOT NULL;

ALTER TABLE app_order
    ADD COLUMN sell_allocations JSONB;

ALTER INDEX idx_oder_ibkr_order_id
    RENAME TO idx_app_order_ibkr_order_id;