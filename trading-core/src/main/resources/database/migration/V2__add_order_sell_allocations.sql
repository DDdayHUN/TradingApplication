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