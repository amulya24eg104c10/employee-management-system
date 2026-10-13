// Node.js server that serves the frontend. The UI talks to the Java Spring Boot REST API.
const express = require('express');
const path = require('path');
const app = express();

const PORT = process.env.PORT || 3000;
const API_URL = process.env.API_URL || 'http://localhost:8080/api';

app.get('/config.js', (req, res) => {
  res.type('application/javascript').send(`window.API_URL = ${JSON.stringify(API_URL)};`);
});
app.use(express.static(path.join(__dirname, 'public')));

app.listen(PORT, () => console.log(`Frontend running at http://localhost:${PORT}  (API: ${API_URL})`));
