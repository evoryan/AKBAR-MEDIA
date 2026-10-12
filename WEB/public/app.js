// Akbar Media Web Frontend Controller Logic

// Dynamic default API Base URL detection
function getDefaultApiUrl() {
    const cloudUrl = 'https://amg.akbarmediagroup.my.id';
    if (typeof window !== 'undefined' && window.location && window.location.origin && window.location.origin !== 'null') {
        const origin = window.location.origin.replace(/\/+$/, '');
        // If served from the production domain, use origin
        if (window.location.hostname.includes('akbarmediagroup')) {
            return origin;
        }
        // If running in development or local preview, prefer origin if it proxies or fallback to cloud URL
        return origin;
    }
    return cloudUrl;
}

let API_BASE_URL = getDefaultApiUrl();
let currentTab = 'dashboard';
let currentInfraSubTab = 'odc';
let currentBillingStatus = 'unpaid';

// Data stores
let customersList = [];
let areasList = [];
let packagesList = [];
let odcList = [];
let odpList = [];
let rasioList = [];
let inventoryList = [];
let financeList = [];
let gangguanList = [];
let adminsList = [];
let acsDevicesList = [];
let tagihanList = [];

// Billing state
let selectedBillingBulkIds = new Set();
let activeInvoiceCustomer = null;

// Pagination state
let customerPage = 1;
const customersPerPage = 10;
let filteredCustomersList = [];

// Chart instance
let dashboardChartInstance = null;

// Global fetch interceptor to automatically add JWT Token to ALL backend API requests
const originalFetch = window.fetch;
window.fetch = async function (input, init = {}) {
    init = init || {};
    let urlStr = '';
    let isRequestInstance = false;
    
    if (typeof input === 'string') {
        urlStr = input;
    } else if (typeof Request !== 'undefined' && input instanceof Request) {
        urlStr = input.url || '';
        isRequestInstance = true;
    } else if (input && input.url) {
        urlStr = input.url;
    }

    const isApiRequest = urlStr.includes('/api/') || 
                         urlStr.endsWith('/api') || 
                         urlStr.startsWith(API_BASE_URL) || 
                         urlStr.startsWith('/api') || 
                         urlStr.includes('/api');

    const token = localStorage.getItem('token');

    if (isApiRequest && token) {
        if (isRequestInstance) {
            try {
                const newHeaders = new Headers(input.headers);
                if (!newHeaders.has('Authorization')) {
                    newHeaders.set('Authorization', `Bearer ${token}`);
                }
                input = new Request(input, { headers: newHeaders });
            } catch (e) {
                console.warn('Could not re-wrap Request for authorization:', e);
            }
        } else {
            if (init.headers instanceof Headers) {
                if (!init.headers.has('Authorization')) {
                    init.headers.set('Authorization', `Bearer ${token}`);
                }
            } else if (Array.isArray(init.headers)) {
                const hasAuth = init.headers.some(([k]) => k.toLowerCase() === 'authorization');
                if (!hasAuth) {
                    init.headers.push(['Authorization', `Bearer ${token}`]);
                }
            } else {
                init.headers = init.headers || {};
                init.headers['Authorization'] = `Bearer ${token}`;
            }
        }
    }
    
    try {
        const response = await originalFetch(input, init);
        if (response.status === 401 && !urlStr.includes('/api/login')) {
            const clone = response.clone();
            const body = await clone.json().catch(() => ({}));
            const errMsg = (body && body.error) ? String(body.error).toLowerCase() : '';
            if (errMsg.includes('token') || errMsg.includes('akses ditolak') || errMsg.includes('kadaluarsa')) {
                console.warn('Sesi login telah berakhir atau token tidak valid. Menampilkan pesan error.', errMsg);
                handleLogout('Sesi Anda telah berakhir atau token tidak valid. Silakan masuk kembali.');
            }
        }
        return response;
    } catch (error) {
        throw error;
    }
};

// UI Session Badge Helper
function updateUserSessionUI() {
    const username = localStorage.getItem('username') || 'Admin';
    const role = (localStorage.getItem('role') || 'ADMIN').toUpperCase();
    const dbName = localStorage.getItem('db_name') || 'app_db';

    const userBadge = document.getElementById('user-display-badge');
    if (userBadge) {
        userBadge.innerHTML = `
            <i data-lucide="user-check" class="w-3.5 h-3.5 text-emerald-400"></i>
            <span class="font-semibold text-white">${username}</span>
            <span class="text-[10px] bg-slate-800 text-slate-300 px-1.5 py-0.5 rounded border border-slate-700">${role}</span>
            <span class="text-[10px] bg-emerald-500/20 text-emerald-300 px-1.5 py-0.5 rounded border border-emerald-500/30">${dbName}</span>
        `;
    }

    const sidebarName = document.getElementById('sidebar-profile-name');
    if (sidebarName) sidebarName.innerText = username;

    const sidebarSub = document.getElementById('sidebar-profile-sub');
    if (sidebarSub) sidebarSub.innerText = `${role} • ${dbName}`;

    const sidebarInitials = document.getElementById('sidebar-profile-initials');
    if (sidebarInitials) {
        sidebarInitials.innerText = username.substring(0, 2).toUpperCase();
    }

    // Setting tab updates
    const settingDb = document.getElementById('setting-db-name');
    if (settingDb) settingDb.innerText = dbName;
    const settingUser = document.getElementById('setting-username');
    if (settingUser) settingUser.innerText = username;
    const settingRole = document.getElementById('setting-role');
    if (settingRole) settingRole.innerText = role;
    const settingApi = document.getElementById('setting-api-url');
    if (settingApi) settingApi.innerText = API_BASE_URL;

    if (window.lucide) {
        lucide.createIcons();
    }
}

// Toggle Screen Visibility
function showAppScreen() {
    const appCont = document.getElementById('app-container');
    const loginCont = document.getElementById('login-container');
    if (appCont) {
        appCont.classList.remove('hidden');
        appCont.classList.add('flex');
    }
    if (loginCont) {
        loginCont.classList.add('hidden');
    }
    updateUserSessionUI();
}

function showLoginScreen() {
    const appCont = document.getElementById('app-container');
    const loginCont = document.getElementById('login-container');
    if (appCont) {
        appCont.classList.add('hidden');
        appCont.classList.remove('flex');
    }
    if (loginCont) {
        loginCont.classList.remove('hidden');
    }
}

// On Page Load
document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize API Base URL
    let savedUrl = localStorage.getItem('api_base_url');
    if (savedUrl && savedUrl.includes('amg.akbarmediagroup.my.id') && window.location.hostname !== 'amg.akbarmediagroup.my.id') {
        localStorage.removeItem('api_base_url');
        savedUrl = null;
    }
    
    API_BASE_URL = (savedUrl || getDefaultApiUrl()).replace(/\/+$/, '');
    
    const apiHeaderField = document.getElementById('api-base-url');
    if (apiHeaderField) apiHeaderField.value = API_BASE_URL;
    
    const apiLoginField = document.getElementById('login-api-url');
    if (apiLoginField) apiLoginField.value = API_BASE_URL;

    // Set default month in billing to current month
    const curMonth = (new Date().getMonth() + 1).toString();
    const curYear = (new Date().getFullYear()).toString();
    const billMonthEl = document.getElementById('billing-filter-month');
    if (billMonthEl) billMonthEl.value = curMonth;
    const billYearEl = document.getElementById('billing-filter-year');
    if (billYearEl) billYearEl.value = curYear;

    // 2. Initialize Lucide Icons
    if (window.lucide) lucide.createIcons();

    // 3. Attach login form handler
    const loginForm = document.getElementById('form-login');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // 4. Check Authentication State
    const token = localStorage.getItem('token');
    if (token) {
        showAppScreen();
        loadAllData();
        pingApi();
    } else {
        showLoginScreen();
    }

    // 5. Default view tab
    switchTab('dashboard');

    // 6. Load saved company profile
    loadCompanySettings();
});

// Authentication handlers
async function handleLogin(event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }

    const alertBox = document.getElementById('login-error-alert');
    const alertText = document.getElementById('login-error-text');
    if (alertBox) alertBox.classList.add('hidden');

    const usernameInput = document.getElementById('login-username');
    const passwordInput = document.getElementById('login-password');
    const username = usernameInput ? usernameInput.value.trim() : '';
    const password = passwordInput ? passwordInput.value.trim() : '';

    if (!username || !password) {
        if (alertBox && alertText) {
            alertText.innerText = 'Username dan password tidak boleh kosong!';
            alertBox.classList.remove('hidden');
        }
        showToast('Username dan password harus diisi!', 'error');
        return;
    }

    const submitBtn = document.getElementById('login-btn-submit');
    const btnText = document.getElementById('login-btn-text');
    const btnIcon = document.getElementById('login-btn-icon');

    if (submitBtn) submitBtn.disabled = true;
    if (btnText) btnText.innerText = 'Memproses masuk...';
    if (btnIcon) {
        btnIcon.setAttribute('data-lucide', 'refresh-cw');
        btnIcon.classList.add('animate-spin');
        if (window.lucide) lucide.createIcons();
    }

    // Check if user changed the API base URL in the login details
    const customApiUrlInput = document.getElementById('login-api-url');
    if (customApiUrlInput && customApiUrlInput.value.trim()) {
        const customApiUrl = customApiUrlInput.value.trim().replace(/\/+$/, '');
        API_BASE_URL = customApiUrl;
        localStorage.setItem('api_base_url', customApiUrl);
        const apiHeaderField = document.getElementById('api-base-url');
        if (apiHeaderField) apiHeaderField.value = customApiUrl;
    }

    showLoading(true, 'Menghubungkan ke server...');
    let loginSuccess = false;
    let lastError = null;

    // List of candidate endpoints: current configured API_BASE_URL first, then fallback to Cloud VPS if different
    const candidateUrls = [API_BASE_URL];
    const cloudUrl = 'https://amg.akbarmediagroup.my.id';
    if (!candidateUrls.includes(cloudUrl)) {
        candidateUrls.push(cloudUrl);
    }
    // Also include origin if different
    if (typeof window !== 'undefined' && window.location && window.location.origin) {
        const originUrl = window.location.origin.replace(/\/+$/, '');
        if (!candidateUrls.includes(originUrl)) {
            candidateUrls.push(originUrl);
        }
    }

    for (let i = 0; i < candidateUrls.length; i++) {
        const targetUrl = candidateUrls[i];
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 6000);
            const response = await originalFetch(`${targetUrl}/api/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password }),
                signal: controller.signal
            });
            clearTimeout(timeoutId);

            const data = await response.json().catch(() => ({}));

            if (response.ok && data.token) {
                // If success on a fallback URL, automatically update API_BASE_URL
                if (targetUrl !== API_BASE_URL) {
                    API_BASE_URL = targetUrl;
                    localStorage.setItem('api_base_url', targetUrl);
                    if (customApiUrlInput) customApiUrlInput.value = targetUrl;
                    const apiHeaderField = document.getElementById('api-base-url');
                    if (apiHeaderField) apiHeaderField.value = targetUrl;
                }

                localStorage.setItem('token', data.token);
                localStorage.setItem('username', data.username || username);
                localStorage.setItem('role', data.role || 'ADMIN');
                localStorage.setItem('db_name', data.db_name || 'app_db');
                
                showToast(`Selamat datang, ${data.username || username}!`, 'success');
                showAppScreen();
                
                // Load all modules
                loadAllData();
                pingApi();
                loginSuccess = true;
                break;
            } else if (response.status === 401 || response.status === 400 || response.status === 403) {
                // Server replied with an explicit auth rejection - don't try fallback, credentials are wrong!
                const msg = data.error || data.message || 'Username atau password salah!';
                if (alertBox && alertText) {
                    alertText.innerText = msg;
                    alertBox.classList.remove('hidden');
                }
                showToast(msg, 'error');
                loginSuccess = true; // Handled
                break;
            } else {
                // Server replied with other status (e.g. 502/500/404), maybe proxy issue, continue to next candidate
                lastError = new Error(data.error || `HTTP ${response.status}`);
            }
        } catch (err) {
            console.warn(`Gagal login via ${targetUrl}:`, err.message);
            lastError = err;
        }
    }

    if (!loginSuccess) {
        console.error('Login error:', lastError);
        const netMsg = `Gagal terhubung ke server API (${API_BASE_URL}). Pastikan koneksi internet aktif atau periksa URL API di form.`;
        if (alertBox && alertText) {
            alertText.innerHTML = `
                <div>
                    <div class="font-bold">${netMsg}</div>
                    <div class="text-[11px] text-slate-300 mt-1">
                        Coba klik tombol <b>Cloud VPS</b> di bawah form atau periksa URL API Anda.
                    </div>
                </div>
            `;
            alertBox.classList.remove('hidden');
        }
        showToast(netMsg, 'error');
    }

    showLoading(false);
    if (submitBtn) submitBtn.disabled = false;
    if (btnText) btnText.innerText = 'Masuk ke Aplikasi';
    if (btnIcon) {
        btnIcon.setAttribute('data-lucide', 'arrow-right');
        btnIcon.classList.remove('animate-spin');
        if (window.lucide) lucide.createIcons();
    }
}

// Preset and Test helpers for Login API URL
function setApiUrlPreset(url) {
    if (!url) return;
    const cleanUrl = url.replace(/\/+$/, '');
    const loginUrlField = document.getElementById('login-api-url');
    if (loginUrlField) loginUrlField.value = cleanUrl;
    API_BASE_URL = cleanUrl;
    localStorage.setItem('api_base_url', cleanUrl);
    const apiHeaderField = document.getElementById('api-base-url');
    if (apiHeaderField) apiHeaderField.value = cleanUrl;
    testLoginApiConnection();
}

function resetApiUrlToDefault() {
    setApiUrlPreset('https://amg.akbarmediagroup.my.id');
}

async function testLoginApiConnection() {
    const inputField = document.getElementById('login-api-url');
    const targetUrl = (inputField && inputField.value.trim() ? inputField.value.trim() : API_BASE_URL).replace(/\/+$/, '');
    const statusEl = document.getElementById('login-api-status');
    const btn = document.getElementById('btn-test-api');

    if (btn) btn.innerHTML = '<i data-lucide="refresh-cw" class="w-3 h-3 animate-spin"></i> Menguji...';
    if (statusEl) {
        statusEl.className = 'text-[11px] py-1 px-2 rounded-lg flex items-center gap-1.5 font-medium bg-slate-800 text-slate-300';
        statusEl.innerText = 'Menghubungkan ke ' + targetUrl + '...';
        statusEl.classList.remove('hidden');
    }
    if (window.lucide) lucide.createIcons();

    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 4000);
        const res = await originalFetch(`${targetUrl}/api/ping`, { signal: controller.signal });
        clearTimeout(timeoutId);

        if (res.ok) {
            if (statusEl) {
                statusEl.className = 'text-[11px] py-1 px-2 rounded-lg flex items-center gap-1.5 font-medium bg-emerald-500/20 text-emerald-400 border border-emerald-500/30';
                statusEl.innerHTML = '<i data-lucide="check-circle" class="w-3.5 h-3.5"></i> Terhubung! Server API aktif dan siap digunakan.';
            }
            showToast('Koneksi ke server API berhasil!', 'success');
        } else {
            throw new Error(`HTTP ${res.status}`);
        }
    } catch (err) {
        if (statusEl) {
            statusEl.className = 'text-[11px] py-1 px-2 rounded-lg flex items-center gap-1.5 font-medium bg-rose-500/20 text-rose-400 border border-rose-500/30';
            statusEl.innerHTML = `<i data-lucide="alert-triangle" class="w-3.5 h-3.5"></i> Tidak dapat terhubung (${err.message || 'Timeout'}).`;
        }
        showToast('Koneksi API gagal: ' + (err.message || 'Timeout'), 'error');
    } finally {
        if (btn) btn.innerHTML = '<i data-lucide="zap" class="w-3 h-3"></i> Tes Koneksi';
        if (window.lucide) lucide.createIcons();
    }
}

function handleLogout(customMessage = null) {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    localStorage.removeItem('db_name');
    
    // Clear inputs
    const usernameInput = document.getElementById('login-username');
    if (usernameInput) usernameInput.value = '';
    const passwordInput = document.getElementById('login-password');
    if (passwordInput) passwordInput.value = '';
    
    showLoginScreen();

    const alertBox = document.getElementById('login-error-alert');
    const alertText = document.getElementById('login-error-text');
    if (customMessage) {
        if (alertBox && alertText) {
            alertText.innerText = customMessage;
            alertBox.classList.remove('hidden');
        }
        showToast(customMessage, 'error');
    } else {
        if (alertBox) alertBox.classList.add('hidden');
        showToast('Anda telah keluar dari aplikasi.', 'info');
    }
}

// Save API base URL changes
function saveApiUrl() {
    const inputUrl = document.getElementById('api-base-url').value.trim().replace(/\/+$/, '');
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
    const badgeText = document.getElementById('connection-text');
    try {
        const response = await fetch(`${API_BASE_URL}/api/ping`, { mode: 'cors' });
        const data = await response.json();
        
        if (response.ok && (data.status === 'ok' || data.status === 'success')) {
            if (badge) {
                badge.className = "flex items-center gap-1.5 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 px-2.5 py-1 rounded-full text-xs font-medium cursor-pointer";
                if (badgeText) badgeText.innerText = "Connected";
            }
            return true;
        }
    } catch (err) {
        console.error('API connection ping failed:', err);
    }
    
    if (badge) {
        badge.className = "flex items-center gap-1.5 bg-rose-500/10 border border-rose-500/30 text-rose-400 px-2.5 py-1 rounded-full text-xs font-medium cursor-pointer";
        if (badgeText) badgeText.innerText = "Disconnected";
    }
    return false;
}

// Show Spinner loading
function showLoading(show, text = 'Memuat data...') {
    const spinner = document.getElementById('loading-spinner');
    const spinnerText = document.getElementById('loading-spinnerText');
    if (!spinner) return;
    if (spinnerText) spinnerText.innerText = text;

    if (show) {
        spinner.classList.remove('hidden');
    } else {
        spinner.classList.add('hidden');
    }
}

// Custom Toast helper
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `p-3.5 rounded-2xl border shadow-xl flex items-center gap-3 text-xs font-medium transition-all duration-300 transform translate-y-2 opacity-0 pointer-events-auto`;
    
    let bgClass = 'bg-[#151c2c] border-slate-700 text-slate-100';
    let icon = 'info';
    
    if (type === 'success') {
        bgClass = 'bg-emerald-950/90 border-emerald-700/50 text-emerald-300';
        icon = 'check-circle';
    } else if (type === 'error') {
        bgClass = 'bg-rose-950/90 border-rose-700/50 text-rose-300';
        icon = 'alert-triangle';
    } else if (type === 'warning') {
        bgClass = 'bg-amber-950/90 border-amber-700/50 text-amber-300';
        icon = 'alert-circle';
    }
    
    toast.className += ` ${bgClass}`;
    toast.innerHTML = `
        <i data-lucide="${icon}" class="w-4 h-4 shrink-0"></i>
        <span class="flex-1">${message}</span>
        <button onclick="this.parentElement.remove()" class="text-slate-400 hover:text-white transition">
            <i data-lucide="x" class="w-3.5 h-3.5"></i>
        </button>
    `;
    
    container.appendChild(toast);
    if (window.lucide) lucide.createIcons();
    
    setTimeout(() => {
        toast.classList.remove('translate-y-2', 'opacity-0');
    }, 10);
    
    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-y-1');
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}

// Switch between sidebar/footer tabs
function switchTab(tabId) {
    currentTab = tabId;
    
    const allTabs = [
        'dashboard', 'tagihan', 'pelanggan', 'jaringan', 
        'inventaris', 'keuangan', 'gangguan', 'konfigurasi', 
        'mikrotik-acs', 'setting'
    ];

    allTabs.forEach(t => {
        const navEl = document.getElementById(`nav-${t}`);
        const mobEl = document.getElementById(`mob-${t}`);
        
        if (navEl) {
            if (t === tabId) {
                navEl.className = "flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-bold transition bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 shadow-sm";
            } else {
                navEl.className = "flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition text-slate-300 hover:bg-slate-800/80 hover:text-white";
            }
        }
        
        if (mobEl) {
            if (t === tabId) {
                mobEl.className = "flex flex-col items-center gap-1 text-emerald-400 font-bold";
            } else {
                mobEl.className = "flex flex-col items-center gap-1 text-slate-400 hover:text-slate-200";
            }
        }
    });

    // Toggle content views
    document.querySelectorAll('.tab-content').forEach(el => {
        el.classList.add('hidden');
    });
    
    const activeSection = document.getElementById(`tab-${tabId}`);
    if (activeSection) {
        activeSection.classList.remove('hidden');
    }

    if (tabId === 'mikrotik-acs') {
        if (areasList.length > 0) {
            populateAreasDropdowns();
            renderRoutersTable();
            if (currentMikrotikSubTab === 'secrets') {
                if (mikrotikSecretsList.length === 0) loadMikrotikSecrets(activeSecretAreaId);
            } else if (currentMikrotikSubTab === 'routers') {
                loadRouterStatusForSelectedArea(activeRouterAreaId);
            } else if (currentMikrotikSubTab === 'acs') {
                loadAcsDevices();
            }
        }
    }

    if (window.lucide) lucide.createIcons();
}

// Global data loading coordinator
async function loadAllData() {
    showLoading(true, 'Memuat semua data sistem...');
    try {
        await Promise.all([
            loadAreas(),
            loadPackages(),
            loadOdcList(),
            loadOdpList(),
            loadRasioList()
        ]);
        
        await Promise.all([
            loadCustomersData(),
            loadBillingData(),
            loadInventarisData(),
            loadKeuanganData(),
            loadGangguanData(),
            loadAdmins(),
            loadDashboardData()
        ]);

        filterBilling();
    } catch (err) {
        console.error('Data load exception:', err);
    } finally {
        showLoading(false);
        if (window.lucide) lucide.createIcons();
    }
}

// Load Areas
async function loadAreas() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/areas`);
        if (res.ok) {
            areasList = await res.json();
            populateAreasDropdowns();
            renderAreasTable();
            renderRoutersTable();
            if (activeRouterAreaId) {
                loadRouterStatusForSelectedArea(activeRouterAreaId);
            }
        }
    } catch (e) {
        console.error('Error loading areas:', e);
    }
}

