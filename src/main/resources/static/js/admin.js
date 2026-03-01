import { apiFetch, getUser, requireAuth, showToast, populateSidebar, initMobileMenu } from './api.js';

requireAuth();
populateSidebar();
initMobileMenu();

// ─── Load Admin Stats ─────────────────────────────────────────────────────────
async function loadStats() {
    try {
        const data = await apiFetch('/api/admin/stats');
        document.getElementById('stat-users').textContent = data.totalUsers ?? '—';
        document.getElementById('stat-txns').textContent = data.totalTransactions ?? '—';
        document.getElementById('stat-volume').textContent = data.totalVolume
            ? `$${Number(data.totalVolume).toLocaleString()}` : '—';
        document.getElementById('stat-active').textContent = data.activeUsers ?? '—';
    } catch (e) { console.error('Stats error:', e); }
}

// ─── Load Fraud Alerts ────────────────────────────────────────────────────────
async function loadFraudAlerts() {
    const tbody = document.getElementById('fraud-tbody');
    const countBadge = document.getElementById('fraud-alert-count');
    try {
        const alerts = await apiFetch('/api/admin/fraud-alerts');
        const open = alerts.filter(a => !a.resolved).length;
        countBadge.textContent = open + ' open';
        countBadge.className = open > 0 ? 'badge badge-danger' : 'badge badge-success';
        document.getElementById('stat-fraud').textContent = open;

        if (alerts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No fraud alerts 🎉</td></tr>';
            return;
        }
        const SEVERITY_COLOR = { CRITICAL: '#ef4444', HIGH: '#f59e0b', MEDIUM: '#6366f1', LOW: '#22c55e' };
        tbody.innerHTML = alerts.map(a => {
            const color = SEVERITY_COLOR[a.severity] || 'var(--text-muted)';
            const date = new Date(a.alertedAt).toLocaleString();
            const status = a.resolved
                ? '<span class="badge badge-success">Resolved</span>'
                : `<span class="badge badge-danger">Open</span>`;
            return `<tr>
                <td>${a.user?.email ?? '—'}</td>
                <td><code style="font-size:0.8rem;color:var(--accent-light)">${a.alertType}</code></td>
                <td>$${a.transactionAmount?.toLocaleString() ?? '—'}</td>
                <td><span style="color:${color};font-weight:700">${a.severity}</span></td>
                <td style="font-size:0.8rem;color:var(--text-muted);max-width:180px;overflow:hidden;text-overflow:ellipsis">${a.details ?? ''}</td>
                <td style="font-size:0.8rem">${date}</td>
                <td>${status}</td>
            </tr>`;
        }).join('');
    } catch (e) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Fraud alerts not available.</td></tr>';
        document.getElementById('stat-fraud').textContent = '—';
    }
}

// ─── Load Users ───────────────────────────────────────────────────────────────
async function loadUsers() {
    const tbody = document.getElementById('users-tbody');
    try {
        const users = await apiFetch('/api/admin/users');
        if (!users?.length) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No users found.</td></tr>';
            return;
        }
        tbody.innerHTML = users.map(u => {
            const roleBadge = u.role === 'ADMIN'
                ? '<span class="badge badge-danger">ADMIN</span>'
                : '<span class="badge badge-info">USER</span>';
            const tfaBadge = u.twoFactorEnabled
                ? '<span class="badge badge-success">ON</span>'
                : '<span class="badge badge-danger">OFF</span>';
            return `<tr>
                <td>${u.id}</td>
                <td><div style="font-weight:600">${u.name}</div><div style="font-size:0.75rem;color:var(--text-muted)">${u.email}</div></td>
                <td><span class="badge" style="background:rgba(99,102,241,0.15);color:var(--accent-light)">${u.currency || 'USD'}</span></td>
                <td>${tfaBadge}</td>
                <td>${roleBadge}</td>
                <td style="font-weight:600;color:var(--success)">$${Number(u.balance).toLocaleString()}</td>
                <td><button class="btn btn-sm btn-secondary" onclick="promoteUser(${u.id})">${u.role === 'ADMIN' ? '↓ Demote' : '↑ Promote'}</button></td>
            </tr>`;
        }).join('');
    } catch (e) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Could not load users.</td></tr>';
    }
}

// ─── Load All Transactions ────────────────────────────────────────────────────
async function loadAllTransactions() {
    const tbody = document.getElementById('all-txns-tbody');
    try {
        const txns = await apiFetch('/api/admin/transactions');
        if (!txns?.length) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No transactions yet.</td></tr>';
            return;
        }
        tbody.innerHTML = txns.map(t => {
            const badge = t.status === 'SUCCESS'
                ? '<span class="badge badge-success">Success</span>'
                : `<span class="badge badge-danger">${t.status}</span>`;
            const hash = t.blockchainHash
                ? `<span style="font-family:monospace;font-size:0.7rem;color:var(--accent-light)" title="${t.blockchainHash}">${t.blockchainHash.substring(0, 10)}…</span>`
                : '—';
            const date = new Date(t.transactionDate).toLocaleDateString();
            return `<tr>
                <td>${t.id}</td>
                <td>${t.senderName} → ${t.receiverName}</td>
                <td style="font-weight:600">$${Number(t.amount).toLocaleString()}</td>
                <td>${hash}</td>
                <td>${badge}</td>
                <td>${date}</td>
            </tr>`;
        }).join('');
    } catch (e) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">Could not load transactions.</td></tr>';
    }
}

// ─── Promote / Demote ─────────────────────────────────────────────────────────
window.promoteUser = async (userId) => {
    try {
        const txt = await apiFetch(`/api/admin/users/${userId}/promote`, { method: 'PUT' });
        showToast(txt, 'success');
        loadUsers();
    } catch (err) { showToast(err.message || 'Action failed', 'error'); }
};

// ─── Init ─────────────────────────────────────────────────────────────────────
loadStats();
loadFraudAlerts();
loadUsers();
loadAllTransactions();
