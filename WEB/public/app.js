// Akbar Media Web Frontend Controller Logic

let API_BASE_URL = 'http://amg.akbarmediagroup.my.id';
let currentTab = 'dashboard';
let currentInfraSubTab = 'odc';

// Data stores
let customersList = [];
let areasList = [];
let packagesList = [];
let odcList = [];
let odpList = [];
let rasioList = [];
let inventoryList = [];
let financeList = [];

// Pagination state
let customerPage = 1;
const customersPerPage = 10;
let filteredCustomersList = [];

// Chart instance
let dashboardChartInstance = null;

// Global fetch interceptor to automatically add JWT Token and handle 401 Unauthorized errors
const originalFetch = window.fetch;
window.fetch = async function (url, options = {}) {
    // If it's a request to our API_BASE_URL
    if (typeof url === 'string' && url.startsWith(API_BASE_URL)) {
        options.headers = options.headers || {};
        const token = localStorage.getItem('token');
        if (token) {
            options.headers['Authorization'] = `Bearer ${token}`;
        }
    }
    
    try {
        const response = await originalFetch(url, options);
        if (response.status === 401 && !url.includes('/api/login')) {
            // Handle unauthorized / token expired (but don't trigger on login failures)
            console.warn('Unauthorized or token expired. Logging out.');
            handleLogout();
        }
        return response;
    } catch (error) {
        throw error;
    }
};

// Toggle Screen Visibility
function showAppScreen() {
    document.getElementById('app-container').classList.remove('hidden');
    document.getElementById('app-container').classList.add('flex');
    document.getElementById('login-container').classList.add('hidden');
}

function showLoginScreen() {
    document.getElementById('app-container').classList.add('hidden');
    document.getElementById('app-container').classList.remove('flex');
    document.getElementById('login-container').classList.remove('hidden');
}

// On Page Load
document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize API Base URL
    const savedUrl = localStorage.getItem('api_base_url');
    
    // Auto-detect best default URL based on how the app is accessed
    let defaultUrl = 'http://amg.akbarmediagroup.my.id'; // fallback
    const isIpAddress = /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/.test(window.location.hostname);
    
    if (isIpAddress) {
        // If accessed via IP (e.g. http://103.253.245.25:4100), default API to port 4500 on the same IP
        defaultUrl = `${window.location.protocol}//${window.location.hostname}:4500`;
    } else if (window.location.hostname && window.location.hostname !== 'localhost') {
        // If accessed via a real domain (e.g. https://amg.akbarmediagroup.me), default API to the same origin (reverse proxied)
        defaultUrl = window.location.origin;
    }
    
    API_BASE_URL = savedUrl || defaultUrl;
    
    // Set both settings fields in Header and Login
    document.getElementById('api-base-url').value = API_BASE_URL;
    document.getElementById('login-api-url').value = API_BASE_URL;

    // 2. Initialize Lucide Icons
    lucide.createIcons();

    // 3. Check Authentication State
    const token = localStorage.getItem('token');
    if (token) {
        showAppScreen();
        loadAllData();
        pingApi();
    } else {
        showLoginScreen();
    }

    // 4. Default view tab
    switchTab('dashboard');
});

// Authentication handlers
async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;
    
    // Check if user changed the API base URL in the login details
    const customApiUrl = document.getElementById('login-api-url').value.trim();
    if (customApiUrl) {
        API_BASE_URL = customApiUrl;
        localStorage.setItem('api_base_url', customApiUrl);
        document.getElementById('api-base-url').value = customApiUrl;
    }

    showLoading(true);
    try {
        const response = await originalFetch(`${API_BASE_URL}/api/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();
        if (response.ok && data.token) {
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', data.username || username);
            localStorage.setItem('role', data.role || '');
            localStorage.setItem('db_name', data.db_name || '');
            
            showToast(`Selamat datang kembali, ${data.username || username}!`, 'success');
            showAppScreen();
            
            // Load everything and ping asynchronously
            loadAllData();
            pingApi();
        } else {
            showToast(data.error || 'Username atau password salah!', 'error');
        }
    } catch (err) {
        console.error('Login error:', err);
        showToast('Koneksi ke VPS gagal. Periksa kembali URL API Anda.', 'error');
    } finally {
        showLoading(false);
    }
}

function handleLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    localStorage.removeItem('db_name');
    
    // Clear inputs
    document.getElementById('login-username').value = '';
    document.getElementById('login-password').value = '';
    
    showLoginScreen();
    showToast('Anda telah keluar dari aplikasi.', 'info');
}

// Save API base URL changes
function saveApiUrl() {
    const inputUrl = document.getElementById('api-base-url').value.trim();
    if (inputUrl) {
        API_BASE_URL = inputUrl;
        localStorage.setItem('api_base_url', inputUrl);
        const loginUrlField = document.getElementById('login-api-url');
        if (loginUrlField) {
            loginUrlField.value = inputUrl;
        }
        showToast('API URL berhasil disimpan!', 'success');
        loadAllData();
        pingApi();
    }
}

// Ping API to test connectivity
async function pingApi() {
    const badge = document.getElementById('connection-badge');
    try {
        const response = await fetch(`${API_BASE_URL}/api/ping`, { mode: 'cors' });
        const data = await response.json();
        
        if (response.ok && (data.status === 'ok' || data.status === 'success')) {
            badge.innerHTML = `<span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span> Connected`;
            badge.className = "flex items-center gap-1.5 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 px-2.5 py-1 rounded-full text-xs font-medium";
            return true;
        }
    } catch (err) {
        console.error('API connection failed:', err);
    }
    
    badge.innerHTML = `<span class="w-2 h-2 rounded-full bg-rose-500 animate-pulse"></span> Disconnected`;
    badge.className = "flex items-center gap-1.5 bg-rose-500/10 border border-rose-500/30 text-rose-400 px-2.5 py-1 rounded-full text-xs font-medium";
    showToast('Koneksi ke VPS Gagal. Silakan periksa IP/Domain API VPS Anda.', 'error');
    return false;
}

// Show Spinner loading
function showLoading(show) {
    const spinner = document.getElementById('loading-spinner');
    if (show) {
        spinner.classList.remove('hidden');
    } else {
        spinner.classList.add('hidden');
    }
}

// Custom Toast helper
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    
    let bgClass = 'bg-slate-900 border-slate-800 text-slate-100';
    let icon = 'info';
    
    if (type === 'success') {
        bgClass = 'bg-emerald-950 border-emerald-800 text-emerald-400';
        icon = 'check-circle';
    } else if (type === 'error') {
        bgClass = 'bg-rose-950 border-rose-800 text-rose-400';
        icon = 'alert-triangle';
    } else if (type === 'warning') {
        bgClass = 'bg-amber-950 border-amber-800 text-amber-400';
        icon = 'alert-circle';
    }
    
    toast.className = `flex items-center gap-3 px-4 py-3 rounded-xl border ${bgClass} shadow-xl max-w-sm transition-all duration-300 transform translate-y-2 opacity-0`;
    toast.innerHTML = `
        <i data-lucide="${icon}" class="w-5 h-5 flex-shrink-0"></i>
        <div class="text-xs font-semibold">${message}</div>
    `;
    
    container.appendChild(toast);
    lucide.createIcons();
    
    // Animate In
    setTimeout(() => {
        toast.classList.remove('translate-y-2', 'opacity-0');
    }, 10);
    
    // Animate Out and Remove
    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-y-1');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Switch between sidebar/footer tabs
function switchTab(tabId) {
    currentTab = tabId;
    
    // Highlight sidebar menus
    const tabs = ['dashboard', 'pelanggan', 'jaringan', 'inventaris', 'keuangan', 'konfigurasi'];
    tabs.forEach(t => {
        const navEl = document.getElementById(`nav-${t}`);
        const mobEl = document.getElementById(`mob-${t}`);
        
        if (navEl) {
            if (t === tabId) {
                navEl.className = "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-semibold transition-all duration-150 bg-emerald-600/10 text-emerald-400 border-l-4 border-emerald-500 pl-2";
            } else {
                navEl.className = "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 text-slate-300 hover:bg-slate-800 hover:text-white";
            }
        }
        
        if (mobEl) {
            if (t === tabId) {
                mobEl.className = "flex flex-col items-center gap-1 text-emerald-400 focus:text-emerald-400 font-semibold";
            } else {
                mobEl.className = "flex flex-col items-center gap-1 text-slate-400 hover:text-slate-200";
            }
        }
    });

    // Toggle content views
    document.querySelectorAll('.tab-content').forEach(el => {
        el.classList.add('hidden');
        el.classList.remove('active');
    });
    
    const activeSection = document.getElementById(`tab-${tabId}`);
    if (activeSection) {
        activeSection.classList.remove('hidden');
        setTimeout(() => activeSection.classList.add('active'), 50);
    }
}

// Global data loading coordinator
async function loadAllData() {
    showLoading(true);
    try {
        // Fetch baseline configurations
        await Promise.all([
            loadAreas(),
            loadPackages(),
            loadOdcList(),
            loadOdpList(),
            loadRasioList()
        ]);
        
        // Populated dependents
        await Promise.all([
            loadCustomersData(),
            loadInventarisData(),
            loadKeuanganData(),
            loadDashboardData()
        ]);
        
        showToast('Semua data berhasil disinkronkan!', 'success');
    } catch (err) {
        console.error('Error synchronizing dataset:', err);
        showToast('Beberapa data gagal dimuat dari server.', 'warning');
    } finally {
        showLoading(false);
    }
}

// ----------------------------------------------------
// FETCH HELPER FUNCTIONS (API wrappers)
// ----------------------------------------------------
async function loadAreas() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/areas`);
        if (res.ok) {
            areasList = await res.json();
            populateAreasDropdowns();
            renderAreasTable();
        }
    } catch (e) { console.error('Failed areas load', e); }
}

