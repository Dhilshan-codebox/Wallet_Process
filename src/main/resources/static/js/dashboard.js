import {
    apiFetch, requireAuth, populateSidebar, initMobileMenu,
    formatCurrency, formatDate, showToast
} from './api.js';

requireAuth();
populateSidebar();
initMobileMenu();

async function loadDashboard() {
    // ── Balance ──
    try {
        const balance = await apiFetch('/api/wallet/balance');
        document.getElementById('balance-amount').textContent = formatCurrency(balance);
    } catch {
        document.getElementById('balance-amount').textContent = '—';
    }

    // ── Recent transactions ──
    try {
        const txns = await apiFetch('/api/transfer/history');
        const tbody = document.getElementById('recent-tbody');
        const recent = txns.slice(0, 5);
        if (recent.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4">
        <div class="empty-state"><div class="empty-icon">📭</div><p>No transactions yet</p></div>
      </td></tr>`;
            return;
        }
        const user = JSON.parse(localStorage.getItem('wallet_user') || '{}');
        tbody.innerHTML = recent.map(t => {
            const isOut = t.senderEmail === user.email;
            const counterpart = isOut ? t.receiverName || t.receiverEmail : t.senderName || t.senderEmail;
            const sign = isOut ? '-' : '+';
            const cls = isOut ? 'text-danger' : 'text-success';
            return `<tr>
        <td>${formatDate(t.transactionDate)}</td>
        <td>${counterpart || '—'}</td>
        <td class="${cls}">${sign}${formatCurrency(t.amount)}</td>
        <td><span class="badge ${statusBadge(t.status)}">${t.status}</span></td>
      </tr>`;
        }).join('');
    } catch {
        /* silent */
    }
}

function statusBadge(s) {
    const map = { COMPLETED: 'badge-success', PENDING: 'badge-warning', CANCELLED: 'badge-danger', FAILED: 'badge-danger' };
    return map[s?.toUpperCase()] || 'badge-info';
}

// ── Deposit modal ──
const depositModal = document.getElementById('deposit-modal');
document.getElementById('open-deposit')?.addEventListener('click', () => depositModal.classList.remove('hidden'));
document.getElementById('close-deposit')?.addEventListener('click', () => depositModal.classList.add('hidden'));

document.getElementById('deposit-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button[type=submit]');
    const amount = parseFloat(document.getElementById('deposit-amount').value);
    if (!amount || amount <= 0) { showToast('Enter a valid amount', 'error'); return; }
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span>';
    try {
        const user = JSON.parse(localStorage.getItem('wallet_user') || '{}');
        await apiFetch('/api/wallet/deposit', {
            method: 'POST',
            body: JSON.stringify({ email: user.email, amount }),
        });
        showToast(`₹${amount} deposited successfully! 💰`, 'success');
        depositModal.classList.add('hidden');
        document.getElementById('deposit-amount').value = '';
        loadDashboard();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = 'Deposit';
    }
});

loadDashboard();
