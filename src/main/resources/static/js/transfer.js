import { apiFetch, getUser, setUser, requireAuth, showToast, populateSidebar, initMobileMenu } from './api.js';

requireAuth();
populateSidebar();
initMobileMenu();

const API_BASE = '';  // Relative paths — works on any port/deployment
const user = getUser() || {};
const SYMBOLS = { USD: '$', EUR: '€', GBP: '£', JPY: '¥', INR: '₹', CAD: 'C$', AUD: 'A$', SGD: 'S$', CHF: 'Fr', CNY: '¥' };
const userCurrency = user.currency || 'USD';

// Set initial currency in selector
const currencySelect = document.getElementById('transfer-currency');
if (currencySelect) currencySelect.value = userCurrency;

// ─── Spending limits display ──────────────────────────────────────────────────
(async () => {
    try {
        const profile = await apiFetch('/api/user/profile');
        const dailyLimit = (profile.dailyTransactionLimit > 0 ? profile.dailyTransactionLimit : 10000);
        const singleLimit = (profile.singleTransactionLimit > 0 ? profile.singleTransactionLimit : 5000);
        const dailySpent = profile.dailySpent || 0;
        const pct = Math.min((dailySpent / dailyLimit) * 100, 100);
        document.getElementById('limit-bar').style.width = pct + '%';
        document.getElementById('limit-pct').textContent = Math.round(pct) + '%';
        document.getElementById('limit-label').textContent =
            `$${dailySpent.toLocaleString()} / $${dailyLimit.toLocaleString()} used today`;
        document.getElementById('single-limit-label').textContent = `$${singleLimit.toLocaleString()}`;
    } catch {
        document.getElementById('limit-label').textContent = 'Limit info unavailable';
        document.getElementById('single-limit-label').textContent = '$5,000';
    }
})();

// ─── Tab switching ────────────────────────────────────────────────────────────
window.switchTab = function (tab) {
    document.getElementById('single-panel').classList.toggle('hidden', tab !== 'single');
    document.getElementById('multi-panel').classList.toggle('hidden', tab !== 'multi');
    document.getElementById('tab-single').classList.toggle('active', tab === 'single');
    document.getElementById('tab-multi').classList.toggle('active', tab === 'multi');
};

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

// ─── SINGLE Transfer form ─────────────────────────────────────────────────────
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
        const profile = await apiFetch('/api/user/profile');
        const recData = await apiFetch(`/api/user/find?email=${encodeURIComponent(receiverEmail)}`);

        const transferResult = await apiFetch('/api/transfer', {
            method: 'POST',
            body: JSON.stringify({ senderId: profile.id, receiverId: recData.id, amount, description })
        });
        const text = typeof transferResult === 'string' ? transferResult : JSON.stringify(transferResult);

        if (text.toLowerCase().includes('blocked') || text.toLowerCase().includes('insufficient')) {
            showToast(text, 'error'); return;
        }

        // Show success card
        document.getElementById('transfer-card').classList.add('hidden');
        const successCard = document.getElementById('success-card');
        successCard.classList.remove('hidden');

        const sym = SYMBOLS[currencySelect?.value || userCurrency] || '$';
        document.getElementById('success-amount').textContent = `${sym}${amount.toLocaleString()}`;
        document.getElementById('success-to').textContent = receiverEmail;

        const hashMatch = text.match(/[0-9a-f]{64}/i);
        if (hashMatch) {
            document.getElementById('blockchain-hash-display').textContent = hashMatch[0];
            document.getElementById('blockchain-badge').classList.remove('hidden');
        }

        showToast(text.includes('[Flagged') ? '⚠️ Transfer flagged for review' : 'Transfer successful! 🎉',
            text.includes('[Flagged') ? 'info' : 'success');

    } catch (err) {
        showToast(err.message || 'Transfer failed', 'error');
        console.error(err);
    } finally {
        btn.disabled = false;
        btn.textContent = '🚀 Send Money';
    }
});

// Send Again (single)
document.getElementById('new-transfer-btn')?.addEventListener('click', () => {
    document.getElementById('success-card').classList.add('hidden');
    document.getElementById('transfer-card').classList.remove('hidden');
    document.getElementById('transfer-form').reset();
    if (currencySelect) currencySelect.value = userCurrency;
    document.getElementById('conversion-preview')?.classList.add('hidden');
    document.getElementById('blockchain-badge')?.classList.add('hidden');
});

// ─── MULTI Transfer ───────────────────────────────────────────────────────────
const MAX_RECIPIENTS = 10;
let rowCount = 0;