async function loadPackages() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/packages`);
        if (res.ok) {
            packagesList = await res.json();
            populatePackagesDropdowns();
            renderPackagesTable();
        }
    } catch (e) { console.error('Failed packages load', e); }
}

async function loadOdcList() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/odc`);
        if (res.ok) {
            odcList = await res.json();
            populateOdcDropdowns();
            renderOdcTable();
        }
    } catch (e) { console.error('Failed ODC load', e); }
}

async function loadOdpList() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/odp`);
        if (res.ok) {
            odpList = await res.json();
            populateOdpDropdowns();
            renderOdpTable();
        }
    } catch (e) { console.error('Failed ODP load', e); }
}

async function loadRasioList() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/rasio`);
        if (res.ok) {
            rasioList = await res.json();
            renderRasioTable();
        }
    } catch (e) { console.error('Failed Rasio load', e); }
}

// ----------------------------------------------------
// TAB 1: DASHBOARD LOGIC
// ----------------------------------------------------
async function loadDashboardData() {
    // Basic numbers from loaded lists
    document.getElementById('dash-total-pelanggan').innerText = customersList.length;
    document.getElementById('dash-total-jaringan').innerText = odcList.length + odpList.length;
    document.getElementById('dash-total-barang').innerText = inventoryList.length;

    // Load money in Admin
    try {
        const res = await fetch(`${API_BASE_URL}/api/uang-di-admin`);
        if (res.ok) {
            const data = await res.json();
            const totalUang = data.total || data.total_uang || 0;
            document.getElementById('dash-uang-admin').innerText = formatRupiah(totalUang);
            if (data.rekap_kasir) {
                document.getElementById('dash-kas-kasir').innerText = `Kas Kasir: ${formatRupiah(data.rekap_kasir)}`;
            }
        }
    } catch (e) {
        document.getElementById('dash-uang-admin').innerText = 'Rp 0';
    }

    // Load PPPoE Offline
    try {
        const res = await fetch(`${API_BASE_URL}/api/dashboard/pppoe-offline`);
        if (res.ok) {
            const list = await res.json();
            const counter = document.getElementById('dash-offline-counter');
            counter.innerText = `${list.length} Offline`;
            counter.className = list.length > 0 ? "text-xs text-rose-400 font-bold animate-pulse" : "text-xs text-slate-400 font-medium";

            const container = document.getElementById('dash-pppoe-offline-list');
            if (list.length === 0) {
                container.innerHTML = '<p class="text-slate-500 text-xs text-center py-12">Semua router PPPoE aktif & terhubung</p>';
            } else {
                container.innerHTML = list.map(item => `
                    <div class="flex items-center justify-between bg-slate-900/50 border border-slate-800 p-2.5 rounded-lg">
                        <div class="flex flex-col">
                            <span class="text-xs font-semibold text-white">${item.name || item.username}</span>
                            <span class="text-[10px] text-slate-400">${item.area || 'Tanpa Area'}</span>
                        </div>
                        <span class="text-[10px] bg-rose-500/10 text-rose-400 border border-rose-500/20 px-2 py-0.5 rounded font-medium">OFFLINE</span>
                    </div>
                `).join('');
            }
        }
    } catch (e) { console.error('Failed offline list load', e); }

    // Setup and render Financial Chart
    setupDashboardChart();
}

