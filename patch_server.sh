#!/bin/bash
sed -i "s/'SELECT id, name, username, role FROM users/'SELECT id, name, username, role, area_id FROM users/g" VPS/server.js
sed -i "s/const { name, username, role, password } = req.body;/const { name, username, role, password, area_id } = req.body;/g" VPS/server.js
sed -i "s/'INSERT INTO users (name, username, role, password, db_name) VALUES (?, ?, ?, ?, ?)'/'INSERT INTO users (name, username, role, password, db_name, area_id) VALUES (?, ?, ?, ?, ?, ?)'/g" VPS/server.js
sed -i "s/\[name, username, role || 'ADMIN', password || '', db_name\]/[name, username, role || 'ADMIN', password || '', db_name, area_id || 'semua']/g" VPS/server.js
sed -i "s/UPDATE users SET name = ?, username = ?, role = ?, password = ? WHERE/UPDATE users SET name = ?, username = ?, role = ?, password = ?, area_id = ? WHERE/g" VPS/server.js
sed -i "s/\[name, username, role || 'ADMIN', password, req.params.id, db_name\]/[name, username, role || 'ADMIN', password, area_id || 'semua', req.params.id, db_name]/g" VPS/server.js
sed -i "s/UPDATE users SET name = ?, username = ?, role = ? WHERE/UPDATE users SET name = ?, username = ?, role = ?, area_id = ? WHERE/g" VPS/server.js
sed -i "s/\[name, username, role || 'ADMIN', req.params.id, db_name\]/[name, username, role || 'ADMIN', area_id || 'semua', req.params.id, db_name]/g" VPS/server.js
