const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

// Mock Data
const adminList = [
    { id: "1", name: "Super Admin", username: "superadmin", role: "SUPER_ADMIN", password: "password" },
    { id: "2", name: "Admin Biasa", username: "admin", role: "ADMIN", password: "password" },
    { id: "3", name: "Teknisi 1", username: "teknisi1", role: "TEKNISI", password: "password" },
    { id: "4", name: "Collector 1", username: "collector1", role: "COLLECTOR", password: "password" }
];

const customers = [
    { id: "1", name: "Budi Santoso", phone: "081234567890", area: "Talun", username: "budi.talun", billingDate: "15", status: "Aktif", price: "Rp. 150.000", discount: "0" },
    { id: "2", name: "Siti Aminah", phone: "081987654321", area: "Kedung", username: "siti.kedung", billingDate: "01", status: "Nonaktif", price: "Rp. 150.000", discount: "0" }
];

const pembukuan = { pemasukan: 2575000, pengeluaran: 0 };

const areas = [
    { id: "1", name: "Talun", address: "Area cakupan desa Talun dan sekitarnya", customerCount: 15, routerIp: "192.168.1.1:8728", apiDomain: "http://192.168.1.1:7557" },
    { id: "2", name: "Kedung", address: "Area cakupan desa Kedung", customerCount: 8, routerIp: "192.168.2.1:8728", apiDomain: "http://192.168.2.1:7557" },
    { id: "3", name: "Bate", address: "Area cakupan desa Bate, wilayah timur", customerCount: 24, routerIp: "192.168.3.1:8728", apiDomain: "http://192.168.3.1:7557" }
];

const odcList = [
    { id: "1", name: "ODC-01", location: "Pusat" },
    { id: "2", name: "ODC-02", location: "Cabang Utara" }
];

const odpList = [
    { id: "1", name: "ODP-01", odcId: "1", portCount: 8 },
    { id: "2", name: "ODP-02", odcId: "1", portCount: 16 },
    { id: "3", name: "ODP-03", odcId: "2", portCount: 8 }
];

// Endpoints
app.post('/api/login', (req, res) => {
    const { username, password } = req.body;
    const user = adminList.find(u => u.username === username && u.password === password);
    if (user) {
        const { password, ...userWithoutPassword } = user;
        res.json(userWithoutPassword);
    } else {
        res.status(401).json({ error: "Username atau password salah" });
    }
});

app.get('/api/customers', (req, res) => res.json(customers));
app.get('/api/pembukuan', (req, res) => res.json(pembukuan));
app.get('/api/areas', (req, res) => res.json(areas));
app.get('/api/odc', (req, res) => res.json(odcList));
app.get('/api/odp', (req, res) => res.json(odpList));

const PORT = 4500;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
});

app.delete('/api/areas/:id', (req, res) => {
    const { id } = req.params;
    const index = areas.findIndex(a => a.id === id);
    if (index !== -1) {
        areas.splice(index, 1);
        res.json({ message: "Area deleted" });
    } else {
        res.status(404).json({ error: "Area not found" });
    }
});

// Mock ACS Devices
const acsDevices = [
    { id: "1", username: "turifah", isOnline: true, ssid: "ELS_A", connectedUsers: 0, customerNumber: "-", rxPower: "-23.27", areaName: "Talun" },
    { id: "2", username: "harti", isOnline: true, ssid: "Harti_WIFI", connectedUsers: 3, customerNumber: "-", rxPower: "-21.40", areaName: "Talun" },
    { id: "3", username: "bila", isOnline: true, ssid: "Bila_WIFI", connectedUsers: 2, customerNumber: "-", rxPower: "-19.10", areaName: "Kedung" },
    { id: "4", username: "dendi", isOnline: false, ssid: "Dendi_Net", connectedUsers: 0, customerNumber: "-", rxPower: "-", areaName: "Kedung" },
    { id: "5", username: "dinda", isOnline: true, ssid: "Dinda_Home", connectedUsers: 4, customerNumber: "-", rxPower: "-22.15", areaName: "Bate" },
    { id: "6", username: "fadil_gd", isOnline: true, ssid: "Fadil_Gg", connectedUsers: 1, customerNumber: "-", rxPower: "-20.00", areaName: "Bate" },
    { id: "7", username: "rabo", isOnline: true, ssid: "Rabo_Free", connectedUsers: 5, customerNumber: "-", rxPower: "-24.10", areaName: "Talun" },
    { id: "8", username: "miftah", isOnline: false, ssid: "Miftah_WIFI", connectedUsers: 0, customerNumber: "-", rxPower: "-", areaName: "Kedung" },
    { id: "9", username: "anwar", isOnline: true, ssid: "Anwar_Net", connectedUsers: 2, customerNumber: "-", rxPower: "-18.50", areaName: "Bate" },
    { id: "10", username: "yuli", isOnline: true, ssid: "Yuli_Wifi", connectedUsers: 3, customerNumber: "-", rxPower: "-21.90", areaName: "Talun" }
];

app.get('/api/acs/devices', (req, res) => {
    res.json(acsDevices);
});

app.post('/api/acs/devices/:id/action', (req, res) => {
    const { id } = req.params;
    const { action, value } = req.body;
    
    const device = acsDevices.find(d => d.id === id);
    if (!device) {
        return res.status(404).json({ error: "Device tidak ditemukan" });
    }

    if (action === "set_ssid") {
        device.ssid = value;
        res.json({ message: "Ubah SSID berhasil!" });
    } else if (action === "set_password") {
        res.json({ message: "Ubah password berhasil!" });
    } else if (action === "reboot") {
        res.json({ message: "Restart perangkat berhasil!" });
    } else {
        res.status(400).json({ error: "Aksi tidak valid" });
    }
});