function setupDashboardChart() {
    const ctx = document.getElementById('dashboardChart').getContext('2d');
    
    // Destroy previous instance
    if (dashboardChartInstance) {
        dashboardChartInstance.destroy();
    }

    // Aggregate monthly data from financials
    const labels = ['Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul', 'Ags', 'Sep', 'Okt', 'Nov', 'Des'];
    const incomeData = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
    const outcomeData = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];

    // Read real dates from financeList
    financeList.forEach(tx => {
        const date = new Date(tx.date || tx.created_at);
        const month = date.getMonth();
        const amt = parseFloat(tx.amount || tx.nominal || 0);
        if (tx.type === 'IN' || tx.type === 'pemasukan') {
            incomeData[month] += amt;
        } else {
            outcomeData[month] += amt;
        }
    });

    // Check if empty, fallback to demo/initial values so chart has aesthetics on empty database
    const totalIncome = incomeData.reduce((a,b)=>a+b, 0);
    const totalOutcome = outcomeData.reduce((a,b)=>a+b, 0);
    
    if (totalIncome === 0 && totalOutcome === 0) {
        // Initial aesthetics fallback
        incomeData[5] = 1250000;
        incomeData[6] = 2450000;
        outcomeData[5] = 450000;
        outcomeData[6] = 800000;
    }

    dashboardChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Pemasukan (Rp)',
                    data: incomeData,
                    backgroundColor: 'rgba(34, 197, 94, 0.4)', // Emerald
                    borderColor: '#22c55e',
                    borderWidth: 1.5,
                    borderRadius: 4
                },
                {
                    label: 'Pengeluaran (Rp)',
                    data: outcomeData,
                    backgroundColor: 'rgba(244, 63, 94, 0.4)', // Rose
                    borderColor: '#f43f5e',
                    borderWidth: 1.5,
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: '#1e293b' },
                    ticks: { color: '#94a3b8', font: { size: 10 } }
                },
                x: {
                    grid: { display: false },
                    ticks: { color: '#94a3b8', font: { size: 10 } }
                }
            },
            plugins: {
                legend: {
                    position: 'top',
                    labels: { color: '#e2e8f0', font: { size: 11 } }
                }
            }
        }
    });
}

// ----------------------------------------------------
// TAB 2: PELANGGAN LOGIC (CRUD & WA Blasting)
// ----------------------------------------------------
async function loadCustomersData() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/customers`);
        if (res.ok) {
            customersList = await res.json();
            filterCustomers();
        }
    } catch (e) {
        console.error('Failed load customers', e);
        showToast('Koneksi database pelanggan gagal', 'error');
    }
}

function filterCustomers() {
    const search = document.getElementById('cust-search').value.toLowerCase().trim();
    const status = document.getElementById('cust-filter-status').value;
    const area = document.getElementById('cust-filter-area').value;
    const paket = document.getElementById('cust-filter-paket').value;

    filteredCustomersList = customersList.filter(c => {
        const matchesSearch = !search || 
            (c.name && c.name.toLowerCase().includes(search)) ||
            (c.username && c.username.toLowerCase().includes(search)) ||
            (c.phone && c.phone.includes(search));
            
        const matchesStatus = !status || c.status === status;
        const matchesArea = !area || c.area === area;
        const matchesPaket = !paket || c.packageName === paket;

        return matchesSearch && matchesStatus && matchesArea && matchesPaket;
    });

    customerPage = 1;
    renderCustomersTable();
}

function renderCustomersTable() {
    const start = (customerPage - 1) * customersPerPage;
    const end = start + customersPerPage;
    const paginated = filteredCustomersList.slice(start, end);
    const tbody = document.getElementById('customers-table-body');

    // Info paging
    document.getElementById('cust-pagination-info').innerText = 
        `Menampilkan ${Math.min(start + 1, filteredCustomersList.length)}-${Math.min(end, filteredCustomersList.length)} dari ${filteredCustomersList.length} pelanggan`;
        
    document.getElementById('btn-cust-prev').disabled = customerPage === 1;
    document.getElementById('btn-cust-next').disabled = end >= filteredCustomersList.length;

    if (paginated.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="px-6 py-12 text-center text-slate-500">Tidak ada pelanggan yang cocok</td></tr>`;
        return;
    }

    tbody.innerHTML = paginated.map(c => {
        // Status styling
        let statusBadge = '';
        if (c.status === 'LUNAS') {
            statusBadge = `<span class="bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-2 py-0.5 rounded text-xs font-semibold">LUNAS</span>`;
        } else if (c.status === 'ISOLIR') {
            statusBadge = `<span class="bg-rose-500/10 text-rose-400 border border-rose-500/20 px-2 py-0.5 rounded text-xs font-semibold">ISOLIR</span>`;
        } else {
            statusBadge = `<span class="bg-amber-500/10 text-amber-400 border border-amber-500/20 px-2 py-0.5 rounded text-xs font-semibold">BELUM BAYAR</span>`;
        }

        // Action controls
        return `
            <tr class="hover:bg-slate-900/40 border-b border-slate-800 transition">
                <td class="px-6 py-4">
                    <div class="flex flex-col">
                        <span class="font-bold text-white text-sm">${c.name}</span>
                        <span class="text-xs text-slate-400 flex items-center gap-1 mt-0.5">
                            <i data-lucide="phone" class="w-3 h-3 text-slate-500"></i> ${c.phone} | User: ${c.username}
                        </span>
                    </div>
                </td>
                <td class="px-6 py-4">
                    <div class="flex flex-col">
                        <span class="text-xs font-semibold text-slate-200">${c.packageName}</span>
                        <span class="text-xs text-emerald-400 font-bold">${c.price || 'Rp 0'}</span>
                    </div>
                </td>
                <td class="px-6 py-4">
                    <div class="flex flex-col">
                        <span class="text-xs font-medium text-slate-300">${c.area}</span>
                        <span class="text-[10px] text-slate-500">ODP: ${c.odpId ? odpList.find(o => o.id == c.odpId)?.name || 'ID ' + c.odpId : '-'} (Port ${c.odpPort || '-'})</span>
                    </div>
                </td>
                <td class="px-6 py-4 text-center font-bold text-slate-300 text-sm">
                    ${c.billingDate || '-'}
                </td>
                <td class="px-6 py-4 text-center">
                    ${statusBadge}
                </td>
                <td class="px-6 py-4 text-right">
                    <div class="flex items-center justify-end gap-1.5">
                        <button onclick="sendWhatsAppInvoice('${c.id}')" class="p-1.5 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded hover:bg-emerald-500 hover:text-white transition" title="Kirim Tagihan WA">
                            <i data-lucide="message-square" class="w-4 h-4"></i>
                        </button>
                        <button onclick="quickTogglePayStatus('${c.id}', '${c.status}')" class="p-1.5 bg-blue-500/10 text-blue-400 border border-blue-500/20 rounded hover:bg-blue-500 hover:text-white transition" title="Ubah Pembayaran">
                            <i data-lucide="credit-card" class="w-4 h-4"></i>
                        </button>
                        <button onclick="isolateCustomer('${c.id}')" class="p-1.5 bg-rose-500/10 text-rose-400 border border-rose-500/20 rounded hover:bg-rose-500 hover:text-white transition" title="Isolir Pelanggan">
                            <i data-lucide="lock" class="w-4 h-4"></i>
                        </button>
                        <button onclick="editCustomer('${c.id}')" class="p-1.5 bg-amber-500/10 text-amber-400 border border-amber-500/20 rounded hover:bg-amber-500 hover:text-white transition" title="Edit Data">
                            <i data-lucide="edit-3" class="w-4 h-4"></i>
                        </button>
                        <button onclick="deleteCustomer('${c.id}')" class="p-1.5 bg-rose-500/10 text-rose-400 border border-rose-500/20 rounded hover:bg-rose-500 hover:text-white transition" title="Hapus">
                            <i data-lucide="trash-2" class="w-4 h-4"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
    
    lucide.createIcons();
}

function prevCustPage() {
    if (customerPage > 1) {
        customerPage--;
        renderCustomersTable();
    }
}

function nextCustPage() {
    if (customerPage * customersPerPage < filteredCustomersList.length) {
        customerPage++;
        renderCustomersTable();
    }
}

function sendWhatsAppInvoice(id) {
    const c = customersList.find(cust => cust.id == id);
    if (!c) return;

    const message = `Yth. Pelanggan Akbar Media
Nama: ${c.name}
Tagihan Paket: ${c.packageName} (${c.price || 'Rp 0'})
Status Tagihan: *BELUM LUNAS*
Jatuh Tempo: Tanggal ${c.billingDate || '-'} setiap bulannya.

Mohon segera melakukan pembayaran guna menjaga kontinivitas jaringan internet Anda. Jika sudah membayar, abaikan pesan ini. Terima Kasih.`;

    // Strip non-numerical phone characters
    const cleanPhone = c.phone.replace(/[^0-9]/g, '');
    const url = `https://wa.me/${cleanPhone.startsWith('0') ? '62' + cleanPhone.slice(1) : cleanPhone}?text=${encodeURIComponent(message)}`;
    window.open(url, '_blank');
}

// Quick Payment validation
async function quickTogglePayStatus(id, currentStatus) {
    const c = customersList.find(cust => cust.id == id);
    if (!c) return;
    
    const newStatus = currentStatus === 'LUNAS' ? 'BELUM BAYAR' : 'LUNAS';
    if (!confirm(`Ubah status pembayaran ${c.name} menjadi ${newStatus}?`)) return;

    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/customers/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...c, status: newStatus })
        });

        if (res.ok) {
            showToast(`Status billing ${c.name} berhasil diubah!`, 'success');
            await loadCustomersData();
            await loadDashboardData();
        } else {
            showToast('Gagal merubah status pembayaran di server.', 'error');
        }
    } catch (e) {
        showToast('Koneksi gagal saat update status.', 'error');
    } finally {
        showLoading(false);
    }
}

