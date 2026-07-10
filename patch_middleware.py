import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

strict_middleware = """// Middleware to extract tenant context
const tenantContext = (req, res, next) => {
    // Skip auth for login
    if (req.path === '/api/login') {
        return next();
    }
    
    const authHeader = req.headers.authorization;
    if (authHeader && authHeader.startsWith('Bearer ')) {
        const token = authHeader.split(' ')[1];
        try {
            const decoded = jwt.verify(token, JWT_SECRET);
            req.user = decoded;
            if (decoded.db_name) {
                req.pool = getTenantPool(decoded.db_name);
            } else {
                req.pool = masterPool; // fallback for users without specific tenant
            }
            return next();
        } catch (err) {
            console.error("JWT verify error:", err.message);
            return res.status(401).json({ error: "Token tidak valid atau kadaluarsa" });
        }
    } else {
        return res.status(401).json({ error: "Akses ditolak, token tidak ditemukan" });
    }
};"""

content = re.sub(
    r'// Middleware to extract tenant context.*?};',
    strict_middleware,
    content,
    flags=re.DOTALL
)

with open('VPS/server.js', 'w') as f:
    f.write(content)
