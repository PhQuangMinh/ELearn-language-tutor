-- liquibase formatted sql

-- changeset ai:seed-missing-scenarios-01

-- Backfill scenarios for lessons that currently have none.
-- Safe to run multiple times due to NOT EXISTS guards.

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'First Day Introduction',
       'Practice introducing yourself and asking simple classmate questions.',
       'Classmate',
       'New Student',
       'Introduce your name, major, and ask at least two follow-up questions.',
       'Hi! I am new here too. Can you tell me a bit about yourself?',
       'Use simple present tense and short clear sentences.',
       'Ban tap gioi thieu ban than trong ngay dau den lop.',
       1
FROM lessons l
JOIN topics t ON t.id = l.topic_id
WHERE l.id = 42
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Build Your Starter Vocabulary',
       'Use everyday beginner words in short practical dialogue.',
       'English Tutor',
       'Beginner Learner',
       'Use at least five basic words correctly in context.',
       'Great! Let us start with simple daily words. Are you ready?',
       'Try patterns like: This is..., I have..., I need..., I like...',
       'Ban luyen dung tu vung co ban trong hoi thoai don gian.',
       1
FROM lessons l
JOIN topics t ON t.id = l.topic_id
WHERE l.id = 43
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Grammar Accuracy Drill',
       'Correct subject-verb agreement mistakes in realistic sentences.',
       'Grammar Coach',
       'Student',
       'Fix at least four incorrect sentences and explain one correction.',
       'Let us practice subject-verb agreement with real examples.',
       'Check singular/plural subjects and present tense verb endings.',
       'Ban luyen sua loi hoa hop chu ngu va dong tu.',
       1
FROM lessons l
JOIN topics t ON t.id = l.topic_id
WHERE l.id = 45
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Travel Plan Conversation',
       'Discuss itinerary, transportation, and timing for a short trip.',
       'Travel Partner',
       'Trip Planner',
       'Propose a 2-day plan and confirm transport and schedule details.',
       'We have two days. How should we plan our trip?',
       'Use phrases: We should..., Let us..., How about..., What time...?',
       'Ban trao doi ke hoach du lich ngan ngay voi ban dong hanh.',
       1
FROM lessons l
JOIN topics t ON t.id = l.topic_id
WHERE l.id = 48
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Client Follow-up Email',
       'Write and refine a professional follow-up email after a meeting.',
       'Client',
       'Account Executive',
       'Summarize meeting points, next steps, and clear timeline.',
       'Thanks for the meeting today. Could you send a follow-up summary?',
       'Keep tone polite, concise, and action-oriented.',
       'Ban soan email follow-up chuyen nghiep sau buoi hop voi khach hang.',
       1
FROM lessons l
JOIN topics t ON t.id = l.topic_id
WHERE l.id = 49
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id
  );