// Isolate customer
async function isolateCustomer(id) {
    const c = customersList.find(cust => cust.id == id);
    if (!c) return;

    if (!confirm(`Isolir pelanggan ${c.name}?`)) return;

    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/customers/${id}/isolir`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (res.ok) {
            showToast(`Pelanggan ${c.name} berhasil diisolir!`, 'success');
            await loadCustomersData();
            await loadDashboardData();
        } else {
            showToast('Gagal mengisolir pelanggan di server.', 'error');
        }
    } catch (e) {
        showToast('Koneksi gagal saat isolir pelanggan.', 'error');
    } finally {
        showLoading(false);
    }
}

// Add Customer modal activation
function openAddCustomerModal() {
    document.getElementById('modal-customer-title').innerText = 'Tambah Pelanggan Baru';
    document.getElementById('form-customer').reset();
    document.getElementById('cust-id').value = '';
    document.getElementById('cust-username').readOnly = false;
    openModal('modal-customer');
}

async function editCustomer(id) {
    const c = customersList.find(cust => cust.id == id);
    if (!c) return;

    document.getElementById('modal-customer-title').innerText = 'Edit Pelanggan';
    document.getElementById('cust-id').value = c.id;
    document.getElementById('cust-name').value = c.name || '';
    document.getElementById('cust-phone').value = c.phone || '';
    document.getElementById('cust-area').value = c.area || '';
    document.getElementById('cust-address').value = c.address || '';
    document.getElementById('cust-username').value = c.username || '';
    document.getElementById('cust-username').readOnly = true;
    document.getElementById('cust-packageName').value = c.packageName || '';
    document.getElementById('cust-odpId').value = c.odpId || '';
    document.getElementById('cust-odpPort').value = c.odpPort || '';
    document.getElementById('cust-billingDate').value = c.billingDate || '10';
    document.getElementById('cust-status').value = c.status || 'BELUM BAYAR';
    
    // Additional pricing fields
    document.getElementById('cust-add1').value = c.additionalCost1 ? c.additionalCost1.replace(/[^0-9]/g, '') : '0';
    document.getElementById('cust-add2').value = c.additionalCost2 ? c.additionalCost2.replace(/[^0-9]/g, '') : '0';
    document.getElementById('cust-discount').value = c.discount ? c.discount.replace(/[^0-9]/g, '') : '0';

    openModal('modal-customer');
}

async function saveCustomer(e) {
    e.preventDefault();
    const id = document.getElementById('cust-id').value;
    const payload = {
        name: document.getElementById('cust-name').value,
        phone: document.getElementById('cust-phone').value,
        area: document.getElementById('cust-area').value,
        address: document.getElementById('cust-address').value,
        username: document.getElementById('cust-username').value,
        packageName: document.getElementById('cust-packageName').value,
        odpId: document.getElementById('cust-odpId').value,
        odpPort: document.getElementById('cust-odpPort').value,
        billingDate: document.getElementById('cust-billingDate').value,
        status: document.getElementById('cust-status').value,
        additionalCost1: "Rp. " + parseInt(document.getElementById('cust-add1').value || '0').toLocaleString('id-ID'),
        additionalCost2: "Rp. " + parseInt(document.getElementById('cust-add2').value || '0').toLocaleString('id-ID'),
        discount: "- Dskn : Rp. " + parseInt(document.getElementById('cust-discount').value || '0').toLocaleString('id-ID')
    };

    showLoading(true);
    try {
        let res;
        if (id) {
            // Update
            res = await fetch(`${API_BASE_URL}/api/customers/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ...customersList.find(c => c.id == id), ...payload })
            });
        } else {
            // Create
            res = await fetch(`${API_BASE_URL}/api/customers`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        }

        if (res.ok) {
            showToast('Data pelanggan berhasil disimpan!', 'success');
            closeModal('modal-customer');
            await loadCustomersData();
            await loadDashboardData();
        } else {
            const errData = await res.json();
            showToast(`Gagal: ${errData.message || 'Kesalahan Server'}`, 'error');
        }
    } catch (e) {
        showToast('Koneksi internet error saat submit.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deleteCustomer(id) {
    if (!confirm('Apakah Anda yakin ingin menghapus pelanggan ini dari database? Jaringan PPPoE dan data tagihan juga akan dihapus.')) return;
    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/customers/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Pelanggan berhasil dihapus.', 'success');
            await loadCustomersData();
            await loadDashboardData();
        } else {
            showToast('Gagal menghapus pelanggan di server.', 'error');
        }
    } catch (e) {
        showToast('Gagal melakukan request hapus.', 'error');
    } finally {
        showLoading(false);
    }
}

// ----------------------------------------------------
// TAB 3: INFRASTRUKTUR JARINGAN (ODC, ODP, RASIO)
// ----------------------------------------------------
function switchInfraSubTab(tab) {
    currentInfraSubTab = tab;
    
    const subtabs = ['odc', 'odp', 'rasio'];
    subtabs.forEach(s => {
        const btn = document.getElementById(`btn-infra-${s}`);
        const el = document.getElementById(`subtab-${s}`);
        if (s === tab) {
            btn.className = "border-b-2 border-emerald-500 text-emerald-400 px-4 py-2.5 text-sm font-semibold transition duration-150";
            el.classList.remove('hidden');
            el.classList.add('active');
        } else {
            btn.className = "border-b-2 border-transparent text-slate-400 hover:text-white px-4 py-2.5 text-sm font-semibold transition duration-150";
            el.classList.add('hidden');
            el.classList.remove('active');
        }
    });
}

