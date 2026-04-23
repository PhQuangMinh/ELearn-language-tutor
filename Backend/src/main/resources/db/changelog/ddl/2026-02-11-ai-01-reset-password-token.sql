-- liquibase formatted sql

-- changeset ai:reset-password-token-columns-01
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS reset_password_token VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS reset_password_token_expiry TIMESTAMP NULL;

