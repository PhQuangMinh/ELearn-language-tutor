-- liquibase formatted sql

-- changeset ai:seed-expanded-learning-pack-01

-- =========================================================
-- 1) Seed 11 new topics
-- =========================================================
INSERT INTO topics (name, description, image_url, created_by)
SELECT s.name, s.description, s.image_url, 1
FROM (
    SELECT 'Everyday Communication Plus' AS name, 'Daily life conversations with natural phrases and real contexts.' AS description, 'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=1200' AS image_url
    UNION ALL SELECT 'Academic Speaking Lab', 'Classroom discussions, presentations, and academic Q and A practice.', 'https://images.unsplash.com/photo-1456324504439-367cee3b3c32?w=1200'
    UNION ALL SELECT 'Travel Dialog Mastery', 'Airport, hotel, transport, and local interaction speaking practice.', 'https://images.unsplash.com/photo-1467269204594-9661b134dd2b?w=1200'
    UNION ALL SELECT 'Workplace Collaboration English', 'Meetings, updates, feedback, and team collaboration language.', 'https://images.unsplash.com/photo-1497215728101-856f4ea42174?w=1200'
    UNION ALL SELECT 'Customer Service Excellence', 'Professional service language for requests, complaints, and follow-up.', 'https://images.unsplash.com/photo-1556740749-887f6717d7e4?w=1200'
    UNION ALL SELECT 'Healthcare Conversation Basics', 'Doctor-patient communication for symptoms, advice, and treatment.', 'https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=1200'
    UNION ALL SELECT 'Tech Team English Sprint', 'Sprint planning, bug triage, standups, and delivery communication.', 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200'
    UNION ALL SELECT 'Interview Confidence Builder', 'Job interview introductions, strengths, and scenario-based answers.', 'https://images.unsplash.com/photo-1450101499163-c8848c66ca85?w=1200'
    UNION ALL SELECT 'Business Email Writing Pro', 'Write concise, polite, and action-driven business emails.', 'https://images.unsplash.com/photo-1455390582262-044cdead277a?w=1200'
    UNION ALL SELECT 'Grammar Accuracy Intensive', 'Targeted grammar correction and structured sentence production.', 'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=1200'
    UNION ALL SELECT 'Fluency Booster Sessions', 'Improve speaking flow, confidence, and response speed in context.', 'https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=1200'
) s
WHERE NOT EXISTS (
    SELECT 1 FROM topics t WHERE t.name = s.name
);

-- =========================================================
-- 2) Seed 2 lessons per topic (22 lessons total)
-- =========================================================
INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, CONCAT(t.name, ' - Lesson 1'), 'LISTENING',
       'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=1200', NULL, 1
FROM topics t
WHERE t.name IN (
    'Everyday Communication Plus',
    'Academic Speaking Lab',
    'Travel Dialog Mastery',
    'Workplace Collaboration English',
    'Customer Service Excellence',
    'Healthcare Conversation Basics',
    'Tech Team English Sprint',
    'Interview Confidence Builder',
    'Business Email Writing Pro',
    'Grammar Accuracy Intensive',
    'Fluency Booster Sessions'
)
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = CONCAT(t.name, ' - Lesson 1')
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, CONCAT(t.name, ' - Lesson 2'), 'PRACTICING',
       'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=1200', NULL, 1
FROM topics t
WHERE t.name IN (
    'Everyday Communication Plus',
    'Academic Speaking Lab',
    'Travel Dialog Mastery',
    'Workplace Collaboration English',
    'Customer Service Excellence',
    'Healthcare Conversation Basics',
    'Tech Team English Sprint',
    'Interview Confidence Builder',
    'Business Email Writing Pro',
    'Grammar Accuracy Intensive',
    'Fluency Booster Sessions'
)
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = CONCAT(t.name, ' - Lesson 2')
  );

