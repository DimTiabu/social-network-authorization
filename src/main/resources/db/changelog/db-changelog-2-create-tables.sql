-- Создание таблицы "users" в схеме schema_authorization
CREATE TABLE IF NOT EXISTS schema_authorization.users (
                                                          id UUID PRIMARY KEY,
                                                          email VARCHAR(255) NOT NULL UNIQUE,
                                                          password VARCHAR(255) NOT NULL,
                                                          account_id UUID
);

-- Создание таблицы "user_roles" в схеме schema_authorization
CREATE TABLE IF NOT EXISTS schema_authorization.user_roles (
                                                               user_id UUID NOT NULL,
                                                               roles VARCHAR(50) NOT NULL
);

-- Добавление внешнего ключа для таблицы "user_roles"
ALTER TABLE schema_authorization.user_roles
    ADD CONSTRAINT fk_user_roles_users
        FOREIGN KEY (user_id)
            REFERENCES schema_authorization.users(id)
            ON DELETE CASCADE;
