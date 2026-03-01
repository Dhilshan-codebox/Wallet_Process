import { apiFetch, getUser, requireAuth, showToast, populateSidebar, initMobileMenu } from './api.js';

requireAuth();
populateSidebar();
initMobileMenu();

const user = getUser() || {};
const SYMBOLS = { USD: '$', EUR: '€', GBP: '£', JPY: '¥', INR: '₹', CAD: 'C$', AUD: 'A$', SGD: 'S$', CHF: 'Fr', CNY: '¥' };

// ─── 2FA Status Toggle ────────────────────────────────────────────────────────
const tfaBadge = document.getElementById('tfa-status-badge');
const tfaOff = document.getElementById('tfa-disabled-section');
const tfaOn = document.getElementById('tfa-enabled-section');

function render2FAState(enabled) {
    if (enabled) {
        tfaBadge.textContent = 'Enabled'; tfaBadge.className = 'badge badge-success';
        tfaOff.classList.add('hidden'); tfaOn.classList.remove('hidden');
    } else {
        tfaBadge.textContent = 'Disabled'; tfaBadge.className = 'badge badge-danger';
        tfaOn.classList.add('hidden'); tfaOff.classList.remove('hidden');
    }
}

render2FAState(!!user.twoFactorEnabled);

document.getElementById('enable-2fa-btn')?.addEventListener('click', async () => {
    try {
        const res = await fetch(`http://localhost:8081/auth/2fa/enable`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('wallet_token')}` }
        });
        const txt = await res.text();
        if (res.ok) { render2FAState(true); showToast(txt || '2FA enabled ✅', 'success'); }
        else { showToast(txt || 'Failed to enable 2FA', 'error'); }
    } catch { showToast('Network error', 'error'); }
});

document.getElementById('disable-2fa-btn')?.addEventListener('click', async () => {
    if (!confirm('Disable 2FA? Your account will be less secure.')) return;
    try {
        const res = await fetch(`http://localhost:8081/auth/2fa/disable`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('wallet_token')}` }
        });
        if (res.ok) { render2FAState(false); showToast('2FA disabled', 'success'); }
        else { showToast('Failed to disable 2FA', 'error'); }
    } catch { showToast('Network error', 'error'); }
});

// ─── Wallet Address ───────────────────────────────────────────────────────────
const wallet = user.walletAddress || '';
document.getElementById('wallet-address-full').textContent = wallet || 'Not generated yet';
document.getElementById('copy-wallet-btn')?.addEventListener('click', () => {
    if (!wallet) return;
    navigator.clipboard.writeText(wallet)
        .then(() => showToast('Wallet address copied! 📋', 'success'))
        .catch(() => showToast('Copy failed', 'error'));
});

// ─── Transaction Limits ───────────────────────────────────────────────────────
document.getElementById('daily-limit-display').textContent = '$10,000';
document.getElementById('single-limit-display').textContent = '$5,000';
document.getElementById('daily-spent-display').textContent = 'Spent today: $0';

// ─── Currency Preference & Live Rates ────────────────────────────────────────
const currencySelect = document.getElementById('currency-select');
const rateDisplay = document.getElementById('live-rate-display');
const conversionTable = document.getElementById('conversion-table');
if (currencySelect) currencySelect.value = user.currency || 'USD';

async function loadRates(base) {
    if (rateDisplay) rateDisplay.textContent = 'Fetching live rates…';
    if (conversionTable) conversionTable.innerHTML = '';
    try {
        const data = await apiFetch(`/api/currency/rates?base=${base}`);
        if (data.rates) {
            if (rateDisplay) rateDisplay.textContent = `1 ${base} live rates`;
            ['USD', 'EUR', 'GBP', 'INR', 'JPY', 'AUD', 'CAD', 'SGD', 'CHF', 'CNY'].forEach(cur => {
                if (data.rates[cur] && cur !== base) {
                    const rate = data.rates[cur].toFixed(cur === 'JPY' ? 1 : 4);
                    conversionTable.innerHTML += `<div style="background:var(--input-bg);border-radius:8px;padding:0.6rem 0.75rem;text-align:center">
                        <div style="font-size:0.7rem;color:var(--text-muted)">${cur}</div>
                        <div style="font-weight:700;color:var(--accent-light)">${SYMBOLS[cur] || ''}${rate}</div>
                    </div>`;
                }
            });
        }
    } catch {
        if (rateDisplay) rateDisplay.textContent = 'Rate unavailable';
    }
}

loadRates(user.currency || 'USD');
currencySelect?.addEventListener('change', () => loadRates(currencySelect.value));
