-- liquibase formatted sql

-- changeset thanhnn:add media to question
alter table questions
    add media_id int null;

alter table questions
    add constraint questions_media_id_fk
        foreign key (media_id) references media (id);