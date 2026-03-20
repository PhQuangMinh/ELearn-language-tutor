-- liquibase formatted sql

-- changeset thanhnn:change media of message to nullable
alter table speaking_messages
    modify media_id int null;