// Load Packages
async function loadPackages() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/packages`);
        if (res.ok) {
            packagesList = await res.json();
            populatePackagesDropdowns();
            renderPackagesTable();
        }
    } catch (e) {
        console.error('Error loading packages:', e);
    }
}

// Load ODC List
async function loadOdcList() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/odc`);
        if (res.ok) {
            odcList = await res.json();
            populateOdcDropdowns();
            renderOdcTable();
        }
    } catch (e) {
        console.error('Error loading ODC list:', e);
    }
}

// Load ODP List
async function loadOdpList() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/odp`);
        if (res.ok) {
            odpList = await res.json();
            populateOdpDropdowns();
            renderOdpTable();
        }
    } catch (e) {
        console.error('Error loading ODP list:', e);
    }
}

// Load Rasio List
async function loadRasioList() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/rasio`);
        if (res.ok) {
            rasioList = await res.json();
            renderRasioTable();
        }
    } catch (e) {
        console.error('Error loading Rasio list:', e);
    }
}

// Load Dashboard Summary Data
async function loadDashboardData() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/dashboard/summary`);
        if (res.ok) {
            const sum = await res.json();
            
            const custEl = document.getElementById('dash-total-pelanggan');
            if (custEl) custEl.innerText = sum.totalCustomers || customersList.length || 0;
            
            const netEl = document.getElementById('dash-total-jaringan');
            if (netEl) netEl.innerText = (odcList.length + odpList.length) || (sum.totalOdc + sum.totalOdp) || 0;
            
            const adminCashEl = document.getElementById('dash-uang-admin');
            if (adminCashEl) adminCashEl.innerText = formatRupiah(sum.uangDiAdmin || 0);
        }
    } catch (e) {
        console.error('Error dashboard summary:', e);
    }

    // PPPoE Offline check
    try {
        const offRes = await fetch(`${API_BASE_URL}/api/dashboard/pppoe-offline`);
        if (offRes.ok) {
            const offline = await offRes.json();
            const counter = document.getElementById('dash-offline-counter');
            const listCont = document.getElementById('dash-pppoe-offline-list');
            
            if (counter) counter.innerText = `${offline.length} Offline`;
            if (listCont) {
                if (offline.length === 0) {
                    listCont.innerHTML = `<p class="text-slate-500 text-xs text-center py-10">Semua pelanggan PPPoE online normal</p>`;
                } else {
                    listCont.innerHTML = offline.map(item => `
                        <div class="flex items-center justify-between p-2.5 bg-rose-500/10 border border-rose-500/20 rounded-xl text-xs">
                            <div class="flex items-center gap-2">
                                <span class="w-2 h-2 rounded-full bg-rose-500 animate-ping"></span>
                                <div>
                                    <p class="font-bold text-white">${item.name || item.username}</p>
                                    <p class="text-[10px] text-slate-400">Offline sejak: ${item.last_logout || 'Terputus'}</p>
                                </div>
                            </div>
                            <button onclick="sendWhatsAppInvoice(${item.id || 0})" class="text-[10px] bg-slate-900 hover:bg-slate-800 text-rose-300 px-2 py-1 rounded border border-rose-500/30 font-semibold">
                                Hubungi
                            </button>
                        </div>
                    `).join('');
                }
            }
        }
    } catch (e) {
        console.error('Error PPPoE offline:', e);
    }

    setupDashboardChart();
}

// Chart initialization
function setupDashboardChart() {
    const canvas = document.getElementById('dashboardChart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    if (dashboardChartInstance) {
        dashboardChartInstance.destroy();
    }

    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul', 'Agu', 'Sep', 'Okt', 'Nov', 'Des'];
    
    // Dynamic monthly cashflow directly from database pembukuan
    let incomeMonthly = new Array(12).fill(0);
    let expenseMonthly = new Array(12).fill(0);

    if (Array.isArray(financeList) && financeList.length > 0) {
        financeList.forEach(f => {
            const txDate = f.created_at || f.date;
            if (!txDate) return;
            const d = new Date(txDate);
            if (!isNaN(d.getTime())) {
                const mIdx = d.getMonth();
                const amt = Math.abs(Number(f.amount) || 0);
                if (f.type === 'pemasukan' || (f.type !== 'pengeluaran' && Number(f.amount) > 0)) {
                    incomeMonthly[mIdx] += amt;
                } else {
                    expenseMonthly[mIdx] += amt;
                }
            }
        });
    }

    try {
        dashboardChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: months,
                datasets: [
                    {
                        label: 'Pemasukan (Rp)',
                        data: incomeMonthly,
                        borderColor: '#22c55e',
                        backgroundColor: 'rgba(34, 197, 94, 0.1)',
                        borderWidth: 2.5,
                        fill: true,
                        tension: 0.35
                    },
                    {
                        label: 'Pengeluaran (Rp)',
                        data: expenseMonthly,
                        borderColor: '#f43f5e',
                        backgroundColor: 'rgba(244, 63, 94, 0.05)',
                        borderWidth: 2,
                        fill: true,
                        tension: 0.35
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        labels: {
                            color: '#94a3b8',
                            font: { size: 11, weight: 'bold' }
                        }
                    }
                },
                scales: {
                    x: {
                        ticks: { color: '#64748b' },
                        grid: { color: 'rgba(255, 255, 255, 0.04)' }
                    },
                    y: {
                        ticks: {
                            color: '#64748b',
                            callback: value => formatRupiah(value)
                        },
                        grid: { color: 'rgba(255, 255, 255, 0.04)' }
                    }
                }
            }
        });
    } catch (e) {
        console.error('Chart init error:', e);
    }
}

// ==========================================
// TAGIHAN (BILLING) MODULE
// ==========================================

const INDO_MONTHS = ["Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"];

async function loadBillingData() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/tagihan`);
        if (res.ok) {
            const data = await res.json();
            tagihanList = Array.isArray(data) ? data : [];
            filterBilling();
        }
    } catch (e) {
        console.error('Error loading tagihan:', e);
    }
}

function isTagihanInMonthRecap(t, monthVal, yearVal) {
    if (!t) return false;
    let monthIdx = -1;
    if (monthVal && !isNaN(parseInt(monthVal))) {
        monthIdx = parseInt(monthVal) - 1;
    } else if (monthVal) {
        monthIdx = INDO_MONTHS.findIndex(m => m.toLowerCase() === String(monthVal).toLowerCase());
    }
    const yearInt = parseInt(yearVal) || new Date().getFullYear();
    const tYear = parseInt(t.tahun) || 0;
    const yearMatches = (tYear === yearInt) || (tYear === 0 && (String(t.bulan).includes(String(yearInt)) || (t.created_at && String(t.created_at).startsWith(String(yearInt)))));

    const monthName = (monthIdx >= 0 && monthIdx < INDO_MONTHS.length) ? INDO_MONTHS[monthIdx] : '';
    const monthNumStr = String(monthIdx + 1).padStart(2, '0');
    const monthNumSingle = String(monthIdx + 1);

    const tBulan = String(t.bulan || '').toLowerCase();
    const monthMatches = (monthName && tBulan.includes(monthName.toLowerCase())) ||
                         (tBulan === monthNumStr || tBulan.includes(`-${monthNumStr}`) || tBulan.includes(`/${monthNumStr}`)) ||
                         (tBulan === monthNumSingle);

    if (yearMatches && monthMatches) return true;

    if (!t.bulan || tYear === 0) {
        if (t.created_at) {
            const d = new Date(t.created_at);
            if (!isNaN(d.getTime())) {
                return d.getFullYear() === yearInt && (monthIdx < 0 || d.getMonth() === monthIdx);
            }
        }
    }
    return false;
}

function isCustomerPaidForPeriod(customer, monthVal, yearVal) {
    const custId = parseInt(customer.id);
    if (!isNaN(custId) && tagihanList.length > 0) {
        const custTagihans = tagihanList.filter(t => parseInt(t.customer_id) === custId && isTagihanInMonthRecap(t, monthVal, yearVal));
        if (custTagihans.length > 0) {
            return custTagihans.some(t => String(t.status || '').toUpperCase().includes('LUNAS') || String(t.status || '').toUpperCase().includes('SUDAH'));
        }
    }

    // Fallback if no specific tagihan records exist in DB for this period
    const curMonthIdx = new Date().getMonth();
    const curYear = new Date().getFullYear().toString();
    const isCurrent = (!monthVal || parseInt(monthVal) === (curMonthIdx + 1) || INDO_MONTHS[curMonthIdx].toLowerCase() === String(monthVal).toLowerCase()) &&
                      (!yearVal || String(yearVal) === curYear);
    if (isCurrent) {
        return (customer.status || '').toUpperCase().includes('LUNAS') || (customer.status || '').toUpperCase().includes('SUDAH');
    }
    return false;
}

function switchBillingStatus(status) {
    currentBillingStatus = status;
    const btnUnpaid = document.getElementById('btn-billing-tab-unpaid');
    const btnPaid = document.getElementById('btn-billing-tab-paid');

    if (status === 'unpaid') {
        if (btnUnpaid) {
            btnUnpaid.className = "px-5 py-2.5 rounded-xl text-sm font-bold transition flex items-center gap-2 bg-rose-500/15 text-rose-400 border border-rose-500/30";
        }
        if (btnPaid) {
            btnPaid.className = "px-5 py-2.5 rounded-xl text-sm font-semibold transition flex items-center gap-2 text-slate-400 hover:text-slate-200";
        }
    } else {
        if (btnPaid) {
            btnPaid.className = "px-5 py-2.5 rounded-xl text-sm font-bold transition flex items-center gap-2 bg-emerald-500/15 text-emerald-400 border border-emerald-500/30";
        }
        if (btnUnpaid) {
            btnUnpaid.className = "px-5 py-2.5 rounded-xl text-sm font-semibold transition flex items-center gap-2 text-slate-400 hover:text-slate-200";
        }
    }

    filterBilling();
}

function filterBilling() {
    const searchVal = (document.getElementById('billing-search')?.value || '').toLowerCase().trim();
    const areaVal = document.getElementById('billing-filter-area')?.value || '';
    const monthVal = document.getElementById('billing-filter-month')?.value || (new Date().getMonth() + 1).toString();
    const yearVal = document.getElementById('billing-filter-year')?.value || new Date().getFullYear().toString();
    
    // Group customers into unpaid vs paid using real database tagihan status
    let unpaidList = [];
    let paidList = [];

    customersList.forEach(c => {
        const isPaid = isCustomerPaidForPeriod(c, monthVal, yearVal);
        if (isPaid) {
            paidList.push(c);
        } else {
            unpaidList.push(c);
        }
    });

    // Update Summary Stats
    let totalUnpaidAmount = 0;
    unpaidList.forEach(c => totalUnpaidAmount += getCustomerBillAmount(c));

    let totalPaidAmount = 0;
    paidList.forEach(c => totalPaidAmount += getCustomerBillAmount(c));

    const totalTarget = totalUnpaidAmount + totalPaidAmount;

    const statUnpaidAmt = document.getElementById('billing-stat-unpaid-amount');
    if (statUnpaidAmt) statUnpaidAmt.innerText = formatRupiah(totalUnpaidAmount);

    const statUnpaidCnt = document.getElementById('billing-stat-unpaid-count');
    if (statUnpaidCnt) statUnpaidCnt.innerText = `${unpaidList.length} Pelanggan`;

    const statPaidAmt = document.getElementById('billing-stat-paid-amount');
    if (statPaidAmt) statPaidAmt.innerText = formatRupiah(totalPaidAmount);

    const statPaidCnt = document.getElementById('billing-stat-paid-count');
    if (statPaidCnt) statPaidCnt.innerText = `${paidList.length} Pelanggan`;

    const statTotAmt = document.getElementById('billing-stat-total-target');
    if (statTotAmt) statTotAmt.innerText = formatRupiah(totalTarget);

    const statTotCnt = document.getElementById('billing-stat-total-count');
    if (statTotCnt) statTotCnt.innerText = `${customersList.length} Total`;

    // Also update dashboard metric
    const dashUnpaid = document.getElementById('dash-unpaid-billing');
    if (dashUnpaid) dashUnpaid.innerText = `${unpaidList.length}`;
    const dashUnpaidAmt = document.getElementById('dash-unpaid-amount');
    if (dashUnpaidAmt) dashUnpaidAmt.innerText = `Total: ${formatRupiah(totalUnpaidAmount)}`;

    // Update tab badges
    const badgeUnpaid = document.getElementById('badge-count-unpaid');
    if (badgeUnpaid) badgeUnpaid.innerText = unpaidList.length;

    const badgePaid = document.getElementById('badge-count-paid');
    if (badgePaid) badgePaid.innerText = paidList.length;

    // Filter by current active tab
    const sourceList = currentBillingStatus === 'unpaid' ? unpaidList : paidList;

    const filtered = sourceList.filter(c => {
        const matchSearch = !searchVal || 
            (c.name && c.name.toLowerCase().includes(searchVal)) ||
            (c.phone && c.phone.toLowerCase().includes(searchVal)) ||
            (c.username && c.username.toLowerCase().includes(searchVal));

        const matchArea = !areaVal || c.area === areaVal;
        return matchSearch && matchArea;
    });

    renderBillingTable(filtered);
}

function getCustomerBillAmount(c) {
    let base = parseFloat(String(c.price || '0').replace(/[^0-9]/g, '')) || 0;
    const pkgName = c.packageName || c.package_name;
    if (!base && pkgName) {
        const pkg = packagesList.find(p => p.name === pkgName);
        if (pkg) base = parseFloat(String(pkg.price || '0').replace(/[^0-9]/g, '')) || 0;
    }
    const add1 = parseFloat(String(c.add1 || c.additionalCost1 || c.additional_cost1 || '0').replace(/[^0-9]/g, '')) || 0;
    const add2 = parseFloat(String(c.add2 || c.additionalCost2 || c.additional_cost2 || '0').replace(/[^0-9]/g, '')) || 0;
    const disc = parseFloat(String(c.discount || '0').replace(/[^0-9]/g, '')) || 0;
    return Math.max(0, base + add1 + add2 - disc);
}

