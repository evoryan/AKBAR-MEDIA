const express = require('express');
const app = express();
app.post('/api/mikrotik/secrets/:id', (req, res) => res.send('MATCHED ID'));
app.post('/api/mikrotik/secrets/:id/disable', (req, res) => res.send('MATCHED DISABLE'));
const request = require('supertest');
request(app)
  .post('/api/mikrotik/secrets/1/disable')
  .end(function(err, res) {
    console.log(res.text);
    process.exit();
  });
