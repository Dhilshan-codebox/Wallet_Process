import {
    apiFetch, requireAuth, populateSidebar, initMobileMenu,
    formatCurrency, formatDate, showToast
} from './api.js';

requireAuth();
populateSidebar();
initMobileMenu();

document.getElementById('transfer-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button[type=submit]');
    const receiverEmail = document.getElementById('receiver-email').value.trim();
    const amount = parseFloat(document.getElementById('transfer-amount').value);
    const description = document.getElementById('transfer-desc').value.trim();

    if (!receiverEmail) { showToast('Enter recipient email', 'error'); return; }
    if (!amount || amount <= 0) { showToast('Enter a valid amount', 'error'); return; }

    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Sending…';

    try {
        await apiFetch('/api/transfer', {
            method: 'POST',
            body: JSON.stringify({ receiverEmail, amount, description }),
        });
        showToast(`${formatCurrency(amount)} sent successfully! 🚀`, 'success');
        e.target.reset();
        // Show success UI
        document.getElementById('success-card').classList.remove('hidden');
        document.getElementById('success-amount').textContent = formatCurrency(amount);
        document.getElementById('success-to').textContent = receiverEmail;
    } catch (err) {
        showToast(err.message || 'Transfer failed', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '🚀 Send Money';
    }
});

document.getElementById('new-transfer-btn')?.addEventListener('click', () => {
    document.getElementById('success-card').classList.add('hidden');
});