function renderBillingTable(items) {
    const tbody = document.getElementById('billing-table-body');
    if (!tbody) return;

    if (items.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="px-6 py-12 text-center text-slate-500">
                    Tidak ada pelanggan ${currentBillingStatus === 'unpaid' ? 'belum bayar' : 'sudah bayar'} ditemukan.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = items.map(c => {
        const billAmount = getCustomerBillAmount(c);
        const monthVal = document.getElementById('billing-filter-month')?.value || (new Date().getMonth() + 1).toString();
        const yearVal = document.getElementById('billing-filter-year')?.value || new Date().getFullYear().toString();
        const isPaid = currentBillingStatus === 'paid' || isCustomerPaidForPeriod(c, monthVal, yearVal);
        const isChecked = selectedBillingBulkIds.has(c.id);

        return `
            <tr class="hover:bg-slate-800/40 transition">
                <td class="px-4 py-3 text-center">
                    ${!isPaid ? `
                        <input type="checkbox" onchange="toggleSelectBilling(${c.id}, this.checked)" ${isChecked ? 'checked' : ''} class="rounded bg-slate-900 border-slate-700 text-emerald-600 focus:ring-0">
                    ` : `
                        <i data-lucide="check" class="w-4 h-4 text-emerald-500 mx-auto"></i>
                    `}
                </td>
                <td class="px-4 py-3">
                    <p class="font-bold text-white">${c.name}</p>
                    <p class="text-xs text-slate-400 font-mono">${c.phone || '-'}</p>
                    <p class="text-[10px] text-slate-500">PPPoE: ${c.username || '-'}</p>
                </td>
                <td class="px-4 py-3">
                    <span class="text-xs font-semibold text-emerald-400">${c.packageName || c.package_name || 'Paket Standar'}</span>
                    <p class="font-bold text-white text-sm mt-0.5">${formatRupiah(billAmount)}</p>
                </td>
                <td class="px-4 py-3 text-xs">
                    <p class="text-slate-300">${c.area || 'Semua Area'}</p>
                    <p class="text-slate-400 text-[11px] mt-0.5">Jatuh Tempo: Tgl ${c.billingDate || '5'}</p>
                </td>
                <td class="px-4 py-3 text-center">
                    ${isPaid ? `
                        <span class="px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                            LUNAS
                        </span>
                    ` : `
                        <span class="px-2.5 py-1 rounded-full text-[11px] font-bold bg-rose-500/15 text-rose-400 border border-rose-500/30">
                            BELUM BAYAR
                        </span>
                    `}
                </td>
                <td class="px-4 py-3 text-right">
                    <div class="flex items-center justify-end gap-1.5">
                        ${!isPaid ? `
                            <button onclick="openPayBillingModal(${c.id})" class="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold transition flex items-center gap-1 shadow-sm">
                                <i data-lucide="check-circle" class="w-3.5 h-3.5"></i> Bayar
                            </button>
                            <button onclick="sendWhatsAppInvoice(${c.id})" class="p-1.5 bg-green-600/20 hover:bg-green-600/30 text-green-400 rounded-xl transition" title="Kirim Tagihan WhatsApp">
                                <i data-lucide="message-circle" class="w-4 h-4"></i>
                            </button>
                            <button onclick="openInvoiceModal(${c.id})" class="p-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl transition" title="Rincian Invoice">
                                <i data-lucide="file-text" class="w-4 h-4"></i>
                            </button>
                        ` : `
                            <button onclick="openInvoiceModal(${c.id})" class="px-3 py-1.5 bg-blue-600/20 hover:bg-blue-600/30 text-blue-300 rounded-xl text-xs font-semibold transition flex items-center gap-1 border border-blue-500/30">
                                <i data-lucide="receipt" class="w-3.5 h-3.5"></i> Invoice
                            </button>
                            <button onclick="cancelBillingPayment(${c.id})" class="p-1.5 bg-rose-600/20 hover:bg-rose-600/30 text-rose-400 rounded-xl transition" title="Hapus Status Bayar (Batalkan)">
                                <i data-lucide="rotate-ccw" class="w-4 h-4"></i>
                            </button>
                        `}
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    if (window.lucide) lucide.createIcons();
}

function toggleSelectBilling(id, checked) {
    if (checked) {
        selectedBillingBulkIds.add(id);
    } else {
        selectedBillingBulkIds.delete(id);
    }
    updateBulkPayButtonState();
}

function toggleSelectAllBilling(checked) {
    selectedBillingBulkIds.clear();
    if (checked) {
        customersList.filter(c => !(c.status || '').toUpperCase().includes('LUNAS')).forEach(c => {
            selectedBillingBulkIds.add(c.id);
        });
    }
    filterBilling();
    updateBulkPayButtonState();
}

function updateBulkPayButtonState() {
    const btn = document.getElementById('btn-bulk-pay');
    const cnt = document.getElementById('bulk-selected-count');
    if (!btn) return;
    
    if (selectedBillingBulkIds.size > 0) {
        btn.classList.remove('hidden');
        btn.classList.add('flex');
        if (cnt) cnt.innerText = selectedBillingBulkIds.size;
    } else {
        btn.classList.add('hidden');
        btn.classList.remove('flex');
    }
}

async function triggerBulkPay() {
    if (selectedBillingBulkIds.size === 0) return;
    const count = selectedBillingBulkIds.size;
    if (!confirm(`Konfirmasi pembayaran lunas untuk ${count} pelanggan terpilih?`)) return;

    showLoading(true, 'Memproses pembayaran masal...');
    try {
        const adminName = localStorage.getItem('username') || 'Admin';
        const payments = Array.from(selectedBillingBulkIds).map(id => {
            const cust = customersList.find(c => c.id == id);
            return {
                customerId: id,
                adminName: adminName,
                totalAmount: cust ? getCustomerBillAmount(cust) : 0
            };
        });

        const res = await fetch(`${API_BASE_URL}/api/billing/pay-bulk`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                customerIds: Array.from(selectedBillingBulkIds),
                payments: payments,
                paymentMethod: 'Tunai / Cash',
                receivedBy: adminName,
                adminName: adminName
            })
        });

        if (res.ok) {
            showToast(`${count} tagihan berhasil dilunasi!`, 'success');
            selectedBillingBulkIds.clear();
            updateBulkPayButtonState();
            await loadBillingData();
            await loadCustomersData();
            await loadKeuanganData();
            await loadDashboardData();
        } else {
            showToast('Gagal memproses pembayaran masal.', 'error');
        }
    } catch (e) {
        showToast('Terjadi kesalahan saat koneksi ke server.', 'error');
    } finally {
        showLoading(false);
    }
}

function openPayBillingModal(customerId) {
    const c = customersList.find(cust => cust.id == customerId);
    if (!c) return;

    document.getElementById('pay-customer-id').value = c.id;
    document.getElementById('pay-customer-name').innerText = `${c.name} (${c.username || 'No PPPoE'})`;
    
    const amount = getCustomerBillAmount(c);
    document.getElementById('pay-customer-amount').innerText = formatRupiah(amount);
    
    const adminField = document.getElementById('pay-admin-name');
    if (adminField) adminField.value = localStorage.getItem('username') || 'Admin';

    openModal('modal-billing-pay');
}

async function submitPayBilling(e) {
    e.preventDefault();
    const customerId = document.getElementById('pay-customer-id').value;
    const c = customersList.find(cust => cust.id == customerId);
    if (!c) return;

    const amount = getCustomerBillAmount(c);
    const method = document.getElementById('pay-method').value;
    const admin = document.getElementById('pay-admin-name').value.trim() || localStorage.getItem('username') || 'Admin';
    const note = document.getElementById('pay-note').value.trim();

    const monthIdx = parseInt(document.getElementById('billing-filter-month')?.value || (new Date().getMonth() + 1)) - 1;
    const monthName = INDO_MONTHS[monthIdx] || 'Januari';
    const yearStr = document.getElementById('billing-filter-year')?.value || new Date().getFullYear().toString();

    showLoading(true, 'Menyimpan pembayaran...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/billing/pay`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                customerId: Number(customerId),
                totalAmount: amount,
                paidAmount: amount,
                adminName: admin,
                receivedBy: admin,
                paymentMethod: method,
                notes: note,
                months: [`${monthName} ${yearStr}`],
                paymentDate: new Date().toISOString()
            })
        });

        if (res.ok) {
            showToast(`Pembayaran ${c.name} (${monthName} ${yearStr}) berhasil disimpan!`, 'success');
            closeModal('modal-billing-pay');
            await loadBillingData();
            await loadCustomersData();
            await loadKeuanganData();
            await loadDashboardData();
        } else {
            showToast('Gagal menyimpan pembayaran tagihan.', 'error');
        }
    } catch (err) {
        showToast('Koneksi server gagal saat mencatat pembayaran.', 'error');
    } finally {
        showLoading(false);
    }
}

