-- liquibase formatted sql

-- changeset thanhnn:add-field-repeatable-to-question
alter table questions
    add repeatable bool default false not null;