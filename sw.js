/* MoneyMate service worker
 * Strategy:
 *  - App shell (index.html, manifest, icons): cache-first, versioned, refreshed on deploy
 *  - Google Fonts stylesheet: stale-while-revalidate
 *  - Google Fonts font files: cache-first (immutable, long-lived)
 *  - Everything else same-origin: network-first falling back to cache, then offline.html
 * MoneyMate stores all user data in localStorage on-device — no backend calls are made,
 * so once the shell is cached the app is fully usable offline.
 */

const VERSION = "v1.0.0";
const SHELL_CACHE = `moneymate-shell-${VERSION}`;
const FONT_CACHE = `moneymate-fonts-${VERSION}`;
const RUNTIME_CACHE = `moneymate-runtime-${VERSION}`;

const SHELL_ASSETS = [
  "./",
  "./index.html",
  "./manifest.json",
  "./offline.html",
  "./icons/icon-192.png",
  "./icons/icon-512.png",
  "./icons/favicon-32.png"
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(SHELL_CACHE)
      .then((cache) => cache.addAll(SHELL_ASSETS))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", (event) => {
  const keep = new Set([SHELL_CACHE, FONT_CACHE, RUNTIME_CACHE]);
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(
        keys.filter((key) => !keep.has(key)).map((key) => caches.delete(key))
      ))
      .then(() => self.clients.claim())
  );
});

function isGoogleFontsStylesheet(url) {
  return url.origin === "https://fonts.googleapis.com";
}
function isGoogleFontsFile(url) {
  return url.origin === "https://fonts.gstatic.com";
}

self.addEventListener("fetch", (event) => {
  const req = event.request;
  if (req.method !== "GET") return;

  const url = new URL(req.url);

  // Google Fonts CSS: stale-while-revalidate
  if (isGoogleFontsStylesheet(url)) {
    event.respondWith(
      caches.open(FONT_CACHE).then((cache) =>
        cache.match(req).then((cached) => {
          const fetchPromise = fetch(req)
            .then((res) => { cache.put(req, res.clone()); return res; })
            .catch(() => cached);
          return cached || fetchPromise;
        })
      )
    );
    return;
  }

  // Google Fonts files: cache-first (long-lived, immutable)
  if (isGoogleFontsFile(url)) {
    event.respondWith(
      caches.open(FONT_CACHE).then((cache) =>
        cache.match(req).then((cached) => cached || fetch(req).then((res) => {
          cache.put(req, res.clone());
          return res;
        }))
      )
    );
    return;
  }

  // Same-origin navigation/app shell: network-first, fall back to cache, then offline page
  if (url.origin === self.location.origin) {
    if (req.mode === "navigate") {
      event.respondWith(
        fetch(req)
          .then((res) => {
            caches.open(SHELL_CACHE).then((cache) => cache.put(req, res.clone()));
            return res;
          })
          .catch(() =>
            caches.match(req).then((cached) => cached || caches.match("./index.html").then((idx) => idx || caches.match("./offline.html")))
          )
      );
      return;
    }

    event.respondWith(
      caches.match(req).then((cached) => {
        if (cached) return cached;
        return fetch(req)
          .then((res) => {
            caches.open(RUNTIME_CACHE).then((cache) => cache.put(req, res.clone()));
            return res;
          })
          .catch(() => caches.match("./offline.html"));
      })
    );
  }
});
