INSERT INTO users(user_id, created_at) VALUES (1, now());
INSERT INTO account(account_id, user_id, cash) VALUES (1, 1, 200000);
INSERT INTO wallet(wallet_id, ticker, account_id, size, buy_price, roi) VALUES (1, 'BTC', 1, 200000, 10000, 0.1);