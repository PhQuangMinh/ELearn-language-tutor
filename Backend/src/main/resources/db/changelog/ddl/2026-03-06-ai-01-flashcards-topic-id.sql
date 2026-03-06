-- liquibase formatted sql

-- changeset ai:flashcards-topic-id-01
ALTER TABLE flash_cards
    ADD COLUMN topic_id INT NULL,
    ADD CONSTRAINT flash_cards_topics_id_fk
        FOREIGN KEY (topic_id) REFERENCES topics (id);
