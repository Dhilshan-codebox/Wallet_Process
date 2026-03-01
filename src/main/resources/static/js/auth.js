import { apiFetch, getToken, setToken, setUser, getUser, requireGuest, showToast, populateSidebar } from './api.js';

// Redirect if already logged in
requireGuest();

const API_BASE = 'http://localhost:8081';
const loginForm = document.getElementById('login-form');
const otpPanel = document.getElementById('otp-panel');
let pendingEmail = '';

// ─── Step 1: Login ───────────────────────────────────────────────────────────
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;
        const btn = document.getElementById('login-btn');

        if (!email || !password) { showToast('Please fill in all fields', 'error'); return; }

        btn.disabled = true;
        btn.textContent = 'Signing in…';

        try {
            const res = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            let data;
            try { data = await res.json(); } catch { data = {}; }

            if (!res.ok) {
                showToast(typeof data === 'string' ? data : (data.message || 'Login failed'), 'error');
                return;
            }

            if (data.requiresOtp) {
                // 2FA enabled — show OTP panel
                pendingEmail = email;
                loginForm.style.display = 'none';
                otpPanel.style.display = 'block';
                document.getElementById('otp-input')?.focus();
                showToast('OTP sent to your email 📧', 'success');
            } else {
                // No 2FA — save session and go to dashboard
                saveSession(data);
            }
        } catch (err) {
            showToast('Network error. Is the server running on port 8081?', 'error');
            console.error(err);
        } finally {
            btn.disabled = false;
            btn.textContent = 'Sign In 🔐';
        }
    });
}

// ─── Step 2: OTP Verify ──────────────────────────────────────────────────────
const otpForm = document.getElementById('otp-form');
if (otpForm) {
    otpForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const otp = document.getElementById('otp-input').value.trim();
        const btn = document.getElementById('otp-btn');

        if (!otp || otp.length !== 6) { showToast('Enter the 6-digit OTP', 'error'); return; }
        btn.disabled = true; btn.textContent = 'Verifying…';

        try {
            const res = await fetch(`${API_BASE}/auth/2fa/verify`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: pendingEmail, otp })
            });
            let data;
            try { data = await res.json(); } catch { data = {}; }
            if (!res.ok) { showToast(typeof data === 'string' ? data : 'Invalid OTP', 'error'); return; }
            saveSession(data);
        } catch (err) {
            showToast('Network error', 'error');
        } finally {
            btn.disabled = false; btn.textContent = 'Verify & Sign In ✓';
        }
    });
}

// ─── Resend OTP ───────────────────────────────────────────────────────────────
document.getElementById('resend-otp-btn')?.addEventListener('click', async () => {
    try {
        const res = await fetch(`${API_BASE}/auth/2fa/resend`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: pendingEmail })
        });
        showToast(res.ok ? 'OTP resent to your email 📧' : 'Resend failed', res.ok ? 'success' : 'error');
    } catch { showToast('Network error', 'error'); }
});

// ─── Back to login ────────────────────────────────────────────────────────────
document.getElementById('back-to-login')?.addEventListener('click', () => {
    otpPanel.style.display = 'none';
    loginForm.style.display = 'block';
    pendingEmail = '';
    document.getElementById('otp-input').value = '';
});

// ─── Register form (used on register.html via same auth.js) ──────────────────
const regForm = document.getElementById('register-form');
if (regForm) {
    regForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('reg-name').value.trim();
        const email = document.getElementById('reg-email').value.trim();
        const password = document.getElementById('reg-password').value;
        const confirm = document.getElementById('reg-confirm').value;
        const btn = regForm.querySelector('button[type=submit]');

        if (password !== confirm) { showToast('Passwords do not match', 'error'); return; }
        btn.disabled = true; btn.textContent = 'Creating…';

        try {
            const res = await fetch(`${API_BASE}/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password })
            });
            const text = await res.text();
            if (!res.ok) { showToast(text || 'Registration failed', 'error'); return; }
            showToast('Account created! Signing in…', 'success');
            setTimeout(() => window.location.href = '/index.html', 1200);
        } catch { showToast('Network error', 'error'); }
        finally { btn.disabled = false; btn.textContent = 'Create Account'; }
    });
}

// ─── Save session & redirect ──────────────────────────────────────────────────
function saveSession(data) {
    setToken(data.token);
    setUser({
        email: data.email,
        name: data.name,
        role: data.role,
        balance: data.balance,
        currency: data.currency || 'USD',
        walletAddress: data.walletAddress || '',
        twoFactorEnabled: data.twoFactorEnabled || false
    });
    showToast(`Welcome back, ${data.name}! 🎉`, 'success');
    setTimeout(() => { window.location.href = '/dashboard.html'; }, 800);
}
