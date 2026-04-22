-- liquibase formatted sql

-- changeset thanhnn:add-user-device-tokens-table-for-push-notifications
create table if not exists user_device_tokens
(
    id           int auto_increment primary key,
    user_id      int          not null,
    fcm_token    varchar(512) not null,
    platform     varchar(30)  not null default 'ANDROID',
    device_id    varchar(255) null,
    app_version  varchar(50)  null,
    is_active    boolean      not null default true,
    last_seen_at timestamp    null,
    created_at   timestamp    null,
    created_by   int          null,
    updated_at   timestamp    null,
    updated_by   int          null,
    constraint uk_user_device_tokens_fcm_token unique (fcm_token),
    constraint user_device_tokens_users_id_fk
        foreign key (user_id) references users (id)
);

create index idx_user_device_tokens_user_active
    on user_device_tokens (user_id, is_active);

create index idx_user_device_tokens_last_seen
    on user_device_tokens (last_seen_at);
