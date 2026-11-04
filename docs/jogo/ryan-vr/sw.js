// Guarda o jogo no celular para abrir mesmo sem internet (depois da primeira vez).
const CACHE = 'ryanvr-v24';
const ARQUIVOS = ['./', './index.html', './manifest.json', './icon-192.png', './icon-512.png', '../blocos-vr/index.html', '../comum/maos-vr.js',
  'https://cdn.jsdelivr.net/npm/aframe@1.6.0/dist/aframe-v1.6.0.min.js'];
self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => Promise.all(ARQUIVOS.map(u => c.add(u).catch(() => {})))).then(() => self.skipWaiting()));
});
self.addEventListener('activate', e => {
  e.waitUntil(caches.keys().then(ks => Promise.all(ks.filter(k => k !== CACHE).map(k => caches.delete(k)))).then(() => self.clients.claim()));
});
self.addEventListener('fetch', e => {
  if (e.request.method !== 'GET' || e.request.url.endsWith('.apk')) return;
  // rede primeiro (pega a versão nova); sem internet, usa o que está guardado
  e.respondWith(fetch(e.request).then(r => {
    if (r.ok && (r.type === 'basic' || r.type === 'cors')) { const cp = r.clone(); caches.open(CACHE).then(c => c.put(e.request, cp)); }
    return r;
  }).catch(() => caches.match(e.request).then(m => m || caches.match('./index.html'))));
});
