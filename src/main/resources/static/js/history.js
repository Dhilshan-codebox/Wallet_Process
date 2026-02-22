import {
    apiFetch, requireAuth, populateSidebar, initMobileMenu,
    formatCurrency, formatDate, showToast
} from './api.js';

requireAuth();
populateSidebar();
initMobileMenu();

let allTxns = [];

function statusBadge(s) {
    const map = { COMPLETED: 'badge-success', PENDING: 'badge-warning', CANCELLED: 'badge-danger', FAILED: 'badge-danger' };
    return map[s?.toUpperCase()] || 'badge-info';
}

async function loadHistory() {
    const tbody = document.getElementById('history-tbody');
    tbody.innerHTML = `<tr><td colspan="5" class="text-center"><span class="spinner"></span></td></tr>`;
    try {
        allTxns = await apiFetch('/api/transfer/history');
        renderTable(allTxns);
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Failed to load transactions</td></tr>`;
    }
}

function renderTable(txns) {
    const tbody = document.getElementById('history-tbody');
    const user = JSON.parse(localStorage.getItem('wallet_user') || '{}');
    if (!txns.length) {
        tbody.innerHTML = `<tr><td colspan="5">
      <div class="empty-state"><div class="empty-icon">📭</div><p>No transactions found</p></div>
    </td></tr>`;
        document.getElementById('txn-count').textContent = '0 transactions';
        return;
    }
    document.getElementById('txn-count').textContent = `${txns.length} transaction${txns.length !== 1 ? 's' : ''}`;
    tbody.innerHTML = txns.map(t => {
        const isOut = t.senderEmail === user.email;
        const sign = isOut ? '-' : '+';
        const cls = isOut ? 'text-danger' : 'text-success';
        const direction = isOut
            ? `To: ${t.receiverName || t.receiverEmail || '—'}`
            : `From: ${t.senderName || t.senderEmail || '—'}`;
        return `<tr>
      <td><span class="text-xs text-muted">#${t.id}</span></td>
      <td>
        <div style="font-weight:500">${direction}</div>
        <div class="text-xs text-muted">${t.description || '—'}</div>
      </td>
      <td class="${cls}" style="font-weight:600">${sign}${formatCurrency(t.amount)}</td>
      <td><span class="badge ${statusBadge(t.status)}">${t.status}</span></td>
      <td>
        <div>${formatDate(t.transactionDate)}</div>
        ${t.status === 'PENDING' ? `<button class="btn btn-danger btn-sm mt-1" onclick="cancelTxn(${t.id})">Cancel</button>` : ''}
      </td>
    </tr>`;
    }).join('');
}

// ── Filters ──
document.getElementById('filter-status')?.addEventListener('change', applyFilters);
document.getElementById('filter-start')?.addEventListener('change', applyFilters);
document.getElementById('filter-end')?.addEventListener('change', applyFilters);
document.getElementById('sort-amount')?.addEventListener('change', applyFilters);
document.getElementById('search-input')?.addEventListener('input', applyFilters);

function applyFilters() {
    const status = document.getElementById('filter-status').value;
    const start = document.getElementById('filter-start').value;
    const end = document.getElementById('filter-end').value;
    const sort = document.getElementById('sort-amount').value;
    const search = document.getElementById('search-input').value.toLowerCase();

    let filtered = [...allTxns];
    if (status) filtered = filtered.filter(t => t.status?.toUpperCase() === status);
    if (start) filtered = filtered.filter(t => new Date(t.transactionDate) >= new Date(start));
    if (end) filtered = filtered.filter(t => new Date(t.transactionDate) <= new Date(end + 'T23:59:59'));
    if (search) filtered = filtered.filter(t =>
        (t.senderEmail || '').toLowerCase().includes(search) ||
        (t.receiverEmail || '').toLowerCase().includes(search) ||
        (t.description || '').toLowerCase().includes(search)
    );
    if (sort === 'asc') filtered.sort((a, b) => a.amount - b.amount);
    if (sort === 'desc') filtered.sort((a, b) => b.amount - a.amount);
    renderTable(filtered);
}

document.getElementById('reset-filters')?.addEventListener('click', () => {
    document.getElementById('filter-status').value = '';
    document.getElementById('filter-start').value = '';
    document.getElementById('filter-end').value = '';
    document.getElementById('sort-amount').value = '';
    document.getElementById('search-input').value = '';
    renderTable(allTxns);
});

// ── Cancel transaction ──
window.cancelTxn = async (id) => {
    if (!confirm('Cancel this transaction?')) return;
    try {
        await apiFetch(`/api/transfer/${id}/cancel`, { method: 'PUT' });
        showToast('Transaction cancelled', 'info');
        loadHistory();
    } catch (err) {
        showToast(err.message, 'error');
    }
};

loadHistory();
