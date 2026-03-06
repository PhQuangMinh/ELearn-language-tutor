-- liquibase formatted sql

-- changeset ai:drop-flashcards-topic-id-01
ALTER TABLE flash_cards
    DROP FOREIGN KEY flash_cards_topics_id_fk;

ALTER TABLE flash_cards
    DROP COLUMN topic_id;
