-- =====================================================
-- PORTFOLIO
-- =====================================================

CREATE TABLE app_portfolio (
   id UUID PRIMARY KEY
);

-- =====================================================
-- USER
-- =====================================================

CREATE TABLE app_user (
  id UUID PRIMARY KEY,
  user_name VARCHAR(20) NOT NULL,
  portfolio_id UUID NOT NULL UNIQUE,

  CONSTRAINT fk_app_user_portfolio
      FOREIGN KEY (portfolio_id)
          REFERENCES app_portfolio (id)
          ON DELETE CASCADE
);

-- =====================================================
-- TRADER
-- =====================================================

CREATE TABLE app_trader (
    id UUID PRIMARY KEY,

    portfolio_fk UUID NOT NULL,

    security_isin VARCHAR(12) NOT NULL,
    security_ticker VARCHAR(20) NOT NULL,
    security_currency VARCHAR(3) NOT NULL,

    capital DOUBLE PRECISION NOT NULL,

    algorithm_type VARCHAR(20) NOT NULL,
    algorithm_state JSONB NOT NULL,

CONSTRAINT fk_app_trader_portfolio
    FOREIGN KEY (portfolio_fk)
        REFERENCES app_portfolio (id)
        ON DELETE CASCADE,

CONSTRAINT chk_app_trader_capital
    CHECK (capital >= 0)
);

CREATE INDEX idx_app_trader_portfolio_id
    ON app_trader (portfolio_fk);

-- =====================================================
-- SECURITY HOLDING
-- =====================================================

CREATE TABLE app_security_holding (
  id UUID PRIMARY KEY,

  time_stamp TIMESTAMP WITH TIME ZONE NOT NULL,

  trader_fk UUID NOT NULL,

  entry_price DOUBLE PRECISION NOT NULL,
  amount INTEGER NOT NULL,

  CONSTRAINT fk_app_security_holding_trader
      FOREIGN KEY (trader_fk)
          REFERENCES app_trader (id)
          ON DELETE CASCADE,

  CONSTRAINT chk_app_security_holding_entry_price
      CHECK (entry_price > 0),

  CONSTRAINT chk_app_security_holding_amount
      CHECK (amount > 0)
);

CREATE INDEX idx_app_security_holding_trader_fk
    ON app_security_holding (trader_fk);

-- =====================================================
-- TRADING ORDER (DOMAIN)
-- =====================================================

CREATE TABLE app_order (
   id UUID PRIMARY KEY,

   trader_fk UUID NOT NULL,

   security_isin VARCHAR(12) NOT NULL,
   security_ticker VARCHAR(20) NOT NULL,
   security_currency VARCHAR(3) NOT NULL,

   action VARCHAR(10) NOT NULL,

   buy_amount INTEGER,

   signal_price DOUBLE PRECISION NOT NULL,

   created_at TIMESTAMP WITH TIME ZONE NOT NULL,

   CONSTRAINT fk_app_order_trader
       FOREIGN KEY (trader_fk)
           REFERENCES app_trader (id)
           ON DELETE CASCADE,

   CONSTRAINT chk_app_order_action
       CHECK (action IN ('BUY', 'SELL')),

    CONSTRAINT chk_app_order_buy_amount
        CHECK (
            (action = 'BUY' AND buy_amount IS NOT NULL AND buy_amount > 0)
            OR
            (action = 'SELL' AND buy_amount IS NULL)
        )
);

CREATE INDEX idx_app_order_trader_fk
    ON app_order (trader_fk);

-- =====================================================
-- ORDER SELL ALLOCATION
-- =====================================================

CREATE TABLE app_order_sell_holdings (
 id UUID PRIMARY KEY,

 order_fk UUID NOT NULL,
 holding_fk UUID NOT NULL,

 amount INTEGER NOT NULL,

 CONSTRAINT fk_order_sell_allocation_order
     FOREIGN KEY (order_fk)
         REFERENCES app_order (id)
         ON DELETE CASCADE,

 CONSTRAINT fk_order_sell_allocation_holding
     FOREIGN KEY (holding_fk)
         REFERENCES app_security_holding (id),

 CONSTRAINT chk_order_sell_allocation_amount
     CHECK (amount > 0)
);

CREATE INDEX idx_order_sell_allocation_order_fk
    ON app_order_sell_holdings (order_fk);

CREATE INDEX idx_order_sell_allocation_holding_fk
    ON app_order_sell_holdings (holding_fk);

-- =====================================================
-- INTERACTIVE BROKERS USER
-- =====================================================

CREATE TABLE app_infrastructure_ibkr_user (
  id UUID PRIMARY KEY,

  client_id INTEGER NOT NULL,

  CONSTRAINT fk_ibkr_user
      FOREIGN KEY (id)
          REFERENCES app_user (id)
          ON DELETE CASCADE
);

-- =====================================================
-- INTERACTIVE BROKERS ORDER
-- =====================================================

CREATE TABLE app_infrastructure_ibkr_order (
   id UUID PRIMARY KEY,

   ibkr_order_id INTEGER NOT NULL,

   status VARCHAR(30) NOT NULL,

   filled_quantity VARCHAR(30) NOT NULL DEFAULT '',

   average_fill_price DOUBLE PRECISION,

   CONSTRAINT fk_ibkr_order_trading_order
       FOREIGN KEY (id)
           REFERENCES app_order (id)
           ON DELETE CASCADE
);

CREATE INDEX idx_ibkr_order_ibkr_order_id
    ON app_infrastructure_ibkr_order (ibkr_order_id);