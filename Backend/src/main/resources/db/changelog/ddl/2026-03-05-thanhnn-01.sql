-- liquibase formatted sql

-- changeset thanhnn:add relation between topic and dictionary words
create table topic_vocabulary
(
    topic_id int,
    word_id  int,
    constraint topic_vocabulary_dictionary_words_id_fk
        foreign key (word_id) references dictionary_words (id),
    constraint topic_vocabulary_topics_id_fk
        foreign key (topic_id) references topics (id)
);