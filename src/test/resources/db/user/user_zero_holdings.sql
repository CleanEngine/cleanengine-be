INSERT INTO users(user_id, created_at) VALUES (3, now());
INSERT INTO account(account_id, user_id, cash) VALUES (3, 3, 0);
INSERT INTO wallet(wallet_id, ticker, account_id, size, buy_price, roi) VALUES (5, 'BTC', 3, 0, 10000, 0.1);