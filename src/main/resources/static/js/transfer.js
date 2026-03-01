import { apiFetch, getUser, requireAuth, showToast, populateSidebar, initMobileMenu } from './api.js';

requireAuth();
populateSidebar();
initMobileMenu();

const API_BASE = 'http://localhost:8080';
const user = getUser() || {};
const SYMBOLS = { USD: '$', EUR: '€', GBP: '£', JPY: '¥', INR: '₹', CAD: 'C$', AUD: 'A$', SGD: 'S$', CHF: 'Fr', CNY: '¥' };
const userCurrency = user.currency || 'USD';

// Set initial currency in selector
const currencySelect = document.getElementById('transfer-currency');
if (currencySelect) currencySelect.value = userCurrency;

// ─── Spending limits display ──────────────────────────────────────────────────
const dailyLimit = 10000;
const singleLimit = 5000;
const dailySpent = 0;
const pct = Math.min((dailySpent / dailyLimit) * 100, 100);
document.getElementById('limit-bar').style.width = pct + '%';
document.getElementById('limit-pct').textContent = Math.round(pct) + '%';
document.getElementById('limit-label').textContent =
    `$${dailySpent.toLocaleString()} / $${dailyLimit.toLocaleString()} used today`;
document.getElementById('single-limit-label').textContent = `$${singleLimit.toLocaleString()}`;

// ─── Live currency conversion preview ────────────────────────────────────────
let conversionRates = null;

async function fetchRates(base) {
    try {
        const data = await apiFetch(`/api/currency/rates?base=${base}`);
        conversionRates = data.rates || {};
    } catch (e) {
        console.warn('Rate fetch failed', e);
    }
}

fetchRates(userCurrency);
currencySelect?.addEventListener('change', () => fetchRates(currencySelect.value));

function updateConversionPreview() {
    const amount = parseFloat(document.getElementById('transfer-amount').value);
    const from = currencySelect?.value || userCurrency;
    const previewDiv = document.getElementById('conversion-preview');
    const previewTxt = document.getElementById('converted-preview');

    if (!amount || !conversionRates) { previewDiv?.classList.add('hidden'); return; }

    const recipientCurrency = from === 'USD' ? 'EUR' : 'USD';
    const rate = conversionRates[recipientCurrency];
    if (!rate) { previewDiv?.classList.add('hidden'); return; }

    previewTxt.textContent = `${(amount * rate).toFixed(2)} ${recipientCurrency}`;
    previewDiv?.classList.remove('hidden');
}

document.getElementById('transfer-amount')?.addEventListener('input', updateConversionPreview);
currencySelect?.addEventListener('change', updateConversionPreview);

// ─── Transfer form ────────────────────────────────────────────────────────────
document.getElementById('transfer-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const receiverEmail = document.getElementById('receiver-email').value.trim();
    const amount = parseFloat(document.getElementById('transfer-amount').value);
    const description = document.getElementById('transfer-desc').value.trim();
    const btn = e.target.querySelector('button[type=submit]');

    if (!receiverEmail || !amount || amount <= 0) {
        showToast('Please fill in all required fields', 'error'); return;
    }

    btn.disabled = true;
    btn.textContent = 'Processing…';

    try {
        // Get sender id from profile
        const profile = await apiFetch('/api/user/profile');
        const recData = await apiFetch(`/api/user/find?email=${encodeURIComponent(receiverEmail)}`);

        const transferResult = await apiFetch('/api/transfer', {
            method: 'POST',
            body: JSON.stringify({ senderId: profile.id, receiverId: recData.id, amount, description })
        });
        const res = { ok: true };
        const text = typeof transferResult === 'string' ? transferResult : JSON.stringify(transferResult);

        // text already extracted above via apiFetch

        if (!res.ok || text.toLowerCase().includes('blocked') || text.toLowerCase().includes('insufficient')) {
            showToast(text, 'error'); return;
        }

        // Show success card
        document.getElementById('transfer-card').classList.add('hidden');
        const successCard = document.getElementById('success-card');
        successCard.classList.remove('hidden');

        const sym = SYMBOLS[currencySelect?.value || userCurrency] || '$';
        document.getElementById('success-amount').textContent = `${sym}${amount.toLocaleString()}`;
        document.getElementById('success-to').textContent = receiverEmail;

        // Extract blockchain hash
        const hashMatch = text.match(/[0-9a-f]{64}/i);
        if (hashMatch) {
            document.getElementById('blockchain-hash-display').textContent = hashMatch[0];
            document.getElementById('blockchain-badge').classList.remove('hidden');
        }

        showToast(text.includes('[Flagged')
            ? '⚠️ Transfer flagged for review — check your email'
            : 'Transfer successful! 🎉',
            text.includes('[Flagged') ? 'info' : 'success');

    } catch (err) {
        showToast(err.message || 'Transfer failed', 'error');
        console.error(err);
    } finally {
        btn.disabled = false;
        btn.textContent = '🚀 Send Money';
    }
});

// ─── Send Again ───────────────────────────────────────────────────────────────
document.getElementById('new-transfer-btn')?.addEventListener('click', () => {
    document.getElementById('success-card').classList.add('hidden');
    document.getElementById('transfer-card').classList.remove('hidden');
    document.getElementById('transfer-form').reset();
    if (currencySelect) currencySelect.value = userCurrency;
    document.getElementById('conversion-preview')?.classList.add('hidden');
    document.getElementById('blockchain-badge')?.classList.add('hidden');
});
