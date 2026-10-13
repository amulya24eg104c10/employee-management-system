// Writes public/config.js using the API_URL environment variable (used by Netlify at build time)
const fs = require('fs');
const api = process.env.API_URL || 'http://localhost:8080/api';
fs.writeFileSync('public/config.js', 'window.API_URL = ' + JSON.stringify(api) + ';\n');
console.log('config.js written, API_URL =', api);
