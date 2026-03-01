import { apiFetch, getToken, getUser, requireAuth, showToast, populateSidebar, initMobileMenu } from './api.js';

requireAuth();
populateSidebar();
initMobileMenu();

const API_BASE = 'http://localhost:8081';
const user = getUser() || {};
const SYMBOLS = { USD: '$', EUR: '€', GBP: '£', JPY: '¥', INR: '₹', CAD: 'C$', AUD: 'A$', SGD: 'S$', CHF: 'Fr', CNY: '¥' };
const currency = user.currency || 'USD';
const sym = SYMBOLS[currency] || '$';
const wallet = user.walletAddress || '';

// ─── Balance card ─────────────────────────────────────────────────────────────
const balance = parseFloat(user.balance || 0);
document.getElementById('balance-amount').textContent =
    balance.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
document.getElementById('balance-currency-symbol').textContent = sym;
document.getElementById('balance-currency-badge').textContent = currency;

if (wallet) {
    document.getElementById('wallet-address-short').textContent = wallet.substring(0, 10) + '…';
}

// ─── 2FA security banner ──────────────────────────────────────────────────────
if (!user.twoFactorEnabled) {
    document.getElementById('security-banner').classList.remove('hidden');
}

// ─── Daily limit bar (static defaults — live from profile if needed) ──────────
const dailyLimit = 10000;
const dailySpent = 0;
const pct = Math.min((dailySpent / dailyLimit) * 100, 100);
document.getElementById('daily-limit-bar').style.width = pct + '%';
document.getElementById('daily-limit-label').textContent =
    `${sym}${dailySpent.toLocaleString()} / ${sym}${dailyLimit.toLocaleString()}`;

// ─── Recent Transactions ──────────────────────────────────────────────────────
async function loadRecent() {
    try {
        const list = await apiFetch('/api/transfer/history');
        const tbody = document.getElementById('recent-tbody');

        if (!Array.isArray(list) || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No transactions yet.</td></tr>';
            return;
        }

        tbody.innerHTML = list.slice(0, 8).map(t => {
            const sent = t.senderEmail === user.email;
            const amount = `${sent ? '-' : '+'}${sym}${Number(t.amount).toLocaleString()}`;
            const color = sent ? 'var(--danger)' : 'var(--success)';
            const peer = sent ? (t.receiverName || t.receiverEmail) : (t.senderName || t.senderEmail);
            const dir = sent ? `→ ${peer}` : `← ${peer}`;
            const date = new Date(t.transactionDate).toLocaleDateString();
            const hash = t.blockchainHash
                ? `<span style="font-family:monospace;font-size:0.7rem;color:var(--accent-light)" title="${t.blockchainHash}">${t.blockchainHash.substring(0, 10)}…</span>`
                : '—';
            const badge = t.status === 'SUCCESS'
                ? '<span class="badge badge-success">Success</span>'
                : `<span class="badge badge-danger">${t.status}</span>`;
            return `<tr>
                <td>${date}</td>
                <td>${dir}</td>
                <td style="color:${color};font-weight:600">${amount}</td>
                <td>${hash}</td>
                <td>${badge}</td>
            </tr>`;
        }).join('');
    } catch (err) {
        console.error('Failed to load transactions:', err);
    }
}

loadRecent();

// ─── Deposit modal ────────────────────────────────────────────────────────────
document.querySelectorAll('#open-deposit').forEach(btn => {
    btn.addEventListener('click', () => {
        document.getElementById('deposit-modal').classList.remove('hidden');
    });
});
document.getElementById('close-deposit')?.addEventListener('click', () => {
    document.getElementById('deposit-modal').classList.add('hidden');
});
document.getElementById('deposit-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const amount = parseFloat(document.getElementById('deposit-amount').value);
    if (!amount || amount <= 0) { showToast('Enter a valid amount', 'error'); return; }

    try {
        await apiFetch('/api/wallet/deposit', {
            method: 'POST',
            body: JSON.stringify({ email: user.email, amount })
        });
        showToast(`Deposited ${sym}${amount} ✅`, 'success');
        document.getElementById('deposit-modal').classList.add('hidden');
        const newBal = balance + amount;
        document.getElementById('balance-amount').textContent =
            newBal.toLocaleString(undefined, { minimumFractionDigits: 2 });
    } catch (err) {
        showToast(err.message || 'Deposit failed', 'error');
    }
});
