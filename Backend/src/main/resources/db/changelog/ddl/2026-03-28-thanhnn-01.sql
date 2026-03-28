-- liquibase formatted sql

-- changeset thanhnn:remove status field of user_streak (no need anymore)
alter table user_streak
    drop column status;