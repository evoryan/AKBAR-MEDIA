const fs = require('fs');
const path = require('path');

const USE_MINIO = process.env.USE_MINIO === 'true';
const MINIO_ENDPOINT = process.env.MINIO_ENDPOINT || 'localhost';
const MINIO_PORT = parseInt(process.env.MINIO_PORT || '9000', 10);
const MINIO_USE_SSL = process.env.MINIO_USE_SSL === 'true';
const MINIO_ACCESS_KEY = process.env.MINIO_ACCESS_KEY || 'minioadmin';
const MINIO_SECRET_KEY = process.env.MINIO_SECRET_KEY || 'minioadmin';
const MINIO_BUCKET = process.env.MINIO_BUCKET || 'akbar-media-bucket';
const MINIO_PUBLIC_URL = process.env.MINIO_PUBLIC_URL || '';

let minioClient = null;

if (USE_MINIO) {
    try {
        const Minio = require('minio');
        minioClient = new Minio.Client({
            endPoint: MINIO_ENDPOINT,
            port: MINIO_PORT,
            useSSL: MINIO_USE_SSL,
            accessKey: MINIO_ACCESS_KEY,
            secretKey: MINIO_SECRET_KEY
        });
    } catch (err) {
        console.warn('⚠️ MinIO client initialization skipped/warning:', err.message);
    }
}

// Base upload directories for Local VPS Storage
const baseUploadsDir = path.join(__dirname, 'uploads');
const baseInvoicesDir = path.join(baseUploadsDir, 'invoices');
const baseProfilesDir = path.join(baseUploadsDir, 'profiles');
const baseCustomersDir = path.join(baseUploadsDir, 'customers');

[baseUploadsDir, baseInvoicesDir, baseProfilesDir, baseCustomersDir].forEach(dir => {
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }
});

/**
 * Upload invoice image to Local VPS Storage with per-tenant folder isolation.
 * Path: VPS/uploads/invoices/<tenant_db_name>/<timestamp>-<random>.<ext>
 */
async function uploadInvoiceImage(fileBuffer, originalName, mimeType, tenantDbName) {
    const ext = path.extname(originalName) || '.jpg';
    const timestamp = Date.now();
    const cleanTenantDb = (tenantDbName || 'default_tenant').replace(/[^a-zA-Z0-9_-]/g, '_');
    const filename = `${timestamp}-${Math.round(Math.random() * 1e6)}${ext}`;

    const tenantDir = path.join(baseInvoicesDir, cleanTenantDb);
    if (!fs.existsSync(tenantDir)) {
        fs.mkdirSync(tenantDir, { recursive: true });
    }

    const localFilePath = path.join(tenantDir, filename);
    fs.writeFileSync(localFilePath, fileBuffer);

    const relativeUrl = `/uploads/invoices/${cleanTenantDb}/${filename}`;
    console.log(`📁 Invoice saved to Local VPS Storage: ${localFilePath}`);

    return {
        url: relativeUrl,
        localUrl: relativeUrl,
        localPath: localFilePath,
        storage: 'local_vps'
    };
}

/**
 * Upload profile picture to Local VPS Storage with per-tenant folder isolation.
 * Path: VPS/uploads/profiles/<tenant_db_name>/user_<userId>_<timestamp>.<ext>
 */
async function uploadProfilePicture(fileBuffer, originalName, mimeType, tenantDbName, userId) {
    const ext = path.extname(originalName) || '.jpg';
    const timestamp = Date.now();
    const cleanTenantDb = (tenantDbName || 'default_tenant').replace(/[^a-zA-Z0-9_-]/g, '_');
    const cleanUserId = (userId || 'admin').toString().replace(/[^a-zA-Z0-9_-]/g, '_');
    const filename = `user_${cleanUserId}_${timestamp}${ext}`;

    const tenantDir = path.join(baseProfilesDir, cleanTenantDb);
    if (!fs.existsSync(tenantDir)) {
        fs.mkdirSync(tenantDir, { recursive: true });
    }

    const localFilePath = path.join(tenantDir, filename);
    fs.writeFileSync(localFilePath, fileBuffer);

    const relativeUrl = `/uploads/profiles/${cleanTenantDb}/${filename}`;
    console.log(`👤 Profile picture saved to Local VPS Storage: ${localFilePath}`);

    return {
        url: relativeUrl,
        localUrl: relativeUrl,
        localPath: localFilePath,
        storage: 'local_vps'
    };
}

/**
 * Upload customer header background image to Local VPS Storage with per-tenant folder isolation.
 * Path: VPS/uploads/customers/<tenant_db_name>/header_<customerId>_<timestamp>.<ext>
 */
async function uploadCustomerHeaderBg(fileBuffer, originalName, mimeType, tenantDbName, customerId) {
    const ext = path.extname(originalName) || '.jpg';
    const timestamp = Date.now();
    const cleanTenantDb = (tenantDbName || 'default_tenant').replace(/[^a-zA-Z0-9_-]/g, '_');
    const cleanCustId = (customerId || '0').toString().replace(/[^a-zA-Z0-9_-]/g, '_');
    const filename = `header_${cleanCustId}_${timestamp}${ext}`;

    const tenantDir = path.join(baseCustomersDir, cleanTenantDb);
    if (!fs.existsSync(tenantDir)) {
        fs.mkdirSync(tenantDir, { recursive: true });
    }

    const localFilePath = path.join(tenantDir, filename);
    fs.writeFileSync(localFilePath, fileBuffer);

    const relativeUrl = `/uploads/customers/${cleanTenantDb}/${filename}`;
    console.log(`🖼️ Customer header background saved to Local VPS Storage: ${localFilePath}`);

    return {
        url: relativeUrl,
        localUrl: relativeUrl,
        localPath: localFilePath,
        storage: 'local_vps'
    };
}

module.exports = {
    minioClient,
    uploadInvoiceImage,
    uploadProfilePicture,
    uploadCustomerHeaderBg
};
