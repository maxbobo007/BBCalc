/* BBCalc Service Worker：离线可用 + 更新及时 */
const CACHE = 'bbcalc-v1.6.0';
const ASSETS = [
    './',
    './index.html',
    './manifest.webmanifest',
    './icons/icon-192.png',
    './icons/icon-512.png',
    './icons/icon-maskable-512.png',
    './icons/apple-touch-icon.png'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE)
            .then(cache => cache.addAll(ASSETS))
            .then(() => self.skipWaiting())
    );
});

self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys()
            .then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
            .then(() => self.clients.claim())
    );
});

self.addEventListener('fetch', event => {
    const req = event.request;
    if (req.method !== 'GET') return;

    const url = new URL(req.url);
    // 第三方请求（汇率 API、Firebase、CDN）不拦截、不缓存
    if (url.origin !== self.location.origin) return;

    // 页面导航：网络优先（保证部署后立刻拿到新版本），失败回退缓存（离线可用）
    if (req.mode === 'navigate' || url.pathname.endsWith('/index.html')) {
        event.respondWith(
            fetch(req)
                .then(res => {
                    const copy = res.clone();
                    caches.open(CACHE).then(cache => cache.put(req, copy));
                    return res;
                })
                .catch(() =>
                    caches.match(req).then(hit => hit || caches.match('./index.html'))
                )
        );
        return;
    }

    // 静态资源（图标等）：缓存优先
    event.respondWith(
        caches.match(req).then(hit => hit || fetch(req).then(res => {
            const copy = res.clone();
            caches.open(CACHE).then(cache => cache.put(req, copy));
            return res;
        }))
    );
});
