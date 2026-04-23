-- liquibase formatted sql

-- changeset ai:add-auth-tables-01
create table if not exists refresh_tokens
(
    id          int auto_increment
        primary key,
    token       varchar(500)                         not null,
    user_id     int                                  not null,
    expiry_date timestamp                            not null,
    created_at  timestamp default current_timestamp   not null,
    revoked     boolean   default false              not null,
    constraint refresh_tokens_token_uq
        unique (token),
    constraint refresh_tokens_users_id_fk
        foreign key (user_id) references users (id)
);

-- changeset ai:add-auth-tables-02
create table if not exists blacklisted_tokens
(
    id         int auto_increment
        primary key,
    token      varchar(500)                         not null,
    expires_at timestamp                            not null,
    created_at timestamp default current_timestamp   not null,
    constraint blacklisted_tokens_token_uq
        unique (token)
);
