import { apiFetch, requireAuth, populateSidebar, initMobileMenu, showToast } from './api.js';

requireAuth();
populateSidebar();
initMobileMenu();

async function loadProfile() {
    try {
        const user = await apiFetch('/api/user/profile');
        document.getElementById('prof-name').value = user.name || '';
        document.getElementById('prof-email').value = user.email || '';
        const avatarEl = document.getElementById('profile-avatar');
        if (avatarEl) avatarEl.textContent = (user.name || user.email || 'U')[0].toUpperCase();
        const headerName = document.getElementById('profile-header-name');
        if (headerName) headerName.textContent = user.name || user.email;
        const headerRole = document.getElementById('profile-header-role');
        if (headerRole) headerRole.textContent = user.role || 'USER';
        const memberSince = document.getElementById('member-since');
        if (memberSince && user.createdAt) {
            memberSince.textContent = new Date(user.createdAt).toLocaleDateString('en-IN', { dateStyle: 'long' });
        }
    } catch (err) {
        showToast('Could not load profile', 'error');
    }
}

document.getElementById('profile-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button[type=submit]');
    const name = document.getElementById('prof-name').value.trim();
    const password = document.getElementById('prof-password').value;
    const confirm = document.getElementById('prof-confirm').value;

    if (password && password !== confirm) { showToast('Passwords do not match', 'error'); return; }
    if (password && password.length < 6) { showToast('Password must be at least 6 chars', 'error'); return; }

    const body = { name };
    if (password) body.password = password;

    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Saving…';
    try {
        await apiFetch('/api/user/profile', { method: 'PUT', body: JSON.stringify(body) });
        // Update cached user name
        const cached = JSON.parse(localStorage.getItem('wallet_user') || '{}');
        cached.name = name;
        localStorage.setItem('wallet_user', JSON.stringify(cached));
        showToast('Profile updated! ✅', 'success');
        document.getElementById('prof-password').value = '';
        document.getElementById('prof-confirm').value = '';
    } catch (err) {
        showToast(err.message || 'Update failed', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '💾 Save Changes';
    }
});

loadProfile();
