-- liquibase formatted sql

-- changeset thanhnn:change end date of session to nullable
alter table speaking_sessions
    modify ended_at timestamp null;