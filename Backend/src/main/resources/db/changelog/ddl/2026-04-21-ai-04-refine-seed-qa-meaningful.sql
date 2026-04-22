-- liquibase formatted sql

-- changeset ai:refine-seed-qa-meaningful-01

-- =========================================================
-- 1) Make seeded question content short and meaningful
-- =========================================================
UPDATE questions q
JOIN (
    SELECT
        sq.id,
        ROW_NUMBER() OVER (PARTITION BY sq.lesson_id ORDER BY sq.id) AS qn
    FROM questions sq
    WHERE sq.content LIKE 'Choose the most natural response for this context in %'
       OR sq.content LIKE 'Seed Pack:%'
) ranked ON ranked.id = q.id
SET q.content = CASE ranked.qn
    WHEN 1 THEN 'How do you greet a new partner politely?'
    WHEN 2 THEN 'How do you ask for clarification?'
    WHEN 3 THEN 'How do you confirm a meeting time?'
    WHEN 4 THEN 'How do you give a short project update?'
    WHEN 5 THEN 'How do you request support politely?'
    WHEN 6 THEN 'How do you respond to feedback well?'
    WHEN 7 THEN 'How do you handle a customer concern?'
    WHEN 8 THEN 'How do you suggest a practical solution?'
    WHEN 9 THEN 'How do you summarize next steps?'
    ELSE 'How do you close a conversation professionally?'
END;

-- =========================================================
-- 2) Make seeded answers short but meaningful
-- =========================================================
UPDATE answers a
JOIN (
    SELECT
        sa.id AS answer_id,
        sa.is_correct,
        ROW_NUMBER() OVER (PARTITION BY sa.question_id ORDER BY sa.id) AS opt,
        qq.qn
    FROM answers sa
    JOIN (
        SELECT
            sq.id,
            ROW_NUMBER() OVER (PARTITION BY sq.lesson_id ORDER BY sq.id) AS qn
        FROM questions sq
        WHERE sq.content IN (
            'How do you greet a new partner politely?',
            'How do you ask for clarification?',
            'How do you confirm a meeting time?',
            'How do you give a short project update?',
            'How do you request support politely?',
            'How do you respond to feedback well?',
            'How do you handle a customer concern?',
            'How do you suggest a practical solution?',
            'How do you summarize next steps?',
            'How do you close a conversation professionally?'
        )
    ) qq ON qq.id = sa.question_id
) x ON x.answer_id = a.id
SET a.content = CASE x.qn
    WHEN 1 THEN
        CASE
            WHEN x.is_correct THEN 'Nice to meet you.'
            WHEN x.opt = 2 THEN 'Close the window.'
            WHEN x.opt = 3 THEN 'I am very late.'
            ELSE 'No need to talk.'
        END
    WHEN 2 THEN
        CASE
            WHEN x.is_correct THEN 'Could you explain that?'
            WHEN x.opt = 2 THEN 'I will sleep now.'
            WHEN x.opt = 3 THEN 'That is my bag.'
            ELSE 'We can skip this.'
        END
    WHEN 3 THEN
        CASE
            WHEN x.is_correct THEN 'Shall we meet at 3?'
            WHEN x.opt = 2 THEN 'The coffee is hot.'
            WHEN x.opt = 3 THEN 'I lost my phone.'
            ELSE 'Open the document.'
        END
    WHEN 4 THEN
        CASE
            WHEN x.is_correct THEN 'The task is on track.'
            WHEN x.opt = 2 THEN 'I like blue shoes.'
            WHEN x.opt = 3 THEN 'My room is clean.'
            ELSE 'Turn off the light.'
        END
    WHEN 5 THEN
        CASE
            WHEN x.is_correct THEN 'Could you help me, please?'
            WHEN x.opt = 2 THEN 'I can do nothing.'
            WHEN x.opt = 3 THEN 'This is not tasty.'
            ELSE 'Please ignore this.'
        END
    WHEN 6 THEN
        CASE
            WHEN x.is_correct THEN 'Thanks, I will improve it.'
            WHEN x.opt = 2 THEN 'You are always wrong.'
            WHEN x.opt = 3 THEN 'I do not care.'
            ELSE 'Stop talking now.'
        END
    WHEN 7 THEN
        CASE
            WHEN x.is_correct THEN 'I understand your concern.'
            WHEN x.opt = 2 THEN 'That is your problem.'
            WHEN x.opt = 3 THEN 'Come back next year.'
            ELSE 'I cannot hear you.'
        END
    WHEN 8 THEN
        CASE
            WHEN x.is_correct THEN 'Let us try this plan.'
            WHEN x.opt = 2 THEN 'Everything is impossible.'
            WHEN x.opt = 3 THEN 'No one can help.'
            ELSE 'We should do nothing.'
        END
    WHEN 9 THEN
        CASE
            WHEN x.is_correct THEN 'Next, I send the report.'
            WHEN x.opt = 2 THEN 'Maybe no next step.'
            WHEN x.opt = 3 THEN 'I forgot everything.'
            ELSE 'The table is round.'
        END
    ELSE
        CASE
            WHEN x.is_correct THEN 'Thank you for your time.'
            WHEN x.opt = 2 THEN 'I refuse to speak.'
            WHEN x.opt = 3 THEN 'This is very random.'
            ELSE 'Goodbye, maybe never.'
        END
END
WHERE a.content IS NOT NULL;