function renderOdcTable() {
    const tbody = document.getElementById('odc-table-body');
    if (odcList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="px-6 py-6 text-center text-slate-500">Tidak ada data Optical Distribution Cabinet (ODC)</td></tr>`;
        return;
    }
    tbody.innerHTML = odcList.map(o => `
        <tr class="hover:bg-slate-900/40 border-b border-slate-800 transition">
            <td class="px-6 py-4 font-bold text-white">${o.name}</td>
            <td class="px-6 py-4">${o.portCount} Port</td>
            <td class="px-6 py-4 text-slate-400">${o.portInput || '-'}</td>
            <td class="px-6 py-4 text-xs font-semibold">
                IN: <span class="text-emerald-400">${o.redamanIn || '-'} dB</span> | 
                OUT: <span class="text-amber-400">${o.redamanOut || '-'} dB</span>
            </td>
            <td class="px-6 py-4 text-right">
                <div class="flex items-center justify-end gap-1.5">
                    <button onclick="editNetworkNode('ODC', '${o.id}')" class="p-1.5 bg-amber-500/10 text-amber-400 border border-amber-500/20 rounded hover:bg-amber-500 hover:text-white transition"><i data-lucide="edit-3" class="w-4 h-4"></i></button>
                    <button onclick="deleteNetworkNode('ODC', '${o.id}')" class="p-1.5 bg-rose-500/10 text-rose-400 border border-rose-500/20 rounded hover:bg-rose-500 hover:text-white transition"><i data-lucide="trash-2" class="w-4 h-4"></i></button>
                </div>
            </td>
        </tr>
    `).join('');
    lucide.createIcons();
}

function renderOdpTable() {
    const tbody = document.getElementById('odp-table-body');
    if (odpList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="px-6 py-6 text-center text-slate-500">Tidak ada data Optical Distribution Point (ODP)</td></tr>`;
        return;
    }
    tbody.innerHTML = odpList.map(o => `
        <tr class="hover:bg-slate-900/40 border-b border-slate-800 transition">
            <td class="px-6 py-4 font-bold text-white">${o.name} <span class="text-xs bg-slate-800 text-slate-400 px-2 py-0.5 rounded ml-2">${o.portCount || 8} Port</span></td>
            <td class="px-6 py-4 text-slate-400">${o.portInput || '-'}</td>
            <td class="px-6 py-4 text-xs font-semibold">
                IN: <span class="text-emerald-400">${o.redamanIn || '-'} dB</span> | 
                OUT: <span class="text-amber-400">${o.redamanOut || '-'} dB</span>
            </td>
            <td class="px-6 py-4 text-xs text-slate-400">${o.latitude ? o.latitude + ', ' + o.longitude : '-'}</td>
            <td class="px-6 py-4 text-right">
                <div class="flex items-center justify-end gap-1.5">
                    <button onclick="editNetworkNode('ODP', '${o.id}')" class="p-1.5 bg-amber-500/10 text-amber-400 border border-amber-500/20 rounded hover:bg-amber-500 hover:text-white transition"><i data-lucide="edit-3" class="w-4 h-4"></i></button>
                    <button onclick="deleteNetworkNode('ODP', '${o.id}')" class="p-1.5 bg-rose-500/10 text-rose-400 border border-rose-500/20 rounded hover:bg-rose-500 hover:text-white transition"><i data-lucide="trash-2" class="w-4 h-4"></i></button>
                </div>
            </td>
        </tr>
    `).join('');
    lucide.createIcons();
}

