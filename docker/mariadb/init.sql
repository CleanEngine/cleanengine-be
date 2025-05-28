create table account
(
    account_id int auto_increment
        primary key,
    cash       double not null,
    user_id    int    not null
);

create table asset
(
    ticker varchar(10)  not null
        primary key,
    name   varchar(100) null,
    icon blob null
);

create table buy_orders
(
    buy_order_id      bigint auto_increment
        primary key,
    created_at        datetime(6)                       not null,
    is_bot            bit                               not null,
    is_marketorder    bit                               not null,
    order_size        double                            null,
    price             double                            null,
    remaining_size    double                            null,
    state             enum ('CANCELED', 'DONE', 'WAIT') not null,
    ticker            varchar(10)                       not null,
    user_id           int                               not null,
    locked_deposit    double                            not null,
    remaining_deposit double                            not null
);

create table oauth
(
    oauth_id         int auto_increment
        primary key,
    access_token     text         null,
    email            varchar(255) null,
    expires_at       datetime(6)  null,
    nickname         varchar(255) null,
    provider         varchar(20)  not null,
    provider_user_id varchar(255) not null,
    refresh_token    text         null,
    scope            varchar(255) null,
    user_id          int          not null
);

create table sell_orders
(
    sell_order_id  bigint auto_increment
        primary key,
    created_at     datetime(6)                       not null,
    is_bot         bit                               not null,
    is_marketorder bit                               not null,
    order_size     double                            null,
    price          double                            null,
    remaining_size double                            null,
    state          enum ('CANCELED', 'DONE', 'WAIT') not null,
    ticker         varchar(10)                       not null,
    user_id        int                               not null
);

create table trade
(
    trade_id     int auto_increment
        primary key,
    buy_user_id  int          not null,
    price        double       not null,
    sell_user_id int          not null,
    size         double       not null,
    ticker       varchar(255) not null,
    trade_time   datetime(6)  not null
);

create table users
(
    user_id    int auto_increment
        primary key,
    created_at datetime(6) not null
);

create table wallet
(
    wallet_id  bigint auto_increment
        primary key,
    account_id int         not null,
    buy_price  double      null,
    roi        double      null,
    size       double      not null,
    ticker     varchar(10) not null
);



INSERT INTO `if`.users (user_id, created_at) VALUES (1, '2025-05-16 09:30:00.000000');
INSERT INTO `if`.users (user_id, created_at) VALUES (2, '2025-05-16 09:30:00.000000');

INSERT INTO `if`.account (account_id, cash, user_id) VALUES (1, 0, 1);
INSERT INTO `if`.account (account_id, cash, user_id) VALUES (2, 500000000, 2);

INSERT INTO `if`.asset (ticker, name) VALUES ('BTC', '비트코인');
INSERT INTO `if`.asset (ticker, name) VALUES ('TRUMP', '오피셜트럼프');

INSERT INTO `if`.wallet (wallet_id, account_id, buy_price, roi, size, ticker) VALUES (1, 1, 0, 0, 500000000, 'BTC');
INSERT INTO `if`.wallet (wallet_id, account_id, buy_price, roi, size, ticker) VALUES (2, 1, 0, 0, 500000000, 'TRUMP');
