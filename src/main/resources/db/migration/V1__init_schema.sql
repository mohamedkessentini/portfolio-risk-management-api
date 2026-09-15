CREATE TABLE portfolios (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    owner_name    VARCHAR(120) NOT NULL,
    base_currency VARCHAR(3) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE instruments (
    id               BIGSERIAL PRIMARY KEY,
    symbol           VARCHAR(20) NOT NULL UNIQUE,
    name             VARCHAR(150) NOT NULL,
    type             VARCHAR(20) NOT NULL,
    currency         VARCHAR(3) NOT NULL,
    current_price    NUMERIC(19, 4) NOT NULL,
    price_updated_at TIMESTAMPTZ
);

CREATE TABLE positions (
    id            BIGSERIAL PRIMARY KEY,
    portfolio_id  BIGINT NOT NULL REFERENCES portfolios (id) ON DELETE CASCADE,
    instrument_id BIGINT NOT NULL REFERENCES instruments (id),
    quantity      NUMERIC(19, 6) NOT NULL,
    average_cost  NUMERIC(19, 6) NOT NULL,
    CONSTRAINT uq_position_portfolio_instrument UNIQUE (portfolio_id, instrument_id)
);

CREATE TABLE transactions (
    id                BIGSERIAL PRIMARY KEY,
    portfolio_id      BIGINT NOT NULL REFERENCES portfolios (id) ON DELETE CASCADE,
    instrument_id     BIGINT NOT NULL REFERENCES instruments (id),
    type              VARCHAR(10) NOT NULL,
    quantity          NUMERIC(19, 6) NOT NULL,
    price             NUMERIC(19, 4) NOT NULL,
    transaction_date  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_positions_portfolio_id ON positions (portfolio_id);
CREATE INDEX idx_transactions_portfolio_id ON transactions (portfolio_id);
CREATE INDEX idx_transactions_portfolio_date ON transactions (portfolio_id, transaction_date);
CREATE INDEX idx_instruments_type ON instruments (type);
