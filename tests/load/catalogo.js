import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  ...(__ENV.LOAD_SMOKE === '1' ? { vus: 5, duration: '30s' } : {
    stages: [{ duration: '1m', target: 1 }, { duration: '3m', target: 1 }, { duration: '3m', target: 5 }, { duration: '3m', target: 10 }, { duration: '30s', target: 0 }]
  }),
  thresholds: { http_req_duration: ['p(95)<2000'], http_req_failed: ['rate<0.01'] }
};
export default function () {
  const base = __ENV.BASE_URL || 'http://127.0.0.1:8080';
  for (const route of ['/', '/servicos', '/profissionais']) {
    const res = http.get(base + route);
    check(res, { 'HTTP 200': r => r.status === 200 });
  }
  sleep(1);
}
