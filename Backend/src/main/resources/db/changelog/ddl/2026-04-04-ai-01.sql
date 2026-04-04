-- liquibase formatted sql

-- changeset ai:seed-admin-real-data-01

-- 1) Seed topics with real image URLs
INSERT INTO topics (name, description, image_url, created_by)
SELECT 'English Basics', 'Introduction to core English communication for beginners', 'https://images.unsplash.com/photo-1456283174360-12bccda6dda9?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'English Basics'
);

INSERT INTO topics (name, description, image_url, created_by)
SELECT 'Advanced Grammar', 'Master complex grammar structures in practical contexts', 'https://images.unsplash.com/photo-1434282176406-e6f4a1b912b8?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'Advanced Grammar'
);

INSERT INTO topics (name, description, image_url, created_by)
SELECT 'Conversational English', 'Daily speaking practice for real-world communication', 'https://images.unsplash.com/photo-1491438590914-bc09fcaaf77a?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'Conversational English'
);

INSERT INTO topics (name, description, image_url, created_by)
SELECT 'Business English', 'Professional communication in meetings, emails, and presentations', 'https://images.unsplash.com/photo-1552664730-d307ca884978?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'Business English'
);

-- 2) Seed lessons mapped to each topic
INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Welcome to English', 'LISTENING', 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'English Basics'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Welcome to English'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Basic Vocabulary', 'VOCABULARY', 'https://images.unsplash.com/photo-1516979187457-637abb4f9353?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'English Basics'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Basic Vocabulary'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Simple Conversations', 'PRACTICING', 'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'English Basics'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Simple Conversations'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Subject-Verb Agreement', 'LISTENING', 'https://images.unsplash.com/photo-1509062522246-3755977927d7?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'Advanced Grammar'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Subject-Verb Agreement'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Tenses Practice', 'PRACTICING', 'https://images.unsplash.com/photo-1456324504439-367cee3b3c32?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'Advanced Grammar'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Tenses Practice'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Restaurant Dialogues', 'LISTENING', 'https://images.unsplash.com/photo-1495521821757-a1efb6729352?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'Conversational English'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Restaurant Dialogues'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Travel Conversations', 'PRACTICING', 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'Conversational English'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Travel Conversations'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Email Writing', 'PRACTICING', 'https://images.unsplash.com/photo-1455390582262-044cdead277a?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'Business English'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Email Writing'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Business Meetings', 'LISTENING', 'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'Business English'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Business Meetings'
  );

-- 3) Seed scenarios linked to lesson + topic
INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Welcome Dialogue',
       'Basic greeting and self-introduction at first meeting',
       'Tutor',
       'Learner',
       'Introduce yourself and ask simple follow-up questions',
       'Hello! Nice to meet you. Can you introduce yourself?',
       'Use short present-tense sentences and common greeting phrases.',
       'Ban dang tap gioi thieu ban than trong buoi gap dau tien.',
       1
FROM topics t
JOIN lessons l ON l.topic_id = t.id AND l.title = 'Welcome to English'
WHERE t.name = 'English Basics'
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id AND s.title = 'Welcome Dialogue'
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Shopping at a Store',
       'Ask prices and sizes while shopping for clothes',
       'Shopkeeper',
       'Customer',
       'Ask for price, size, and color options politely',
       'Welcome! How can I help you today?',
       'Try using: How much is this?, Do you have size M?, Can I try this on?',
       'Ban dong vai khach mua sam trong cua hang quan ao.',
       1
FROM topics t
JOIN lessons l ON l.topic_id = t.id AND l.title = 'Simple Conversations'
WHERE t.name = 'English Basics'
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id AND s.title = 'Shopping at a Store'
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Grammar Coach Session',
       'Practice correcting tense and agreement mistakes in context',
       'English Teacher',
       'Student',
       'Rewrite incorrect sentences into correct grammar forms',
       'Great! Let us practice grammar with a few examples.',
       'Focus on subject-verb agreement and tense consistency.',
       'Ban dang hoc ngu phap voi giao vien va sua cau sai.',
       1
FROM topics t
JOIN lessons l ON l.topic_id = t.id AND l.title = 'Tenses Practice'
WHERE t.name = 'Advanced Grammar'
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id AND s.title = 'Grammar Coach Session'
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Restaurant Ordering',
       'Order food, ask about ingredients, and request changes',
       'Waiter',
       'Customer',
       'Order a full meal and ask for one special customization',
       'Good evening! Are you ready to order?',
       'Use polite requests: I would like..., Could I have..., without...',
       'Ban vao nha hang goi mon va yeu cau dieu chinh mon an.',
       1
FROM topics t
JOIN lessons l ON l.topic_id = t.id AND l.title = 'Restaurant Dialogues'
WHERE t.name = 'Conversational English'
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id AND s.title = 'Restaurant Ordering'
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Quarterly Business Meeting',
       'Present results and discuss next-quarter priorities',
       'Team Manager',
       'Team Member',
       'Present one KPI and propose one action for improvement',
       'Thanks everyone for joining. Let us begin with Q1 results.',
       'Use clear business phrases: revenue grew, target, action plan, timeline.',
       'Ban tham gia hop quy va trinh bay ket qua cong viec.',
       1
FROM topics t
JOIN lessons l ON l.topic_id = t.id AND l.title = 'Business Meetings'
WHERE t.name = 'Business English'
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id AND s.title = 'Quarterly Business Meeting'
  );
