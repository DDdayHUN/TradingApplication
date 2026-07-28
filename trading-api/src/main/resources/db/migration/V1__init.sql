CREATE TABLE app_user(
    id UUID PRIMARY KEY,
    keycloak_sub VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE app_trader(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    security_isin VARCHAR(12) NOT NULL,
    security_ticker VARCHAR(20) NOT NULL,
    security_currency VARCHAR(3) NOT NULL,
    capital DOUBLE PRECISION NOT NULL,
    algorithm_type VARCHAR(20) NOT NULL,
    algorithm_state JSONB NOT NULL,

    CONSTRAINT fk_app_traders_user
            FOREIGN KEY (user_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_app_traders_capital
            CHECK (capital >= 0)
);

CREATE INDEX idx_app_traders_user_id ON app_trader (user_id);

CREATE TABLE app_security_holding(
    id UUID PRIMARY KEY,
    trader_id UUID NOT NULL,
    entry_price DOUBLE PRECISION NOT NULL,
    amount INTEGER NOT NULL,

    CONSTRAINT fk_holdings_trader
            FOREIGN KEY  (trader_id)
            REFERENCES app_trader (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_holding_entry_price
        CHECK (entry_price > 0),

    CONSTRAINT chk_holding_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_holdings_trader_id ON app_security_holding (trader_id);


INSERT INTO app_user (id, keycloak_sub)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'test-keycloak-sub'
);