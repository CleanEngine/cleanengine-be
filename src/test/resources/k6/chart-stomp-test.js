import ws from 'k6/ws';
import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Counter, Rate, Gauge } from 'k6/metrics';

// --- 상세한 메트릭 정의 (기존과 동일) ---
const connectionDuration = new Trend('stomp_connection_duration', true);
const subscriptionDuration = new Trend('stomp_subscription_duration', true);
const messageLatency = new Trend('stomp_message_latency', true);
const messagesReceived = new Counter('stomp_messages_received');
const messagesPerSecond = new Rate('stomp_messages_per_second');
const connectionSuccess = new Rate('stomp_connection_success');
const subscriptionSuccess = new Rate('stomp_subscription_success');
const activeConnections = new Gauge('stomp_active_connections');
const errorCount = new Counter('stomp_errors');
const tradeEventsTriggered = new Counter('trade_events_triggered');

// --- 태그를 활용한 메트릭 (기존과 동일) ---
const realTimeMessages = new Counter('stomp_realtime_messages');
const prevRateMessages = new Counter('stomp_prevrate_messages');

// --- 테스트 옵션 (기존과 동일) ---
export let options = {
    stages: [
        { duration: '30s', target: 1000 },
        // { duration: '1m', target: 1000 },
        // { duration: '5m', target: 10000 },
        // { duration: '2m', target: 50 },
        // { duration: '30s', target: 0 },
    ],
    thresholds: {
        'stomp_connection_success': ['rate>0.95'],
        'stomp_subscription_success': ['rate>0.95'],
        'stomp_message_latency': ['p(95)<1000', 'p(99)<2000'],
        'stomp_connection_duration': ['p(95)<5000'],
    },
    tags: {
        testType: 'stomp-dual-websocket', // 테스트 타입 이름 변경
        service: 'chart-service',
    },
};

// --- 기본 설정 ---
const TICKERS = ['BTC', 'TRUMP', 'ETC'];
const BASE_URL = '172.16.24.113:8080';
const MIN_ENDPOINT_URL = `ws://${BASE_URL}/api/coin/min`;
const PREV_ENDPOINT_URL = `ws://${BASE_URL}/api/coin/prev`;

// --- 테스트용 거래 이벤트 발생 함수 (기존과 동일, 에러 카운트 추가) ---
function triggerTradeEvent(ticker, vuId) {
    const tradeData = {
        ticker: ticker,
        size: Math.random() * 2 + 0.1,
        price: Math.random() * 10000 + 40000,
        timestamp: new Date().toISOString()
    };

    try {
        const response = http.post(
            `http://${BASE_URL}/api/test/trigger-trade/${ticker}`,
            JSON.stringify(tradeData),
            {
                headers: { 'Content-Type': 'application/json', 'User-Agent': `k6-vu-${vuId}` },
                timeout: '5s'
            }
        );

        if (response.status === 200) {
            tradeEventsTriggered.add(1, { ticker: ticker });
            console.log(`VU ${vuId}: 거래 이벤트 발생 성공 - ${ticker}`);
            return true;
        } else {
            console.error(`VU ${vuId}: 거래 이벤트 발생 실패 - ${response.status}`);
            errorCount.add(1, { error: 'trade_event_failed', ticker: ticker });
            return false;
        }
    } catch (error) {
        console.error(`VU ${vuId}: 거래 이벤트 API 호출 오류 - ${error}`);
        return false;
    }
}

