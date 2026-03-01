// ============================================================
//  api.js — Central API helper with JWT support
// ============================================================

const API_BASE = '';  // Relative paths — works on any port/deployment

// ── Token management ──
export const getToken = () => localStorage.getItem('wallet_token');
export const setToken = (t) => localStorage.setItem('wallet_token', t);
export const clearToken = () => localStorage.removeItem('wallet_token');

export const getUser = () => {
    try { return JSON.parse(localStorage.getItem('wallet_user') || 'null'); } catch { return null; }
};
export const setUser = (u) => localStorage.setItem('wallet_user', JSON.stringify(u));
export const clearUser = () => localStorage.removeItem('wallet_user');

export const logout = () => {
    clearToken(); clearUser();
    window.location.href = '/index.html';
};

// ── Guard: redirect to login if no token ──
export const requireAuth = () => {
    if (!getToken()) { window.location.href = '/index.html'; return false; }
    return true;
};

// ── Guard: redirect to dashboard if already logged in ──
export const requireGuest = () => {
    if (getToken()) { window.location.href = '/dashboard.html'; return false; }
    return true;
};

// ── Core fetch wrapper ──
export async function apiFetch(path, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
        ...(options.headers || {}),
    };

    const res = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers,
    });

    if (res.status === 401 || res.status === 403) {
        logout();
        throw new Error('Session expired. Please log in again.');
    }

    const text = await res.text();
    let data;
    try { data = JSON.parse(text); } catch { data = text; }

    if (!res.ok) {
        const msg = typeof data === 'string' ? data : (data?.message || `Error ${res.status}`);
        throw new Error(msg);
    }
    return data;
}

// ── Toast notifications ──
export function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }
    const icons = { success: '✅', error: '❌', info: 'ℹ️' };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${icons[type] || 'ℹ️'}</span><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4200);
}

// ── Populate sidebar user info ──
export function populateSidebar() {
    const user = getUser();
    if (!user) return;
    const nameEl = document.getElementById('sidebar-user-name');
    const roleEl = document.getElementById('sidebar-user-role');
    const avatarEl = document.getElementById('sidebar-avatar');
    if (nameEl) nameEl.textContent = user.name || user.email;
    if (roleEl) roleEl.textContent = user.role || 'USER';
    if (avatarEl) avatarEl.textContent = (user.name || user.email || 'U')[0].toUpperCase();

    // Show admin link only for ADMIN role
    const adminLink = document.getElementById('nav-admin');
    if (adminLink && user.role === 'ADMIN') adminLink.classList.remove('hidden');
}

// ── Number formatting ──
export const formatCurrency = (n) =>
    new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(n ?? 0);

export const formatDate = (dt) =>
    new Date(dt).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });

// ── Hamburger menu for mobile ──
export function initMobileMenu() {
    const ham = document.getElementById('hamburger');
    const sidebar = document.getElementById('sidebar');
    const backdrop = document.getElementById('sidebar-backdrop');
    if (!ham) return;
    ham.addEventListener('click', () => {
        sidebar.classList.toggle('open');
        backdrop.classList.toggle('show');
    });
    if (backdrop) {
        backdrop.addEventListener('click', () => {
            sidebar.classList.remove('open');
            backdrop.classList.remove('show');
        });
    }
}
