INSERT INTO `if`.users (user_id, created_at)
VALUES (1, '2025-05-16 09:30:00.000000'),
       (2, '2025-05-16 09:30:00.000000');

INSERT INTO `if`.account (account_id, cash, user_id)
VALUES (1, 0, 1),
       (2, 500000000, 2);

INSERT INTO `if`.asset (ticker, name)
VALUES ('BTC', '비트코인'),
       ('TRUMP', '오피셜트럼프');

INSERT INTO `if`.wallet (wallet_id, account_id, buy_price, roi, size, ticker)
VALUES (1, 1, 0, 0, 500000000, 'BTC'),
       (2, 1, 0, 0, 500000000, 'TRUMP');