// --- 메인 테스트 로직 ---
export default function () {
    const ticker = TICKERS[Math.floor(Math.random() * TICKERS.length)];
    const vuId = __VU;
    const iterationId = __ITER;

    // 그룹 1: 실시간 데이터 엔드포인트 (/api/coin/min)
    group('Realtime Endpoint Test', function () {
        const res = ws.connect(MIN_ENDPOINT_URL, { tags: { endpoint: 'min' } }, function (socket) {
            let connected = false;
            const connectionStartTime = Date.now();
            activeConnections.add(1, { endpoint: 'min' });

            socket.on('open', () => {
                const connectFrame = `CONNECT\naccept-version:1.0,1.1,2.0\nheart-beat:10000,10000\nuser-id:vu-${vuId}\n\n\x00`;
                socket.send(connectFrame);
            });

            socket.on('message', (data) => {
                const timestamp = Date.now();
                if (data.startsWith('CONNECTED')) {
                    connected = true;
                    connectionDuration.add(timestamp - connectionStartTime, { endpoint: 'min' });
                    connectionSuccess.add(1, { endpoint: 'min' });
                    console.log(`VU ${vuId} [MIN]: STOMP 연결 완료`);

                    // 실시간 체결가 토픽 구독
                    const topic = `/topic/realTimeTradeRate/${ticker}`;
                    const subId = `sub-realtime-${vuId}-${iterationId}`;
                    socket.send(`SUBSCRIBE\nid:${subId}\ndestination:${topic}\n\n\x00`);
                    socket.send(`SEND\ndestination:/app/subscribe/realTimeTradeRate/${ticker}\n\n\x00`);
                    subscriptionSuccess.add(1, { endpoint: 'min' });

                    // 연결 및 구독 완료 후, 주기적으로 거래 이벤트를 발생시켜 데이터 흐름을 만듦
                    const tradeInterval = setInterval(() => {
                        if (connected) triggerTradeEvent(ticker, vuId);
                        else clearInterval(tradeInterval);
                    }, 5000);

                    setTimeout(() => clearInterval(tradeInterval), 25000); // 25초 후 중단

                } else if (data.startsWith('MESSAGE')) {
                    messagesReceived.add(1, { endpoint: 'min' });
                    messagesPerSecond.add(1, { endpoint: 'min' });
                    realTimeMessages.add(1, { ticker: ticker });

                    // 메시지 레이턴시 측정
                    try {
                        const body = data.substring(data.indexOf('\n\n') + 2).replace('\x00', '');
                        const latency = timestamp - new Date(JSON.parse(body).timestamp).getTime();
                        messageLatency.add(latency, { endpoint: 'min', ticker: ticker });
                    } catch (e) { errorCount.add(1, { error: 'json_parse_min' }); }
                }
            });

            socket.on('close', () => activeConnections.add(-1, { endpoint: 'min' }));
            socket.on('error', (e) => {
                errorCount.add(1, { error: 'ws_error_min' });
                connectionSuccess.add(0, { endpoint: 'min' });
            });

            setTimeout(() => socket.close(), 28000); // 28초 후 연결 종료
        });
        check(res, { '[MIN] WebSocket 연결 성공 (101)': (r) => r && r.status === 101 });
    });

    // 그룹 2: 전일 대비 데이터 엔드포인트 (/api/coin/prev)
    group('PrevRate Endpoint Test', function () {
        const res = ws.connect(PREV_ENDPOINT_URL, { tags: { endpoint: 'prev' } }, function (socket) {
            let connected = false;
            const connectionStartTime = Date.now();
            activeConnections.add(1, { endpoint: 'prev' });

            socket.on('open', () => {
                const connectFrame = `CONNECT\naccept-version:1.0,1.1,2.0\nheart-beat:10000,10000\nuser-id:vu-${vuId}\n\n\x00`;
                socket.send(connectFrame);
            });

            socket.on('message', (data) => {
                const timestamp = Date.now();
                if (data.startsWith('CONNECTED')) {
                    connected = true;
                    connectionDuration.add(timestamp - connectionStartTime, { endpoint: 'prev' });
                    connectionSuccess.add(1, { endpoint: 'prev' });
                    console.log(`VU ${vuId} [PREV]: STOMP 연결 완료`);

                    // 전일 대비 토픽 구독
                    const topic = `/topic/prevRate/${ticker}`;
                    const subId = `sub-prevrate-${vuId}-${iterationId}`;
                    socket.send(`SUBSCRIBE\nid:${subId}\ndestination:${topic}\n\n\x00`);
                    socket.send(`SEND\ndestination:/app/subscribe/realTimePrevRate/${ticker}\n\n\x00`);
                    subscriptionSuccess.add(1, { endpoint: 'prev' });

                } else if (data.startsWith('MESSAGE')) {
                    messagesReceived.add(1, { endpoint: 'prev' });
                    messagesPerSecond.add(1, { endpoint: 'prev' });
                    prevRateMessages.add(1, { ticker: ticker });

                    // 메시지 레이턴시 측정
                    try {
                        const body = data.substring(data.indexOf('\n\n') + 2).replace('\x00', '');
                        const latency = timestamp - new Date(JSON.parse(body).timestamp).getTime();
                        messageLatency.add(latency, { endpoint: 'prev', ticker: ticker });
                    } catch (e) { errorCount.add(1, { error: 'json_parse_prev' }); }
                }
            });

            socket.on('close', () => activeConnections.add(-1, { endpoint: 'prev' }));
            socket.on('error', (e) => {
                errorCount.add(1, { error: 'ws_error_prev' });
                connectionSuccess.add(0, { endpoint: 'prev' });
            });

            setTimeout(() => socket.close(), 28000); // 28초 후 연결 종료
        });
        check(res, { '[PREV] WebSocket 연결 성공 (101)': (r) => r && r.status === 101 });
    });

    sleep(30); // VU가 두 WebSocket 세션을 유지하도록 충분히 대기
}