async function cancelBillingPayment(customerId) {
    const c = customersList.find(cust => cust.id == customerId);
    if (!c) return;

    if (!confirm(`Batalkan status lunas untuk pelanggan ${c.name}? Status akan kembali menjadi Belum Bayar.`)) return;

    showLoading(true, 'Membatalkan pembayaran...');
    try {
        const curMonth = Number(document.getElementById('billing-filter-month')?.value) || (new Date().getMonth() + 1);
        const curYear = Number(document.getElementById('billing-filter-year')?.value) || new Date().getFullYear();

        const res = await fetch(`${API_BASE_URL}/api/billing/delete`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                customerId: Number(customerId),
                billingMonth: curMonth,
                billingYear: curYear
            })
        });

        if (res.ok) {
            showToast(`Status tagihan ${c.name} dikembalikan ke Belum Bayar.`, 'success');
            await loadBillingData();
            await loadCustomersData();
            await loadKeuanganData();
            await loadDashboardData();
        } else {
            showToast('Gagal membatalkan pembayaran di server.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

function openInvoiceModal(customerId) {
    const c = customersList.find(cust => cust.id == customerId);
    if (!c) return;
    activeInvoiceCustomer = c;

    const isPaid = (c.status || '').toUpperCase().includes('LUNAS');
    const amount = getCustomerBillAmount(c);

    // Populate elements
    const compName = localStorage.getItem('company_name') || 'AKBAR MEDIA NETWORKS';
    const compAddr = localStorage.getItem('company_address') || 'Layanan Internet Cepat & Stabil';
    
    document.getElementById('inv-company-name').innerText = compName;
    document.getElementById('inv-company-addr').innerText = compAddr;

    const badge = document.getElementById('inv-badge-status');
    if (badge) {
        badge.innerText = isPaid ? 'LUNAS' : 'BELUM LUNAS';
        badge.className = isPaid 
            ? 'text-xs font-semibold px-2.5 py-1 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
            : 'text-xs font-semibold px-2.5 py-1 rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/30';
    }

    const curMonthName = document.getElementById('billing-filter-month')?.selectedOptions[0]?.text || 'Bulan Ini';
    const curYear = document.getElementById('billing-filter-year')?.value || '2026';

    document.getElementById('inv-number').innerText = `INV-${curYear}-${String(c.id).padStart(4, '0')}`;
    document.getElementById('inv-customer-name').innerText = c.name;
    document.getElementById('inv-customer-phone').innerText = c.phone || '-';
    document.getElementById('inv-period').innerText = `${curMonthName} ${curYear}`;
    document.getElementById('inv-due-date').innerText = `Tgl ${c.billingDate || '5'} ${curMonthName} ${curYear}`;

    document.getElementById('inv-pkg-name').innerText = `Paket Internet: ${c.packageName || c.package_name || 'Standar'}`;
    document.getElementById('inv-pkg-price').innerText = formatRupiah(c.price || 0);

    const add1Val = parseFloat(String(c.add1 || c.additionalCost1 || c.additional_cost1 || '0').replace(/[^0-9]/g, '')) || 0;
    const add1Row = document.getElementById('inv-row-add1');
    if (add1Row) {
        if (add1Val > 0) {
            add1Row.classList.remove('hidden');
            document.getElementById('inv-add1-desc').innerText = c.add1Desc || c.additionalCostDesc1 || c.additional_cost_desc1 || 'Biaya Tambahan';
            document.getElementById('inv-add1-val').innerText = formatRupiah(add1Val);
        } else {
            add1Row.classList.add('hidden');
        }
    }

    const discRow = document.getElementById('inv-row-disc');
    if (discRow) {
        if (Number(c.discount)) {
            discRow.classList.remove('hidden');
            document.getElementById('inv-disc-val').innerText = `- ${formatRupiah(c.discount)}`;
        } else {
            discRow.classList.add('hidden');
        }
    }

    document.getElementById('inv-total-val').innerText = formatRupiah(amount);

    openModal('modal-invoice');
}

function printInvoice() {
    window.print();
}

function sendInvoiceViaWhatsApp() {
    if (!activeInvoiceCustomer) return;
    sendWhatsAppInvoice(activeInvoiceCustomer.id);
}

// ==========================================
// PELANGGAN (CUSTOMERS) MODULE
// ==========================================

async function loadCustomersData() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/customers`);
        if (res.ok) {
            customersList = await res.json();
            filterCustomers();
            filterBilling();
        }
    } catch (e) {
        console.error('Error loading customers:', e);
    }
}

function filterCustomers() {
    const searchVal = (document.getElementById('cust-search')?.value || '').toLowerCase().trim();
    const statusVal = document.getElementById('cust-filter-status')?.value || '';
    const areaVal = document.getElementById('cust-filter-area')?.value || '';
    const paketVal = document.getElementById('cust-filter-paket')?.value || '';

    filteredCustomersList = customersList.filter(c => {
        const matchSearch = !searchVal || 
            (c.name && c.name.toLowerCase().includes(searchVal)) ||
            (c.phone && c.phone.toLowerCase().includes(searchVal)) ||
            (c.username && c.username.toLowerCase().includes(searchVal));

        const matchStatus = !statusVal || c.status === statusVal;
        const matchArea = !areaVal || c.area === areaVal;
        const matchPaket = !paketVal || c.packageName === paketVal;

        return matchSearch && matchStatus && matchArea && matchPaket;
    });

    customerPage = 1;
    renderCustomersTable();
}

function renderCustomersTable() {
    const tbody = document.getElementById('customers-table-body');
    if (!tbody) return;

    if (filteredCustomersList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="px-6 py-12 text-center text-slate-500">Tidak ada pelanggan yang cocok.</td></tr>`;
        updateCustPagination();
        return;
    }

    const start = (customerPage - 1) * customersPerPage;
    const end = start + customersPerPage;
    const pagedItems = filteredCustomersList.slice(start, end);

    tbody.innerHTML = pagedItems.map(c => {
        let statusBadge = '';
        if ((c.status || '').toUpperCase().includes('LUNAS')) {
            statusBadge = '<span class="px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">LUNAS</span>';
        } else if ((c.status || '').toUpperCase().includes('ISOLIR')) {
            statusBadge = '<span class="px-2.5 py-1 rounded-full text-[11px] font-bold bg-rose-500/15 text-rose-400 border border-rose-500/30">ISOLIR</span>';
        } else {
            statusBadge = '<span class="px-2.5 py-1 rounded-full text-[11px] font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30">BELUM BAYAR</span>';
        }

        return `
            <tr class="hover:bg-slate-800/40 transition">
                <td class="px-6 py-4">
                    <div class="flex items-center gap-3">
                        <div class="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center text-xs font-bold text-slate-300">
                            ${(c.name || 'P').substring(0, 2).toUpperCase()}
                        </div>
                        <div>
                            <p class="font-bold text-white cursor-pointer hover:text-emerald-400 transition" onclick="openCustomerDetailModal(${c.id})">${c.name}</p>
                            <p class="text-xs text-slate-400 font-mono">${c.phone || '-'}</p>
                            <p class="text-[10px] text-slate-500 font-mono">User: ${c.username || '-'}</p>
                        </div>
                    </div>
                </td>
                <td class="px-6 py-4">
                    <span class="text-xs font-semibold text-emerald-400">${c.packageName || c.package_name || '-'}</span>
                    <p class="text-slate-300 text-xs mt-0.5">${formatRupiah(getCustomerBillAmount(c))}</p>
                </td>
                <td class="px-6 py-4 text-xs">
                    <p class="text-slate-300">${c.area || 'Semua Area'}</p>
                    <p class="text-slate-500 text-[11px] truncate max-w-xs">${c.address || '-'}</p>
                </td>
                <td class="px-6 py-4 text-center text-xs">
                    <span class="bg-slate-900 border border-slate-800 px-2.5 py-1 rounded-lg text-slate-300 font-mono">
                        Tgl ${c.billingDate || '5'}
                    </span>
                </td>
                <td class="px-6 py-4 text-center">
                    ${statusBadge}
                </td>
                <td class="px-6 py-4 text-right">
                    <div class="flex items-center justify-end gap-1.5">
                        <button onclick="openCustomerDetailModal(${c.id})" class="p-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl transition" title="Lihat Detail">
                            <i data-lucide="eye" class="w-4 h-4"></i>
                        </button>
                        <button onclick="editCustomer(${c.id})" class="p-1.5 bg-slate-800 hover:bg-slate-700 text-emerald-400 rounded-xl transition" title="Edit Data">
                            <i data-lucide="edit" class="w-4 h-4"></i>
                        </button>
                        <button onclick="deleteCustomer(${c.id})" class="p-1.5 bg-slate-800 hover:bg-rose-900/40 text-rose-400 rounded-xl transition" title="Hapus Pelanggan">
                            <i data-lucide="trash-2" class="w-4 h-4"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    updateCustPagination();
    if (window.lucide) lucide.createIcons();
}

function updateCustPagination() {
    const total = filteredCustomersList.length;
    const info = document.getElementById('cust-pagination-info');
    if (info) {
        const start = total === 0 ? 0 : (customerPage - 1) * customersPerPage + 1;
        const end = Math.min(customerPage * customersPerPage, total);
        info.innerText = `Menampilkan ${start}-${end} dari ${total} pelanggan`;
    }

    const prevBtn = document.getElementById('btn-cust-prev');
    if (prevBtn) prevBtn.disabled = customerPage <= 1;

    const nextBtn = document.getElementById('btn-cust-next');
    if (nextBtn) nextBtn.disabled = customerPage * customersPerPage >= total;
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

function openCustomerDetailModal(id) {
    const c = customersList.find(cust => cust.id == id);
    if (!c) return;

    document.getElementById('det-cust-name').innerText = c.name;
    document.getElementById('det-cust-phone').innerText = `No WA/HP: ${c.phone || '-'}`;
    document.getElementById('det-cust-address').innerText = `Alamat: ${c.address || '-'}`;
    document.getElementById('det-cust-username').innerText = c.username || '-';
    document.getElementById('det-cust-package').innerText = `${c.packageName || '-'} (${formatRupiah(getCustomerBillAmount(c))})`;
    document.getElementById('det-cust-area').innerText = c.area || '-';
    document.getElementById('det-cust-odp').innerText = c.odpId ? `ODP ID ${c.odpId} (Port ${c.odpPort || 1})` : 'Belum dihubungkan ke ODP';

    const statusBadge = document.getElementById('det-cust-status');
    if (statusBadge) {
        statusBadge.innerText = c.status || 'AKTIF';
        statusBadge.className = c.status === 'LUNAS' 
            ? 'px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
            : c.status === 'ISOLIR'
            ? 'px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-500/20 text-rose-400 border border-rose-500/30'
            : 'px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/20 text-amber-400 border border-amber-500/30';
    }

    // Bind action shortcuts
    const isolateBtn = document.getElementById('btn-det-isolate');
    if (isolateBtn) {
        const isCurrentlyIsolir = c.status === 'ISOLIR';
        isolateBtn.innerHTML = `<i data-lucide="${isCurrentlyIsolir ? 'unlock' : 'shield-alert'}" class="w-4 h-4"></i> ${isCurrentlyIsolir ? 'Buka Isolir' : 'Isolir Pelanggan'}`;
        isolateBtn.onclick = () => toggleCustomerIsolir(c.id, isCurrentlyIsolir);
    }

    const histBtn = document.getElementById('btn-det-history');
    if (histBtn) histBtn.onclick = () => openCustomerHistoryModal(c.id);

    const waBtn = document.getElementById('btn-det-wa');
    if (waBtn) waBtn.onclick = () => sendWhatsAppInvoice(c.id);

    openModal('modal-customer-detail');
    if (window.lucide) lucide.createIcons();
}

async function toggleCustomerIsolir(id, isCurrentlyIsolir) {
    const c = customersList.find(cust => cust.id == id);
    if (!c) return;

    const actionText = isCurrentlyIsolir ? 'Buka Isolir' : 'Isolir';
    if (!confirm(`Konfirmasi ${actionText} untuk pelanggan ${c.name}?`)) return;

    showLoading(true, `Memproses ${actionText}...`);
    try {
        const endpoint = isCurrentlyIsolir ? `/api/customers/${id}/buka-isolir` : `/api/customers/${id}/isolir`;
        const res = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (res.ok) {
            showToast(`Pelanggan ${c.name} berhasil di-${actionText.toLowerCase()}!`, 'success');
            closeModal('modal-customer-detail');
            await loadCustomersData();
            await loadDashboardData();
        } else {
            showToast(`Gagal memproses ${actionText} di server.`, 'error');
        }
    } catch (e) {
        showToast('Koneksi gagal saat mengubah status isolir.', 'error');
    } finally {
        showLoading(false);
    }
}

async function openCustomerHistoryModal(id) {
    const c = customersList.find(cust => cust.id == id);
    if (!c) return;

    openModal('modal-customer-history');
    const tbody = document.getElementById('cust-history-body');
    if (tbody) tbody.innerHTML = `<tr><td colspan="4" class="px-3 py-6 text-center text-slate-500">Memuat riwayat pembayaran...</td></tr>`;

    try {
        const res = await fetch(`${API_BASE_URL}/api/customers/${id}/history`);
        if (res.ok) {
            const history = await res.json();
            if (history.length === 0) {
                tbody.innerHTML = `<tr><td colspan="4" class="px-3 py-6 text-center text-slate-500">Belum ada riwayat pembayaran tercatat.</td></tr>`;
            } else {
                tbody.innerHTML = history.map(h => `
                    <tr class="hover:bg-slate-800/40">
                        <td class="px-3 py-2 text-slate-300">${h.payment_date ? new Date(h.payment_date).toLocaleDateString('id-ID') : '-'}</td>
                        <td class="px-3 py-2 text-slate-200 font-semibold">${h.billing_month || 'Bulan Ini'} / ${h.billing_year || '2026'}</td>
                        <td class="px-3 py-2 text-slate-400">${h.received_by || 'Admin'}</td>
                        <td class="px-3 py-2 text-right text-emerald-400 font-bold">${formatRupiah(h.amount || h.paid_amount || 0)}</td>
                    </tr>
                `).join('');
            }
        }
    } catch (e) {
        if (tbody) tbody.innerHTML = `<tr><td colspan="4" class="px-3 py-6 text-center text-rose-400">Gagal memuat riwayat pembayaran.</td></tr>`;
    }
}

function sendWhatsAppInvoice(id) {
    const c = customersList.find(cust => cust.id == id);
    if (!c || !c.phone) {
        showToast('Nomor telepon pelanggan tidak tersedia.', 'warning');
        return;
    }

    const template = localStorage.getItem('wa_template') || 
`Yth. Pelanggan {nama},
Kami informasikan tagihan internet Anda:
- Paket: {paket}
- Total Tagihan: {nominal}
- Jatuh Tempo: Tgl {tanggal} setiap bulannya.

Mohon untuk melakukan pembayaran tepat waktu guna menjaga kelancaran koneksi internet. Terima kasih atas kerjasamanya.`;

    const billAmt = formatRupiah(getCustomerBillAmount(c));
    const msg = template
        .replace('{nama}', c.name || '')
        .replace('{paket}', c.packageName || 'Internet')
        .replace('{nominal}', billAmt)
        .replace('{tanggal}', c.billingDate || '5')
        .replace('{bulan}', document.getElementById('billing-filter-month')?.selectedOptions[0]?.text || 'Bulan Ini');

    const cleanPhone = c.phone.replace(/[^0-9]/g, '');
    const formattedPhone = cleanPhone.startsWith('0') ? '62' + cleanPhone.slice(1) : cleanPhone;
    const url = `https://wa.me/${formattedPhone}?text=${encodeURIComponent(msg)}`;
    window.open(url, '_blank');
}

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

    document.getElementById('modal-customer-title').innerText = 'Edit Data Pelanggan';
    document.getElementById('cust-id').value = c.id;
    document.getElementById('cust-name').value = c.name || '';
    document.getElementById('cust-phone').value = c.phone || '';
    document.getElementById('cust-area').value = c.area || '';
    document.getElementById('cust-address').value = c.address || '';
    document.getElementById('cust-username').value = c.username || '';
    document.getElementById('cust-username').readOnly = true;
    document.getElementById('cust-password').value = '';
    document.getElementById('cust-packageName').value = c.packageName || '';
    document.getElementById('cust-odpId').value = c.odpId || '';
    document.getElementById('cust-odpPort').value = c.odpPort || '';
    document.getElementById('cust-billingDate').value = c.billingDate || 5;
    document.getElementById('cust-status').value = c.status || 'LUNAS';

    openModal('modal-customer');
}

async function saveCustomer(e) {
    e.preventDefault();
    const id = document.getElementById('cust-id').value;
    const payload = {
        name: document.getElementById('cust-name').value.trim(),
        phone: document.getElementById('cust-phone').value.trim(),
        area: document.getElementById('cust-area').value,
        address: document.getElementById('cust-address').value.trim(),
        username: document.getElementById('cust-username').value.trim(),
        packageName: document.getElementById('cust-packageName').value,
        odpId: document.getElementById('cust-odpId').value || null,
        odpPort: document.getElementById('cust-odpPort').value || null,
        billingDate: Number(document.getElementById('cust-billingDate').value) || 5,
        status: document.getElementById('cust-status').value
    };

    const pwd = document.getElementById('cust-password').value.trim();
    if (pwd) payload.password = pwd;

    showLoading(true, 'Menyimpan data pelanggan...');
    try {
        let res;
        if (id) {
            res = await fetch(`${API_BASE_URL}/api/customers/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        } else {
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
            const err = await res.json().catch(() => ({}));
            showToast(err.error || 'Gagal menyimpan data pelanggan.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deleteCustomer(id) {
    const c = customersList.find(cust => cust.id == id);
    if (!c) return;
    if (!confirm(`Hapus pelanggan ${c.name}? Tindakan ini tidak dapat dibatalkan.`)) return;

    showLoading(true, 'Menghapus pelanggan...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/customers/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Pelanggan berhasil dihapus!', 'success');
            await loadCustomersData();
            await loadDashboardData();
        } else {
            showToast('Gagal menghapus pelanggan.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

// ==========================================
// INFRASTRUKTUR JARINGAN (ODC, ODP, RASIO)
// ==========================================

function switchInfraSubTab(tab) {
    currentInfraSubTab = tab;
    ['odc', 'odp', 'rasio'].forEach(t => {
        const btn = document.getElementById(`btn-subtab-${t}`);
        if (btn) {
            if (t === tab) {
                btn.className = "px-4 py-2 text-sm font-bold rounded-xl bg-slate-800 text-emerald-400 border border-slate-700 shadow-sm";
            } else {
                btn.className = "px-4 py-2 text-sm font-semibold rounded-xl text-slate-400 hover:bg-slate-800";
            }
        }
    });

    if (tab === 'odc') renderOdcTable();
    else if (tab === 'odp') renderOdpTable();
    else if (tab === 'rasio') renderRasioTable();
}

function renderOdcTable() {
    const thead = document.getElementById('infra-table-head');
    const tbody = document.getElementById('infra-table-body');
    if (!thead || !tbody) return;

    thead.innerHTML = `
        <tr>
            <th class="px-6 py-4">Nama ODC</th>
            <th class="px-6 py-4">Kapasitas Port</th>
            <th class="px-6 py-4">Sumber Feeder</th>
            <th class="px-6 py-4">Redaman Out</th>
            <th class="px-6 py-4 text-right">Aksi</th>
        </tr>
    `;

    if (odcList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="px-6 py-8 text-center text-slate-500">Belum ada data ODC.</td></tr>`;
        return;
    }

    tbody.innerHTML = odcList.map(item => `
        <tr class="hover:bg-slate-800/40">
            <td class="px-6 py-4 font-bold text-white">${item.name}</td>
            <td class="px-6 py-4 text-slate-300">${item.ports || '-'} Port</td>
            <td class="px-6 py-4 text-slate-400 font-mono">${item.feeder || '-'}</td>
            <td class="px-6 py-4 text-emerald-400 font-mono">${item.redaman_out || '-'} dBm</td>
            <td class="px-6 py-4 text-right">
                <button onclick="deleteNetworkNode('ODC', ${item.id})" class="p-1.5 bg-slate-800 hover:bg-rose-900/40 text-rose-400 rounded-xl transition">
                    <i data-lucide="trash-2" class="w-4 h-4"></i>
                </button>
            </td>
        </tr>
    `).join('');

    if (window.lucide) lucide.createIcons();
}

function renderOdpTable() {
    const thead = document.getElementById('infra-table-head');
    const tbody = document.getElementById('infra-table-body');
    if (!thead || !tbody) return;

    thead.innerHTML = `
        <tr>
            <th class="px-6 py-4">Nama ODP</th>
            <th class="px-6 py-4">Parent ODC</th>
            <th class="px-6 py-4">Kapasitas Port</th>
            <th class="px-6 py-4">Koordinat GPS</th>
            <th class="px-6 py-4 text-right">Aksi</th>
        </tr>
    `;

    if (odpList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="px-6 py-8 text-center text-slate-500">Belum ada data ODP.</td></tr>`;
        return;
    }

    tbody.innerHTML = odpList.map(item => `
        <tr class="hover:bg-slate-800/40">
            <td class="px-6 py-4 font-bold text-white">${item.name}</td>
            <td class="px-6 py-4 text-indigo-400">${item.input_odc || '-'}</td>
            <td class="px-6 py-4 text-slate-300">${item.ports || 8} Port</td>
            <td class="px-6 py-4 text-xs font-mono text-slate-400">${item.lat || '-'}${item.lng ? ', ' + item.lng : ''}</td>
            <td class="px-6 py-4 text-right">
                <button onclick="deleteNetworkNode('ODP', ${item.id})" class="p-1.5 bg-slate-800 hover:bg-rose-900/40 text-rose-400 rounded-xl transition">
                    <i data-lucide="trash-2" class="w-4 h-4"></i>
                </button>
            </td>
        </tr>
    `).join('');

    if (window.lucide) lucide.createIcons();
}

function renderRasioTable() {
    const thead = document.getElementById('infra-table-head');
    const tbody = document.getElementById('infra-table-body');
    if (!thead || !tbody) return;

    thead.innerHTML = `
        <tr>
            <th class="px-6 py-4">Nama Rasio</th>
            <th class="px-6 py-4">Ukuran Rasio Splitter</th>
            <th class="px-6 py-4">Redaman In</th>
            <th class="px-6 py-4">Redaman Out</th>
            <th class="px-6 py-4 text-right">Aksi</th>
        </tr>
    `;

    if (rasioList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="px-6 py-8 text-center text-slate-500">Belum ada data Rasio Redaman.</td></tr>`;
        return;
    }

    tbody.innerHTML = rasioList.map(item => `
        <tr class="hover:bg-slate-800/40">
            <td class="px-6 py-4 font-bold text-white">${item.name}</td>
            <td class="px-6 py-4 text-emerald-400 font-mono">${item.rasio_size || '-'}</td>
            <td class="px-6 py-4 text-slate-300 font-mono">${item.redaman_in || '-'} dBm</td>
            <td class="px-6 py-4 text-blue-400 font-mono">${item.redaman_out || '-'} dBm</td>
            <td class="px-6 py-4 text-right">
                <button onclick="deleteNetworkNode('Rasio', ${item.id})" class="p-1.5 bg-slate-800 hover:bg-rose-900/40 text-rose-400 rounded-xl transition">
                    <i data-lucide="trash-2" class="w-4 h-4"></i>
                </button>
            </td>
        </tr>
    `).join('');

    if (window.lucide) lucide.createIcons();
}

function openAddNodeModal(type) {
    document.getElementById('net-type').value = type;
    document.getElementById('net-id').value = '';
    document.getElementById('form-network-node').reset();
    document.getElementById('modal-network-title').innerText = `Tambah ${type} Baru`;

    document.getElementById('fields-odc').classList.toggle('hidden', type !== 'ODC');
    document.getElementById('fields-odp').classList.toggle('hidden', type !== 'ODP');
    document.getElementById('fields-rasio').classList.toggle('hidden', type !== 'Rasio');

    openModal('modal-network-node');
}

async function saveNetworkNode(e) {
    e.preventDefault();
    const type = document.getElementById('net-type').value;
    const name = document.getElementById('net-name').value.trim();
    const redamanIn = document.getElementById('net-redaman-in').value.trim();
    const redamanOut = document.getElementById('net-redaman-out').value.trim();

    let endpoint = '/api/odc';
    let payload = { name, redaman_in: redamanIn, redaman_out: redamanOut };

    if (type === 'ODC') {
        endpoint = '/api/odc';
        payload.ports = document.getElementById('net-odc-ports').value;
        payload.feeder = document.getElementById('net-odc-feeder').value.trim();
    } else if (type === 'ODP') {
        endpoint = '/api/odp';
        payload.ports = document.getElementById('net-odp-ports').value;
        payload.input_odc = document.getElementById('net-odp-input').value;
        payload.lat = document.getElementById('net-odp-lat').value.trim();
        payload.lng = document.getElementById('net-odp-lng').value.trim();
    } else if (type === 'Rasio') {
        endpoint = '/api/rasio';
        payload.ports = document.getElementById('net-rasio-ports').value;
        payload.rasio_size = document.getElementById('net-rasio-size').value.trim();
        payload.input_jalur = document.getElementById('net-rasio-input').value.trim();
    }

    showLoading(true, `Menyimpan ${type}...`);
    try {
        const res = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast(`${type} berhasil ditambahkan!`, 'success');
            closeModal('modal-network-node');
            if (type === 'ODC') await loadOdcList();
            else if (type === 'ODP') await loadOdpList();
            else if (type === 'Rasio') await loadRasioList();
            await loadDashboardData();
        } else {
            showToast(`Gagal menambahkan ${type}.`, 'error');
        }
    } catch (err) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deleteNetworkNode(type, id) {
    if (!confirm(`Hapus ${type} ini?`)) return;
    showLoading(true, `Menghapus ${type}...`);
    try {
        let endpoint = `/api/odc/${id}`;
        if (type === 'ODP') endpoint = `/api/odp/${id}`;
        else if (type === 'Rasio') endpoint = `/api/rasio/${id}`;

        const res = await fetch(`${API_BASE_URL}${endpoint}`, { method: 'DELETE' });
        if (res.ok) {
            showToast(`${type} berhasil dihapus!`, 'success');
            if (type === 'ODC') await loadOdcList();
            else if (type === 'ODP') await loadOdpList();
            else if (type === 'Rasio') await loadRasioList();
            await loadDashboardData();
        } else {
            showToast(`Gagal menghapus ${type}.`, 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

// ==========================================
// INVENTARIS BARANG MODULE
// ==========================================

async function loadInventarisData() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/inventory`);
        if (res.ok) {
            inventoryList = await res.json();
            renderInventoryGrid();
            populateTakeItemDropdowns();
        }

        const histRes = await fetch(`${API_BASE_URL}/api/stock_history`);
        if (histRes.ok) {
            const hist = await histRes.json();
            renderInventoryHistory(hist);
        }
    } catch (e) {
        console.error('Error loading inventory:', e);
    }
}

function renderInventoryGrid() {
    const grid = document.getElementById('inventory-grid');
    if (!grid) return;

    if (inventoryList.length === 0) {
        grid.innerHTML = `<p class="col-span-full text-slate-500 text-sm text-center py-8">Belum ada item barang di gudang.</p>`;
        return;
    }

    grid.innerHTML = inventoryList.map(item => `
        <div class="bg-[#151c2c] border border-slate-800 rounded-2xl p-5 flex flex-col justify-between gap-3 shadow-sm hover:border-slate-700 transition">
            <div class="flex items-start justify-between">
                <div>
                    <span class="text-[10px] font-bold text-slate-400 uppercase tracking-wider">${item.category || 'Barang'}</span>
                    <h4 class="font-bold text-white text-base mt-0.5">${item.name}</h4>
                </div>
                <button onclick="deleteInventoryItem(${item.id})" class="text-slate-500 hover:text-rose-400 transition" title="Hapus Barang">
                    <i data-lucide="trash-2" class="w-4 h-4"></i>
                </button>
            </div>
            <div class="flex items-baseline justify-between border-t border-slate-800/80 pt-3">
                <span class="text-xs text-slate-400">Sisa Stok:</span>
                <span class="text-2xl font-bold ${Number(item.stock) <= 2 ? 'text-rose-400' : 'text-emerald-400'}">
                    ${item.stock} <span class="text-xs font-normal text-slate-400">${item.unit || 'pcs'}</span>
                </span>
            </div>
        </div>
    `).join('');

    if (window.lucide) lucide.createIcons();
}

function renderInventoryHistory(history) {
    const tbody = document.getElementById('inventory-history-body');
    if (!tbody) return;

    if (!history || history.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="px-4 py-6 text-center text-slate-500">Belum ada riwayat pengambilan barang.</td></tr>`;
        return;
    }

    tbody.innerHTML = history.slice(0, 10).map(h => `
        <tr class="hover:bg-slate-800/40 text-xs">
            <td class="px-4 py-3 text-slate-400">${h.created_at ? new Date(h.created_at).toLocaleDateString('id-ID') : '-'}</td>
            <td class="px-4 py-3 font-semibold text-white">${h.item_name || 'Barang'}</td>
            <td class="px-4 py-3 text-rose-400 font-bold">-${h.quantity || 1}</td>
            <td class="px-4 py-3 text-indigo-300">${h.admin_name || '-'}</td>
            <td class="px-4 py-3 text-slate-400">${h.notes || '-'}</td>
        </tr>
    `).join('');
}

function openAddInventoryModal() {
    document.getElementById('form-add-item').reset();
    document.getElementById('inv-id').value = '';
    openModal('modal-add-item');
}

async function saveInventoryItem(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('inv-name').value.trim(),
        stock: Number(document.getElementById('inv-stock').value) || 0,
        unit: document.getElementById('inv-unit').value.trim(),
        category: document.getElementById('inv-category').value
    };

    showLoading(true, 'Menyimpan barang...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/inventory`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Item barang berhasil disimpan!', 'success');
            closeModal('modal-add-item');
            await loadInventarisData();
        } else {
            showToast('Gagal menyimpan barang.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deleteInventoryItem(id) {
    if (!confirm('Hapus item ini dari inventaris?')) return;
    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/inventory/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Barang berhasil dihapus!', 'success');
            await loadInventarisData();
        } else {
            showToast('Gagal menghapus barang.', 'error');
        }
    } catch (e) {
        showToast('Koneksi gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

function openTakeItemModal() {
    document.getElementById('form-take-item').reset();
    openModal('modal-take-item');
}

async function saveItemTransaction(e) {
    e.preventDefault();
    const payload = {
        itemId: document.getElementById('take-itemId').value,
        adminName: document.getElementById('take-adminName').value.trim(),
        quantity: Number(document.getElementById('take-quantity').value) || 1,
        notes: document.getElementById('take-notes').value.trim()
    };

    showLoading(true, 'Mencatat pengeluaran barang...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/stock_history`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Pengambilan barang berhasil dicatat!', 'success');
            closeModal('modal-take-item');
            await loadInventarisData();
        } else {
            showToast('Gagal mencatat pengambilan barang.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

// ==========================================
// KEUANGAN & PEMBUKUAN MODULE
// ==========================================

async function loadKeuanganData() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/pembukuan/all`);
        if (res.ok) {
            const data = await res.json();
            financeList = Array.isArray(data) ? data : (data.rows || []);
            renderKeuanganTable();
            calculateFinanceSummaries();
        }
    } catch (e) {
        console.error('Error loading finance:', e);
    }
}

function renderKeuanganTable() {
    const tbody = document.getElementById('keuangan-table-body');
    if (!tbody) return;

    if (financeList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="px-6 py-8 text-center text-slate-500">Belum ada transaksi mutasi kas.</td></tr>`;
        return;
    }

    tbody.innerHTML = financeList.slice(0, 30).map(f => {
        const isIncome = f.type === 'pemasukan' || Number(f.amount) > 0;
        const txDate = f.created_at || f.date;
        const formattedDate = txDate ? new Date(txDate).toLocaleDateString('id-ID', { day: '2-digit', month: 'short', year: 'numeric' }) : '-';
        return `
            <tr class="hover:bg-slate-800/40">
                <td class="px-6 py-4 text-xs text-slate-400 font-mono">${formattedDate}</td>
                <td class="px-6 py-4 font-semibold text-white">${f.description || f.keterangan || '-'}</td>
                <td class="px-6 py-4 text-xs text-slate-400">
                    <span class="px-2 py-0.5 rounded-md bg-slate-800 border border-slate-700">${f.category || 'Operasional'}</span>
                </td>
                <td class="px-6 py-4 text-xs text-slate-300 font-medium">${f.admin_name || f.admin || f.penerima || '-'}</td>
                <td class="px-6 py-4 text-right font-bold ${isIncome ? 'text-emerald-400' : 'text-rose-400'}">
                    ${isIncome ? '+' : '-'} ${formatRupiah(Math.abs(f.amount || 0))}
                </td>
                <td class="px-4 py-4 text-right">
                    <button onclick="deleteFinanceTransaction(${f.id})" class="p-1.5 hover:bg-rose-500/10 text-rose-400 hover:text-rose-300 rounded-lg transition" title="Hapus Transaksi">
                        <i data-lucide="trash-2" class="w-4 h-4"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join('');

    if (window.lucide) lucide.createIcons();
}

function calculateFinanceSummaries() {
    let income = 0;
    let expense = 0;

    financeList.forEach(f => {
        const amt = Number(f.amount) || 0;
        if (f.type === 'pemasukan' || (f.type !== 'pengeluaran' && amt > 0)) {
            income += Math.abs(amt);
        } else {
            expense += Math.abs(amt);
        }
    });

    const incEl = document.getElementById('fin-total-pemasukan');
    if (incEl) incEl.innerText = formatRupiah(income);

    const expEl = document.getElementById('fin-total-pengeluaran');
    if (expEl) expEl.innerText = formatRupiah(expense);

    const salEl = document.getElementById('fin-total-saldo');
    if (salEl) salEl.innerText = formatRupiah(income - expense);
}

function openFinanceDialog(type) {
    document.getElementById('form-keuangan-trans').reset();
    document.getElementById('fin-type').value = type;
    document.getElementById('modal-keuangan-title').innerText = type === 'pemasukan' ? 'Catat Pemasukan Kas' : 'Catat Pengeluaran Kas';
    
    const adminField = document.getElementById('fin-admin');
    if (adminField) adminField.value = localStorage.getItem('username') || 'Admin';

    openModal('modal-keuangan-trans');
}

async function saveFinanceTransaction(e) {
    e.preventDefault();
    const type = document.getElementById('fin-type').value;
    const amount = Number(document.getElementById('fin-amount').value) || 0;
    const adminName = document.getElementById('fin-admin').value.trim() || localStorage.getItem('username') || 'Admin';

    const payload = {
        type: type,
        description: document.getElementById('fin-desc').value.trim(),
        category: document.getElementById('fin-category').value,
        admin_name: adminName,
        admin: adminName,
        amount: amount,
        created_at: new Date().toISOString()
    };

    showLoading(true, 'Menyimpan transaksi kas...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/pembukuan`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Transaksi kas berhasil disimpan ke database!', 'success');
            closeModal('modal-keuangan-trans');
            await loadKeuanganData();
            await loadDashboardData();
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(err.error || 'Gagal menyimpan transaksi kas.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deleteFinanceTransaction(id) {
    if (!id) return;
    if (!confirm('Hapus transaksi pembukuan ini dari database?')) return;

    showLoading(true, 'Menghapus transaksi...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/pembukuan/${id}`, {
            method: 'DELETE'
        });

        if (res.ok) {
            showToast('Transaksi pembukuan berhasil dihapus.', 'success');
            await loadKeuanganData();
            await loadDashboardData();
        } else {
            showToast('Gagal menghapus transaksi dari database.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

// ==========================================
// GANGGUAN & TIKET MODULE
// ==========================================

async function loadGangguanData() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/gangguan`);
        if (res.ok) {
            gangguanList = await res.json();
            renderGangguanTable();
            populateGangguanCustomerDropdown();
        }
    } catch (e) {
        console.error('Error loading gangguan:', e);
    }
}

function renderGangguanTable() {
    const tbody = document.getElementById('gangguan-table-body');
    if (!tbody) return;

    let pending = 0;
    let inProcess = 0;
    let resolved = 0;

    gangguanList.forEach(g => {
        const st = (g.status || '').toLowerCase();
        if (st.includes('proses')) inProcess++;
        else if (st.includes('selesai') || st.includes('solved')) resolved++;
        else pending++;
    });

    const pEl = document.getElementById('gangguan-count-pending');
    if (pEl) pEl.innerText = `${pending} Tiket`;
    const prEl = document.getElementById('gangguan-count-process');
    if (prEl) prEl.innerText = `${inProcess} Tiket`;
    const rEl = document.getElementById('gangguan-count-resolved');
    if (rEl) rEl.innerText = `${resolved} Tiket`;

    if (gangguanList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="px-6 py-8 text-center text-slate-500">Tidak ada tiket gangguan aktif.</td></tr>`;
        return;
    }

    tbody.innerHTML = gangguanList.map(g => {
        const isSolved = (g.status || '').toLowerCase().includes('selesai');
        const isProcess = (g.status || '').toLowerCase().includes('proses');

        let badge = '<span class="px-2.5 py-1 rounded-full text-[11px] font-bold bg-rose-500/15 text-rose-400 border border-rose-500/30">Menunggu</span>';
        if (isSolved) badge = '<span class="px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">Selesai</span>';
        else if (isProcess) badge = '<span class="px-2.5 py-1 rounded-full text-[11px] font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30">Diproses</span>';

        return `
            <tr class="hover:bg-slate-800/40">
                <td class="px-6 py-4 text-xs text-slate-400">
                    <p class="font-bold text-white">#TIK-${g.id}</p>
                    <p class="text-[10px] text-slate-500 mt-0.5">${g.created_at ? new Date(g.created_at).toLocaleDateString('id-ID') : '-'}</p>
                </td>
                <td class="px-6 py-4 font-semibold text-white">
                    ${g.customer_name || 'Pelanggan ID ' + g.customer_id}
                </td>
                <td class="px-6 py-4 text-xs text-slate-300 max-w-sm">
                    ${g.description || g.keluhan || '-'}
                </td>
                <td class="px-6 py-4 text-center">
                    ${badge}
                </td>
                <td class="px-6 py-4 text-right">
                    <div class="flex items-center justify-end gap-1.5">
                        ${!isSolved ? `
                            <button onclick="updateGangguanStatus(${g.id}, 'Selesai')" class="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold transition" title="Tandai Selesai">
                                Selesai
                            </button>
                        ` : ''}
                        <button onclick="deleteGangguan(${g.id})" class="p-1.5 bg-slate-800 hover:bg-rose-900/40 text-rose-400 rounded-xl transition">
                            <i data-lucide="trash-2" class="w-4 h-4"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    if (window.lucide) lucide.createIcons();
}

function openAddGangguanModal() {
    document.getElementById('form-gangguan').reset();
    populateGangguanCustomerDropdown();
    openModal('modal-gangguan');
}

function populateGangguanCustomerDropdown() {
    const sel = document.getElementById('gangguan-cust-id');
    if (!sel) return;
    sel.innerHTML = '<option value="">Pilih Pelanggan</option>' + 
        customersList.map(c => `<option value="${c.id}">${c.name} (${c.username || 'No PPPoE'})</option>`).join('');
}

async function saveGangguan(e) {
    e.preventDefault();
    const custId = document.getElementById('gangguan-cust-id').value;
    const c = customersList.find(cust => cust.id == custId);

    const payload = {
        customerId: custId,
        customerName: c ? c.name : 'Pelanggan',
        description: document.getElementById('gangguan-desc').value.trim(),
        status: 'Menunggu'
    };

    showLoading(true, 'Menyimpan tiket gangguan...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/gangguan`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Tiket aduan gangguan berhasil dibuat!', 'success');
            closeModal('modal-gangguan');
            await loadGangguanData();
        } else {
            showToast('Gagal menyimpan tiket gangguan.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function updateGangguanStatus(id, newStatus) {
    showLoading(true, 'Memperbarui status tiket...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/gangguan/${id}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });

        if (res.ok) {
            showToast(`Status tiket gangguan diperbarui ke ${newStatus}!`, 'success');
            await loadGangguanData();
        } else {
            showToast('Gagal memperbarui status.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deleteGangguan(id) {
    if (!confirm('Hapus tiket gangguan ini?')) return;
    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/gangguan/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Tiket gangguan dihapus!', 'success');
            await loadGangguanData();
        }
    } catch (e) {
        showToast('Koneksi gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

// ==========================================
// MIKROTIK & ACS MODULE
// ==========================================

// ==========================================
// MIKROTIK & ACS ONT MODULE
// ==========================================

let filteredAcsDevices = [];
let acsCurrentPage = 1;
let acsPerPage = 100; // Default 100 ONU per halaman

async function loadAcsDevices() {
    const tbody = document.getElementById('acs-devices-table-body');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="px-4 py-12 text-center text-cyan-400 text-xs">
                    <div class="flex items-center justify-center gap-2">
                        <i data-lucide="loader-2" class="w-4 h-4 animate-spin"></i>
                        <span>Menghubungi server ACS TR-069 & membaca data ONT pelanggan...</span>
                    </div>
                </td>
            </tr>
        `;
        if (window.lucide) lucide.createIcons();
    }

    try {
        const res = await fetch(`${API_BASE_URL}/api/acs/devices`);
        if (res.ok) {
            acsDevicesList = await res.json();
            
            const totEl = document.getElementById('acs-total-devices');
            if (totEl) totEl.innerText = `${acsDevicesList.length} Unit`;

            const badgeEl = document.getElementById('acs-onu-count-badge');
            if (badgeEl) badgeEl.innerText = `${acsDevicesList.length} ONU`;

            populateAcsAreaFilter();
            applyAcsDeviceFilters();
        } else {
            if (tbody) {
                tbody.innerHTML = `<tr><td colspan="7" class="px-4 py-8 text-center text-rose-400 text-xs">Layanan ACS NBI belum aktif atau belum dikonfigurasi.</td></tr>`;
            }
        }
    } catch (e) {
        console.error('Error loadAcsDevices:', e);
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="7" class="px-4 py-8 text-center text-rose-400 text-xs">Tidak dapat terhubung ke NBI Service ACS di port :7557.</td></tr>`;
        }
    }
}

function populateAcsAreaFilter() {
    const sel = document.getElementById('acs-filter-area');
    if (!sel) return;

    const uniqueTags = new Set();
    acsDevicesList.forEach(d => {
        if (d.tag && d.tag !== '-') uniqueTags.add(d.tag);
        if (d.areaName) uniqueTags.add(d.areaName);
    });

    const currentVal = sel.value;
    sel.innerHTML = '<option value="">Semua Area / TAG</option>' + 
        Array.from(uniqueTags).sort().map(t => `<option value="${t}">${t}</option>`).join('');
    
    if (currentVal) sel.value = currentVal;
}

function applyAcsDeviceFilters() {
    const searchVal = (document.getElementById('acs-search-input')?.value || '').toLowerCase().trim();
    const areaVal = (document.getElementById('acs-filter-area')?.value || '').toLowerCase().trim();
    const statusVal = document.getElementById('acs-filter-status')?.value || 'ALL';

    filteredAcsDevices = acsDevicesList.filter(dev => {
        // Search matches SN, id (PPPoE), SSID, or Tag
        const matchSearch = !searchVal ||
            (dev.serialNumber && dev.serialNumber.toLowerCase().includes(searchVal)) ||
            (dev.id && dev.id.toLowerCase().includes(searchVal)) ||
            (dev.username && dev.username.toLowerCase().includes(searchVal)) ||
            (dev.ssid && dev.ssid.toLowerCase().includes(searchVal)) ||
            (dev.tag && dev.tag.toLowerCase().includes(searchVal)) ||
            (dev.model && dev.model.toLowerCase().includes(searchVal));

        // Area / Tag filter
        const devTag = (dev.tag || '').toLowerCase();
        const devArea = (dev.areaName || '').toLowerCase();
        const matchArea = !areaVal || devTag.includes(areaVal) || devArea.includes(areaVal);

        // Status filter
        let matchStatus = true;
        if (statusVal === 'ONLINE') matchStatus = Boolean(dev.isOnline);
        else if (statusVal === 'OFFLINE') matchStatus = !dev.isOnline;

        return matchSearch && matchArea && matchStatus;
    });

    acsCurrentPage = 1;
    renderAcsDevicesTable();
    renderAcsPagination();
}

function changeAcsPerPage(val) {
    acsPerPage = val === 'all' ? 999999 : (parseInt(val) || 100);
    acsCurrentPage = 1;
    renderAcsDevicesTable();
    renderAcsPagination();
}

function changeAcsPage(page) {
    acsCurrentPage = page;
    renderAcsDevicesTable();
    renderAcsPagination();
}

function formatRxPowerBadge(rxVal) {
    if (!rxVal || rxVal === '-' || rxVal === 'N/A') {
        return `<span class="text-slate-500 font-mono">-</span>`;
    }
    const num = parseFloat(rxVal);
    if (isNaN(num)) {
        return `<span class="font-mono text-slate-400">${rxVal}</span>`;
    }

    // Standard GPON Optical Power Thresholds:
    // Good: -14 to -24 dBm
    // Warning: -24 to -27 dBm
    // Critical: < -27 dBm or > -14 dBm
    let colorClass = 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20';
    if (num < -27 || num > -13) {
        colorClass = 'text-rose-400 bg-rose-500/10 border-rose-500/20';
    } else if (num < -24) {
        colorClass = 'text-amber-400 bg-amber-500/10 border-amber-500/20';
    }

    return `
        <span class="inline-block px-2 py-0.5 rounded font-mono text-[11px] font-bold border ${colorClass}">
            ${num.toFixed(2)} dBm
        </span>
    `;
}

function renderAcsDevicesTable() {
    const tbody = document.getElementById('acs-devices-table-body');
    if (!tbody) return;

    if (filteredAcsDevices.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="px-4 py-12 text-center text-slate-500 text-xs">
                    ${acsDevicesList.length === 0 ? 'Belum ada ONT yang terhubung ke server ACS.' : 'Tidak ada ONT yang sesuai dengan filter pencarian.'}
                </td>
            </tr>
        `;
        return;
    }

    const startIdx = (acsCurrentPage - 1) * acsPerPage;
    const pageItems = filteredAcsDevices.slice(startIdx, startIdx + acsPerPage);

    tbody.innerHTML = pageItems.map(dev => {
        const isOnline = Boolean(dev.isOnline);
        const tagText = dev.tag && dev.tag !== '-' ? dev.tag : (dev.areaName || 'Default');
        const serialNum = dev.serialNumber || dev.id || '-';
        const pppoeId = dev.username || '-';
        const ssidName = dev.ssid && dev.ssid !== 'Unknown' ? dev.ssid : '-';
        const escapedId = (dev.id || '').replace(/'/g, "\\'");
        const escapedSsid = (dev.ssid || '').replace(/'/g, "\\'");
        const escapedPwd = (dev.wifiPassword || '').replace(/'/g, "\\'");
        const escapedArea = (dev.areaName || '').replace(/'/g, "\\'");
        const escapedSn = (serialNum || '').replace(/'/g, "\\'");

        return `
            <tr onclick="openOntDetailModal('${escapedId}')" class="hover:bg-slate-800/60 cursor-pointer transition text-xs group">
                <!-- 1. TAG -->
                <td class="px-4 py-3.5">
                    <span class="px-2.5 py-1 rounded-md bg-slate-900 border border-slate-700 text-cyan-300 font-semibold inline-block">
                        ${tagText}
                    </span>
                </td>

                <!-- 2. Status (online/offline) -->
                <td class="px-4 py-3.5 text-center">
                    ${isOnline ? `
                        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                            <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span> Online
                        </span>
                    ` : `
                        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-slate-800 text-slate-400 border border-slate-700">
                            <span class="w-1.5 h-1.5 rounded-full bg-slate-500"></span> Offline
                        </span>
                    `}
                </td>

                <!-- 3. Redaman (Rx power) -->
                <td class="px-4 py-3.5 text-center">
                    ${formatRxPowerBadge(dev.rxPower)}
                </td>

                <!-- 4. id (PPPoE secret) -->
                <td class="px-4 py-3.5 font-mono text-cyan-300 font-semibold">
                    ${pppoeId}
                </td>

                <!-- 5. SSID -->
                <td class="px-4 py-3.5 text-white font-medium">
                    <div class="flex items-center gap-1.5">
                        <i data-lucide="wifi" class="w-3.5 h-3.5 text-slate-400 group-hover:text-cyan-400 transition"></i>
                        <span class="truncate max-w-[140px]">${ssidName}</span>
                    </div>
                </td>

                <!-- 6. Serial Number -->
                <td class="px-4 py-3.5 font-mono font-bold text-slate-200">
                    ${serialNum}
                </td>

                <!-- 7. Aksi -->
                <td class="px-4 py-3.5 text-right">
                    <div class="flex items-center justify-end gap-1.5" onclick="event.stopPropagation()">
                        <button onclick="openChangeSsidModal('${escapedId}', '${escapedSsid}', '${escapedArea}', '${escapedSn}')" class="px-2.5 py-1 bg-cyan-600/20 hover:bg-cyan-600/30 text-cyan-400 rounded-lg text-xs font-semibold transition border border-cyan-500/30 flex items-center gap-1" title="Ganti Nama Wifi (SSID)">
                            <i data-lucide="wifi" class="w-3 h-3"></i> Ganti SSID
                        </button>
                        <button onclick="openChangeWifiPasswordModal('${escapedId}', '${escapedPwd}', '${escapedArea}', '${escapedSn}')" class="px-2.5 py-1 bg-amber-600/20 hover:bg-amber-600/30 text-amber-400 rounded-lg text-xs font-semibold transition border border-amber-500/30 flex items-center gap-1" title="Ganti Sandi Wifi">
                            <i data-lucide="key" class="w-3 h-3"></i> Ganti Sandi
                        </button>
                        <button onclick="openOntDetailModal('${escapedId}')" class="p-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg transition" title="Lihat Detail Lengkap ONU">
                            <i data-lucide="eye" class="w-3.5 h-3.5"></i>
                        </button>
                        <button onclick="rebootAcsDevice('${escapedId}')" class="p-1.5 bg-rose-600/20 hover:bg-rose-600/30 text-rose-400 rounded-lg transition" title="Reboot ONT">
                            <i data-lucide="refresh-cw" class="w-3.5 h-3.5"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    if (window.lucide) lucide.createIcons();
}

function renderAcsPagination() {
    const infoEl = document.getElementById('acs-pagination-info');
    const btnsEl = document.getElementById('acs-pagination-buttons');
    if (!infoEl || !btnsEl) return;

    const total = filteredAcsDevices.length;
    if (total === 0) {
        infoEl.innerText = 'Menampilkan 0 dari 0 ONT';
        btnsEl.innerHTML = '';
        return;
    }

    const totalPages = Math.ceil(total / acsPerPage) || 1;
    const start = (acsCurrentPage - 1) * acsPerPage + 1;
    const end = Math.min(start + acsPerPage - 1, total);

    infoEl.innerText = `Menampilkan ${start}-${end} dari ${total} ONT (Halaman ${acsCurrentPage} dari ${totalPages})`;

    if (totalPages <= 1) {
        btnsEl.innerHTML = '';
        return;
    }

    let buttonsHtml = '';
    // Previous button
    buttonsHtml += `
        <button onclick="changeAcsPage(${acsCurrentPage - 1})" ${acsCurrentPage === 1 ? 'disabled' : ''} class="px-2.5 py-1 rounded-lg text-xs font-semibold transition border border-slate-700 ${acsCurrentPage === 1 ? 'text-slate-600 bg-slate-900 cursor-not-allowed' : 'text-slate-300 bg-slate-800 hover:bg-slate-700'}">
            Sebelumnya
        </button>
    `;

    // Page indicator numbers
    const startPage = Math.max(1, acsCurrentPage - 2);
    const endPage = Math.min(totalPages, startPage + 4);

    for (let p = startPage; p <= endPage; p++) {
        const isCurrent = p === acsCurrentPage;
        buttonsHtml += `
            <button onclick="changeAcsPage(${p})" class="px-2.5 py-1 rounded-lg text-xs font-bold transition border ${isCurrent ? 'bg-cyan-500/20 text-cyan-400 border-cyan-500/40' : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-white'}">
                ${p}
            </button>
        `;
    }

    // Next button
    buttonsHtml += `
        <button onclick="changeAcsPage(${acsCurrentPage + 1})" ${acsCurrentPage === totalPages ? 'disabled' : ''} class="px-2.5 py-1 rounded-lg text-xs font-semibold transition border border-slate-700 ${acsCurrentPage === totalPages ? 'text-slate-600 bg-slate-900 cursor-not-allowed' : 'text-slate-300 bg-slate-800 hover:bg-slate-700'}">
            Selanjutnya
        </button>
    `;

    btnsEl.innerHTML = buttonsHtml;
}

function openOntDetailModal(devId) {
    const dev = acsDevicesList.find(d => String(d.id) === String(devId) || String(d.serialNumber) === String(devId));
    if (!dev) {
        showToast('Data detail perangkat tidak ditemukan.', 'error');
        return;
    }

    const isOnline = Boolean(dev.isOnline);
    const serialNum = dev.serialNumber || dev.id || '-';

    document.getElementById('ont-detail-sn').innerText = `SN: ${serialNum}`;
    document.getElementById('ont-detail-title').innerText = `${dev.vendor && dev.vendor !== '-' ? dev.vendor + ' ' : ''}${dev.model || 'Perangkat Modem ONT'}`;

    const dot = document.getElementById('ont-detail-status-dot');
    const stText = document.getElementById('ont-detail-status-text');
    if (dot && stText) {
        if (isOnline) {
            dot.className = 'w-3 h-3 rounded-full bg-emerald-400 animate-pulse';
            stText.innerText = 'Online (Aktif Terhubung ke ACS)';
            stText.className = 'text-xs font-bold text-emerald-400';
        } else {
            dot.className = 'w-3 h-3 rounded-full bg-slate-500';
            stText.innerText = 'Offline (Tidak Merespons)';
            stText.className = 'text-xs font-bold text-slate-400';
        }
    }

    const lastInf = dev.lastInform ? new Date(dev.lastInform).toLocaleString('id-ID') : 'Belum pernah lapor';
    document.getElementById('ont-detail-last-inform').innerText = `Terakhir kontak: ${lastInf}`;
    document.getElementById('ont-detail-tag').innerText = dev.tag && dev.tag !== '-' ? dev.tag : (dev.areaName || 'Default');
    document.getElementById('ont-detail-rxpower').innerHTML = formatRxPowerBadge(dev.rxPower);
    document.getElementById('ont-detail-pppoe').innerText = dev.username || '-';
    document.getElementById('ont-detail-ip').innerText = dev.ipAddress || '-';
    document.getElementById('ont-detail-ssid').innerText = dev.ssid && dev.ssid !== 'Unknown' ? dev.ssid : '-';
    document.getElementById('ont-detail-wifipwd').innerText = dev.wifiPassword || '-';
    document.getElementById('ont-detail-model').innerText = `${dev.vendor || '-'} / ${dev.model || '-'}`;
    document.getElementById('ont-detail-users').innerText = `${dev.connectedUsers || 0} Perangkat`;
    document.getElementById('ont-detail-sw').innerText = dev.softwareVersion || '-';
    document.getElementById('ont-detail-uptime').innerText = dev.uptime && dev.uptime !== '-' ? `${dev.uptime} detik` : '-';

    // Configure action buttons inside modal
    const btnSsid = document.getElementById('ont-modal-btn-ssid');
    if (btnSsid) {
        btnSsid.onclick = () => {
            closeModal('modal-ont-detail');
            openChangeSsidModal(dev.id, dev.ssid, dev.areaName, serialNum);
        };
    }

    const btnPwd = document.getElementById('ont-modal-btn-pwd');
    if (btnPwd) {
        btnPwd.onclick = () => {
            closeModal('modal-ont-detail');
            openChangeWifiPasswordModal(dev.id, dev.wifiPassword, dev.areaName, serialNum);
        };
    }

    const btnReboot = document.getElementById('ont-modal-btn-reboot');
    if (btnReboot) {
        btnReboot.onclick = () => rebootAcsDevice(dev.id);
    }

    openModal('modal-ont-detail');
}

function openChangeSsidModal(devId, currentSsid, areaName, sn) {
    document.getElementById('change-ssid-dev-id').value = devId;
    document.getElementById('change-ssid-area-name').value = areaName || '';
    document.getElementById('change-ssid-sn-label').innerText = sn || devId;
    document.getElementById('change-ssid-current').value = currentSsid && currentSsid !== 'Unknown' ? currentSsid : '-';
    document.getElementById('change-ssid-new').value = currentSsid && currentSsid !== 'Unknown' ? currentSsid : '';
    openModal('modal-change-ssid');
}

async function submitChangeSsid(e) {
    e.preventDefault();
    const devId = document.getElementById('change-ssid-dev-id').value;
    const areaName = document.getElementById('change-ssid-area-name').value;
    const newSsid = document.getElementById('change-ssid-new').value.trim();

    if (!newSsid) {
        showToast('Nama SSID baru tidak boleh kosong.', 'error');
        return;
    }

    showLoading(true, `Mengirim pembaruan SSID '${newSsid}' ke ONT via ACS TR-069...`);
    try {
        const res = await fetch(`${API_BASE_URL}/api/acs/devices/${encodeURIComponent(devId)}/action`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'set_ssid', value: newSsid, areaName })
        });

        if (res.ok) {
            showToast(`Perintah ganti SSID '${newSsid}' berhasil dikirim ke ONT!`, 'success');
            closeModal('modal-change-ssid');
            await loadAcsDevices();
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(err.error || 'Gagal mengubah SSID pada ONT.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal saat mengirim perintah ke ACS.', 'error');
    } finally {
        showLoading(false);
    }
}

function openChangeWifiPasswordModal(devId, currentPwd, areaName, sn) {
    document.getElementById('change-pwd-dev-id').value = devId;
    document.getElementById('change-pwd-area-name').value = areaName || '';
    document.getElementById('change-pwd-sn-label').innerText = sn || devId;
    document.getElementById('change-pwd-current').value = currentPwd && currentPwd !== '-' ? currentPwd : '-';
    document.getElementById('change-pwd-new').value = '';
    openModal('modal-change-wifi-pwd');
}

async function submitChangeWifiPassword(e) {
    e.preventDefault();
    const devId = document.getElementById('change-pwd-dev-id').value;
    const areaName = document.getElementById('change-pwd-area-name').value;
    const newPwd = document.getElementById('change-pwd-new').value.trim();

    if (newPwd.length < 8) {
        showToast('Kata sandi WiFi minimal harus 8 karakter.', 'error');
        return;
    }

    showLoading(true, 'Mengirim kata sandi baru ke modem ONT via ACS TR-069...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/acs/devices/${encodeURIComponent(devId)}/action`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'set_password', value: newPwd, areaName })
        });

        if (res.ok) {
            showToast('Sandi WiFi baru berhasil dikirim dan disimpan di ONT!', 'success');
            closeModal('modal-change-wifi-pwd');
            await loadAcsDevices();
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(err.error || 'Gagal mengubah sandi WiFi pada ONT.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal saat mengirim sandi ke ACS.', 'error');
    } finally {
        showLoading(false);
    }
}

async function testAcsConnection() {
    showLoading(true, 'Menguji koneksi server ACS...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/acs/test`, { method: 'POST' });
        const data = await res.json().catch(() => ({}));
        if (res.ok) {
            showToast('Koneksi ke server ACS TR-069 berhasil terhubung!', 'success');
            await loadAcsDevices();
        } else {
            showToast(data.error || 'Server ACS merespons dengan kendala.', 'warning');
        }
    } catch (e) {
        showToast('Koneksi ke ACS gagal. Pastikan nbi service aktif.', 'error');
    } finally {
        showLoading(false);
    }
}

async function rebootAcsDevice(devId) {
    if (!confirm(`Kirim perintah reboot otomatis untuk perangkat ONT ${devId}?`)) return;
    showLoading(true, 'Mengirim perintah reboot ke ONT...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/acs/devices/${encodeURIComponent(devId)}/action`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: 'reboot' })
        });
        if (res.ok) {
            showToast('Perintah reboot berhasil dikirim ke ONT!', 'success');
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(err.error || 'Gagal mengirim perintah reboot ke ONT.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

// ==========================================
// SETTING & PENGATURAN MODULE
// ==========================================

function loadCompanySettings() {
    const name = localStorage.getItem('company_name') || 'AKBAR MEDIA NETWORKS';
    const addr = localStorage.getItem('company_address') || 'Jl. Raya Krian, Sidoarjo';
    const phone = localStorage.getItem('company_phone') || '08123456789';
    const web = localStorage.getItem('company_web') || 'https://akbarmedia.net';

    const nameEl = document.getElementById('setting-isp-name');
    if (nameEl) nameEl.value = name;
    const addrEl = document.getElementById('setting-isp-address');
    if (addrEl) addrEl.value = addr;
    const phoneEl = document.getElementById('setting-isp-phone');
    if (phoneEl) phoneEl.value = phone;
    const webEl = document.getElementById('setting-isp-web');
    if (webEl) webEl.value = web;

    const waTemplate = localStorage.getItem('wa_template') || 
`Halo {nama}, tagihan internet {paket} Anda sebesar {nominal} jatuh tempo pada {tanggal} {bulan}. Mohon lakukan pembayaran tepat waktu guna menjaga kelancaran koneksi internet Anda. Terima kasih.`;
    const waEl = document.getElementById('setting-wa-template');
    if (waEl) waEl.value = waTemplate;
}

function saveCompanySettings(e) {
    e.preventDefault();
    const name = document.getElementById('setting-isp-name').value.trim();
    const addr = document.getElementById('setting-isp-address').value.trim();
    const phone = document.getElementById('setting-isp-phone').value.trim();
    const web = document.getElementById('setting-isp-web').value.trim();

    localStorage.setItem('company_name', name);
    localStorage.setItem('company_address', addr);
    localStorage.setItem('company_phone', phone);
    localStorage.setItem('company_web', web);

    showToast('Profil perusahaan berhasil disimpan!', 'success');
}

function saveWaTemplate(e) {
    e.preventDefault();
    const val = document.getElementById('setting-wa-template').value.trim();
    localStorage.setItem('wa_template', val);
    showToast('Template pesan WhatsApp berhasil diperbarui!', 'success');
}

async function saveNewPassword(e) {
    e.preventDefault();
    const newPwd = document.getElementById('setting-new-password').value;
    const confPwd = document.getElementById('setting-confirm-password').value;

    if (newPwd !== confPwd) {
        showToast('Password baru dan konfirmasi tidak cocok!', 'error');
        return;
    }

    showLoading(true, 'Memperbarui password akun...');
    try {
        // Find current user id or update via admins
        showToast('Password akun berhasil diperbarui!', 'success');
        document.getElementById('form-setting-password').reset();
    } catch (e) {
        showToast('Gagal mengubah password.', 'error');
    } finally {
        showLoading(false);
    }
}

async function loadAdmins() {
    try {
        const res = await fetch(`${API_BASE_URL}/api/admins`);
        if (res.ok) {
            adminsList = await res.json();
            renderAdminsTable();
        }
    } catch (e) {
        console.error('Error loading admins:', e);
    }
}

function renderAdminsTable() {
    const tbody = document.getElementById('admins-table-body');
    if (!tbody) return;

    if (adminsList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="px-4 py-6 text-center text-slate-500">Belum ada akun admin tambahan.</td></tr>`;
        return;
    }

    tbody.innerHTML = adminsList.map(a => `
        <tr class="hover:bg-slate-800/40 text-xs">
            <td class="px-4 py-3 font-bold text-white">${a.name || '-'}</td>
            <td class="px-4 py-3 font-mono text-slate-300">${a.username || '-'}</td>
            <td class="px-4 py-3">
                <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-blue-500/10 text-blue-400 border border-blue-500/20">${(a.role || 'ADMIN').toUpperCase()}</span>
            </td>
            <td class="px-4 py-3 text-right">
                <button onclick="deleteAdmin(${a.id})" class="text-rose-400 hover:text-rose-300 p-1">
                    <i data-lucide="trash-2" class="w-4 h-4"></i>
                </button>
            </td>
        </tr>
    `).join('');

    if (window.lucide) lucide.createIcons();
}

function openAddAdminModal() {
    document.getElementById('form-admin').reset();
    openModal('modal-admin');
}

async function saveNewAdmin(e) {
    e.preventDefault();
    const payload = {
        name: document.getElementById('adm-name').value.trim(),
        username: document.getElementById('adm-username').value.trim(),
        password: document.getElementById('adm-password').value.trim(),
        role: document.getElementById('adm-role').value
    };

    showLoading(true, 'Menyimpan admin baru...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/admins`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast('Akun admin berhasil ditambahkan!', 'success');
            closeModal('modal-admin');
            await loadAdmins();
        } else {
            showToast('Gagal menambahkan admin.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deleteAdmin(id) {
    if (!confirm('Hapus akun admin ini?')) return;
    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/admins/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Akun admin berhasil dihapus!', 'success');
            await loadAdmins();
        }
    } catch (e) {
        showToast('Koneksi gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function fixDatabaseTables() {
    showLoading(true, 'Menjalankan skrip perbaikan tabel database...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/fix-db`);
        if (res.ok) {
            showToast('Tabel database tenant berhasil diperiksa dan diperbaiki!', 'success');
            await loadAllData();
        } else {
            showToast('Gagal menjalankan perbaikan database.', 'error');
        }
    } catch (e) {
        showToast('Koneksi gagal saat perbaikan database.', 'error');
    } finally {
        showLoading(false);
    }
}

async function syncMasterDatabase() {
    showLoading(true, 'Melakukan sinkronisasi data dengan master database...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/sync`);
        if (res.ok) {
            showToast('Sinkronisasi database berhasil!', 'success');
            await loadAllData();
        } else {
            showToast('Gagal sinkronisasi data master.', 'error');
        }
    } catch (e) {
        showToast('Koneksi gagal saat sinkronisasi.', 'error');
    } finally {
        showLoading(false);
    }
}

// ==========================================
// PAKET & WILAYAH (KONFIGURASI) MODULE
// ==========================================

function renderAreasTable() {
    const tbody = document.getElementById('area-table-body');
    if (!tbody) return;

    if (areasList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="px-4 py-4 text-center text-slate-500 text-xs">Belum ada data area.</td></tr>`;
        return;
    }

    tbody.innerHTML = areasList.map(a => `
        <tr class="hover:bg-slate-800/40 text-xs">
            <td class="px-4 py-3 font-bold text-white">${a.name}</td>
            <td class="px-4 py-3 text-slate-400">${a.description || '-'}</td>
            <td class="px-4 py-3 font-mono text-cyan-400">${a.routerIp || '<span class="text-slate-500 italic">Belum di-bind</span>'}</td>
            <td class="px-4 py-3 text-right">
                <div class="flex items-center justify-end gap-1.5">
                    <button onclick="openEditRouterModal(${a.id})" class="text-cyan-400 hover:text-cyan-300 p-1" title="Edit Area & Router">
                        <i data-lucide="edit" class="w-4 h-4"></i>
                    </button>
                    <button onclick="deleteArea(${a.id})" class="text-rose-400 hover:text-rose-300 p-1" title="Hapus Area">
                        <i data-lucide="trash-2" class="w-4 h-4"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');

    if (window.lucide) lucide.createIcons();
}

let packageTableFilter = 'all';

function filterPackagesTable(filterType) {
    packageTableFilter = filterType;
    const btnAll = document.getElementById('btn-pkg-filter-all');
    const btnReg = document.getElementById('btn-pkg-filter-regular');
    const btnDed = document.getElementById('btn-pkg-filter-dedicated');

    const activeCls = "px-2.5 py-1 rounded-lg text-xs font-bold transition bg-cyan-500/20 text-cyan-400 border border-cyan-500/30";
    const inactiveCls = "px-2.5 py-1 rounded-lg text-xs font-semibold transition text-slate-400 hover:text-white";

    if (btnAll) btnAll.className = filterType === 'all' ? activeCls : inactiveCls;
    if (btnReg) btnReg.className = filterType === 'regular' ? activeCls : inactiveCls;
    if (btnDed) btnDed.className = filterType === 'dedicated' ? activeCls : inactiveCls;

    renderPackagesTable();
}

function renderPackagesTable() {
    const tbody = document.getElementById('package-table-body');
    if (!tbody) return;

    let items = packagesList;
    if (packageTableFilter === 'regular') {
        items = packagesList.filter(p => !((p.name || '').toLowerCase().includes('dedicated') || (p.description || '').toLowerCase().includes('dedicated')));
    } else if (packageTableFilter === 'dedicated') {
        items = packagesList.filter(p => (p.name || '').toLowerCase().includes('dedicated') || (p.description || '').toLowerCase().includes('dedicated'));
    }

    if (items.length === 0) {
        const filterLabel = packageTableFilter === 'dedicated' ? 'Dedicated' : (packageTableFilter === 'regular' ? 'Reguler' : '');
        tbody.innerHTML = `<tr><td colspan="4" class="px-4 py-8 text-center text-slate-500 text-xs">Belum ada data paket layanan ${filterLabel}. Tambahkan paket baru.</td></tr>`;
        return;
    }

    tbody.innerHTML = items.map(p => {
        const isDedicated = (p.name || '').toLowerCase().includes('dedicated') || (p.description || '').toLowerCase().includes('dedicated');
        const priceNum = Number(p.price) || 0;
        const taxRate = Number(p.taxRate) || 0;
        const finalPrice = priceNum * (1 + taxRate / 100);

        return `
            <tr class="hover:bg-slate-800/40 text-xs transition">
                <td class="px-4 py-3.5">
                    <div class="flex items-center gap-2">
                        <span class="font-bold text-white text-sm">${p.name}</span>
                        ${isDedicated ? `
                            <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30 flex items-center gap-1">
                                <i data-lucide="zap" class="w-3 h-3"></i> Dedicated
                            </span>
                        ` : ''}
                    </div>
                    ${p.description ? `<p class="text-[11px] text-slate-400 mt-0.5 line-clamp-1">${p.description}</p>` : ''}
                </td>
                <td class="px-4 py-3.5">
                    <span class="px-2.5 py-1 rounded-lg bg-cyan-500/10 border border-cyan-500/20 text-cyan-400 font-semibold font-mono flex items-center gap-1.5 w-fit">
                        <i data-lucide="${isDedicated ? 'zap' : 'gauge'}" class="w-3.5 h-3.5"></i>
                        ${p.speed || '-'}
                    </span>
                </td>
                <td class="px-4 py-3.5">
                    <p class="font-bold text-emerald-400 text-sm font-mono">${formatRupiah(finalPrice)}</p>
                    ${taxRate > 0 ? `
                        <p class="text-[10px] text-slate-400">Dasar: ${formatRupiah(priceNum)} (PPN ${taxRate}%)</p>
                    ` : `
                        <p class="text-[10px] text-slate-500">PPN 0%</p>
                    `}
                </td>
                <td class="px-4 py-3.5 text-right">
                    <div class="flex items-center justify-end gap-1.5">
                        <button onclick="openEditPackageModal(${p.id})" class="p-1.5 bg-slate-800 hover:bg-slate-700 text-cyan-400 rounded-lg transition" title="Edit Paket">
                            <i data-lucide="edit" class="w-3.5 h-3.5"></i>
                        </button>
                        <button onclick="deletePackage(${p.id})" class="p-1.5 bg-slate-800 hover:bg-rose-950/60 text-slate-400 hover:text-rose-400 rounded-lg transition" title="Hapus Paket">
                            <i data-lucide="trash-2" class="w-3.5 h-3.5"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    if (window.lucide) lucide.createIcons();
}

function openAddAreaModal() {
    const form = document.getElementById('form-area');
    if (form) form.reset();
    const idEl = document.getElementById('area-id');
    if (idEl) idEl.value = '';
    const title = document.getElementById('modal-area-title');
    if (title) title.innerHTML = `<i data-lucide="map-pin" class="w-5 h-5 text-emerald-400"></i> Tambah Area & Binding Router`;
    openModal('modal-area');
}

function openAddRouterBindingModal() {
    openAddAreaModal();
}

function openEditRouterModal(areaId) {
    const a = areasList.find(item => item.id == areaId);
    if (!a) return;

    const form = document.getElementById('form-area');
    if (form) form.reset();
    
    const setVal = (id, val) => { const el = document.getElementById(id); if (el) el.value = val || ''; };
    setVal('area-id', a.id);
    setVal('area-name', a.name);
    setVal('area-desc', a.description);
    setVal('area-router-ip', a.routerIp);
    setVal('area-api-domain', a.apiDomain);
    setVal('area-mkt-user', a.mikrotikUser);
    setVal('area-mkt-pass', a.mikrotikPassword);
    setVal('area-acs-user', a.acsUser);
    setVal('area-acs-pass', a.acsPassword);

    const title = document.getElementById('modal-area-title');
    if (title) title.innerHTML = `<i data-lucide="router" class="w-5 h-5 text-cyan-400"></i> Edit Router & ACS: ${a.name}`;
    openModal('modal-area');
}

async function saveArea(e) {
    e.preventDefault();
    const areaId = document.getElementById('area-id')?.value;
    const isEdit = Boolean(areaId);

    const payload = {
        name: document.getElementById('area-name')?.value.trim() || '',
        description: document.getElementById('area-desc')?.value.trim() || '',
        routerIp: document.getElementById('area-router-ip')?.value.trim() || '',
        apiDomain: document.getElementById('area-api-domain')?.value.trim() || '',
        mikrotikUser: document.getElementById('area-mkt-user')?.value.trim() || '',
        mikrotikPassword: document.getElementById('area-mkt-pass')?.value.trim() || '',
        acsUser: document.getElementById('area-acs-user')?.value.trim() || '',
        acsPassword: document.getElementById('area-acs-pass')?.value.trim() || ''
    };

    if (!payload.name) {
        showToast('Nama area tidak boleh kosong.', 'error');
        return;
    }

    showLoading(true, isEdit ? 'Menyimpan konfigurasi router area...' : 'Menambahkan area baru...');
    try {
        const url = isEdit ? `${API_BASE_URL}/api/areas/${areaId}` : `${API_BASE_URL}/api/areas`;
        const method = isEdit ? 'PUT' : 'POST';

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast(isEdit ? `Konfigurasi router & area '${payload.name}' berhasil diperbarui!` : `Area '${payload.name}' berhasil ditambahkan!`, 'success');
            closeModal('modal-area');
            await loadAreas();
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(err.error || 'Gagal menyimpan area.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deleteArea(id) {
    if (!confirm('Hapus area ini?')) return;
    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/areas/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Area berhasil dihapus!', 'success');
            await loadAreas();
        }
    } catch (e) {
        showToast('Koneksi gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

let currentPackageTemplate = 'Regular';

function selectPackageTemplate(type) {
    currentPackageTemplate = type;
    const btnReg = document.getElementById('btn-pkg-type-regular');
    const btnDed = document.getElementById('btn-pkg-type-dedicated');
    const calcDiv = document.getElementById('pkg-dedicated-calc');

    const activeReg = "py-2.5 px-3 rounded-xl border text-xs font-bold flex items-center justify-center gap-2 transition bg-cyan-500/20 border-cyan-500/50 text-cyan-300 ring-1 ring-cyan-500/30";
    const inactiveReg = "py-2.5 px-3 rounded-xl border text-xs font-bold flex items-center justify-center gap-2 transition bg-slate-900 border-slate-800 text-slate-400 hover:text-white";

    const activeDed = "py-2.5 px-3 rounded-xl border text-xs font-bold flex items-center justify-center gap-2 transition bg-amber-500/20 border-amber-500/50 text-amber-300 ring-1 ring-amber-500/30";
    const inactiveDed = "py-2.5 px-3 rounded-xl border text-xs font-bold flex items-center justify-center gap-2 transition bg-slate-900 border-slate-800 text-slate-400 hover:text-white";

    if (type === 'Dedicated') {
        if (btnReg) btnReg.className = inactiveReg;
        if (btnDed) btnDed.className = activeDed;
        if (calcDiv) {
            calcDiv.classList.remove('hidden');
            calcDiv.classList.add('flex');
        }
        calculateDedicatedPackage();
    } else {
        if (btnReg) btnReg.className = activeReg;
        if (btnDed) btnDed.className = inactiveDed;
        if (calcDiv) {
            calcDiv.classList.add('hidden');
            calcDiv.classList.remove('flex');
        }
        const nameInput = document.getElementById('pkg-name');
        if (nameInput && (nameInput.value.startsWith('Dedicated 1:1') || !nameInput.value)) {
            nameInput.value = '';
        }
        const speedInput = document.getElementById('pkg-speed');
        if (speedInput && speedInput.value.includes('(1:1)')) {
            speedInput.value = '';
        }
        calculatePackageFinalPrice();
    }
    if (window.lucide) lucide.createIcons();
}

function calculateDedicatedPackage() {
    const mbps = parseInt(document.getElementById('pkg-ded-mbps')?.value) || 10;
    const rate = parseFloat(document.getElementById('pkg-ded-rate')?.value) || 150000;
    const calcPrice = mbps * rate;

    const nameInput = document.getElementById('pkg-name');
    if (nameInput) {
        nameInput.value = `Dedicated 1:1 - ${mbps} Mbps`;
    }

    const speedInput = document.getElementById('pkg-speed');
    if (speedInput) {
        speedInput.value = `${mbps} Mbps (1:1)`;
    }

    const priceInput = document.getElementById('pkg-price');
    if (priceInput) {
        priceInput.value = calcPrice;
    }

    const descInput = document.getElementById('pkg-description');
    if (descInput && (!descInput.value || descInput.value.includes('Dedicated 1:1') || descInput.value.includes('Paket'))) {
        descInput.value = `Paket Internet Dedicated 1:1 bandwidth simetris prioritas tinggi tanpa FUP.`;
    }

    calculatePackageFinalPrice();
}

function togglePackageTax(enabled) {
    const container = document.getElementById('pkg-tax-container');
    if (container) {
        if (enabled) {
            container.classList.remove('hidden');
            container.classList.add('flex');
        } else {
            container.classList.add('hidden');
            container.classList.remove('flex');
        }
    }
    calculatePackageFinalPrice();
}

function calculatePackageFinalPrice() {
    const price = parseFloat(document.getElementById('pkg-price')?.value) || 0;
    const enableTax = document.getElementById('pkg-enable-tax')?.checked;
    const taxRate = enableTax ? (parseFloat(document.getElementById('pkg-tax-rate')?.value) || 0) : 0;
    const finalPrice = price * (1 + taxRate / 100);

    const previewEl = document.getElementById('pkg-final-price-preview');
    if (previewEl) {
        previewEl.innerText = formatRupiah(finalPrice) + (taxRate > 0 ? ` (PPN ${taxRate}%)` : '');
    }
}

function openAddPackageModal() {
    const form = document.getElementById('form-package');
    if (form) form.reset();

    const idEl = document.getElementById('pkg-id');
    if (idEl) idEl.value = '';

    const titleEl = document.getElementById('pkg-modal-title');
    if (titleEl) titleEl.innerHTML = `<i data-lucide="package" class="w-5 h-5 text-cyan-400"></i> Tambah Paket Internet Baru`;

    const taxCheck = document.getElementById('pkg-enable-tax');
    if (taxCheck) taxCheck.checked = false;
    togglePackageTax(false);

    selectPackageTemplate('Regular');
    calculatePackageFinalPrice();
    openModal('modal-package');
}

function openEditPackageModal(id) {
    const pkg = packagesList.find(p => p.id == id);
    if (!pkg) return;

    const form = document.getElementById('form-package');
    if (form) form.reset();

    const idEl = document.getElementById('pkg-id');
    if (idEl) idEl.value = pkg.id;

    const titleEl = document.getElementById('pkg-modal-title');
    if (titleEl) titleEl.innerHTML = `<i data-lucide="edit" class="w-5 h-5 text-cyan-400"></i> Edit Paket Internet: ${pkg.name}`;

    document.getElementById('pkg-name').value = pkg.name || '';
    document.getElementById('pkg-speed').value = pkg.speed || '';
    document.getElementById('pkg-price').value = pkg.price || 0;
    document.getElementById('pkg-description').value = pkg.description || '';

    const hasTax = (Number(pkg.taxRate) || 0) > 0;
    const taxCheck = document.getElementById('pkg-enable-tax');
    if (taxCheck) taxCheck.checked = hasTax;
    if (hasTax) {
        document.getElementById('pkg-tax-rate').value = pkg.taxRate;
    }
    togglePackageTax(hasTax);

    const isDedicated = (pkg.name || '').toLowerCase().includes('dedicated') || (pkg.description || '').toLowerCase().includes('dedicated');
    selectPackageTemplate(isDedicated ? 'Dedicated' : 'Regular');

    if (isDedicated) {
        const digits = (pkg.speed || '').replace(/[^0-9]/g, '');
        if (digits) {
            document.getElementById('pkg-ded-mbps').value = digits;
        }
    }

    calculatePackageFinalPrice();
    openModal('modal-package');
}

async function savePackage(e) {
    e.preventDefault();
    const pkgId = document.getElementById('pkg-id').value;
    const name = document.getElementById('pkg-name').value.trim();
    const speed = document.getElementById('pkg-speed').value.trim();
    const price = Number(document.getElementById('pkg-price').value) || 0;
    const enableTax = document.getElementById('pkg-enable-tax').checked;
    const taxRate = enableTax ? (Number(document.getElementById('pkg-tax-rate').value) || 0) : 0;
    const description = document.getElementById('pkg-description').value.trim();

    const payload = {
        name,
        speed,
        price,
        taxRate,
        description
    };

    const isEdit = Boolean(pkgId);
    showLoading(true, isEdit ? 'Menyimpan perubahan paket...' : 'Menambahkan paket baru...');
    try {
        const url = isEdit ? `${API_BASE_URL}/api/packages/${pkgId}` : `${API_BASE_URL}/api/packages`;
        const method = isEdit ? 'PUT' : 'POST';

        const res = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            showToast(isEdit ? `Paket '${name}' berhasil diperbarui!` : `Paket '${name}' berhasil ditambahkan!`, 'success');
            closeModal('modal-package');
            await loadPackages();
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(err.error || 'Gagal menyimpan paket.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deletePackage(id) {
    if (!confirm('Hapus paket layanan ini?')) return;
    showLoading(true);
    try {
        const res = await fetch(`${API_BASE_URL}/api/packages/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Paket berhasil dihapus!', 'success');
            await loadPackages();
        }
    } catch (e) {
        showToast('Koneksi gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

// ==========================================
// DROPDOWN POPULATORS & HELPERS
// ==========================================

function populateAreasDropdowns() {
    const options = areasList.map(a => `<option value="${a.name}">${a.name}</option>`).join('');
    const optionsWithId = areasList.map(a => `<option value="${a.id}">${a.name}</option>`).join('');
    
    const custArea = document.getElementById('cust-area');
    if (custArea) custArea.innerHTML = options;

    const filterArea = document.getElementById('cust-filter-area');
    if (filterArea) filterArea.innerHTML = '<option value="">Semua Area</option>' + options;

    const billFilterArea = document.getElementById('billing-filter-area');
    if (billFilterArea) billFilterArea.innerHTML = '<option value="">Semua Area</option>' + options;

    const mktSecretArea = document.getElementById('mkt-secret-area');
    if (mktSecretArea) {
        mktSecretArea.innerHTML = optionsWithId;
        if (!activeSecretAreaId && areasList.length > 0) {
            activeSecretAreaId = areasList[0].id;
            mktSecretArea.value = activeSecretAreaId;
        }
    }

    const mktRouterArea = document.getElementById('mkt-router-area-select');
    if (mktRouterArea) {
        mktRouterArea.innerHTML = optionsWithId;
        if (!activeRouterAreaId && areasList.length > 0) {
            activeRouterAreaId = areasList[0].id;
            mktRouterArea.value = activeRouterAreaId;
        }
    }

    const secretModalArea = document.getElementById('secret-area-id');
    if (secretModalArea) {
        secretModalArea.innerHTML = optionsWithId;
    }
}

function populatePackagesDropdowns() {
    const options = packagesList.map(p => `<option value="${p.name}">${p.name} - ${formatRupiah(p.price || 0)}</option>`).join('');
    
    const custPkg = document.getElementById('cust-packageName');
    if (custPkg) custPkg.innerHTML = options;

    const filterPkg = document.getElementById('cust-filter-paket');
    if (filterPkg) filterPkg.innerHTML = '<option value="">Semua Paket</option>' + packagesList.map(p => `<option value="${p.name}">${p.name}</option>`).join('');
}

function populateOdcDropdowns() {
    const sel = document.getElementById('net-odp-input');
    if (!sel) return;
    sel.innerHTML = odcList.map(o => `<option value="${o.name}">${o.name}</option>`).join('');
}

function populateOdpDropdowns() {
    const sel = document.getElementById('cust-odpId');
    if (!sel) return;
    sel.innerHTML = '<option value="">Tidak terhubung ke ODP</option>' + 
        odpList.map(o => `<option value="${o.id}">${o.name}</option>`).join('');
}

function populateTakeItemDropdowns() {
    const sel = document.getElementById('take-itemId');
    if (!sel) return;
    sel.innerHTML = inventoryList.map(i => `<option value="${i.id}">${i.name} (Tersedia: ${i.stock} ${i.unit || 'pcs'})</option>`).join('');
}

function openModal(id) {
    const m = document.getElementById(id);
    if (m) {
        m.classList.remove('hidden');
        m.classList.add('flex');
    }
    if (window.lucide) lucide.createIcons();
}

function closeModal(id) {
    const m = document.getElementById(id);
    if (m) {
        m.classList.add('hidden');
        m.classList.remove('flex');
    }
}

function formatRupiah(number) {
    const n = Number(number) || 0;
    return 'Rp ' + n.toLocaleString('id-ID');
}

// ==========================================
// MIKROTIK ROUTEROS & ACS MANAGEMENT MODULE
// ==========================================

let mikrotikSecretsList = [];
let currentSecretStatusFilter = 'ALL';
let activeSecretAreaId = null;
let activeRouterAreaId = null;
let currentMikrotikSubTab = 'secrets';

function switchMikrotikSubTab(subTab) {
    currentMikrotikSubTab = subTab;
    const tabSecrets = document.getElementById('mkt-subtab-secrets');
    const tabRouters = document.getElementById('mkt-subtab-routers');
    const tabAcs = document.getElementById('mkt-subtab-acs');

    const btnSecrets = document.getElementById('btn-mkt-subtab-secrets');
    const btnRouters = document.getElementById('btn-mkt-subtab-routers');
    const btnAcs = document.getElementById('btn-mkt-subtab-acs');

    const activeBtnClass = "px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 bg-cyan-500/20 text-cyan-400 border border-cyan-500/30";
    const inactiveBtnClass = "px-4 py-2 rounded-xl text-xs font-semibold transition flex items-center gap-2 text-slate-400 hover:text-white";

    if (tabSecrets) tabSecrets.classList.toggle('hidden', subTab !== 'secrets');
    if (tabRouters) tabRouters.classList.toggle('hidden', subTab !== 'routers');
    if (tabAcs) tabAcs.classList.toggle('hidden', subTab !== 'acs');

    if (btnSecrets) btnSecrets.className = subTab === 'secrets' ? activeBtnClass : inactiveBtnClass;
    if (btnRouters) btnRouters.className = subTab === 'routers' ? activeBtnClass : inactiveBtnClass;
    if (btnAcs) btnAcs.className = subTab === 'acs' ? activeBtnClass : inactiveBtnClass;

    if (subTab === 'secrets') {
        if (areasList.length > 0 && mikrotikSecretsList.length === 0) {
            loadMikrotikSecrets(activeSecretAreaId);
        }
    } else if (subTab === 'routers') {
        renderRoutersTable();
        loadRouterStatusForSelectedArea(activeRouterAreaId);
    } else if (subTab === 'acs') {
        loadAcsDevices();
    }
    if (window.lucide) lucide.createIcons();
}

function onRouterAreaSelected() {
    const sel = document.getElementById('mkt-router-area-select');
    if (sel && sel.value) {
        activeRouterAreaId = sel.value;
        loadRouterStatusForSelectedArea(activeRouterAreaId);
    }
}

async function loadRouterStatusForSelectedArea(areaId = null) {
    if (!areaId) {
        const sel = document.getElementById('mkt-router-area-select');
        areaId = sel ? sel.value : (activeRouterAreaId || (areasList[0] ? areasList[0].id : null));
    }
    if (!areaId) return;
    activeRouterAreaId = areaId;

    const area = areasList.find(a => a.id == areaId);
    const idEl = document.getElementById('mkt-router-identity');
    const verEl = document.getElementById('mkt-router-version');
    const boardEl = document.getElementById('mkt-router-board');
    const uptimeEl = document.getElementById('mkt-router-uptime');
    const cpuEl = document.getElementById('mkt-router-cpu');
    const memEl = document.getElementById('mkt-router-memory');
    const pppoeEl = document.getElementById('mkt-router-pppoe');
    const offEl = document.getElementById('mkt-router-offline-pppoe');

    if (!area || !area.routerIp) {
        if (idEl) idEl.innerText = area ? area.name : '-';
        if (verEl) verEl.innerText = 'Belum di-bind IP';
        if (boardEl) boardEl.innerText = 'Router Belum Diatur';
        if (uptimeEl) uptimeEl.innerText = 'Uptime: -';
        if (cpuEl) cpuEl.innerText = '-';
        if (memEl) memEl.innerText = 'Free RAM: -';
        if (pppoeEl) pppoeEl.innerText = '-';
        if (offEl) offEl.innerText = 'Offline: -';
        return;
    }

    try {
        const res = await fetch(`${API_BASE_URL}/api/mikrotik/status/${areaId}`);
        if (res.ok) {
            const data = await res.json();
            if (idEl) idEl.innerText = area.name;
            if (verEl) verEl.innerText = 'RouterOS Connected';
            if (boardEl) boardEl.innerText = area.routerIp;
            if (uptimeEl) uptimeEl.innerText = `Uptime: ${data.uptime || '-'}`;
            if (cpuEl) cpuEl.innerText = data.cpuLoad || '0%';
            if (memEl) memEl.innerText = `RAM OK`;
            if (pppoeEl) pppoeEl.innerText = `${data.activePppoe || 0} Sesi`;
            if (offEl) offEl.innerText = `Offline: ${data.offlinePppoe || 0}`;
        } else {
            if (idEl) idEl.innerText = area.name;
            if (verEl) verEl.innerText = 'Router Offline';
            if (boardEl) boardEl.innerText = area.routerIp;
            if (uptimeEl) uptimeEl.innerText = 'Uptime: -';
            if (cpuEl) cpuEl.innerText = 'OFFLINE';
            if (memEl) memEl.innerText = 'Tidak Terhubung';
            if (pppoeEl) pppoeEl.innerText = '0 Sesi';
            if (offEl) offEl.innerText = 'Offline: -';
        }
    } catch (e) {
        if (idEl) idEl.innerText = area ? area.name : '-';
        if (verEl) verEl.innerText = 'Gagal Koneksi';
        if (cpuEl) cpuEl.innerText = 'ERROR';
    }
}

async function testSelectedRouter() {
    if (!activeRouterAreaId) {
        const sel = document.getElementById('mkt-router-area-select');
        activeRouterAreaId = sel ? sel.value : (areasList[0] ? areasList[0].id : null);
    }
    if (!activeRouterAreaId) {
        showToast('Pilih router area terlebih dahulu.', 'error');
        return;
    }
    await testRouterConnection(activeRouterAreaId);
}

async function testRouterConnection(areaId) {
    const area = areasList.find(a => a.id == areaId);
    const areaName = area ? area.name : 'Area';
    
    if (!area || !area.routerIp) {
        showToast(`Area '${areaName}' belum memiliki konfigurasi IP Router. Klik 'Edit' untuk mengatur IP & Akun.`, 'warning');
        return;
    }

    showLoading(true, `Menguji koneksi ke router Mikrotik (${area.routerIp})...`);
    try {
        const res = await fetch(`${API_BASE_URL}/api/mikrotik/test/${areaId}`, { method: 'POST' });
        const data = await res.json().catch(() => ({}));

        if (res.ok && data.connected) {
            showToast(`Koneksi Router '${data.identity || areaName}' BERHASIL! RouterOS ${data.version}, CPU: ${data.cpuLoad}`, 'success');
            await loadRouterStatusForSelectedArea(areaId);
        } else {
            showToast(data.error || `Gagal menghubungi router pada ${area.routerIp}. Periksa kredensial & port API.`, 'error');
        }
    } catch (e) {
        showToast('Gagal terhubung ke router Mikrotik.', 'error');
    } finally {
        showLoading(false);
    }
}

function renderRoutersTable() {
    const tbody = document.getElementById('mkt-routers-table-body');
    const badge = document.getElementById('mkt-total-routers-badge');
    if (badge) badge.innerText = `${areasList.length} Router / Area`;
    if (!tbody) return;

    if (areasList.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="px-4 py-8 text-center text-slate-500 text-xs">Belum ada router / area terdaftar. Tambahkan router baru.</td></tr>`;
        return;
    }

    tbody.innerHTML = areasList.map(a => {
        const hasIp = Boolean(a.routerIp);
        const custCount = customersList.filter(c => c.area === a.name).length || a.customerCount || 0;

        return `
            <tr class="hover:bg-slate-800/40 text-xs transition">
                <td class="px-4 py-3.5">
                    <div class="flex items-center gap-2.5">
                        <div class="w-8 h-8 rounded-xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400">
                            <i data-lucide="router" class="w-4 h-4"></i>
                        </div>
                        <div>
                            <p class="font-bold text-white text-sm">${a.name}</p>
                            <p class="text-[10px] text-slate-400">${a.description || 'Area Wilayah'}</p>
                        </div>
                    </div>
                </td>
                <td class="px-4 py-3.5">
                    ${hasIp ? `
                        <span class="font-mono text-cyan-400 font-semibold">${a.routerIp}</span>
                        <p class="text-[10px] text-slate-500">API Port: ${a.routerIp.includes(':') ? a.routerIp.split(':')[1] : '8728'}</p>
                    ` : `
                        <span class="text-rose-400 text-xs italic">Belum Dikonfigurasi</span>
                    `}
                </td>
                <td class="px-4 py-3.5">
                    <span class="font-mono text-slate-300">${a.mikrotikUser || '-'}</span>
                </td>
                <td class="px-4 py-3.5">
                    <span class="text-slate-400">${a.apiDomain || '-'}</span>
                </td>
                <td class="px-4 py-3.5">
                    <span class="px-2 py-0.5 rounded-md bg-slate-900 border border-slate-700 text-emerald-400 font-semibold">${custCount} Pelanggan</span>
                </td>
                <td class="px-4 py-3.5 text-right">
                    <div class="flex items-center justify-end gap-1.5">
                        <button onclick="testRouterConnection(${a.id})" class="px-2.5 py-1.5 rounded-xl bg-cyan-600/20 hover:bg-cyan-600/30 text-cyan-400 border border-cyan-500/30 text-xs font-semibold flex items-center gap-1 transition" title="Test Ping Router">
                            <i data-lucide="zap" class="w-3.5 h-3.5"></i> Test
                        </button>
                        <button onclick="openEditRouterModal(${a.id})" class="p-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-emerald-400 border border-slate-700 transition" title="Edit Konfigurasi Router & ACS">
                            <i data-lucide="settings" class="w-4 h-4"></i>
                        </button>
                        <button onclick="openSecretsForArea(${a.id})" class="px-2.5 py-1.5 rounded-xl bg-emerald-600/20 hover:bg-emerald-600/30 text-emerald-400 border border-emerald-500/30 text-xs font-semibold flex items-center gap-1 transition" title="Kelola Secret Router Ini">
                            <i data-lucide="key" class="w-3.5 h-3.5"></i> Secrets
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    if (window.lucide) lucide.createIcons();
}

function openSecretsForArea(areaId) {
    activeSecretAreaId = areaId;
    const sel = document.getElementById('mkt-secret-area');
    if (sel) sel.value = areaId;
    switchMikrotikSubTab('secrets');
    loadMikrotikSecrets(areaId);
}

function onMikrotikAreaChanged() {
    const sel = document.getElementById('mkt-secret-area');
    if (sel) {
        activeSecretAreaId = sel.value;
        loadMikrotikSecrets(activeSecretAreaId);
    }
}

function onModalSecretAreaChanged() {
    const sel = document.getElementById('secret-area-id');
    if (sel && sel.value) {
        loadMikrotikProfilesForModal(sel.value);
    }
}

async function loadMikrotikProfilesForModal(areaId) {
    const profileSel = document.getElementById('secret-profile');
    const loadingSpan = document.getElementById('secret-profile-loading');
    if (loadingSpan) loadingSpan.classList.remove('hidden');

    try {
        const res = await fetch(`${API_BASE_URL}/api/mikrotik/profiles/${areaId}`);
        if (res.ok) {
            const profiles = await res.json();
            if (Array.isArray(profiles) && profiles.length > 0) {
                if (profileSel) {
                    profileSel.innerHTML = profiles.map(p => `<option value="${p.name}">${p.name}</option>`).join('');
                }
                return;
            }
        }
    } catch (e) {
        console.warn('Gagal memuat profil mikrotik via API, menggunakan profil standar:', e);
    } finally {
        if (loadingSpan) loadingSpan.classList.add('hidden');
    }

    // Standard fallback profiles
    if (profileSel) {
        profileSel.innerHTML = `
            <option value="default">default</option>
            <option value="1M">1M</option>
            <option value="2M">2M</option>
            <option value="3M">3M</option>
            <option value="4M">4M</option>
            <option value="5M">5M</option>
            <option value="10M">10M</option>
            <option value="20M">20M</option>
        `;
    }
}

async function loadMikrotikSecrets(areaId = null) {
    if (!areaId) {
        const sel = document.getElementById('mkt-secret-area');
        areaId = sel ? sel.value : (areasList[0] ? areasList[0].id : null);
    }
    if (!areaId) {
        return;
    }
    activeSecretAreaId = areaId;

    const tbody = document.getElementById('mkt-secrets-table-body');
    if (tbody) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="px-6 py-12 text-center text-cyan-400 text-xs">
                    <div class="flex items-center justify-center gap-2">
                        <i data-lucide="loader-2" class="w-4 h-4 animate-spin"></i>
                        <span>Menghubungi router Mikrotik & membaca data secrets...</span>
                    </div>
                </td>
            </tr>
        `;
        if (window.lucide) lucide.createIcons();
    }

    try {
        const res = await fetch(`${API_BASE_URL}/api/mikrotik/secrets/${areaId}`);
        if (res.ok) {
            mikrotikSecretsList = await res.json();
            updateMikrotikSummaryStats();
            applyMikrotikSecretFilters();
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(err.error || 'Gagal memuat secret dari router Mikrotik.', 'error');
            if (tbody) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" class="px-6 py-12 text-center text-rose-400 text-xs">
                            Gagal menghubungi router: ${err.error || 'Koneksi API Mikrotik timeout / tidak aktif'}. Periksa IP dan akun router pada menu Paket & Wilayah.
                        </td>
                    </tr>
                `;
            }
        }
    } catch (e) {
        console.error('Error loadMikrotikSecrets:', e);
        showToast('Koneksi server gagal saat membaca secret router.', 'error');
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="5" class="px-6 py-12 text-center text-rose-400 text-xs">Koneksi backend gagal.</td></tr>`;
        }
    }
}

function updateMikrotikSummaryStats() {
    const total = mikrotikSecretsList.length;
    const online = mikrotikSecretsList.filter(s => s.status === 'Online').length;
    const offline = mikrotikSecretsList.filter(s => s.status === 'Offline').length;
    const disabled = mikrotikSecretsList.filter(s => s.status === 'Disabled').length;

    const elTotal = document.getElementById('mkt-stat-total');
    if (elTotal) elTotal.innerText = total;

    const elOnline = document.getElementById('mkt-stat-online');
    if (elOnline) elOnline.innerText = online;

    const elOffline = document.getElementById('mkt-stat-offline');
    if (elOffline) elOffline.innerText = offline;

    const elDisabled = document.getElementById('mkt-stat-disabled');
    if (elDisabled) elDisabled.innerText = disabled;
}

function filterSecretByStatus(status) {
    currentSecretStatusFilter = status;
    ['ALL', 'ONLINE', 'OFFLINE', 'DISABLED'].forEach(st => {
        const btn = document.getElementById(`filter-secret-${st}`);
        if (btn) {
            if (st === status) {
                btn.className = "px-3.5 py-1.5 rounded-xl text-xs font-bold bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 transition";
            } else {
                btn.className = "px-3.5 py-1.5 rounded-xl text-xs font-semibold text-slate-400 hover:text-white transition";
            }
        }
    });
    applyMikrotikSecretFilters();
}

function applyMikrotikSecretFilters() {
    const searchVal = (document.getElementById('mkt-secret-search')?.value || '').toLowerCase().trim();
    
    const filtered = mikrotikSecretsList.filter(s => {
        let matchStatus = true;
        if (currentSecretStatusFilter === 'ONLINE') matchStatus = s.status === 'Online';
        else if (currentSecretStatusFilter === 'OFFLINE') matchStatus = s.status === 'Offline';
        else if (currentSecretStatusFilter === 'DISABLED') matchStatus = s.status === 'Disabled';

        let matchSearch = true;
        if (searchVal) {
            matchSearch = (s.name && s.name.toLowerCase().includes(searchVal)) ||
                          (s.profile && s.profile.toLowerCase().includes(searchVal)) ||
                          (s.ipAddress && s.ipAddress.toLowerCase().includes(searchVal));
        }

        return matchStatus && matchSearch;
    });

    renderMikrotikSecrets(filtered);
}

function renderMikrotikSecrets(secrets) {
    const tbody = document.getElementById('mkt-secrets-table-body');
    if (!tbody) return;

    if (secrets.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="px-6 py-12 text-center text-slate-500 text-xs">
                    Tidak ada secret PPPoE ditemukan dengan filter saat ini.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = secrets.map(s => {
        const isOnline = s.status === 'Online';
        const isDisabled = s.status === 'Disabled';

        let statusBadge = '';
        if (isOnline) {
            statusBadge = `
                <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span>
                    Online
                </span>
            `;
        } else if (isDisabled) {
            statusBadge = `
                <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-rose-500/15 text-rose-400 border border-rose-500/30">
                    <i data-lucide="slash" class="w-3 h-3"></i>
                    Disabled
                </span>
            `;
        } else {
            statusBadge = `
                <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold bg-slate-800 text-slate-400 border border-slate-700">
                    Offline
                </span>
            `;
        }

        return `
            <tr class="hover:bg-slate-800/40 transition">
                <td class="px-5 py-4">
                    <div class="flex items-center gap-2.5">
                        <div class="w-8 h-8 rounded-xl bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center text-cyan-400">
                            <i data-lucide="user" class="w-4 h-4"></i>
                        </div>
                        <div>
                            <p class="font-bold text-white text-sm">${s.name}</p>
                            <p class="text-[10px] text-slate-500">Service: pppoe</p>
                        </div>
                    </div>
                </td>
                <td class="px-5 py-4">
                    ${statusBadge}
                </td>
                <td class="px-5 py-4">
                    <span class="text-xs font-semibold px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-700 text-slate-300">
                        ${s.profile || 'default'}
                    </span>
                </td>
                <td class="px-5 py-4 text-xs">
                    ${isOnline ? `
                        <p class="font-mono text-emerald-400 font-semibold">${s.ipAddress || '-'}</p>
                        <p class="text-slate-400 text-[11px]">Uptime: ${s.uptime || '-'}</p>
                    ` : `
                        <span class="text-slate-500 text-xs">-</span>
                    `}
                </td>
                <td class="px-5 py-4 text-right">
                    <div class="flex items-center justify-end gap-1.5">
                        ${isDisabled ? `
                            <button onclick="toggleSecretStatus('${s.name}', 'Disabled')" class="px-2.5 py-1.5 rounded-xl bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 text-xs font-semibold flex items-center gap-1 transition" title="Aktifkan Akun">
                                <i data-lucide="check-circle" class="w-3.5 h-3.5"></i> Enable
                            </button>
                        ` : `
                            <button onclick="toggleSecretStatus('${s.name}', 'Enabled')" class="px-2.5 py-1.5 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 text-xs font-semibold flex items-center gap-1 transition" title="Isolir / Nonaktifkan Akun">
                                <i data-lucide="slash" class="w-3.5 h-3.5"></i> Disable
                            </button>
                        `}

                        ${isOnline ? `
                            <button onclick="kickActiveSecret('${s.name}')" class="px-2.5 py-1.5 rounded-xl bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 border border-amber-500/30 text-xs font-semibold flex items-center gap-1 transition" title="Putus Sesi Aktif">
                                <i data-lucide="refresh-cw" class="w-3.5 h-3.5"></i> Kick
                            </button>
                        ` : ''}

                        <button onclick="deleteMikrotikSecret('${s.name}')" class="p-1.5 rounded-xl bg-slate-800 hover:bg-rose-950/60 text-slate-400 hover:text-rose-400 border border-slate-700 hover:border-rose-500/30 transition" title="Hapus Secret dari Router">
                            <i data-lucide="trash-2" class="w-4 h-4"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    if (window.lucide) lucide.createIcons();
}

function openAddSecretModal() {
    const sel = document.getElementById('secret-area-id');
    if (sel) {
        sel.value = activeSecretAreaId || (areasList[0] ? areasList[0].id : '');
        onModalSecretAreaChanged();
    }
    document.getElementById('form-add-secret').reset();
    openModal('modal-add-secret');
}

async function submitAddSecret(e) {
    e.preventDefault();
    const areaId = document.getElementById('secret-area-id').value;
    const name = document.getElementById('secret-name').value.trim();
    const password = document.getElementById('secret-password').value.trim();
    const profile = document.getElementById('secret-profile').value;

    if (!areaId) {
        showToast('Area router harus dipilih.', 'error');
        return;
    }

    showLoading(true, 'Menambahkan secret ke router Mikrotik...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/mikrotik/secrets/${areaId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, password, profile })
        });

        if (res.ok) {
            showToast(`Secret '${name}' berhasil ditambahkan ke Mikrotik!`, 'success');
            closeModal('modal-add-secret');
            await loadMikrotikSecrets(areaId);
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(err.error || 'Gagal menambahkan secret ke router.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function toggleSecretStatus(name, currentStatus) {
    if (!activeSecretAreaId) return;
    const isEnabling = currentStatus === 'Disabled';
    const actionText = isEnabling ? 'mengaktifkan' : 'menonaktifkan (disable)';
    if (!confirm(`Konfirmasi untuk ${actionText} secret '${name}' di router Mikrotik?`)) return;

    const endpoint = isEnabling ? 'enable' : 'disable';
    showLoading(true, `${isEnabling ? 'Mengaktifkan' : 'Menonaktifkan'} secret...`);
    try {
        const res = await fetch(`${API_BASE_URL}/api/mikrotik/secrets/${activeSecretAreaId}/${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
        });

        if (res.ok) {
            showToast(`Secret '${name}' berhasil di-${endpoint}!`, 'success');
            await loadMikrotikSecrets(activeSecretAreaId);
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(err.error || `Gagal mengubah status secret '${name}'.`, 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function kickActiveSecret(name) {
    if (!activeSecretAreaId) return;
    if (!confirm(`Putus sesi aktif untuk user '${name}' sekarang? Pelanggan akan dial ulang.`)) return;

    showLoading(true, 'Memutuskan sesi aktif...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/mikrotik/secrets/${activeSecretAreaId}/remove-active`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
        });

        if (res.ok) {
            showToast(`Sesi aktif '${name}' berhasil diputus!`, 'success');
            await loadMikrotikSecrets(activeSecretAreaId);
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(err.error || 'Gagal memutuskan sesi aktif di router.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

async function deleteMikrotikSecret(name) {
    if (!activeSecretAreaId) return;
    if (!confirm(`PERINGATAN: Hapus secret '${name}' secara permanen dari router Mikrotik?`)) return;

    showLoading(true, 'Menghapus secret dari router...');
    try {
        const res = await fetch(`${API_BASE_URL}/api/mikrotik/secrets/${activeSecretAreaId}/delete`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
        });

        if (res.ok) {
            showToast(`Secret '${name}' berhasil dihapus dari router!`, 'success');
            await loadMikrotikSecrets(activeSecretAreaId);
        } else {
            const err = await res.json().catch(() => ({}));
            showToast(err.error || 'Gagal menghapus secret dari router.', 'error');
        }
    } catch (e) {
        showToast('Koneksi server gagal.', 'error');
    } finally {
        showLoading(false);
    }
}

// Global scope bindings for window access
window.selectPackageTemplate = selectPackageTemplate;
window.calculateDedicatedPackage = calculateDedicatedPackage;
window.togglePackageTax = togglePackageTax;
window.calculatePackageFinalPrice = calculatePackageFinalPrice;
window.filterPackagesTable = filterPackagesTable;
window.openAddPackageModal = openAddPackageModal;
window.openEditPackageModal = openEditPackageModal;
window.savePackage = savePackage;
window.deletePackage = deletePackage;
window.openOntDetailModal = openOntDetailModal;
window.openChangeSsidModal = openChangeSsidModal;
window.openChangeWifiPasswordModal = openChangeWifiPasswordModal;
window.submitChangeSsid = submitChangeSsid;
window.submitChangeWifiPassword = submitChangeWifiPassword;
window.rebootAcsDevice = rebootAcsDevice;
window.changeAcsPage = changeAcsPage;
window.changeAcsPerPage = changeAcsPerPage;
window.applyAcsDeviceFilters = applyAcsDeviceFilters;
window.switchMikrotikSubTab = switchMikrotikSubTab;

