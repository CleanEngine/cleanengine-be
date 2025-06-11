INSERT INTO asset(ticker, name) VALUES ('BTC', '비트코인');

INSERT INTO users (user_id, created_at) VALUES (1, '2025-05-16 09:30:00.000000');
INSERT INTO users (user_id, created_at) VALUES (2, '2025-05-16 09:30:00.000000');

INSERT INTO account (account_id, cash, user_id) VALUES (1, 0, 1);
INSERT INTO account (account_id, cash, user_id) VALUES (2, 500000000, 2);

INSERT INTO wallet (wallet_id, account_id, buy_price, roi, size, ticker) VALUES (1, 1, 0, 0, 500000000, 'BTC');
INSERT INTO wallet (wallet_id, account_id, buy_price, roi, size, ticker) VALUES (2, 1, 0, 0, 500000000, 'TRUMP');
