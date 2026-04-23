-- liquibase formatted sql

-- changeset thanhnn:add-unique-constraint-for-user_id
alter table user_streak
    add constraint user_streak_pk
        unique if not exists (user_id);