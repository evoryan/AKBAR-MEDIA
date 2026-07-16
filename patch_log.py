import re

with open("VPS/server.js", "r") as f:
    content = f.read()

content = content.replace("app.use(express.json());", "app.use(express.json());\napp.use((req, res, next) => { console.log(req.method, req.url); next(); });")

with open("VPS/server.js", "w") as f:
    f.write(content)
