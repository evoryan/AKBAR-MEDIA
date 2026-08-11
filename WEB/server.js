const express = require('express');
const path = require('path');
const app = express();

// Determine port from environment or fallback
const PORT = process.env.PORT || 8080;

// Serve static assets from public folder
app.use(express.static(path.join(__dirname, 'public')));

// Fallback to index.html for SPA routing
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, () => {
    console.log('===================================================');
    console.log(`🚀 Akbar Media Web Frontend is running!`);
    console.log(`🌐 URL: http://localhost:${PORT}`);
    console.log(`📂 Serving static files from: ${path.join(__dirname, 'public')}`);
    console.log('===================================================');
});
