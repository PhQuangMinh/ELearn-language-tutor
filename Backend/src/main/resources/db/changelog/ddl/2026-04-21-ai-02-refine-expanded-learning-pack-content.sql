-- liquibase formatted sql

-- changeset ai:refine-expanded-learning-pack-content-01

-- =========================================================
-- 1) Refine seeded dictionary words from placeholder format
-- =========================================================
UPDATE dictionary_words dw
JOIN (
    SELECT id, CAST(RIGHT(word, 2) AS UNSIGNED) AS idx
    FROM dictionary_words
    WHERE word REGEXP '^[A-Z]{3}_word_[0-9]{2}$'
) seed ON seed.id = dw.id
SET
    dw.word = CASE seed.idx
        WHEN 1 THEN 'greeting'
        WHEN 2 THEN 'schedule'
        WHEN 3 THEN 'request'
        WHEN 4 THEN 'meeting'
        WHEN 5 THEN 'feedback'
        WHEN 6 THEN 'deadline'
        WHEN 7 THEN 'strategy'
        WHEN 8 THEN 'improve'
        WHEN 9 THEN 'confident'
        WHEN 10 THEN 'organize'
        WHEN 11 THEN 'discussion'
        WHEN 12 THEN 'clarify'
        WHEN 13 THEN 'priority'
        WHEN 14 THEN 'supportive'
        WHEN 15 THEN 'solution'
        WHEN 16 THEN 'respond'
        WHEN 17 THEN 'flexible'
        WHEN 18 THEN 'collaboration'
        WHEN 19 THEN 'negotiate'
        ELSE 'effective'
    END,
    dw.pronunciation = CASE seed.idx
        WHEN 1 THEN '/ˈɡriː.tɪŋ/'
        WHEN 2 THEN '/ˈskedʒ.uːl/'
        WHEN 3 THEN '/rɪˈkwest/'
        WHEN 4 THEN '/ˈmiː.tɪŋ/'
        WHEN 5 THEN '/ˈfiːd.bæk/'
        WHEN 6 THEN '/ˈded.laɪn/'
        WHEN 7 THEN '/ˈstræt.ə.dʒi/'
        WHEN 8 THEN '/ɪmˈpruːv/'
        WHEN 9 THEN '/ˈkɒn.fɪ.dənt/'
        WHEN 10 THEN '/ˈɔː.ɡə.naɪz/'
        WHEN 11 THEN '/dɪˈskʌʃ.ən/'
        WHEN 12 THEN '/ˈklær.ɪ.faɪ/'
        WHEN 13 THEN '/praɪˈɒr.ə.ti/'
        WHEN 14 THEN '/səˈpɔː.tɪv/'
        WHEN 15 THEN '/səˈluː.ʃən/'
        WHEN 16 THEN '/rɪˈspɒnd/'
        WHEN 17 THEN '/ˈflek.sə.bəl/'
        WHEN 18 THEN '/kəˌlæb.əˈreɪ.ʃən/'
        WHEN 19 THEN '/nɪˈɡəʊ.ʃi.eɪt/'
        ELSE '/ɪˈfek.tɪv/'
    END,
    dw.meaning = CONCAT(
        CASE seed.idx
            WHEN 1 THEN 'A polite way to start a conversation'
            WHEN 2 THEN 'A planned time arrangement for activities'
            WHEN 3 THEN 'A formal or polite ask for something'
            WHEN 4 THEN 'A discussion session with others'
            WHEN 5 THEN 'Comments that help improve performance'
            WHEN 6 THEN 'The final time limit for completion'
            WHEN 7 THEN 'A long-term plan to reach a goal'
            WHEN 8 THEN 'To become better in quality or skill'
            WHEN 9 THEN 'Feeling sure about your ability'
            WHEN 10 THEN 'To arrange things in a clear structure'
            WHEN 11 THEN 'A focused exchange of ideas'
            WHEN 12 THEN 'To make meaning clear and understandable'
            WHEN 13 THEN 'Something that should be handled first'
            WHEN 14 THEN 'Showing encouragement and understanding'
            WHEN 15 THEN 'An answer that resolves a problem'
            WHEN 16 THEN 'To reply appropriately to a message'
            WHEN 17 THEN 'Able to adapt easily to changes'
            WHEN 18 THEN 'Working together effectively as a team'
            WHEN 19 THEN 'To discuss terms and reach agreement'
            ELSE 'Producing the result you want efficiently'
        END,
        ' in practical communication context.'
    );

-- =========================================================
-- 2) Refine flashcard examples from placeholders
-- =========================================================
UPDATE flash_cards f
JOIN dictionary_words dw ON dw.id = f.dictionary_word_id
SET f.example = CONCAT(
    'In today''s conversation, I used the word "',
    dw.word,
    '" correctly to express my idea clearly.'
)
WHERE f.example LIKE 'Example sentence with %';

-- =========================================================
-- 3) Refine question content from placeholders
-- =========================================================
UPDATE questions q
SET q.content = CONCAT(
    'Choose the most natural response for this context in ',
    (SELECT l.title FROM lessons l WHERE l.id = q.lesson_id),
    ': ',
    CASE CAST(SUBSTRING_INDEX(q.content, 'Question ', -1) AS UNSIGNED)
        WHEN 1 THEN 'greeting a new partner politely'
        WHEN 2 THEN 'asking for clarification'
        WHEN 3 THEN 'confirming a meeting time'
        WHEN 4 THEN 'giving a short project update'
        WHEN 5 THEN 'requesting support respectfully'
        WHEN 6 THEN 'responding to feedback positively'
        WHEN 7 THEN 'handling a customer concern'
        WHEN 8 THEN 'negotiating a practical solution'
        WHEN 9 THEN 'summarizing next action items'
        ELSE 'closing the conversation professionally'
    END
)
WHERE q.content LIKE 'Seed Pack:% - Question %';

-- =========================================================
-- 4) Refine seeded answers from placeholders
-- =========================================================
UPDATE answers a
SET a.content = CASE
    WHEN a.is_correct = true THEN 'That sounds good. Let us proceed with this plan.'
    ELSE 'I maybe can do something maybe later maybe not.'
END
WHERE a.content LIKE 'Correct option A for Q%'
   OR a.content LIKE 'Distractor % for Q%';

-- =========================================================
-- 5) Refine scenario text to read naturally
-- =========================================================
UPDATE scenarios s
SET
    s.description = CONCAT('Realistic conversation practice for ', s.title, ' with clear speaking goals.'),
    s.tasks = 'Ask at least two follow-up questions, confirm one key detail, and end politely.',
    s.openning_message = 'Hi! Let us begin. I will play my role, and you respond naturally in English.',
    s.suggestion = 'Use short, clear sentences first, then expand with one supporting detail.',
    s.translation = 'Luyen hoi thoai thuc te voi muc tieu ro rang va phan hoi tu nhien.'
WHERE s.title LIKE '% Scenario %'
  AND s.description LIKE 'Conversation practice for %';
