-- liquibase formatted sql

-- changeset ai:seed-flashcards-topic-data-01

-- 1) Ensure demo topics exist
INSERT INTO topics (name, description, image_url, created_by)
SELECT 'Welcome to school', 'Tu vung co ban trong moi truong hoc duong', 'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'Welcome to school'
);

INSERT INTO topics (name, description, image_url, created_by)
SELECT 'Travelling', 'Tu vung chu de du lich va di chuyen', 'https://images.unsplash.com/photo-1488085061387-422e29b40080?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'Travelling'
);

INSERT INTO topics (name, description, image_url, created_by)
SELECT 'Summer festival', 'Tu vung lien quan den le hoi mua he', 'https://images.unsplash.com/photo-1521334884684-d80222895322?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'Summer festival'
);

-- 2) Ensure media for flashcards exists
INSERT INTO media (type, url, size, name, created_by)
SELECT 'IMAGE', 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=1200', 120, 'Destination image', 1
WHERE NOT EXISTS (
    SELECT 1 FROM media WHERE name = 'Destination image'
);

INSERT INTO media (type, url, size, name, created_by)
SELECT 'IMAGE', 'https://images.unsplash.com/photo-1496417263034-38ec4f0b665a?w=1200', 115, 'Accommodation image', 1
WHERE NOT EXISTS (
    SELECT 1 FROM media WHERE name = 'Accommodation image'
);

INSERT INTO media (type, url, size, name, created_by)
SELECT 'IMAGE', 'https://images.unsplash.com/photo-1500835556837-99ac94a94552?w=1200', 118, 'Sightseeing image', 1
WHERE NOT EXISTS (
    SELECT 1 FROM media WHERE name = 'Sightseeing image'
);

INSERT INTO media (type, url, size, name, created_by)
SELECT 'IMAGE', 'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=1200', 116, 'Departure image', 1
WHERE NOT EXISTS (
    SELECT 1 FROM media WHERE name = 'Departure image'
);

INSERT INTO media (type, url, size, name, created_by)
SELECT 'IMAGE', 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=1200', 110, 'Classroom image', 1
WHERE NOT EXISTS (
    SELECT 1 FROM media WHERE name = 'Classroom image'
);

INSERT INTO media (type, url, size, name, created_by)
SELECT 'IMAGE', 'https://images.unsplash.com/photo-1509062522246-3755977927d7?w=1200', 109, 'Teacher image', 1
WHERE NOT EXISTS (
    SELECT 1 FROM media WHERE name = 'Teacher image'
);

-- 3) Ensure dictionary words exist
INSERT INTO dictionary_words (word, pronunciation, meaning, type, created_by)
SELECT 'Destination', '/.destiˈneɪʃən/', 'Diem den', 'NOUN', 1
WHERE NOT EXISTS (
    SELECT 1 FROM dictionary_words WHERE word = 'Destination'
);

INSERT INTO dictionary_words (word, pronunciation, meaning, type, created_by)
SELECT 'Accommodation', '/əˌkɒməˈdeɪʃən/', 'Cho o', 'NOUN', 1
WHERE NOT EXISTS (
    SELECT 1 FROM dictionary_words WHERE word = 'Accommodation'
);

INSERT INTO dictionary_words (word, pronunciation, meaning, type, created_by)
SELECT 'Sightseeing', '/ˈsaɪtˌsiːɪŋ/', 'Tham quan', 'NOUN', 1
WHERE NOT EXISTS (
    SELECT 1 FROM dictionary_words WHERE word = 'Sightseeing'
);

INSERT INTO dictionary_words (word, pronunciation, meaning, type, created_by)
SELECT 'Departure', '/dɪˈpɑːtʃə/', 'Su khoi hanh', 'NOUN', 1
WHERE NOT EXISTS (
    SELECT 1 FROM dictionary_words WHERE word = 'Departure'
);

INSERT INTO dictionary_words (word, pronunciation, meaning, type, created_by)
SELECT 'Classroom', '/ˈklɑːsruːm/', 'Lop hoc', 'NOUN', 1
WHERE NOT EXISTS (
    SELECT 1 FROM dictionary_words WHERE word = 'Classroom'
);

INSERT INTO dictionary_words (word, pronunciation, meaning, type, created_by)
SELECT 'Teacher', '/ˈtiːtʃə/', 'Giao vien', 'NOUN', 1
WHERE NOT EXISTS (
    SELECT 1 FROM dictionary_words WHERE word = 'Teacher'
);