function addRecipientRow(email = '', amount = '', desc = '') {
    if (rowCount >= MAX_RECIPIENTS) {
        showToast(`Maximum ${MAX_RECIPIENTS} recipients allowed`, 'error'); return;
    }
    rowCount++;
    const id = `row-${rowCount}`;
    const div = document.createElement('div');
    div.className = 'recipient-row';
    div.id = id;
    div.dataset.row = rowCount;
    div.innerHTML = `
        <div style="display:flex;flex-direction:column;gap:0.3rem">
            <label style="font-size:0.78rem;color:var(--text-muted);font-weight:600">Recipient Email</label>
            <input type="email" class="row-email" placeholder="recipient@example.com"
                   value="${email}"
                   style="padding:0.6rem 0.8rem;background:var(--input-bg);border:1px solid var(--border);border-radius:8px;color:var(--text-primary);font-size:0.88rem;width:100%"/>
        </div>
        <div style="display:flex;flex-direction:column;gap:0.3rem">
            <label style="font-size:0.78rem;color:var(--text-muted);font-weight:600">Amount ($)</label>
            <input type="number" class="row-amount" placeholder="0.00" min="1" step="0.01"
                   value="${amount}"
                   style="padding:0.6rem 0.8rem;background:var(--input-bg);border:1px solid var(--border);border-radius:8px;color:var(--text-primary);font-size:0.88rem;width:100%"
                   oninput="updateBulkTotal()"/>
        </div>
        <button class="remove-row-btn" onclick="removeRow('${id}')" title="Remove">✕</button>
    `;
    document.getElementById('recipients-list').appendChild(div);
    updateBulkTotal();
}

window.removeRow = function (id) {
    const el = document.getElementById(id);
    if (el) { el.remove(); rowCount = Math.max(0, rowCount - 1); updateBulkTotal(); }
};

window.updateBulkTotal = function () {
    const amounts = [...document.querySelectorAll('.row-amount')]
        .map(i => parseFloat(i.value) || 0);
    const total = amounts.reduce((a, b) => a + b, 0);
    document.getElementById('bulk-total-label').textContent =
        `Total: $${total.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

// Seed with 2 default rows
addRecipientRow();
addRecipientRow();

document.getElementById('add-recipient-btn')?.addEventListener('click', () => addRecipientRow());

// Send All button
document.getElementById('send-all-btn')?.addEventListener('click', async () => {
    const rows = [...document.querySelectorAll('#recipients-list .recipient-row')];
    const items = rows.map(row => ({
        receiverEmail: row.querySelector('.row-email').value.trim(),
        amount: parseFloat(row.querySelector('.row-amount').value),
        description: ''
    })).filter(r => r.receiverEmail && r.amount > 0);

    if (items.length === 0) {
        showToast('Add at least one recipient with a valid amount', 'error'); return;
    }

    const btn = document.getElementById('send-all-btn');
    btn.disabled = true;
    btn.textContent = '⏳ Processing…';

    try {
        const profile = await apiFetch('/api/user/profile');
        const results = await apiFetch('/api/transfer/bulk', {
            method: 'POST',
            body: JSON.stringify({ senderId: profile.id, items })
        });

        // Show results
        const successCount = results.filter(r => r.status === 'SUCCESS').length;
        const failCount = results.length - successCount;

        document.getElementById('bulk-summary').innerHTML = `
            <span style="color:var(--success);font-weight:700">✅ ${successCount} Successful</span>
            ${failCount > 0 ? `<span style="color:var(--danger);font-weight:700">❌ ${failCount} Failed</span>` : ''}
            <span style="color:var(--text-muted)">Total: ${results.length}</span>
        `;

        const tbody = document.getElementById('bulk-results-body');
        tbody.innerHTML = results.map(r => `
            <tr>
                <td style="color:var(--text-secondary)">${r.receiverEmail}</td>
                <td style="font-weight:600">$${Number(r.amount).toLocaleString()}</td>
                <td class="${r.status === 'SUCCESS' ? 'status-badge-success' : 'status-badge-failed'}">
                    ${r.status === 'SUCCESS' ? '✅ Success' : '❌ Failed'}
                    ${r.status === 'FAILED' ? `<br><span style="font-size:0.75rem;font-weight:400;color:var(--text-muted)">${r.message || ''}</span>` : ''}
                </td>
                <td style="font-family:monospace;font-size:0.7rem;color:var(--accent-light)">
                    ${r.blockchainHash ? r.blockchainHash.substring(0, 12) + '…' : '—'}
                </td>
            </tr>
        `).join('');

        document.getElementById('bulk-results-card').classList.remove('hidden');
        document.getElementById('recipients-list').closest('.card') ?
            document.querySelector('#multi-panel .card').classList.add('hidden') : null;

        showToast(`${successCount}/${results.length} transfers completed`, successCount === results.length ? 'success' : 'info');

    } catch (err) {
        showToast(err.message || 'Bulk transfer failed', 'error');
        console.error(err);
    } finally {
        btn.disabled = false;
        btn.textContent = '🚀 Send All';
    }
});

// New Batch button
document.getElementById('bulk-new-btn')?.addEventListener('click', () => {
    document.getElementById('bulk-results-card').classList.add('hidden');
    document.querySelectorAll('#multi-panel .card').forEach(c => c.classList.remove('hidden'));
    document.getElementById('recipients-list').innerHTML = '';
    rowCount = 0;
    addRecipientRow();
    addRecipientRow();
});
