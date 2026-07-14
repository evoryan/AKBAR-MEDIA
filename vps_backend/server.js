const express = require('express');
const mysql = require('mysql2/promise');
const cron = require('node-cron');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

// Konfigurasi Database
const dbConfig = {
    host: 'localhost',
    user: 'root',
    password: 'password',
    database: 'wifi_billing'
};

let pool = mysql.createPool(dbConfig);

// 1. Cron Job: Berjalan setiap hari jam 00:01
// Mengecek apakah hari ini adalah tanggal tagihan (billingDate) untuk pelanggan yang aktif.
cron.schedule('1 0 * * *', async () => {
    console.log('Menjalankan pengecekan tagihan bulanan...');
    try {
        const today = new Date();
        const dateStr = today.getDate().toString();
        const monthNames = ["Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"];
        const currentMonth = monthNames[today.getMonth()];
        const currentYear = today.getFullYear();

        // Ambil pelanggan yang tanggal tagihannya hari ini dan statusnya bukan TERHAPUS
        const [customers] = await pool.execute(
            'SELECT * FROM customers WHERE billingDate = ? AND status != "TERHAPUS"',
            [dateStr]
        );

        for (const customer of customers) {
            // Cek apakah tagihan bulan ini sudah dibuat agar tidak double
            const [existing] = await pool.execute(
                'SELECT id FROM tagihan_bulanan WHERE customer_id = ? AND bulan = ? AND tahun = ?',
                [customer.id, currentMonth, currentYear]
            );

            if (existing.length === 0) {
                // Buat tagihan baru dengan status BELUM BAYAR
                await pool.execute(
                    'INSERT INTO tagihan_bulanan (customer_id, bulan, tahun, amount, status) VALUES (?, ?, ?, ?, "BELUM BAYAR")',
                    [customer.id, currentMonth, currentYear, customer.price]
                );
                
                // Update status di tabel customers menjadi BELUM BAYAR
                await pool.execute(
                    'UPDATE customers SET status = "BELUM BAYAR" WHERE id = ?',
                    [customer.id]
                );
                console.log(`Tagihan dibuat untuk ${customer.name} bulan ${currentMonth} ${currentYear}`);
            }
        }
    } catch (error) {
        console.error('Error saat membuat tagihan bulanan:', error);
    }
});

// 2. Endpoint Pembayaran
app.post('/api/billing/pay', async (req, res) => {
    const { customerId, adminName, totalAmount } = req.body;
    
    try {
        // Ambil tagihan yang belum dibayar (contoh ini menandai semua atau tagihan bulan berjalan)
        const [tagihan] = await pool.execute(
            'SELECT id, amount, bulan, tahun FROM tagihan_bulanan WHERE customer_id = ? AND status = "BELUM BAYAR" LIMIT 1',
            [customerId]
        );

        if (tagihan.length === 0) {
            return res.status(400).json({ message: "Tidak ada tagihan yang belum dibayar" });
        }

        const tagihanId = tagihan[0].id;
        const description = `Pembayaran tagihan ${tagihan[0].bulan} ${tagihan[0].tahun} (Admin: ${adminName})`;

        // Update tagihan menjadi LUNAS
        await pool.execute(
            'UPDATE tagihan_bulanan SET status = "LUNAS CASH" WHERE id = ?',
            [tagihanId]
        );

        // Update status pelanggan
        await pool.execute(
            'UPDATE customers SET status = "LUNAS CASH" WHERE id = ?',
            [customerId]
        );

        // Masukkan ke Pembukuan (Pemasukan)
        await pool.execute(
            'INSERT INTO pembukuan (type, category, amount, description) VALUES ("Pemasukan", "Pembayaran Pelanggan", ?, ?)',
            [totalAmount, description]
        );

        res.json({ message: "Pembayaran berhasil dicatat" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Terjadi kesalahan server" });
    }
});

// 3. Endpoint Pembatalan Pembayaran
app.post('/api/billing/delete', async (req, res) => {
    const { customerId } = req.body;
    
    try {
        // Cari tagihan terakhir yang sudah lunas untuk dibatalkan
        const [tagihan] = await pool.execute(
            'SELECT id, amount, bulan, tahun FROM tagihan_bulanan WHERE customer_id = ? AND status = "LUNAS CASH" ORDER BY id DESC LIMIT 1',
            [customerId]
        );

        if (tagihan.length === 0) {
            return res.status(400).json({ message: "Tidak ada pembayaran yang bisa dibatalkan" });
        }

        const tagihanId = tagihan[0].id;
        const refundAmount = tagihan[0].amount;
        const description = `Pembatalan pembayaran tagihan ${tagihan[0].bulan} ${tagihan[0].tahun}`;

        // Kembalikan status tagihan
        await pool.execute(
            'UPDATE tagihan_bulanan SET status = "BELUM BAYAR" WHERE id = ?',
            [tagihanId]
        );

        // Update status pelanggan kembali ke BELUM BAYAR
        await pool.execute(
            'UPDATE customers SET status = "BELUM BAYAR" WHERE id = ?',
            [customerId]
        );

        // Masukkan ke Pembukuan (Pengeluaran / Pengembalian dana)
        await pool.execute(
            'INSERT INTO pembukuan (type, category, amount, description) VALUES ("Pengeluaran", "Pembatalan Pembayaran", ?, ?)',
            [refundAmount, description]
        );

        res.json({ message: "Pembatalan berhasil dan dana dicatat di pembukuan" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: "Terjadi kesalahan server" });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server berjalan di port ${PORT}`);
});