function renderRasioTable() {
    const tbody = document.getElementById('rasio-table-body');
    if (rasioList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="px-6 py-6 text-center text-slate-500">Tidak ada data splitter rasio pasif</td></tr>`;
        return;
    }
    tbody.innerHTML = rasioList.map(r => `
        <tr class="hover:bg-slate-900/40 border-b border-slate-800 transition">
            <td class="px-6 py-4 font-bold text-white">${r.name}</td>
            <td class="px-6 py-4"><span class="bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 px-2 py-0.5 rounded text-xs">${r.size || '1:2'}</span></td>
            <td class="px-6 py-4 text-slate-400">${r.portInput || '-'}</td>
            <td class="px-6 py-4 text-xs font-semibold">
                IN: <span class="text-emerald-400">${r.redamanIn || '-'} dB</span> | 
                OUT: <span class="text-amber-400">${r.redamanOut || '-'} dB</span>
            </td>
            <td class="px-6 py-4 text-right">
                <div class="flex items-center justify-end gap-1.5">
                    <button onclick="editNetworkNode('Rasio', '${r.id}')" class="p-1.5 bg-amber-500/10 text-amber-400 border border-amber-500/20 rounded hover:bg-amber-500 hover:text-white transition"><i data-lucide="edit-3" class="w-4 h-4"></i></button>
                    <button onclick="deleteNetworkNode('Rasio', '${r.id}')" class="p-1.5 bg-rose-500/10 text-rose-400 border border-rose-500/20 rounded hover:bg-rose-500 hover:text-white transition"><i data-lucide="trash-2" class="w-4 h-4"></i></button>
                </div>
            </td>
        </tr>
    `).join('');
    lucide.createIcons();
}

function openAddNodeModal(type) {
    document.getElementById('modal-network-title').innerText = `Tambah Perangkat ${type}`;
    document.getElementById('form-network-node').reset();
    document.getElementById('net-type').value = type;
    document.getElementById('net-id').value = '';
    
    // Switch specific sub fields visibility
    document.getElementById('fields-odc').className = type === 'ODC' ? 'block flex flex-col gap-4' : 'hidden';
    document.getElementById('fields-odp').className = type === 'ODP' ? 'block flex flex-col gap-4' : 'hidden';
    document.getElementById('fields-rasio').className = type === 'Rasio' ? 'block flex flex-col gap-4' : 'hidden';

    openModal('modal-network-node');
}

function editNetworkNode(type, id) {
    let node = null;
    if (type === 'ODC') node = odcList.find(o => o.id == id);
    if (type === 'ODP') node = odpList.find(o => o.id == id);
    if (type === 'Rasio') node = rasioList.find(o => o.id == id);
    if (!node) return;

    document.getElementById('modal-network-title').innerText = `Edit Perangkat ${type}`;
    document.getElementById('net-type').value = type;
    document.getElementById('net-id').value = node.id;
    document.getElementById('net-name').value = node.name || '';
    document.getElementById('net-redaman-in').value = node.redamanIn || '';
    document.getElementById('net-redaman-out').value = node.redamanOut || '';

    if (type === 'ODC') {
        document.getElementById('net-odc-ports').value = node.portCount || '';
        document.getElementById('net-odc-feeder').value = node.portInput || '';
    } else if (type === 'ODP') {
        document.getElementById('net-odp-ports').value = node.portCount || '';
        document.getElementById('net-odp-input').value = node.portInput || '';
        document.getElementById('net-odp-lat').value = node.latitude || '';
        document.getElementById('net-odp-lng').value = node.longitude || '';
    } else if (type === 'Rasio') {
        document.getElementById('net-rasio-ports').value = node.portCount || '';
        document.getElementById('net-rasio-size').value = node.size || '';
        document.getElementById('net-rasio-input').value = node.portInput || '';
    }

    // Toggle fields visibility
    document.getElementById('fields-odc').className = type === 'ODC' ? 'block flex flex-col gap-4' : 'hidden';
    document.getElementById('fields-odp').className = type === 'ODP' ? 'block flex flex-col gap-4' : 'hidden';
    document.getElementById('fields-rasio').className = type === 'Rasio' ? 'block flex flex-col gap-4' : 'hidden';

    openModal('modal-network-node');
}

async function saveNetworkNode(e) {
    e.preventDefault();
    const type = document.getElementById('net-type').value;
    const id = document.getElementById('net-id').value;
    const endpointMap = { 'ODC': 'odc', 'ODP': 'odp', 'Rasio': 'rasio' };
    const apiName = endpointMap[type];

    // Build standard payload
    let payload = {
        name: document.getElementById('net-name').value,
        redamanIn: document.getElementById('net-redaman-in').value,
        redamanOut: document.getElementById('net-redaman-out').value
    };

    if (type === 'ODC') {
        payload.portCount = parseInt(document.getElementById('net-odc-ports').value || '12');
        payload.portInput = document.getElementById('net-odc-feeder').value;
    } else if (type === 'ODP') {
        payload.portCount = parseInt(document.getElementById('net-odp-ports').value || '8');
        payload.portInput = document.getElementById('net-odp-input').value;
        payload.latitude = document.getElementById('net-odp-lat').value;
        payload.longitude = document.getElementById('net-odp-lng').value;
    } else if (type === 'Rasio') {
        payload.portCount = parseInt(document.getElementById('net-rasio-ports').value || '2');
        payload.size = document.getElementById('net-rasio-size').value;
        payload.portInput = document.getElementById('net-rasio-input').value;
    }

    showLoading(true);
    try {
        let res;
        if (id) {
            res = await fetch(`${API_BASE_URL}/api/${apiName}/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        } else {
            res = await fetch(`${API_BASE_URL}/api/${apiName}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        }

        if (res.ok) {
            showToast(`Data perangkat ${type} berhasil disimpan!`, 'success');
            closeModal('modal-network-node');
            
            // Reload tables
            if (type === 'ODC') await loadOdcList();
            if (type === 'ODP') await loadOdpList();
            if (type === 'Rasio') await loadRasioList();
            await loadDashboardData();
        } else {
            showToast('Gagal memproses simpan perangkat.', 'error');
        }
    } catch (err) {
        showToast('Koneksi terputus saat menghubungi server.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deleteNetworkNode(type, id) {
    if (!confirm(`Hapus perangkat ${type} ini? Jalur distribusi yang terikat padanya mungkin kehilangan koneksi.`)) return;
    const endpointMap = { 'ODC': 'odc', 'ODP': 'odp', 'Rasio': 'rasio' };
    const apiName = endpointMap[type];

    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/${apiName}/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast(`${type} berhasil dihapus dari database.`, 'success');
            if (type === 'ODC') await loadOdcList();
            if (type === 'ODP') await loadOdpList();
            if (type === 'Rasio') await loadRasioList();
            await loadDashboardData();
        } else {
            showToast('Gagal menghapus perangkat di VPS.', 'error');
        }
    } catch (e) {
        showToast('Terjadi gangguan jaringan.', 'error');
    } finally {
        showLoading(false);
    }
}

// ----------------------------------------------------
// TAB 4: INVENTARIS BARANG LOGIC
// ----------------------------------------------------
async function loadInventarisData() {
    try {
        // Fetch inventory stock list
        const resStock = await fetch(`${API_BASE_URL}/api/inventory`);
        if (resStock.ok) {
            inventoryList = await resStock.ok ? await resStock.json() : [];
            renderInventoryGrid();
            populateTakeItemDropdowns();
        }

        // Fetch stock change logs
        const resHistory = await fetch(`${API_BASE_URL}/api/stock_history`);
        if (resHistory.ok) {
            const history = await resHistory.json();
            renderInventoryHistory(history);
        }
    } catch (e) { console.error('Failed inventory load', e); }
}

function renderInventoryGrid() {
    const grid = document.getElementById('inventory-items-grid');
    if (inventoryList.length === 0) {
        grid.innerHTML = '<p class="text-slate-500 text-xs text-center py-12 col-span-2">Belum ada barang diinventarisasikan.</p>';
        return;
    }

    grid.innerHTML = inventoryList.map(item => {
        const isLow = parseInt(item.stock) <= 5;
        const cardClass = isLow ? 'border-rose-500/30 bg-rose-950/10' : 'border-slate-800 bg-[#151c2c]';
        const badgeClass = isLow ? 'bg-rose-500/15 text-rose-400 border border-rose-500/20' : 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/20';

        return `
            <div class="border rounded-xl p-5 flex flex-col gap-3 shadow-sm ${cardClass}">
                <div class="flex items-start justify-between">
                    <div>
                        <span class="text-[10px] uppercase font-bold text-slate-500 tracking-wider">${item.category || 'Alat Kerja'}</span>
                        <h4 class="text-sm font-bold text-white mt-0.5">${item.name}</h4>
                    </div>
                    <span class="text-xs px-2.5 py-0.5 rounded-full font-semibold ${badgeClass}">
                        ${item.stock} ${item.unit || 'Unit'}
                    </span>
                </div>
                <div class="flex items-center justify-between border-t border-slate-800/80 pt-3 mt-1">
                    <span class="text-[10px] text-slate-400 flex items-center gap-1">
                        <i data-lucide="info" class="w-3.5 h-3.5 ${isLow ? 'text-rose-400 animate-pulse' : 'text-slate-500'}"></i>
                        ${isLow ? 'Stok kritis - segera restock!' : 'Stok aman & tercukupi'}
                    </span>
                    <div class="flex gap-1.5">
                        <button onclick="editInventoryItem('${item.id}')" class="p-1 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded transition" title="Edit/Restock"><i data-lucide="edit-3" class="w-3.5 h-3.5"></i></button>
                        <button onclick="deleteInventoryItem('${item.id}')" class="p-1 bg-rose-500/10 hover:bg-rose-500 text-rose-400 hover:text-white rounded transition" title="Hapus"><i data-lucide="trash-2" class="w-3.5 h-3.5"></i></button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
    lucide.createIcons();
}

function renderInventoryHistory(history) {
    const list = document.getElementById('inventory-history-list');
    if (history.length === 0) {
        list.innerHTML = '<p class="text-slate-500 text-xs text-center py-20">Belum ada riwayat keluar/masuk barang</p>';
        return;
    }

    list.innerHTML = history.slice(0, 15).map(h => {
        const isOut = h.type === 'OUT' || h.type === 'take';
        const colorClass = isOut ? 'text-rose-400' : 'text-emerald-400';
        const icon = isOut ? 'minus-circle' : 'plus-circle';

        return `
            <div class="flex items-start gap-3 bg-slate-900/50 border border-slate-800/60 p-3 rounded-lg hover:border-slate-700 transition">
                <i data-lucide="${icon}" class="w-4 h-4 mt-0.5 flex-shrink-0 ${colorClass}"></i>
                <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between gap-2">
                        <span class="text-xs font-bold text-white truncate">${h.itemName || 'Barang'}</span>
                        <span class="text-xs font-bold ${colorClass}">${isOut ? '-' : '+'}${h.quantity}</span>
                    </div>
                    <p class="text-[10px] text-slate-400 mt-0.5">Oleh: ${h.adminName} | Keperluan: ${h.notes || '-'}</p>
                    <span class="text-[9px] text-slate-500 font-medium block mt-1">${h.date || h.created_at || '-'}</span>
                </div>
            </div>
        `;
    }).join('');
    lucide.createIcons();
}

function openAddInventoryModal() {
    document.getElementById('form-add-item').reset();
    document.getElementById('inv-id').value = '';
    openModal('modal-add-item');
}

function editInventoryItem(id) {
    const item = inventoryList.find(i => i.id == id);
    if (!item) return;

    document.getElementById('inv-id').value = item.id;
    document.getElementById('inv-name').value = item.name || '';
    document.getElementById('inv-stock').value = item.stock || '0';
    document.getElementById('inv-unit').value = item.unit || 'Pcs';
    document.getElementById('inv-category').value = item.category || 'Perangkat';

    openModal('modal-add-item');
}

async function saveInventoryItem(e) {
    e.preventDefault();
    const id = document.getElementById('inv-id').value;
    const payload = {
        name: document.getElementById('inv-name').value,
        stock: parseInt(document.getElementById('inv-stock').value || '0'),
        unit: document.getElementById('inv-unit').value,
        category: document.getElementById('inv-category').value
    };

    showLoading(true);
    try {
        let res;
        if (id) {
            res = await fetch(`${API_BASE_URL}/api/inventory/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        } else {
            res = await fetch(`${API_BASE_URL}/api/inventory`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        }

        if (res.ok) {
            showToast('Barang inventaris berhasil disimpan!', 'success');
            closeModal('modal-add-item');
            await loadInventarisData();
            await loadDashboardData();
        } else {
            showToast('Gagal menyimpan barang di VPS.', 'error');
        }
    } catch (e) {
        showToast('Kesalahan jaringan database.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deleteInventoryItem(id) {
    if (!confirm('Apakah Anda ingin menghapus barang ini dari sistem inventaris?')) return;
    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/inventory/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Barang berhasil dihapus.', 'success');
            await loadInventarisData();
            await loadDashboardData();
        } else {
            showToast('Gagal menghapus data barang.', 'error');
        }
    } catch (e) { showToast('Jaringan bermasalah.', 'error'); }
    finally { showLoading(false); }
}

function openTakeItemModal() {
    document.getElementById('form-take-item').reset();
    openModal('modal-take-item');
}

async function saveItemTransaction(e) {
    e.preventDefault();
    const payload = {
        itemId: document.getElementById('take-itemId').value,
        adminName: document.getElementById('take-adminName').value,
        quantity: parseInt(document.getElementById('take-quantity').value || '1'),
        notes: document.getElementById('take-notes').value
    };

    // Find and check remaining stock first
    const item = inventoryList.find(i => i.id == payload.itemId);
    if (item && item.stock < payload.quantity) {
        alert(`Sisa stok ${item.name} hanya tinggal ${item.stock} unit. Tidak dapat mengambil melebihi stok!`);
        return;
    }

    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/inventory/take`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Log transaksi barang keluar tercatat!', 'success');
            closeModal('modal-take-item');
            await loadInventarisData();
            await loadDashboardData();
        } else {
            showToast('Gagal mengurangi stok barang di server.', 'error');
        }
    } catch (err) {
        showToast('Request timeout.', 'error');
    } finally {
        showLoading(false);
    }
}

// ----------------------------------------------------
// TAB 5: KEUANGAN LOGIC
// ----------------------------------------------------
async function loadKeuanganData() {
    try {
        const resAll = await fetch(`${API_BASE_URL}/api/pembukuan/all`);
        if (resAll.ok) {
            financeList = await resAll.json();
            renderKeuanganTable();
            calculateFinanceSummaries();
        }
    } catch (e) { console.error('Failed finances load', e); }
}

function renderKeuanganTable() {
    const tbody = document.getElementById('keuangan-table-body');
    if (financeList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="px-6 py-12 text-center text-slate-500">Mutasi keuangan kas kosong</td></tr>`;
        return;
    }

    tbody.innerHTML = financeList.slice(0, 50).map(f => {
        const isIncome = f.type === 'IN' || f.type === 'pemasukan';
        const colorClass = isIncome ? 'text-emerald-400' : 'text-rose-400';
        const formattedAmount = formatRupiah(f.amount || f.nominal || 0);

        return `
            <tr class="hover:bg-slate-900/40 border-b border-slate-800 transition">
                <td class="px-6 py-4 text-xs text-slate-400">${f.date || f.created_at || '-'}</td>
                <td class="px-6 py-4 font-bold text-white text-xs">${f.description || f.keterangan || '-'}</td>
                <td class="px-6 py-4"><span class="bg-slate-800 text-slate-300 px-2.5 py-0.5 rounded text-xs">${f.category || 'Sistem'}</span></td>
                <td class="px-6 py-4 text-xs text-slate-400">${f.adminName || f.admin || '-'}</td>
                <td class="px-6 py-4 text-right font-bold ${colorClass}">
                    ${isIncome ? '+' : '-'}${formattedAmount}
                </td>
            </tr>
        `;
    }).join('');
}

function calculateFinanceSummaries() {
    let income = 0;
    let outcome = 0;

    financeList.forEach(f => {
        const amt = parseFloat(f.amount || f.nominal || 0);
        if (f.type === 'IN' || f.type === 'pemasukan') {
            income += amt;
        } else {
            outcome += amt;
        }
    });

    document.getElementById('fin-total-pemasukan').innerText = formatRupiah(income);
    document.getElementById('fin-total-pengeluaran').innerText = formatRupiah(outcome);
    document.getElementById('fin-total-saldo').innerText = formatRupiah(income - outcome);
}

function openFinanceDialog(type) {
    document.getElementById('form-keuangan-trans').reset();
    document.getElementById('fin-type').value = type;
    
    const title = document.getElementById('modal-keuangan-title');
    const btn = document.getElementById('btn-save-fin');
    
    if (type === 'pemasukan') {
        title.innerText = 'Catat Pemasukan Kas Tunai';
        btn.className = "px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-medium transition shadow-md";
    } else {
        title.innerText = 'Catat Pengeluaran Operasional';
        btn.className = "px-5 py-2 bg-rose-600 hover:bg-rose-700 text-white rounded-lg text-sm font-medium transition shadow-md";
    }

    openModal('modal-keuangan-trans');
}

async function saveFinanceTransaction(e) {
    e.preventDefault();
    const type = document.getElementById('fin-type').value;
    const isIncome = type === 'pemasukan';
    const payload = {
        description: document.getElementById('fin-desc').value,
        category: document.getElementById('fin-category').value,
        adminName: document.getElementById('fin-admin').value,
        amount: parseFloat(document.getElementById('fin-amount').value || '0'),
        type: isIncome ? 'IN' : 'OUT'
    };

    const endpoint = isIncome ? 'pemasukan' : 'pengeluaran';

    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Aliran dana transaksi berhasil dibukukan!', 'success');
            closeModal('modal-keuangan-trans');
            await loadKeuanganData();
            await loadDashboardData();
        } else {
            showToast('Gagal membukukan transaksi di server.', 'error');
        }
    } catch (e) { showToast('Koneksi terhambat.', 'error'); }
    finally { showLoading(false); }
}

// ----------------------------------------------------
// TAB 6: CONFIGURATION LAYOUT (AREAS & PACKAGES)
// ----------------------------------------------------
function renderAreasTable() {
    const tbody = document.getElementById('area-table-body');
    tbody.innerHTML = areasList.map(a => `
        <tr class="hover:bg-slate-900/40 border-b border-slate-800 transition">
            <td class="px-4 py-3 font-semibold text-white text-sm">${a.name}</td>
            <td class="px-4 py-3 text-xs text-indigo-400 font-bold">${a.code || '-'}</td>
            <td class="px-4 py-3 text-right">
                <button onclick="deleteArea('${a.id}')" class="p-1 bg-rose-500/10 text-rose-400 hover:bg-rose-500 hover:text-white rounded transition"><i data-lucide="trash-2" class="w-3.5 h-3.5"></i></button>
            </td>
        </tr>
    `).join('');
    lucide.createIcons();
}

function renderPackagesTable() {
    const tbody = document.getElementById('package-table-body');
    tbody.innerHTML = packagesList.map(p => `
        <tr class="hover:bg-slate-900/40 border-b border-slate-800 transition">
            <td class="px-4 py-3 font-semibold text-white text-sm">${p.name}</td>
            <td class="px-4 py-3 text-xs text-slate-400 font-semibold">${p.speed || '-'}</td>
            <td class="px-4 py-3 text-xs text-emerald-400 font-bold">${formatRupiah(p.price || 0)}</td>
            <td class="px-4 py-3 text-right">
                <button onclick="deletePackage('${p.id}')" class="p-1 bg-rose-500/10 text-rose-400 hover:bg-rose-500 hover:text-white rounded transition"><i data-lucide="trash-2" class="w-3.5 h-3.5"></i></button>
            </td>
        </tr>
    `).join('');
    lucide.createIcons();
}

function openAddAreaModal() {
    document.getElementById('form-area').reset();
    openModal('modal-area');
}

async function saveArea(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('area-name').value,
        code: document.getElementById('area-code').value
    };

    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/areas`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Area operasional berhasil didaftarkan!', 'success');
            closeModal('modal-area');
            await loadAreas();
        } else {
            showToast('Gagal menambahkan area.', 'error');
        }
    } catch (e) { showToast('Gagal koneksi database.', 'error'); }
    finally { showLoading(false); }
}

async function deleteArea(id) {
    if (!confirm('Apakah anda yakin ingin menghapus area ini dari sistem?')) return;
    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/areas/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Area berhasil dihapus.', 'success');
            await loadAreas();
        } else {
            showToast('Gagal menghapus area.', 'error');
        }
    } catch (e) { showToast('Jaringan error.', 'error'); }
    finally { showLoading(false); }
}