-- =========================================================
-- 3) Seed 20 words per topic (220 words total) and map to topic
-- =========================================================
INSERT INTO dictionary_words (word, pronunciation, meaning, type, created_by)
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 20
),
topic_seed AS (
    SELECT 'ECP' AS code, 'Everyday Communication Plus' AS topic_name
    UNION ALL SELECT 'ASL', 'Academic Speaking Lab'
    UNION ALL SELECT 'TDM', 'Travel Dialog Mastery'
    UNION ALL SELECT 'WCE', 'Workplace Collaboration English'
    UNION ALL SELECT 'CSE', 'Customer Service Excellence'
    UNION ALL SELECT 'HCB', 'Healthcare Conversation Basics'
    UNION ALL SELECT 'TTS', 'Tech Team English Sprint'
    UNION ALL SELECT 'ICB', 'Interview Confidence Builder'
    UNION ALL SELECT 'BEP', 'Business Email Writing Pro'
    UNION ALL SELECT 'GAI', 'Grammar Accuracy Intensive'
    UNION ALL SELECT 'FBS', 'Fluency Booster Sessions'
)
SELECT
    CONCAT(ts.code, '_word_', LPAD(seq.n, 2, '0')) AS word,
    CONCAT('/', LOWER(ts.code), '_', LPAD(seq.n, 2, '0'), '/') AS pronunciation,
    CONCAT('Core vocabulary #', seq.n, ' for ', ts.topic_name) AS meaning,
    CASE
        WHEN MOD(seq.n, 3) = 1 THEN 'NOUN'
        WHEN MOD(seq.n, 3) = 2 THEN 'VERB'
        ELSE 'ADJECTIVE'
    END AS type,
    1
FROM topic_seed ts
CROSS JOIN seq
WHERE NOT EXISTS (
    SELECT 1
    FROM dictionary_words dw
    WHERE dw.word = CONCAT(ts.code, '_word_', LPAD(seq.n, 2, '0'))
);

INSERT INTO topic_vocabulary (topic_id, word_id)
WITH topic_seed AS (
    SELECT 'ECP' AS code, 'Everyday Communication Plus' AS topic_name
    UNION ALL SELECT 'ASL', 'Academic Speaking Lab'
    UNION ALL SELECT 'TDM', 'Travel Dialog Mastery'
    UNION ALL SELECT 'WCE', 'Workplace Collaboration English'
    UNION ALL SELECT 'CSE', 'Customer Service Excellence'
    UNION ALL SELECT 'HCB', 'Healthcare Conversation Basics'
    UNION ALL SELECT 'TTS', 'Tech Team English Sprint'
    UNION ALL SELECT 'ICB', 'Interview Confidence Builder'
    UNION ALL SELECT 'BEP', 'Business Email Writing Pro'
    UNION ALL SELECT 'GAI', 'Grammar Accuracy Intensive'
    UNION ALL SELECT 'FBS', 'Fluency Booster Sessions'
)
SELECT t.id, dw.id
FROM topic_seed ts
JOIN topics t ON t.name = ts.topic_name
JOIN dictionary_words dw ON dw.word LIKE CONCAT(ts.code, '_word_%')
WHERE NOT EXISTS (
    SELECT 1 FROM topic_vocabulary tv WHERE tv.topic_id = t.id AND tv.word_id = dw.id
);

-- =========================================================
-- 4) Seed media and flashcards (one flashcard per seeded word)
-- =========================================================
INSERT INTO media (type, url, size, name, created_by)
SELECT 'IMAGE',
       CONCAT('https://images.unsplash.com/photo-1523240795612-9a054b0db644?w=1200&sig=', s.seq_id),
       120,
       CONCAT('Seed pack media ', s.code),
       1
FROM (
    SELECT 1 AS seq_id, 'ECP' AS code
    UNION ALL SELECT 2, 'ASL'
    UNION ALL SELECT 3, 'TDM'
    UNION ALL SELECT 4, 'WCE'
    UNION ALL SELECT 5, 'CSE'
    UNION ALL SELECT 6, 'HCB'
    UNION ALL SELECT 7, 'TTS'
    UNION ALL SELECT 8, 'ICB'
    UNION ALL SELECT 9, 'BEP'
    UNION ALL SELECT 10, 'GAI'
    UNION ALL SELECT 11, 'FBS'
) s
WHERE NOT EXISTS (
    SELECT 1 FROM media m WHERE m.name = CONCAT('Seed pack media ', s.code)
);

INSERT INTO flash_cards (dictionary_word_id, media_id, example, created_by)
WITH topic_seed AS (
    SELECT 'ECP' AS code, 'Everyday Communication Plus' AS topic_name
    UNION ALL SELECT 'ASL', 'Academic Speaking Lab'
    UNION ALL SELECT 'TDM', 'Travel Dialog Mastery'
    UNION ALL SELECT 'WCE', 'Workplace Collaboration English'
    UNION ALL SELECT 'CSE', 'Customer Service Excellence'
    UNION ALL SELECT 'HCB', 'Healthcare Conversation Basics'
    UNION ALL SELECT 'TTS', 'Tech Team English Sprint'
    UNION ALL SELECT 'ICB', 'Interview Confidence Builder'
    UNION ALL SELECT 'BEP', 'Business Email Writing Pro'
    UNION ALL SELECT 'GAI', 'Grammar Accuracy Intensive'
    UNION ALL SELECT 'FBS', 'Fluency Booster Sessions'
)
SELECT
    dw.id,
    m.id,
    CONCAT('Example sentence with ', dw.word, ' in ', ts.topic_name, '.'),
    1
