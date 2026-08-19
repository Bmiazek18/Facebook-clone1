import sse from 'k6/x/sse';
import { check, sleep } from 'k6';

// k6 Options: defining phases of concurrent virtual users (VUs)
export const options = {
    stages: [
        { duration: '30s', target: 100 },  // Ramp up to 100 concurrent SSE connections
        { duration: '2m', target: 100 },   // Maintain 100 active connections
        { duration: '15s', target: 0 },    // Gradual ramp down
    ]
};

export default function () {
    // Generate a unique user ID per Virtual User (VU) and iteration
    const userId = `load-user-${__VU}-${__ITER}`;
    
    // Read the target base URL from the environment variable TARGET_URL, defaulting to localhost
    const baseUrl = __ENV.TARGET_URL || 'http://localhost:8083';
    const url = `${baseUrl}/api/notifications/subscribe/${userId}?ticket=bypass-load-test`;

    sse.connect(url, {}, function (client) {
        client.on('open', function () {
            // Connection established successfully
        });

        client.on('event', function (event) {
            // Check if we receive heartbeats or actual notifications
            if (event.name === 'notification') {
                check(event, {
                    'notification received': (e) => e.data !== null,
                });
            } else if (event.name === 'ping') {
                check(event, {
                    'ping received': (e) => e.data === 'heartbeat',
                });
            }
        });

        client.on('error', function (e) {
            check(e, {
                'connection error': (err) => false // forces failure metrics in k6 logs on error
            });
        });

        // Each VU keeps its connection open for 30 seconds before disconnecting
        sleep(30);
        client.close();
    });
}
