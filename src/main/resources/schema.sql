create table country
(
    country_id bigint auto_increment primary key,
    name       varchar(255) null
);

create table language
(
    language_id bigint auto_increment primary key,
    abbr        varchar(255) null,
    name        varchar(255) null
);

create table users
(
    user_id       bigint auto_increment primary key,
    content       varchar(2000) null,
    email         varchar(255)  not null,
    facebook_url  varchar(255)  null,
    image_url     varchar(1000) null,
    instagram_url varchar(255)  null,
    name          varchar(255)  not null,
    password      varchar(255)  not null,
    reg_date      DATETIME default CURRENT_TIMESTAMP,
    role          varchar(255)  not null,
    school        varchar(255)  not null,
    views         bigint        not null,
    web_email     varchar(255)  not null,
    rep_country       varchar(255) null,
    rep_language      varchar(255) null,
    rep_hope_language varchar(255) null,
    unique (email)
);

create table follow
(
    follow_id bigint auto_increment primary key,
    reg_date  DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_from bigint      null,
    user_to   bigint      null,
    foreign key (user_to) references users (user_id),
    foreign key (user_from) references users (user_id)
);

create table views
(
    view_id   bigint auto_increment primary key,
    user_from bigint,
    user_to   bigint,
    foreign key (user_to) references users (user_id),
    foreign key (user_from) references users (user_id)
);



create table user_country
(
    user_country_id bigint auto_increment primary key,
    country_id      bigint null,
    user_id         bigint null,
    foreign key (country_id) references country (country_id),
    foreign key (user_id) references users (user_id)
);

create table user_language
(
    user_language_id bigint auto_increment primary key,
    language_id      bigint null,
    user_id          bigint null,
    foreign key (user_id) references users (user_id),
    foreign key (language_id) references language (language_id)
);

create table user_hope_language
(
    user_hope_language_id bigint auto_increment primary key,
    language_id           bigint null,
    user_id               bigint null,
    foreign key (user_id) references users (user_id),
    foreign key (language_id) references language (language_id)
);



