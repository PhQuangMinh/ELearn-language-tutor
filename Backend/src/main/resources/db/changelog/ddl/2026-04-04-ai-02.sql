-- liquibase formatted sql

-- changeset ai:seed-admin-real-data-02

-- 1) Seed 5 new topics
INSERT INTO topics (name, description, image_url, created_by)
SELECT 'Travel Survival English', 'Essential English phrases for airports, hotels, and local transport', 'https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'Travel Survival English'
);

INSERT INTO topics (name, description, image_url, created_by)
SELECT 'Job Interview English', 'Practice introductions, strengths, and role-specific interview answers', 'https://images.unsplash.com/photo-1450101499163-c8848c66ca85?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'Job Interview English'
);

INSERT INTO topics (name, description, image_url, created_by)
SELECT 'Customer Support English', 'Handling complaints and support requests with professional tone', 'https://images.unsplash.com/photo-1556740749-887f6717d7e4?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'Customer Support English'
);

INSERT INTO topics (name, description, image_url, created_by)
SELECT 'Healthcare English', 'Speaking with doctors, describing symptoms, and understanding advice', 'https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'Healthcare English'
);

INSERT INTO topics (name, description, image_url, created_by)
SELECT 'Tech Workplace English', 'Discussing tasks, bugs, deadlines, and sprint updates in English', 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200', 1
WHERE NOT EXISTS (
    SELECT 1 FROM topics WHERE name = 'Tech Workplace English'
);

-- 2) Seed 5 new lessons (one per new topic)
INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Airport Check-in Practice', 'PRACTICING', 'https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'Travel Survival English'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Airport Check-in Practice'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Tell Me About Yourself', 'LISTENING', 'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'Job Interview English'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Tell Me About Yourself'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Handling Angry Customers', 'LISTENING', 'https://images.unsplash.com/photo-1556742393-d75f468bfcb0?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'Customer Support English'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Handling Angry Customers'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Describe Symptoms Clearly', 'PRACTICING', 'https://images.unsplash.com/photo-1584515933487-779824d29309?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'Healthcare English'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Describe Symptoms Clearly'
  );

INSERT INTO lessons (topic_id, title, type, image_url, parent_id, created_by)
SELECT t.id, 'Daily Standup Updates', 'VOCABULARY', 'https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=1200', NULL, 1
FROM topics t
WHERE t.name = 'Tech Workplace English'
  AND NOT EXISTS (
      SELECT 1 FROM lessons l WHERE l.topic_id = t.id AND l.title = 'Daily Standup Updates'
  );

-- 3) Seed 5 new scenarios
INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Airport Counter Conversation',
       'Practice passport, baggage, and boarding pass communication.',
       'Airline Staff',
       'Passenger',
       'Confirm flight details and answer check-in questions confidently.',
       'Good morning. May I have your passport, please?',
       'Use simple past and present tense. Keep answers short and clear.',
       'Ban dang check-in tai san bay va tra loi nhan vien hang khong.',
       1
FROM topics t
JOIN lessons l ON l.topic_id = t.id AND l.title = 'Airport Check-in Practice'
WHERE t.name = 'Travel Survival English'
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id AND s.title = 'Airport Counter Conversation'
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Interview Self-Introduction',
       'Answer the opening interview question naturally and professionally.',
       'Interviewer',
       'Candidate',
       'Introduce your background, key strengths, and motivation for the role.',
       'Could you briefly introduce yourself?',
       'Follow structure: present role, past experience, future goal.',
       'Ban dang tra loi cau hoi mo dau trong phong van xin viec.',
       1
FROM topics t
JOIN lessons l ON l.topic_id = t.id AND l.title = 'Tell Me About Yourself'
WHERE t.name = 'Job Interview English'
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id AND s.title = 'Interview Self-Introduction'
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Refund Request Call',
       'Handle a refund complaint while keeping the customer calm.',
       'Customer',
       'Support Agent',
       'Acknowledge issue, apologize, and present a clear next step.',
       'I am really unhappy with this service and want a refund.',
       'Use empathy phrases: I understand, I am sorry, Let me help you.',
       'Ban dong vai nhan vien ho tro va xu ly yeu cau hoan tien.',
       1
FROM topics t
JOIN lessons l ON l.topic_id = t.id AND l.title = 'Handling Angry Customers'
WHERE t.name = 'Customer Support English'
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id AND s.title = 'Refund Request Call'
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Clinic Visit Symptoms',
       'Describe pain level, duration, and related symptoms to a doctor.',
       'Doctor',
       'Patient',
       'Explain symptoms clearly and answer follow-up medical questions.',
       'Can you describe your symptoms in detail?',
       'Mention when it started, where it hurts, and how severe it is.',
       'Ban di kham benh va can mo ta trieu chung ro rang cho bac si.',
       1
FROM topics t
JOIN lessons l ON l.topic_id = t.id AND l.title = 'Describe Symptoms Clearly'
WHERE t.name = 'Healthcare English'
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id AND s.title = 'Clinic Visit Symptoms'
  );

INSERT INTO scenarios (topic_id, lesson_id, title, description, ai_role, user_role, tasks, openning_message, suggestion, translation, created_by)
SELECT t.id, l.id,
       'Sprint Standup Update',
       'Give yesterday progress, today plan, and blockers in a standup.',
       'Scrum Master',
       'Developer',
       'Report concise status update with one blocker and expected resolution.',
       'Please share your standup update for today.',
       'Use format: Yesterday I..., Today I..., Blocker is...',
       'Ban cap nhat cong viec trong buoi daily standup cua team tech.',
       1
FROM topics t
JOIN lessons l ON l.topic_id = t.id AND l.title = 'Daily Standup Updates'
WHERE t.name = 'Tech Workplace English'
  AND NOT EXISTS (
      SELECT 1 FROM scenarios s WHERE s.lesson_id = l.id AND s.title = 'Sprint Standup Update'
  );
