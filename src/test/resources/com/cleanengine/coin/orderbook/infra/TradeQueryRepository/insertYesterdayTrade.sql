DELETE FROM trade;

INSERT INTO trade
    (trade_id, ticker, trade_time, buy_user_id, sell_user_id, price, size)
VALUES
    (1, 'BTC', '2025-07-01 19:00:00.000000',1, 2, 300, 30),
    (2, 'BTC', '2025-07-01 20:00:00.000000',1, 2, 400, 30),
    (3, 'BTC', '2025-07-01 17:00:00.000000',1, 2, 500, 30),
    (4, 'BTC', '2025-07-01 18:00:00.000000',1, 2, 600, 30);