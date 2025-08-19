-- Создание таблицы для инвайт-кодов
CREATE TABLE IF NOT EXISTS schema_authorization.invitation_codes (
                                                                     id UUID PRIMARY KEY,
                                                                     email VARCHAR(255) NOT NULL UNIQUE,
                                                                     confirmation_code VARCHAR(50) NOT NULL,
                                                                     created_at TIMESTAMP NOT NULL,
                                                                     expires_at TIMESTAMP NOT NULL,
                                                                     is_used BOOLEAN NOT NULL DEFAULT FALSE
);

-- Индекс для быстрого поиска по email и коду
CREATE INDEX idx_invitation_codes_email_code
    ON schema_authorization.invitation_codes(email, confirmation_code);

-- Индекс для очистки просроченных кодов
CREATE INDEX idx_invitation_codes_expires
    ON schema_authorization.invitation_codes(expires_at);