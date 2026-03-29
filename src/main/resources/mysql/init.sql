create database if not exists pdf_helper;

create table user_chat
(
    user_name   varchar(15)                        not null
        primary key,
    chat_id     varchar(255)                       null,
    status      tinyint  default 1                 not null,
    create_time datetime default CURRENT_TIMESTAMP not null
);