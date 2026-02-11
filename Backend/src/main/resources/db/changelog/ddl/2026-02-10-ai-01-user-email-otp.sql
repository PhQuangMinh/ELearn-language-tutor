-- liquibase formatted sql

-- changeset ai:user-email-otp-columns-01
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email_verification_code VARCHAR(6) NULL,
    ADD COLUMN IF NOT EXISTS email_verification_expiry TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS reset_password_code VARCHAR(6) NULL,
    ADD COLUMN IF NOT EXISTS reset_password_expiry TIMESTAMP NULL;

