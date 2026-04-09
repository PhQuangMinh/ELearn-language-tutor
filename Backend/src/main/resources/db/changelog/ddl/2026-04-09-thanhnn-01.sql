-- liquibase formatted sql

-- changeset thanhnn:modify_content_column
alter table user_question_answers
    modify content text null;