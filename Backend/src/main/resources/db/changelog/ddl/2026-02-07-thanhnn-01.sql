-- liquibase formatted sql

-- changeset thanhnn:init-schemas
create table if not exists users
(
    id             int auto_increment
        primary key,
    username       varchar(20)  not null,
    password       varchar(255) not null,
    full_name      varchar(50)  not null,
    email          varchar(50)  not null,
    provider varchar(50)  not null,
    provider_id    varchar(100) null,
    last_logout_at timestamp    null,
    role           varchar(50)  not null,
    enabled        boolean      default true not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null
);

create table if not exists user_speaking_duration
(
    id                      int auto_increment
        primary key,
    user_id                 int  not null,
    date                    date not null comment 'Ngay ghi nhan',
    speaking_count          int  not null comment 'Tong so lan speaking',
    valid_speaking_count    int  not null comment 'Tong so lan speaking hop ly',
    valid_speaking_duration int  null comment 'Thoi luong speaking hop le',
    speaking_duration       int  null comment 'Tong thoi luong speaking',
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint user_speaking_duration_users_id_fk
        foreign key (user_id) references users (id)
);

create table if not exists user_preliminary_evaluation
(
    id       int auto_increment
        primary key,
    user_id  int          not null,
    language varchar(50)  not null,
    level    varchar(20)  not null,
    purpose  varchar(255) not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint user_preliminary_evaluation_users_id_fk
        foreign key (user_id) references users (id)
)
    comment 'Danh gia so bo nguoi dung';


create table if not exists user_streak
(
    id                  int auto_increment
        primary key,
    user_id             int         not null,
    current_streak      int         not null,
    last_streak_updated timestamp   null,
    longest_streak      int         not null,
    status              varchar(50) not null comment 'Da bat dau streak hay chua',
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint user_streak_users_id_fk
        foreign key (user_id) references users (id)
);

create table if not exists topics
(
    id          int auto_increment
        primary key,
    name        varchar(255) not null,
    description varchar(500) not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null
);

create table if not exists lessons
(
    id        int auto_increment
        primary key,
    topic_id  int          not null,
    title     varchar(255) not null,
    type      varchar(255) not null,
    parent_id int          null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint lessons_lessons_id_fk
        foreign key (parent_id) references lessons (id),
    constraint lessons_topics_id_fk
        foreign key (topic_id) references topics (id)
);

create table if not exists questions
(
    id        int auto_increment
        primary key,
    lesson_id int          not null,
    content   varchar(255) not null,
    type      varchar(50)  not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint questions_lessons_id_fk
        foreign key (lesson_id) references lessons (id)
);

create table if not exists answers
(
    id          int auto_increment
        primary key,
    question_id int          not null,
    content     varchar(255) not null,
    is_correct  boolean      not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint answers_questions_id_fk
        foreign key (question_id) references questions (id)
);

create table if not exists scenarios
(
    id          int auto_increment
        primary key,
    topic_id    int          not null,
    title       varchar(255) not null,
    description varchar(500) not null,
    ai_role     varchar(50)  not null,
    user_role   varchar(50)  not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint scenarios_topics_id_fk
        foreign key (topic_id) references topics (id)
);

create table if not exists media
(
    id   int auto_increment
        primary key,
    type varchar(50)   not null,
    url  varchar(1000) not null,
    size int           not null,
    name varchar(255)  not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null
);

create table if not exists dictionary_words
(
    id            int auto_increment
        primary key,
    word          varchar(50)  not null,
    pronunciation varchar(50)  not null,
    meaning       varchar(255) not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null
);


create table if not exists flash_cards
(
    id                 int auto_increment
        primary key,
    dictionary_word_id int          not null,
    media_id           int          not null,
    example            varchar(255) not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint flash_cards_dictionary_words_id_fk
        foreign key (dictionary_word_id) references dictionary_words (id),
    constraint flash_cards_media_id_fk
        foreign key (media_id) references media (id)
);

create table if not exists user_vocabulary_collections
(
    id         int auto_increment
        primary key,
    user_id    int 								   not null,
    name       varchar(255)                        not null,
    color      varchar(50)                         not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint user_vocabulary_collections_users_id_fk
        foreign key (user_id) references users (id),
    constraint user_vocabulary_collections_pk_2
        unique (name)
);

create table if not exists vocabulary_collection_words
(
    id                       int auto_increment
        primary key,
    vocabulary_collection_id int       not null,
    is_practicing            boolean   not null,
    last_practice_at         timestamp null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint vocabulary_collection_words__id_fk
        foreign key (vocabulary_collection_id) references `user_vocabulary_collections`(id)
);

create table if not exists speaking_sessions
(
    id                  int auto_increment
        primary key,
    user_id             int       not null,
    scenario_id         int       not null,
    started_at          timestamp not null,
    ended_at            timestamp not null,
    grammar_score       double    not null,
    vocabulary_score    double    not null,
    pronunciation_score double    not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint speaking_sessions_scenarios_id_fk
        foreign key (scenario_id) references scenarios (id),
    constraint speaking_sessions_users_id_fk
        foreign key (user_id) references users (id)
);

create table if not exists speaking_messages
(
    id                  int auto_increment
        primary key,
    speaking_session_id int          not null,
    media_id			int 		 not null,
    sender              varchar(50)  not null comment 'AI / User',
    content             varchar(255) not null,
    duration            int          not null,
    grammar_score       double       not null,
    vocabulary_score    double       not null,
    pronunciation_score double          not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint speaking_messages_media_id_fk
        foreign key (media_id) references media (id),
    constraint speaking_messages_speaking_sessions_id_fk
        foreign key (speaking_session_id) references speaking_sessions (id)
);

create table if not exists user_lesson_results
(
    id         int auto_increment
        primary key,
    user_id    int       not null,
    lesson_id  int       not null,
    started_at timestamp not null,
    duration   int       not null,
    score      double    not null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint user_lesson_results_lessons_id_fk
        foreign key (lesson_id) references lessons (id),
    constraint user_lesson_results_users_id_fk
        foreign key (user_id) references users (id)
);

create table if not exists user_question_answers
(
    id               int auto_increment
        primary key,
    lesson_result_id int          not null,
    question_id      int          not null,
    answer_id        int          null,
    media_id         int          null,
    content          varchar(255) null,
    created_at timestamp default current_timestamp not null,
    created_by int                                 not null,
    updated_at timestamp                           null,
    updated_by int                                 null,
    constraint user_question_answers_answers_id_fk
        foreign key (answer_id) references answers (id),
    constraint user_question_answers_media_id_fk
        foreign key (media_id) references media (id),
    constraint user_question_answers_questions_id_fk
        foreign key (question_id) references questions (id),
    constraint user_question_answers_user_lesson_results_id_fk
        foreign key (lesson_result_id) references user_lesson_results (id)
);