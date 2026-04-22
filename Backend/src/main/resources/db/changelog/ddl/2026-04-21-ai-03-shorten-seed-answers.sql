-- liquibase formatted sql

-- changeset ai:shorten-seed-answers-01

-- Keep seeded answers short so they fit mobile 4-choice buttons.
UPDATE answers a
JOIN questions q ON q.id = a.question_id
SET a.content = CASE
    WHEN a.is_correct = true THEN 'That works.'
    ELSE 'Not quite.'
END
WHERE (
    q.content LIKE 'Choose the most natural response for this context in %'
    OR q.content LIKE 'Seed Pack:%'
)
  AND (
    a.content IN (
        'That sounds good. Let us proceed with this plan.',
        'I maybe can do something maybe later maybe not.'
    )
    OR a.content LIKE 'Correct option A for Q%'
    OR a.content LIKE 'Distractor % for Q%'
  );
