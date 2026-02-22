import { apiFetch, showToast, setToken, setUser, requireGuest } from './api.js';

requireGuest();

// ─── LOGIN ───────────────────────────────────────────────
const loginForm = document.getElementById('login-form');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = loginForm.querySelector('button[type=submit]');
        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Signing in…';

        try {
            const data = await apiFetch('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ email, password }),
            });
            setToken(data.token);
            setUser({ name: data.name, email: data.email, role: data.role });
            showToast('Welcome back! 🎉', 'success');
            setTimeout(() => window.location.href = '/dashboard.html', 600);
        } catch (err) {
            showToast(err.message || 'Login failed', 'error');
            btn.disabled = false;
            btn.innerHTML = 'Sign In';
        }
    });
}

// ─── REGISTER ────────────────────────────────────────────
const registerForm = document.getElementById('register-form');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = registerForm.querySelector('button[type=submit]');
        const name = document.getElementById('reg-name').value.trim();
        const email = document.getElementById('reg-email').value.trim();
        const password = document.getElementById('reg-password').value;
        const confirm = document.getElementById('reg-confirm').value;

        if (password !== confirm) { showToast('Passwords do not match', 'error'); return; }
        if (password.length < 6) { showToast('Password must be at least 6 characters', 'error'); return; }

        btn.disabled = true;
        btn.innerHTML = '<span class="spinner"></span> Creating account…';

        try {
            await apiFetch('/auth/register', {
                method: 'POST',
                body: JSON.stringify({ name, email, password }),
            });
            showToast('Account created! Please log in. 🎉', 'success');
            setTimeout(() => window.location.href = '/index.html', 1200);
        } catch (err) {
            showToast(err.message || 'Registration failed', 'error');
            btn.disabled = false;
            btn.innerHTML = 'Create Account';
        }
    });
}