FROM topic_seed ts
JOIN dictionary_words dw ON dw.word LIKE CONCAT(ts.code, '_word_%')
JOIN media m ON m.name = CONCAT('Seed pack media ', ts.code)
LEFT JOIN flash_cards f ON f.dictionary_word_id = dw.id
WHERE f.id IS NULL;

-- =========================================================
-- 5) Seed 2 scenarios per topic (22 scenarios total)
-- =========================================================
INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT
    t.id,
    l.id,
    CONCAT(t.name, ' Scenario ', CASE WHEN l.title LIKE '%Lesson 1' THEN '1' ELSE '2' END),
    CONCAT('Conversation practice for ', t.name, ' - ', l.title, '.'),
    CASE WHEN l.title LIKE '%Lesson 1' THEN 'Coach' ELSE 'Mentor' END,
    'Learner',
    'Complete the dialogue naturally and ask one follow-up question.',
    CASE
        WHEN l.title LIKE '%Lesson 1' THEN 'Great! Let us start this scenario.'
        ELSE 'Nice progress. Let us continue with a deeper situation.'
    END,
    'Use clear sentence structure and context-specific vocabulary.',
    CONCAT('Ban dang luyen hoi thoai cho chu de ', t.name, '.'),
    1
FROM topics t
JOIN lessons l ON l.topic_id = t.id AND (l.title LIKE '% - Lesson 1' OR l.title LIKE '% - Lesson 2')
WHERE t.name IN (
    'Everyday Communication Plus',
    'Academic Speaking Lab',
    'Travel Dialog Mastery',
    'Workplace Collaboration English',
    'Customer Service Excellence',
    'Healthcare Conversation Basics',
    'Tech Team English Sprint',
    'Interview Confidence Builder',
    'Business Email Writing Pro',
    'Grammar Accuracy Intensive',
    'Fluency Booster Sessions'
)
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id
  );

-- =========================================================
-- 6) Seed 10 one-selection questions per lesson (220 questions)
-- =========================================================
INSERT INTO questions (lesson_id, content, type, media_id, repeatable, created_by)
WITH RECURSIVE seq(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 10
),
target_lessons AS (
    SELECT l.id, l.title
    FROM lessons l
    JOIN topics t ON t.id = l.topic_id
    WHERE t.name IN (
        'Everyday Communication Plus',
        'Academic Speaking Lab',
        'Travel Dialog Mastery',
        'Workplace Collaboration English',
        'Customer Service Excellence',
        'Healthcare Conversation Basics',
        'Tech Team English Sprint',
        'Interview Confidence Builder',
        'Business Email Writing Pro',
        'Grammar Accuracy Intensive',
        'Fluency Booster Sessions'
    )
      AND (l.title LIKE '% - Lesson 1' OR l.title LIKE '% - Lesson 2')
)
SELECT
    tl.id,
    CONCAT('Seed Pack: ', tl.title, ' - Question ', seq.n),
    'ONE_SELECTION',
    NULL,
    false,
    1
FROM target_lessons tl
CROSS JOIN seq
WHERE NOT EXISTS (
    SELECT 1
    FROM questions q
    WHERE q.lesson_id = tl.id
      AND q.content = CONCAT('Seed Pack: ', tl.title, ' - Question ', seq.n)
);

-- =========================================================
-- 7) Seed 4 answers per seeded question (A is correct)
-- =========================================================
INSERT INTO answers (question_id, content, is_correct, created_by)
WITH option_seed AS (
    SELECT 1 AS opt, 'A' AS label
    UNION ALL SELECT 2, 'B'
    UNION ALL SELECT 3, 'C'
    UNION ALL SELECT 4, 'D'
)
SELECT
    q.id,
    CASE
        WHEN os.opt = 1 THEN CONCAT('Correct option A for Q', q.id)
        ELSE CONCAT('Distractor ', os.label, ' for Q', q.id)
    END AS content,
    CASE WHEN os.opt = 1 THEN true ELSE false END AS is_correct,
    1
FROM questions q
CROSS JOIN option_seed os
WHERE q.content LIKE 'Seed Pack:%'
  AND NOT EXISTS (
      SELECT 1
      FROM answers a
      WHERE a.question_id = q.id
        AND a.content = CASE
            WHEN os.opt = 1 THEN CONCAT('Correct option A for Q', q.id)
            ELSE CONCAT('Distractor ', os.label, ' for Q', q.id)
        END
  );
