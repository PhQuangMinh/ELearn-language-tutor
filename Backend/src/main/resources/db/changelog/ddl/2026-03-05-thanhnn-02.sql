-- liquibase formatted sql

-- changeset thanhnn:add type of word
alter table dictionary_words
    add type varchar(20) not null;