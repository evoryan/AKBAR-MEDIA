const axios = require('axios');
const jwt = require('jsonwebtoken');
require('dotenv').config({ path: 'VPS/.env' });
const token = jwt.sign({ username: 'admin', db_name: 'akbar_media_master' }, 'super-secret-key-akbar', { expiresIn: '1h' });
axios.post('http://localhost:4500/api/mikrotik/secrets/1/disable', { secretId: 'test_identifier' }, { headers: { Authorization: `Bearer ${token}` } })
  .then(res => console.log(res.data))
  .catch(err => console.error(err.response ? err.response.data : err.message));
