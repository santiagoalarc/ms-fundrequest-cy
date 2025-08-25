CREATE TABLE IF NOT EXISTS fund_application (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount VARCHAR(255),
    term BIGINT,
    email VARCHAR(255),
    id_status VARCHAR(255),
    id_loan_type VARCHAR(255)
);
