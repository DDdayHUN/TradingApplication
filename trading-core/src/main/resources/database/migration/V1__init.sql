CREATE TABLE app_user(
    id UUID PRIMARY KEY,
    user_name VARCHAR(20) NOT NULL
);

CREATE TABLE app_portfolio(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,

    CONSTRAINT fk_app_portfolio_user
            FOREIGN KEY (user_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE
);

CREATE TABLE app_trader(
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL,
    security_isin VARCHAR(12) NOT NULL,
    security_ticker VARCHAR(20) NOT NULL,
    security_currency VARCHAR(3) NOT NULL,
    capital DOUBLE PRECISION NOT NULL,
    algorithm_type VARCHAR(20) NOT NULL,
    algorithm_state JSONB NOT NULL,

    CONSTRAINT fk_app_traders_portfolio
            FOREIGN KEY (portfolio_id)
            REFERENCES app_portfolio (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_app_traders_capital
            CHECK (capital >= 0)
);

CREATE INDEX idx_app_traders_portfolio_id ON app_trader (portfolio_id);

CREATE TABLE app_order(
    id UUID PRIMARY KEY,
    ibkr_order_id INTEGER NOT NULL,
    trader_id UUID NOT NULL,
    action VARCHAR(10) NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    status VARCHAR(30) NOT NULL,
    filled_quantity VARCHAR(30) NOT NULL,
    signal_price DOUBLE PRECISION NOT NULL,
    average_fill_price DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT  fk_app_order_trader
            FOREIGN KEY (trader_id)
            REFERENCES app_trader (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_oder_ibkr_order_id ON app_order (ibkr_order_id);
CREATE INDEX idx_app_order_trader_id ON app_order (trader_id);

CREATE TABLE app_security_holding(
    id UUID PRIMARY KEY,
    time_stamp TIMESTAMP WITH TIME ZONE NOT NULL,
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

CREATE TABLE app_order_sell_allocation (
       id UUID PRIMARY KEY,
       order_id UUID NOT NULL,
       holding_id UUID NOT NULL,
       amount INTEGER NOT NULL,

       CONSTRAINT fk_order_sell_allocation_order
           FOREIGN KEY (order_id)
               REFERENCES app_order (id)
               ON DELETE CASCADE
);

CREATE INDEX idx_order_sell_allocation_order_id
    ON app_order_sell_allocation (order_id);

CREATE INDEX idx_order_sell_allocation_holding_id
    ON app_order_sell_allocation (holding_id);

