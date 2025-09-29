// server.js
const express = require('express');
const axios = require('axios');
const cheerio = require('cheerio');
const rateLimit = require('express-rate-limit');
const robotsParser = require('robots-parser');
const { URL } = require('url');

const app = express();
app.use(express.json());

// rate limiting: small values for safety
app.use(rateLimit({
  windowMs: 60*1000,
  max: 20, // per IP per minute
  standardHeaders: true,
  legacyHeaders: false,
}));

// Basic in-memory API key (for demo) - replace with DB or env var in production
const VALID_API_KEYS = new Set([ process.env.SCAN_API_KEY || 'demo-key-123' ]);

function checkApiKey(req, res, next) {
  const key = req.headers['x-api-key'];
  if (!key || !VALID_API_KEYS.has(key)) {
    return res.status(401).send('Invalid or missing API key. Set X-API-KEY header.');
  }
  next();
}

function simpleFetch(url){
  return axios.get(url, {
    headers: { 'User-Agent': 'site-scanner-bot/1.0 (+you@domain.com)' },
    timeout: 10000,
    responseType: 'text'
  });
}

// regex patterns for common "API-key like" strings (heuristic only)
const KEY_PATTERNS = [
  { name: 'Google API key (AIza...)', re: /AIza[0-9A-Za-z\-_]{35}/g },
  { name: 'AWS Access Key (AKIA...)', re: /AKIA[0-9A-Z]{16}/g },
  { name: 'Firebase config (apiKey)', re: /apiKey['"]?\s*:\s*['"][A-Za-z0-9\-_:.]{16,45}['"]/gi },
  { name: 'JWT-like (3-part dot)', re: /\beyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{6,}\b/g },
  { name: 'Heroku-like token', re: /[A-Za-z0-9]{20,}\.[A-Za-z0-9]{20,}\.[A-Za-z0-9_-]{20,}/g }
];

app.post('/scan', checkApiKey, async (req, res) => {
  const target = req.body && req.body.url;
  if (!target) return res.status(400).send('Missing url in JSON body');

  let parsed;
  try {
    parsed = new URL(target);
  } catch (e) {
    return res.status(400).send('Invalid URL');
  }

  // robots.txt check - respectful, not authoritative
  try {
    const robotsUrl = `${parsed.origin}/robots.txt`;
    const rtxt = (await axios.get(robotsUrl, { timeout: 4000 })).data;
    const robots = robotsParser(robotsUrl, rtxt);
    if (!robots.isAllowed(target, 'site-scanner-bot')) {
      return res.status(403).send('Blocked by robots.txt');
    }
  } catch (e) {
    // ignore robots errors (allow scan) but log
    console.warn('robots.txt fetch error (ignored):', e.message);
  }

  try {
    const response = await simpleFetch(target);
    const html = response.data;
    const $ = cheerio.load(html);

    const scripts = [];
    const inline_scripts = [];
    const external_assets = new Set();
    const flags = new Set();

    // find script tags
    $('script').each((i, el) => {
      const src = $(el).attr('src');
      if (src) {
        scripts.push({ type: 'external', src });
        external_assets.add(
