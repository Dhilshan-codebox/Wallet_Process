import {
    apiFetch, requireAuth, populateSidebar, initMobileMenu,
    formatCurrency, formatDate, showToast, getUser
} from './api.js';

requireAuth();
const currentUser = getUser();
if (currentUser?.role !== 'ADMIN') {
    showToast('Admin access required', 'error');
    setTimeout(() => window.location.href = '/dashboard.html', 1200);
}
populateSidebar();
initMobileMenu();

function statusBadge(s) {
    const map = { COMPLETED: 'badge-success', PENDING: 'badge-warning', CANCELLED: 'badge-danger', FAILED: 'badge-danger' };
    return map[s?.toUpperCase()] || 'badge-info';
}

async function loadAdmin() {
    // ── Stats ──
    try {
        const stats = await apiFetch('/api/admin/stats');
        document.getElementById('stat-users').textContent = stats.totalUsers ?? '—';
        document.getElementById('stat-txns').textContent = stats.totalTransactions ?? '—';
        document.getElementById('stat-volume').textContent = formatCurrency(stats.totalVolume ?? 0);
        document.getElementById('stat-active').textContent = stats.activeUsers ?? '—';
    } catch { /* silent */ }

    // ── Users ──
    try {
        const users = await apiFetch('/api/admin/users');
        const tbody = document.getElementById('users-tbody');
        if (!users.length) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No users</td></tr>`;
        } else {
            tbody.innerHTML = users.map(u => `<tr>
        <td>#${u.id}</td>
        <td>
          <div style="font-weight:500">${u.name || '—'}</div>
          <div class="text-xs text-muted">${u.email}</div>
        </td>
        <td><span class="badge ${u.role === 'ADMIN' ? 'badge-info' : 'badge-success'}">${u.role}</span></td>
        <td>${formatCurrency(u.balance)}</td>
        <td>
          <button class="btn btn-danger btn-sm" onclick="deleteUser(${u.id}, '${u.email}')">Delete</button>
          <button class="btn btn-secondary btn-sm" onclick="toggleRole(${u.id}, '${u.role}')">
            ${u.role === 'ADMIN' ? 'Set USER' : 'Set ADMIN'}
          </button>
        </td>
      </tr>`).join('');
        }
    } catch { /* silent */ }

    // ── Transactions ──
    try {
        const txns = await apiFetch('/api/admin/transactions');
        const tbody = document.getElementById('all-txns-tbody');
        if (!txns.length) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No transactions</td></tr>`;
        } else {
            tbody.innerHTML = txns.map(t => `<tr>
        <td><span class="text-xs text-muted">#${t.id}</span></td>
        <td>
          <div class="text-xs">${t.senderEmail || '—'}</div>
          <div class="text-xs text-muted">→ ${t.receiverEmail || '—'}</div>
        </td>
        <td style="font-weight:600">${formatCurrency(t.amount)}</td>
        <td><span class="badge ${statusBadge(t.status)}">${t.status}</span></td>
        <td class="text-xs text-muted">${formatDate(t.transactionDate)}</td>
      </tr>`).join('');
        }
    } catch { /* silent */ }
}

window.deleteUser = async (id, email) => {
    if (!confirm(`Delete user ${email}? This cannot be undone.`)) return;
    try {
        await apiFetch(`/api/admin/users/${id}`, { method: 'DELETE' });
        showToast('User deleted', 'info');
        loadAdmin();
    } catch (err) { showToast(err.message, 'error'); }
};

window.toggleRole = async (id, currentRole) => {
    const newRole = currentRole === 'ADMIN' ? 'USER' : 'ADMIN';
    if (!confirm(`Change role to ${newRole}?`)) return;
    try {
        await apiFetch(`/api/admin/users/${id}/role?role=${newRole}`, { method: 'PUT' });
        showToast(`Role updated to ${newRole}`, 'success');
        loadAdmin();
    } catch (err) { showToast(err.message, 'error'); }
};

loadAdmin();
