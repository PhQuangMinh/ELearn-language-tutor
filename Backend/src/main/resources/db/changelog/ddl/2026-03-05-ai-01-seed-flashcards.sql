-- liquibase formatted sql

-- changeset ai:seed-flashcards-01 splitStatements:true

-- Seed sample data for flashcards (dictionary_words + media + flash_cards)
-- Note: Inserts are idempotent (won't duplicate on re-run).

-- dictionary_words
insert into dictionary_words (word, pronunciation, meaning, created_by)
select 'apple', '/ˈæp.əl/', 'quả táo', 1
where not exists (select 1 from dictionary_words where word = 'apple');

insert into dictionary_words (word, pronunciation, meaning, created_by)
select 'book', '/bʊk/', 'quyển sách', 1
where not exists (select 1 from dictionary_words where word = 'book');

insert into dictionary_words (word, pronunciation, meaning, created_by)
select 'travel', '/ˈtræv.əl/', 'du lịch', 1
where not exists (select 1 from dictionary_words where word = 'travel');

insert into dictionary_words (word, pronunciation, meaning, created_by)
select 'practice', '/ˈpræk.tɪs/', 'luyện tập', 1
where not exists (select 1 from dictionary_words where word = 'practice');

insert into dictionary_words (word, pronunciation, meaning, created_by)
select 'listen', '/ˈlɪs.ən/', 'lắng nghe', 1
where not exists (select 1 from dictionary_words where word = 'listen');

insert into dictionary_words (word, pronunciation, meaning, created_by)
select 'improve', '/ɪmˈpruːv/', 'cải thiện', 1
where not exists (select 1 from dictionary_words where word = 'improve');

-- media (images)
insert into media (type, url, size, name, created_by)
select 'IMAGE', 'https://placehold.co/600x400?text=apple', 0, 'apple.png', 1
where not exists (select 1 from media where url = 'https://placehold.co/600x400?text=apple');

insert into media (type, url, size, name, created_by)
select 'IMAGE', 'https://placehold.co/600x400?text=book', 0, 'book.png', 1
where not exists (select 1 from media where url = 'https://placehold.co/600x400?text=book');

insert into media (type, url, size, name, created_by)
select 'IMAGE', 'https://placehold.co/600x400?text=travel', 0, 'travel.png', 1
where not exists (select 1 from media where url = 'https://placehold.co/600x400?text=travel');

insert into media (type, url, size, name, created_by)
select 'IMAGE', 'https://placehold.co/600x400?text=practice', 0, 'practice.png', 1
where not exists (select 1 from media where url = 'https://placehold.co/600x400?text=practice');

insert into media (type, url, size, name, created_by)
select 'IMAGE', 'https://placehold.co/600x400?text=listen', 0, 'listen.png', 1
where not exists (select 1 from media where url = 'https://placehold.co/600x400?text=listen');

insert into media (type, url, size, name, created_by)
select 'IMAGE', 'https://placehold.co/600x400?text=improve', 0, 'improve.png', 1
where not exists (select 1 from media where url = 'https://placehold.co/600x400?text=improve');

-- flash_cards (linking dictionary_words + media)
insert into flash_cards (dictionary_word_id, media_id, example, created_by)
select dw.id, m.id, 'I ate an apple after lunch.', 1
from dictionary_words dw, media m
where dw.word = 'apple'
  and m.url = 'https://placehold.co/600x400?text=apple'
  and not exists (
    select 1 from flash_cards fc
    where fc.dictionary_word_id = dw.id and fc.media_id = m.id
  );

insert into flash_cards (dictionary_word_id, media_id, example, created_by)
select dw.id, m.id, 'This book is easy to understand.', 1
from dictionary_words dw, media m
where dw.word = 'book'
  and m.url = 'https://placehold.co/600x400?text=book'
  and not exists (
    select 1 from flash_cards fc
    where fc.dictionary_word_id = dw.id and fc.media_id = m.id
  );

insert into flash_cards (dictionary_word_id, media_id, example, created_by)
select dw.id, m.id, 'I want to travel to Japan next year.', 1
from dictionary_words dw, media m
where dw.word = 'travel'
  and m.url = 'https://placehold.co/600x400?text=travel'
  and not exists (
    select 1 from flash_cards fc
    where fc.dictionary_word_id = dw.id and fc.media_id = m.id
  );

insert into flash_cards (dictionary_word_id, media_id, example, created_by)
select dw.id, m.id, 'You should practice speaking every day.', 1
from dictionary_words dw, media m
where dw.word = 'practice'
  and m.url = 'https://placehold.co/600x400?text=practice'
  and not exists (
    select 1 from flash_cards fc
    where fc.dictionary_word_id = dw.id and fc.media_id = m.id
  );

insert into flash_cards (dictionary_word_id, media_id, example, created_by)
select dw.id, m.id, 'Listen carefully to the pronunciation.', 1
from dictionary_words dw, media m
where dw.word = 'listen'
  and m.url = 'https://placehold.co/600x400?text=listen'
  and not exists (
    select 1 from flash_cards fc
    where fc.dictionary_word_id = dw.id and fc.media_id = m.id
  );

insert into flash_cards (dictionary_word_id, media_id, example, created_by)
select dw.id, m.id, 'Reading daily can improve your vocabulary.', 1
from dictionary_words dw, media m
where dw.word = 'improve'
  and m.url = 'https://placehold.co/600x400?text=improve'
  and not exists (
    select 1 from flash_cards fc
    where fc.dictionary_word_id = dw.id and fc.media_id = m.id
  );
