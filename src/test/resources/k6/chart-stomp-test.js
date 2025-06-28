import ws from 'k6/ws';
import http from 'k6/http';
import { Trend, Counter, Rate, Gauge } from 'k6/metrics';
import { sleep } from 'k6';

// --- 메트릭 ---
const connectionDuration = new Trend('stomp_connection_duration', true);
const subscriptionDuration = new Trend('stomp_subscription_duration', true);
const messageLatency = new Trend('stomp_message_latency', true);
const messagesReceived = new Counter('stomp_messages_received');
const connectionSuccess = new Rate('stomp_connection_success');
const subscriptionSuccess = new Rate('stomp_subscription_success');
const activeConnections = new Gauge('stomp_active_connections');
const errorCount = new Counter('stomp_errors');

// --- 옵션 ---
export let options = {
    stages: [
        { duration: '30s', target: 5000 },
    ],
    thresholds: {
        'stomp_connection_success': ['rate>0.99'],
        'stomp_subscription_success': ['rate>0.95'],
        'stomp_message_latency{endpoint:min}': ['p(95)<2000'],
        'stomp_message_latency{endpoint:prev}': ['p(95)<3000'],
    },
};

const TICKERS = ['BTC', 'TRUMP', 'ETC'];
const HOST = '172.16.24.113:8080';
const ORIGIN = `http://${HOST}`;
const ENDPOINTS = {
    min: `ws://${HOST}/api/coin/min`,
    prev: `ws://${HOST}/api/coin/prev`,
};

