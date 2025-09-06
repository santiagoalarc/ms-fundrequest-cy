CREATE TABLE IF NOT EXISTS fund_application (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount VARCHAR(255),
    term BIGINT,
    email VARCHAR(255),
    id_status VARCHAR(255),
    id_loan_type VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS fund_status (
     id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
     name VARCHAR(255),
     description VARCHAR(255),
     creation_date BIGINT
);

INSERT INTO fund_status (id, name, description, creation_date) VALUES
('7f32a490-9eda-4393-89c2-cd3b9c49ecac', 'PENDING', 'Pending Review', EXTRACT(EPOCH FROM NOW()) * 1000),
('5f59ed26-bf58-44e5-ad5f-15ad22e44498', 'APPROVED', 'Approved', EXTRACT(EPOCH FROM NOW()) * 1000),
('2e63f281-0c7e-4fd2-9e52-2ebdd6e3b5ef', 'REJECTED','Rejected', EXTRACT(EPOCH FROM NOW()) * 1000),
('8274b096-ae8e-4f45-8b67-7b215f75ac6b', 'IN_PROCESS','In Progress', EXTRACT(EPOCH FROM NOW()) * 1000),
('6eee879b-036e-420f-8300-958505399ef9', 'COMPLETED','Completed', EXTRACT(EPOCH FROM NOW()) * 1000),
('8cfdf88a-05a6-461d-b0f1-3116df15f3b5', 'ON_HOLD','On Hold', EXTRACT(EPOCH FROM NOW()) * 1000) ON CONFLICT (id) DO NOTHING;;

CREATE TABLE IF NOT EXISTS loan_type(
    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    creation_date BIGINT,
    max_amount DECIMAL(18, 2),
    min_amount DECIMAL(18, 2),
    interest_rate_taa DECIMAL(5, 2),
    auto_validation BOOLEAN
);

INSERT INTO loan_type (id, name, creation_date, max_amount, min_amount, interest_rate_taa, auto_validation) VALUES
('6e0c1f5a-3d2b-4f81-a7b2-10f8a846f332', 'PERSONAL_LOAN', EXTRACT(EPOCH FROM NOW()) * 1000, 50000.00, 100.00, 12.50, TRUE),
('9d4f23b1-5a2c-490b-9c71-2b0b5d53c8f4', 'MORTGAGE_LOAN', EXTRACT(EPOCH FROM NOW()) * 1000, 500000.00, 50000.00, 3.75, FALSE),
('3a8e9c7d-4b5f-4a6c-8e9d-1b2c3d4e5f60', 'STUDENT_LOAN', EXTRACT(EPOCH FROM NOW()) * 1000, 25000.00, 500.00, 5.00, TRUE) ON CONFLICT (id) DO NOTHING;;