-- 4) Ensure topic_vocabulary mapping exists
INSERT INTO topic_vocabulary (topic_id, word_id)
SELECT t.id, dw.id
FROM dictionary_words dw
JOIN topics t ON t.name = 'Travelling'
WHERE dw.word = 'Destination'
  AND NOT EXISTS (
      SELECT 1
            FROM topic_vocabulary tv
            WHERE tv.word_id = dw.id
                AND tv.topic_id = t.id
  );

INSERT INTO topic_vocabulary (topic_id, word_id)
SELECT t.id, dw.id
FROM dictionary_words dw
JOIN topics t ON t.name = 'Travelling'
WHERE dw.word = 'Accommodation'
  AND NOT EXISTS (
      SELECT 1
            FROM topic_vocabulary tv
            WHERE tv.word_id = dw.id
                AND tv.topic_id = t.id
  );

INSERT INTO topic_vocabulary (topic_id, word_id)
SELECT t.id, dw.id
FROM dictionary_words dw
JOIN topics t ON t.name = 'Travelling'
WHERE dw.word = 'Sightseeing'
  AND NOT EXISTS (
      SELECT 1
            FROM topic_vocabulary tv
            WHERE tv.word_id = dw.id
                AND tv.topic_id = t.id
  );

INSERT INTO topic_vocabulary (topic_id, word_id)
SELECT t.id, dw.id
FROM dictionary_words dw
JOIN topics t ON t.name = 'Travelling'
WHERE dw.word = 'Departure'
  AND NOT EXISTS (
      SELECT 1
            FROM topic_vocabulary tv
            WHERE tv.word_id = dw.id
                AND tv.topic_id = t.id
  );

INSERT INTO topic_vocabulary (topic_id, word_id)
SELECT t.id, dw.id
FROM dictionary_words dw
JOIN topics t ON t.name = 'Welcome to school'
WHERE dw.word = 'Classroom'
  AND NOT EXISTS (
      SELECT 1
            FROM topic_vocabulary tv
            WHERE tv.word_id = dw.id
                AND tv.topic_id = t.id
  );

INSERT INTO topic_vocabulary (topic_id, word_id)
SELECT t.id, dw.id
FROM dictionary_words dw
JOIN topics t ON t.name = 'Welcome to school'
WHERE dw.word = 'Teacher'
  AND NOT EXISTS (
      SELECT 1
            FROM topic_vocabulary tv
            WHERE tv.word_id = dw.id
                AND tv.topic_id = t.id
  );

-- 5) Rebuild flash_cards if data was deleted
-- One flashcard per dictionary word that appears in topic_vocabulary.
INSERT INTO flash_cards (dictionary_word_id, media_id, example, created_by)
SELECT dw.id,
             CASE dw.word
                     WHEN 'Destination' THEN (SELECT id FROM media WHERE name = 'Destination image' LIMIT 1)
                     WHEN 'Accommodation' THEN (SELECT id FROM media WHERE name = 'Accommodation image' LIMIT 1)
                     WHEN 'Sightseeing' THEN (SELECT id FROM media WHERE name = 'Sightseeing image' LIMIT 1)
                     WHEN 'Departure' THEN (SELECT id FROM media WHERE name = 'Departure image' LIMIT 1)
                     WHEN 'Classroom' THEN (SELECT id FROM media WHERE name = 'Classroom image' LIMIT 1)
                     WHEN 'Teacher' THEN (SELECT id FROM media WHERE name = 'Teacher image' LIMIT 1)
                     ELSE (SELECT id FROM media WHERE type = 'IMAGE' ORDER BY id LIMIT 1)
             END AS media_id,
             CASE dw.word
                     WHEN 'Destination' THEN 'Paris is a popular tourist destination.'
                     WHEN 'Accommodation' THEN 'We found cheap accommodation near the station.'
                     WHEN 'Sightseeing' THEN 'They spent the afternoon sightseeing in the old town.'
                     WHEN 'Departure' THEN 'Our departure is scheduled at 6 a.m.'
                     WHEN 'Classroom' THEN 'Our classroom is on the second floor.'
                     WHEN 'Teacher' THEN 'The teacher explained the lesson clearly.'
                     ELSE CONCAT('Example with ', dw.word, '.')
             END AS example,
             1
FROM dictionary_words dw
JOIN topic_vocabulary tv ON tv.word_id = dw.id
LEFT JOIN flash_cards f ON f.dictionary_word_id = dw.id
WHERE f.id IS NULL;