function openAddPackageModal() {
    document.getElementById('form-package').reset();
    openModal('modal-package');
}

async function savePackage(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('pkg-name').value,
        speed: document.getElementById('pkg-speed').value,
        price: parseFloat(document.getElementById('pkg-price').value || '0')
    };

    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/packages`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Paket internet baru berhasil diterbitkan!', 'success');
            closeModal('modal-package');
            await loadPackages();
        } else {
            showToast('Gagal menerbitkan paket.', 'error');
        }
    } catch (e) { showToast('Koneksi terhambat.', 'error'); }
    finally { showLoading(false); }
}

async function deletePackage(id) {
    if (!confirm('Apakah anda ingin menutup paket layanan internet ini?')) return;
    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/packages/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Paket internet berhasil dihapus.', 'success');
            await loadPackages();
        } else {
            showToast('Gagal menghapus paket.', 'error');
        }
    } catch (e) { showToast('Request error.', 'error'); }
    finally { showLoading(false); }
}

// ----------------------------------------------------
// UI UTILS, DROPDOWNS POPULATE & MODALS HELPER
// ----------------------------------------------------
function populateAreasDropdowns() {
    const filters = ['cust-filter-area'];
    const inputs = ['cust-area'];
    
    const filterOptions = '<option value="">Semua Area</option>' + areasList.map(a => `<option value="${a.name}">${a.name}</option>`).join('');
    const inputOptions = areasList.map(a => `<option value="${a.name}">${a.name}</option>`).join('');

    filters.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.innerHTML = filterOptions;
    });

    inputs.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.innerHTML = inputOptions;
    });
}

function populatePackagesDropdowns() {
    const filters = ['cust-filter-paket'];
    const inputs = ['cust-packageName'];
    
    const filterOptions = '<option value="">Semua Paket</option>' + packagesList.map(p => `<option value="${p.name}">${p.name}</option>`).join('');
    const inputOptions = packagesList.map(p => `<option value="${p.name}">${p.name}</option>`).join('');

    filters.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.innerHTML = filterOptions;
    });

    inputs.forEach(id => {
        const el = document.getElementById(id);
        if (el) el.innerHTML = inputOptions;
    });
}

function populateOdcDropdowns() {
    const el = document.getElementById('net-odp-input');
    if (el) {
        el.innerHTML = odcList.map(o => `<option value="${o.name}">${o.name}</option>`).join('');
    }
}

function populateOdpDropdowns() {
    const el = document.getElementById('cust-odpId');
    if (el) {
        el.innerHTML = '<option value="">Tidak terhubung ke ODP</option>' + odpList.map(o => `<option value="${o.id}">${o.name}</option>`).join('');
    }
}

function populateTakeItemDropdowns() {
    const el = document.getElementById('take-itemId');
    if (el) {
        el.innerHTML = inventoryList.map(i => `<option value="${i.id}">${i.name} (Stok: ${i.stock} ${i.unit})</option>`).join('');
    }
}

// Generic Modal Helpers
function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.remove('hidden');
    }
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.add('hidden');
    }
}

// Format number to currency
function formatRupiah(number) {
    return new Intl.NumberFormat('id-ID', {
        style: 'currency',
        currency: 'IDR',
        maximumFractionDigits: 0
    }).format(number);
}