export default function () {
    const vu = __VU;
    const endpointTag = vu % 2 === 0 ? 'min' : 'prev';
    const url = ENDPOINTS[endpointTag];
    const ticker = TICKERS[vu % TICKERS.length];

    const res = ws.connect(
        url,
        { headers: { Origin: ORIGIN } },
        socket => {
            const start = Date.now();
            activeConnections.add(1, { endpoint: endpointTag });

            socket.on('open', () => {
                socket.send(
                    'CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\x00'
                );
            });

            socket.on('message', data => {
                const now = Date.now();
                if (data.startsWith('CONNECTED')) {
                    connectionDuration.add(now - start, { endpoint: endpointTag });
                    connectionSuccess.add(1, { endpoint: endpointTag }
                    // 즉시 구독
                    socket.send(
                        `SUBSCRIBE\nid:sub-${endpointTag}\ndestination:/topic/${endpointTag === 'min' ? 'realTimeTradeRate' : 'prevRate'}/${ticker}\n\n\x00`
                    );
                    subscriptionDuration.add(0, { endpoint: endpointTag });
                    subscriptionSuccess.add(1, { endpoint: endpointTag });

                    // 첫 메시지 요청
                    socket.send(
                        `SEND\ndestination:/app/subscribe/${endpointTag === 'min' ? 'realTimeTradeRate' : 'prevRate'}/${ticker}\n\n\x00`
                    );
                } else if (data.startsWith('MESSAGE')) {
                    messagesReceived.add(1, { endpoint: endpointTag });
                    const body = data.slice(data.indexOf('\n\n') + 2).replace('\x00', '');
                    let timestamp;
                    try {
                        timestamp = new Date(JSON.parse(body).timestamp).getTime();
                    } catch (e) {
                        errorCount.add(
                            1,
                            Object.assign({}, { endpoint: endpointTag }, { error: 'json_parse' })
                        );
                        return;
                    }
                    const latency = now - timestamp;
                    messageLatency.add(latency, { endpoint: endpointTag });
                }
            });

            socket.on('error', e => {
                errorCount.add(
                    1,
                    Object.assign({}, { endpoint: endpointTag }, { error: e.message })
                );
                connectionSuccess.add(0, { endpoint: endpointTag });
            });

            // 60초 후 소켓 종료
            socket.setTimeout(() => {
                socket.close();
                activeConnections.add(-1, { endpoint: endpointTag });
            }, 60 * 1000);
        }
    );

    // 연결 성공 체크
    connectionSuccess.add(res && res.status === 101 ? 1 : 0, { endpoint: endpointTag });

    // 충분히 대기
    sleep(65);
}
//단일 웹소켓 테스트
// import ws from 'k6/ws';
// import http from 'k6/http';
// import { Trend, Counter, Rate, Gauge } from 'k6/metrics';
// import { sleep } from 'k6';
//
// // --- 메트릭 ---
// const connectionDuration = new Trend('stomp_connection_duration', true);
// const subscriptionDuration = new Trend('stomp_subscription_duration', true);
// const messageLatency = new Trend('stomp_message_latency', true);
// const messagesReceived = new Counter('stomp_messages_received');
// const connectionSuccess = new Rate('stomp_connection_success');
// const subscriptionSuccess = new Rate('stomp_subscription_success');
// const activeConnections = new Gauge('stomp_active_connections');
// const errorCount = new Counter('stomp_errors');
//
// // --- 옵션 ---
// export let options = {
//     stages: [
//         { duration: '2m', target: 20000 },
//     ],
//     thresholds: {
//         'stomp_connection_success': ['rate>0.99'],
//         'stomp_subscription_success': ['rate>0.95'],
//         'stomp_message_latency{endpoint:min}': ['p(95)<2000'],
//         'stomp_message_latency{endpoint:prev}': ['p(95)<3000'],
//     },
// };
//
// const TICKERS = ['BTC', 'TRUMP', 'ETC'];
// const HOST = '172.16.24.113:8080';
// const ORIGIN = `http://${HOST}`;
// const ENDPOINTS = {
//     min: `ws://${HOST}/api/coin/min`,
//     prev: `ws://${HOST}/api/coin/prev`,
// };
//
// export default function () {
//     const vu = __VU;
//     const endpointTag = vu % 2 === 0 ? 'min' : 'prev';
//     const url = ENDPOINTS[endpointTag];
//     const ticker = TICKERS[vu % TICKERS.length];
//
//     const res = ws.connect(
//         url,
//         { headers: { Origin: ORIGIN } },
//         socket => {
//             const start = Date.now();
//             activeConnections.add(1, { endpoint: endpointTag });
//
//             socket.on('open', () => {
//                 socket.send(
//                     'CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\x00'
//                 );
//             });
//
//             socket.on('message', data => {
//                 const now = Date.now();
//                 if (data.startsWith('CONNECTED')) {
//                     connectionDuration.add(now - start, { endpoint: endpointTag });
//                     connectionSuccess.add(1, { endpoint: endpointTag });
//
//                     // 즉시 구독
//                     socket.send(
//                         `SUBSCRIBE\nid:sub-${endpointTag}\ndestination:/topic/${endpointTag === 'min' ? 'realTimeTradeRate' : 'prevRate'}/${ticker}\n\n\x00`
//                     );
//                     subscriptionDuration.add(0, { endpoint: endpointTag });
//                     subscriptionSuccess.add(1, { endpoint: endpointTag });
//
//                     // 첫 메시지 요청
//                     socket.send(
//                         `SEND\ndestination:/app/subscribe/${endpointTag === 'min' ? 'realTimeTradeRate' : 'prevRate'}/${ticker}\n\n\x00`
//                     );
//                 } else if (data.startsWith('MESSAGE')) {
//                     messagesReceived.add(1, { endpoint: endpointTag });
//                     const body = data.slice(data.indexOf('\n\n') + 2).replace('\x00', '');
//                     let timestamp;
//                     try {
//                         timestamp = new Date(JSON.parse(body).timestamp).getTime();
//                     } catch (e) {
//                         errorCount.add(
//                             1,
//                             Object.assign({}, { endpoint: endpointTag }, { error: 'json_parse' })
//                         );
//                         return;
//                     }
//                     const latency = now - timestamp;
//                     messageLatency.add(latency, { endpoint: endpointTag });
//                 }
//             });
//
//             socket.on('error', e => {
//                 errorCount.add(
//                     1,
//                     Object.assign({}, { endpoint: endpointTag }, { error: e.message })
//                 );
//                 connectionSuccess.add(0, { endpoint: endpointTag });
//             });
//
//             // 60초 후 소켓 종료
//             socket.setTimeout(() => {
//                 socket.close();
//                 activeConnections.add(-1, { endpoint: endpointTag });
//             }, 60 * 1000);
//         }
//     );
//
//     // 연결 성공 체크
//     connectionSuccess.add(res && res.status === 101 ? 1 : 0, { endpoint: endpointTag });
//
//     // 충분히 대기
//     sleep(65);
// }
