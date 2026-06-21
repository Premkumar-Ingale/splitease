/* ============================================
   SplitEase — Vanilla JS SPA Logic
   ============================================ */

const API = '';

// --- State ---
let currentGroupId = null; // Dynamically set from loaded groups
let allUsers = [];
let groupMembers = [];
let availableCategories = [];
let expenses = [];
let settlements = [];
let jwtToken = localStorage.getItem('splitease_token');
let currentUserId = localStorage.getItem('splitease_user_id');
let currentUserName = localStorage.getItem('splitease_user_name');
let currentPage = 0;
let pageSize = 5;
let searchQuery = '';
let totalPages = 0;
let totalElements = 0;

// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    checkAuth();

    // Export CSV button click
    document.getElementById('btnExportCSV').addEventListener('click', async () => {
        if (!currentGroupId) {
            showToast('Please select a group first', 'error');
            return;
        }
        try {
            showToast('CSV export started', 'info');
            const csvText = await api(`/api/groups/${currentGroupId}/expenses/export/csv`);
            const blob = new Blob([csvText], { type: 'text/csv' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = `expenses.csv`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
            showToast('CSV export complete', 'success');
        } catch (err) {
            showToast('Failed to export CSV: ' + err.message, 'error');
        }
    });

    // Search bar keyup (debounced)
    let searchTimeout;
    document.getElementById('searchExpenses').addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        searchQuery = e.target.value;
        currentPage = 0;
        searchTimeout = setTimeout(() => {
            loadExpenses();
        }, 300);
    });

    // Pagination buttons
    document.getElementById('prevPage').addEventListener('click', () => {
        if (currentPage > 0) {
            currentPage--;
            loadExpenses();
        }
    });

    document.getElementById('nextPage').addEventListener('click', () => {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadExpenses();
        }
    });
});

function checkAuth() {
    if (jwtToken) {
        document.getElementById('authContainer').classList.add('hidden');
        document.getElementById('appContainer').classList.remove('hidden');
        if (currentUserName) {
            const displayEl = document.getElementById('currentUserDisplay');
            if (displayEl) displayEl.textContent = currentUserName;
            const avatarEl = document.getElementById('userAvatar');
            if (avatarEl) avatarEl.textContent = getInitials(currentUserName);
        }
        init();
    } else {
        document.getElementById('authContainer').classList.remove('hidden');
        document.getElementById('appContainer').classList.add('hidden');
    }
}

async function init() {
    await loadUsers();
    await loadGroups();
}

function logout() {
    localStorage.removeItem('splitease_token');
    localStorage.removeItem('splitease_user_id');
    localStorage.removeItem('splitease_user_name');
    jwtToken = null;
    currentUserId = null;
    currentUserName = null;
    currentGroupId = null;
    groupMembers = [];
    allUsers = [];
    expenses = [];
    settlements = [];
    resetPanels();
    checkAuth();
}

function switchAuthTab(tab) {
    document.getElementById('tabLogin').classList.toggle('active', tab === 'login');
    document.getElementById('tabRegister').classList.toggle('active', tab === 'register');
    document.getElementById('formLogin').classList.toggle('active', tab === 'login');
    document.getElementById('formLogin').classList.toggle('hidden', tab !== 'login');
    document.getElementById('formRegister').classList.toggle('active', tab === 'register');
    document.getElementById('formRegister').classList.toggle('hidden', tab !== 'register');
}

// Auth API wrapper to catch 401s
async function api(url, options = {}) {
    if (!options.headers) options.headers = {};
    if (!options.headers['Content-Type']) {
        options.headers['Content-Type'] = 'application/json';
    }
    if (jwtToken) {
        options.headers['Authorization'] = `Bearer ${jwtToken}`;
    }

    try {
        const response = await fetch(API + url, options);
        if (response.status === 401) {
            logout();
            throw new Error('Session expired. Please log in again.');
        }
        if (!response.ok) {
            const contentType = response.headers.get("content-type");
            if (contentType && contentType.indexOf("application/json") !== -1) {
                const errJson = await response.json();
                throw new Error(errJson.error || errJson.message || `API error: ${response.status}`);
            } else {
                const errText = await response.text();
                throw new Error(errText || `API error: ${response.status}`);
            }
        }
        
        // Handle empty 204 No Content
        if (response.status === 204) return null;
        
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") !== -1) {
            return await response.json();
        } else {
            return await response.text();
        }
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
}

// --- Toast Notifications ---
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const icons = {
        success: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>',
        error: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>',
        info: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>',
    };
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span class="toast-icon">${icons[type] || icons.info}</span><span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('removing');
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// --- Auth Handlers ---
document.getElementById('formLogin').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    try {
        const res = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        if (!res.ok) throw new Error('Invalid credentials');
        const data = await res.json();
        
        localStorage.setItem('splitease_token', data.token);
        localStorage.setItem('splitease_user_id', data.userId);
        localStorage.setItem('splitease_user_name', data.name);
        
        jwtToken = data.token;
        currentUserId = data.userId;
        currentUserName = data.name;
        
        checkAuth();
        showToast('Logged in successfully', 'success');
    } catch (err) {
        showToast(err.message, 'error');
    }
});

document.getElementById('formRegister').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('registerName').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;
    try {
        const res = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password })
        });
        if (!res.ok) {
            const errText = await res.text();
            throw new Error(errText || 'Registration failed');
        }
        const data = await res.json();
        
        localStorage.setItem('splitease_token', data.token);
        localStorage.setItem('splitease_user_id', data.userId);
        localStorage.setItem('splitease_user_name', data.name);
        
        jwtToken = data.token;
        currentUserId = data.userId;
        currentUserName = data.name;
        
        checkAuth();
        showToast('Account created successfully', 'success');
    } catch (err) {
        showToast(err.message, 'error');
    }
});

// --- Security Helpers ---
function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe
         .toString()
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
}

// --- UI Logic ---
function openModal(title, bodyHtml) {
    document.getElementById('modalTitle').textContent = title;
    document.getElementById('modalBody').innerHTML = bodyHtml;
    document.getElementById('modalOverlay').classList.add('show');
}

function closeModal() {
    document.getElementById('modalOverlay').classList.remove('show');
}

document.getElementById('modalClose').addEventListener('click', closeModal);
document.getElementById('modalOverlay').addEventListener('click', (e) => {
    if (e.target === e.currentTarget) closeModal();
});

// --- Tab Navigation ---
function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(b => {
        const isActive = b.dataset.tab === tab;
        b.classList.toggle('active', isActive);
        b.setAttribute('aria-selected', isActive);
    });
    document.querySelectorAll('.mobile-nav-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.tab === tab);
    });
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    const panel = document.getElementById(`panel-${tab}`);
    if (panel) panel.classList.add('active');
}

document.getElementById('tabNav').addEventListener('click', (e) => {
    const btn = e.target.closest('.tab-btn');
    if (!btn) return;
    switchTab(btn.dataset.tab);
});

const mobileNav = document.getElementById('mobileNav');
if (mobileNav) {
    mobileNav.addEventListener('click', (e) => {
        const btn = e.target.closest('.mobile-nav-btn');
        if (!btn) return;
        switchTab(btn.dataset.tab);
    });
}

document.getElementById('btnLogout')?.addEventListener('click', () => {
    logout();
    showToast('Signed out successfully', 'info');
});

// --- Avatar helpers ---
function getInitials(name) {
    return name.split(' ').map(w => w[0]).join('').substring(0, 2).toUpperCase();
}

function getAvatarClass(userId) {
    return `avatar-${userId % 8}`;
}

function formatCurrency(amount) {
    const num = parseFloat(amount);
    return '₹' + Math.abs(num).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

// --- Load Users ---
async function loadUsers() {
    try {
        allUsers = await api('/api/users');
    } catch (err) {
        console.error('Failed to load users:', err);
    }
}

// --- Load Groups ---
async function loadGroups() {
    try {
        const groups = await api('/api/groups');
        const dropdown = document.getElementById('groupDropdown');
        const currentVal = dropdown.value;
        dropdown.innerHTML = '<option value="">Select a Group</option>';
        groups.forEach(g => {
            const opt = document.createElement('option');
            opt.value = g.id;
            opt.textContent = g.name; // safe via textContent
            dropdown.appendChild(opt);
        });
        // Restore selection
        if (currentVal) {
            dropdown.value = currentVal;
        }
        // Auto-select first group if none selected
        if (!currentGroupId && groups.length > 0) {
            dropdown.value = groups[0].id;
            currentGroupId = groups[0].id;
            onGroupChange();
        }
    } catch (err) {
        showToast('Failed to load groups', 'error');
    }
}

// --- Group change handler ---
document.getElementById('groupDropdown').addEventListener('change', (e) => {
    currentGroupId = e.target.value ? parseInt(e.target.value) : null;
    onGroupChange();
});

async function onGroupChange() {
    // Reset state
    currentPage = 0;
    searchQuery = '';
    document.getElementById('searchExpenses').value = '';

    if (!currentGroupId) {
        resetPanels();
        return;
    }
    await Promise.all([
        loadGroupDetails(),
        loadBalances(),
        loadExpenses(),
        loadMembers(),
        loadAnalytics(),
        loadActivity()
    ]);
}

function resetPanels() {
    document.getElementById('dashGroupName').textContent = 'Select a group to get started';
    document.getElementById('statMembers').textContent = '0';
    document.getElementById('statTotalExpenses').textContent = '₹0';
    document.getElementById('statSettlements').textContent = '0';
    document.getElementById('balanceGrid').innerHTML = '<div class="empty-state"><p>Select a group to view balances</p></div>';
    document.getElementById('expenseList').innerHTML = '<div class="empty-state"><p>No expenses yet</p></div>';
    document.getElementById('settlementList').innerHTML = '<div class="empty-state"><p>Click "Compute" to generate the settlement plan</p></div>';
    document.getElementById('memberGrid').innerHTML = '<div class="empty-state"><p>No members in this group</p></div>';
    document.getElementById('activityTimeline').innerHTML = '<div class="empty-state"><p>No activity yet.</p></div>';
    document.getElementById('btnConfirmSettlements').style.display = 'none';
}

// --- Load Group Details ---
async function loadGroupDetails() {
    try {
        const group = await api(`/api/groups/${currentGroupId}`);
        document.getElementById('dashGroupName').textContent = group.name;
        document.getElementById('statMembers').textContent = group.members ? group.members.length : 0;
    } catch (err) {
        showToast('Failed to load group details', 'error');
    }
}

// --- Load Balances ---
async function loadBalances() {
    try {
        const balances = await api(`/api/groups/${currentGroupId}/balances`);
        const grid = document.getElementById('balanceGrid');

        if (balances.length === 0) {
            grid.innerHTML = '<div class="empty-state"><p>No members to show</p></div>';
            return;
        }

        grid.innerHTML = balances.map(b => {
            const val = parseFloat(b.balance);
            const cls = val > 0 ? 'positive' : val < 0 ? 'negative' : 'zero';
            const label = val > 0 ? 'Gets back' : val < 0 ? 'Owes' : 'Settled';
            const sign = val > 0 ? '+' : val < 0 ? '-' : '';
            return `
                <div class="balance-card glass-card">
                    <div class="avatar ${getAvatarClass(b.userId)}">${escapeHtml(getInitials(b.userName))}</div>
                    <div class="balance-info">
                        <div class="balance-name">${escapeHtml(b.userName)}</div>
                        <div class="balance-label ${cls}">${label}</div>
                    </div>
                    <div class="balance-amount ${cls}">${sign}${formatCurrency(val)}</div>
                </div>
            `;
        }).join('');
    } catch (err) {
        showToast('Failed to load balances', 'error');
    }
}

// --- Load Expenses ---
async function loadExpenses() {
    try {
        let url = `/api/groups/${currentGroupId}/expenses?page=${currentPage}&size=${pageSize}`;
        if (searchQuery && searchQuery.trim()) {
            url += `&search=${encodeURIComponent(searchQuery.trim())}`;
        }

        const pageData = await api(url);
        expenses = pageData.content;
        totalPages = pageData.totalPages;
        totalElements = pageData.totalElements;
        currentPage = pageData.page;

        const list = document.getElementById('expenseList');
        const paginationInfo = document.getElementById('paginationInfo');
        const prevBtn = document.getElementById('prevPage');
        const nextBtn = document.getElementById('nextPage');

        // Update pagination info and buttons
        const startItem = totalElements === 0 ? 0 : currentPage * pageSize + 1;
        const endItem = Math.min((currentPage + 1) * pageSize, totalElements);
        paginationInfo.textContent = `${startItem}-${endItem} of ${totalElements} expenses`;
        prevBtn.disabled = currentPage === 0;
        nextBtn.disabled = currentPage >= totalPages - 1;

        // Update total stat
        const totalAll = await calculateTotalExpenses();
        document.getElementById('statTotalExpenses').textContent = formatCurrency(totalAll);

        if (expenses.length === 0) {
            list.innerHTML = '<div class="empty-state"><p>No expenses found.</p></div>';
            return;
        }

        list.innerHTML = expenses.map(e => {
            const date = new Date(e.createdAt).toLocaleDateString('en-IN', {
                day: 'numeric', month: 'short', year: 'numeric'
            });
            return `
                <div class="expense-item glass-card">
                    <div class="expense-icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
                    </div>
                    <div class="expense-details">
                        <div class="expense-description">${escapeHtml(e.description)}</div>
                        <div class="expense-meta">Paid by <strong>${escapeHtml(e.paidByUserName)}</strong> · ${date}</div>
                        ${e.categoryDisplayName ? `<div class="category-badge">${escapeHtml(e.categoryDisplayName)}</div>` : ''}
                    </div>
                    <div class="expense-actions">
                        <div class="expense-amount">${formatCurrency(e.amount)}</div>
                        <button class="btn btn-ghost btn-icon btn-delete-expense" onclick="deleteExpense(${e.id})" title="Delete Expense">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    } catch (err) {
        showToast('Failed to load expenses', 'error');
    }
}

async function deleteExpense(expenseId) {
    if (!confirm('Are you sure you want to delete this expense? This will permanently remove it and recalculate balances.')) {
        return;
    }
    try {
        await api(`/api/groups/${currentGroupId}/expenses/${expenseId}`, {
            method: 'DELETE'
        });
        showToast('Expense deleted successfully', 'success');
        // Reload everything to reflect changes
        loadExpenses();
        loadBalances();
        loadAnalytics();
        loadActivity();
        
        // Invalidate settlements view
        document.getElementById('settlementList').innerHTML = '<div class="empty-state"><p>Balances changed. Click "Compute" to generate a new settlement plan.</p></div>';
        document.getElementById('btnConfirmSettlements').style.display = 'none';
        document.getElementById('statSettlements').textContent = '0';
    } catch (err) {
        showToast('Failed to delete expense: ' + err.message, 'error');
    }
}

async function calculateTotalExpenses() {
    try {
        // Get all expenses (without pagination) to calculate total
        const allData = await api(`/api/groups/${currentGroupId}/expenses?page=0&size=1000`);
        const allExpenses = allData.content;
        return allExpenses.reduce((sum, e) => sum + parseFloat(e.amount), 0);
    } catch (e) {
        return 0;
    }
}

// --- Load Members ---
async function loadMembers() {
    try {
        groupMembers = await api(`/api/groups/${currentGroupId}/members`);
        const grid = document.getElementById('memberGrid');

        if (groupMembers.length === 0) {
            grid.innerHTML = '<div class="empty-state"><p>No members yet. Add some!</p></div>';
            return;
        }

        grid.innerHTML = groupMembers.map(m => `
            <div class="member-card glass-card">
                <div class="avatar ${getAvatarClass(m.userId)}">${escapeHtml(getInitials(m.userName))}</div>
                <div class="member-info">
                    <div class="member-name">${escapeHtml(m.userName)}</div>
                    <div class="member-email">${escapeHtml(m.userEmail)}</div>
                </div>
            </div>
        `).join('');
    } catch (err) {
        showToast('Failed to load members', 'error');
    }
}

// --- Load Activity ---
async function loadActivity() {
    try {
        const activities = await api(`/api/groups/${currentGroupId}/activity`);
        const timeline = document.getElementById('activityTimeline');

        if (activities.length === 0) {
            timeline.innerHTML = '<div class="empty-state"><p>No activity yet.</p></div>';
            return;
        }

        timeline.innerHTML = activities.map(a => {
            const date = new Date(a.timestamp);
            const formattedDate = date.toLocaleDateString('en-IN', {
                day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
            });
            return `
                <div class="activity-item glass-card" style="margin-bottom: 0.75rem; padding: 1rem; border-left: 4px solid var(--accent-indigo);">
                    <div class="activity-message" style="font-size: 0.95rem; font-weight: 500;">${escapeHtml(a.message)}</div>
                    <div class="activity-time" style="font-size: 0.75rem; color: var(--text-secondary); margin-top: 0.25rem;">${formattedDate}</div>
                </div>
            `;
        }).join('');
    } catch (err) {
        console.error('Failed to load activity:', err);
    }
}


// --- Compute Settlements ---
document.getElementById('btnComputeSettlements').addEventListener('click', async () => {
    if (!currentGroupId) {
        showToast('Please select a group first', 'error');
        return;
    }
    try {
        const settlements = await api(`/api/groups/${currentGroupId}/settlements`);
        const list = document.getElementById('settlementList');

        document.getElementById('statSettlements').textContent = settlements.length;

        if (settlements.length === 0) {
            list.innerHTML = '<div class="empty-state"><p>🎉 All settled! No transactions needed.</p></div>';
            document.getElementById('btnConfirmSettlements').style.display = 'none';
            return;
        }

        document.getElementById('btnConfirmSettlements').style.display = 'inline-flex';

        list.innerHTML = settlements.map(s => `
            <div class="settlement-item glass-card">
                <div class="settlement-arrow">
                    <div class="settlement-user">
                        <div class="avatar avatar-sm ${getAvatarClass(s.fromUserId)}">${escapeHtml(getInitials(s.fromUserName))}</div>
                        <span class="settlement-user-name">${escapeHtml(s.fromUserName)}</span>
                    </div>
                    <div class="arrow-icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
                    </div>
                    <div class="settlement-user">
                        <div class="avatar avatar-sm ${getAvatarClass(s.toUserId)}">${escapeHtml(getInitials(s.toUserName))}</div>
                        <span class="settlement-user-name">${escapeHtml(s.toUserName)}</span>
                    </div>
                </div>
                <div class="settlement-amount">${formatCurrency(s.amount)}</div>
                <span class="settlement-status ${s.status.toLowerCase()}">${s.status}</span>
            </div>
        `).join('');
    } catch (err) {
        showToast('Failed to compute settlements: ' + err.message, 'error');
    }
});

// --- Confirm Settlements ---
document.getElementById('btnConfirmSettlements').addEventListener('click', async () => {
    if (!currentGroupId) return;
    try {
        const confirmed = await api(`/api/groups/${currentGroupId}/settlements/confirm`, {
            method: 'POST'
        });
        showToast(`${confirmed.length} settlement(s) saved!`, 'success');
        document.getElementById('btnConfirmSettlements').style.display = 'none';
        // Reload with confirmed data
        loadConfirmedSettlements();
    } catch (err) {
        showToast('Failed to confirm: ' + err.message, 'error');
    }
});

async function loadConfirmedSettlements() {
    try {
        const settlements = await api(`/api/groups/${currentGroupId}/settlements/confirmed`);
        const list = document.getElementById('settlementList');

        document.getElementById('statSettlements').textContent = settlements.filter(s => s.status === 'PENDING').length;

        if (settlements.length === 0) {
            list.innerHTML = '<div class="empty-state"><p>No confirmed settlements yet.</p></div>';
            return;
        }

        list.innerHTML = settlements.map(s => `
            <div class="settlement-item glass-card">
                <div class="settlement-arrow">
                    <div class="settlement-user">
                        <div class="avatar avatar-sm ${getAvatarClass(s.fromUserId)}">${escapeHtml(getInitials(s.fromUserName))}</div>
                        <span class="settlement-user-name">${escapeHtml(s.fromUserName)}</span>
                    </div>
                    <div class="arrow-icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
                    </div>
                    <div class="settlement-user">
                        <div class="avatar avatar-sm ${getAvatarClass(s.toUserId)}">${escapeHtml(getInitials(s.toUserName))}</div>
                        <span class="settlement-user-name">${escapeHtml(s.toUserName)}</span>
                    </div>
                </div>
                <div class="settlement-amount">${formatCurrency(s.amount)}</div>
                ${s.status === 'PENDING' 
                    ? `<button class="btn btn-success btn-sm" onclick="markAsPaid(${s.id})">Mark Paid</button>` 
                    : `<span class="settlement-status paid">✓ PAID</span>`
                }
            </div>
        `).join('');
    } catch (err) {
        showToast('Failed to load settlements', 'error');
    }
}

// --- Mark as Paid ---
async function markAsPaid(settlementId) {
    try {
        await api(`/api/settlements/${settlementId}/pay`, { method: 'POST' });
        showToast('Settlement marked as paid!', 'success');
        loadConfirmedSettlements();
        loadBalances();
    } catch (err) {
        showToast('Failed: ' + err.message, 'error');
    }
}

// --- Add User Modal ---
document.getElementById('btnNewUser').addEventListener('click', () => {
    openModal('Add New User', `
        <form id="formNewUser">
            <div class="form-group">
                <label for="inputUserName">Name</label>
                <input type="text" id="inputUserName" class="form-input" placeholder="Enter name" required>
            </div>
            <div class="form-group">
                <label for="inputUserEmail">Email</label>
                <input type="email" id="inputUserEmail" class="form-input" placeholder="name@example.com" required>
            </div>
            <div class="form-group">
                <label for="inputUserPassword">Password</label>
                <input type="password" id="inputUserPassword" class="form-input" placeholder="Enter password" required>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
                <button type="submit" class="btn btn-primary">Create User</button>
            </div>
        </form>
    `);
    document.getElementById('formNewUser').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('inputUserName').value.trim();
        const email = document.getElementById('inputUserEmail').value.trim();
        const password = document.getElementById('inputUserPassword').value;
        if (!name || !email || !password) return;
        try {
            await api('/api/users', {
                method: 'POST',
                body: JSON.stringify({ name, email, password })
            });
            showToast(`User "${name}" created!`, 'success');
            closeModal();
            loadUsers();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });
});

// --- New Group Modal ---
document.getElementById('btnNewGroup').addEventListener('click', () => {
    openModal('Create New Group', `
        <form id="formNewGroup">
            <div class="form-group">
                <label for="inputGroupName">Group Name</label>
                <input type="text" id="inputGroupName" class="form-input" placeholder="e.g., Room 404" required>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
                <button type="submit" class="btn btn-primary">Create Group</button>
            </div>
        </form>
    `);
    document.getElementById('formNewGroup').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('inputGroupName').value.trim();
        if (!name) return;
        try {
            const group = await api('/api/groups', {
                method: 'POST',
                body: JSON.stringify({ name })
            });
            showToast(`Group "${name}" created!`, 'success');
            closeModal();
            await loadGroups();
            // Select the new group
            document.getElementById('groupDropdown').value = group.id;
            currentGroupId = group.id;
            onGroupChange();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });
});

// --- Add Member Modal ---
document.getElementById('btnAddMember').addEventListener('click', async () => {
    if (!currentGroupId) {
        showToast('Please select a group first', 'error');
        return;
    }
    await loadUsers();
    const existingIds = new Set(groupMembers.map(m => m.userId));
    const available = allUsers.filter(u => !existingIds.has(u.id));

    if (available.length === 0) {
        showToast('All users are already in this group', 'info');
        return;
    }

    const options = available.map(u => `<option value="${u.id}">${escapeHtml(u.name)} (${escapeHtml(u.email)})</option>`).join('');
    openModal('Add Member to Group', `
        <form id="formAddMember">
            <div class="form-group">
                <label for="selectMember">Select User</label>
                <select id="selectMember" class="form-select" required>
                    <option value="">Choose a user...</option>
                    ${options}
                </select>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
                <button type="submit" class="btn btn-primary">Add Member</button>
            </div>
        </form>
    `);
    document.getElementById('formAddMember').addEventListener('submit', async (e) => {
        e.preventDefault();
        const userId = document.getElementById('selectMember').value;
        if (!userId) return;
        try {
            await api(`/api/groups/${currentGroupId}/members`, {
                method: 'POST',
                body: JSON.stringify({ userId: parseInt(userId) })
            });
            showToast('Member added!', 'success');
            closeModal();
            onGroupChange();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });
});

// --- Add Expense Modal ---
document.getElementById('btnAddExpense').addEventListener('click', async () => {
    if (!currentGroupId) {
        showToast('Please select a group first', 'error');
        return;
    }
    if (groupMembers.length === 0) {
        showToast('Add members to the group first', 'error');
        return;
    }
    const options = groupMembers.map(m => `<option value="${m.userId}">${escapeHtml(m.userName)}</option>`).join('');
    const catOptions = availableCategories.map(c => `<option value="${c.value}">${escapeHtml(c.label)}</option>`).join('');
    
    openModal('Add New Expense', `
        <form id="formAddExpense">
            <div class="form-group">
                <label for="selectPayer">Paid By</label>
                <select id="selectPayer" class="form-select" required>
                    <option value="">Who paid?</option>
                    ${options}
                </select>
            </div>
            <div class="form-group">
                <label for="selectCategory">Category</label>
                <select id="selectCategory" class="form-select">
                    <option value="OTHER">Other</option>
                    ${catOptions}
                </select>
            </div>
            <div class="form-group">
                <label for="inputAmount">Total Amount (₹)</label>
                <input type="number" id="inputAmount" class="form-input" placeholder="0.00" min="0.01" step="0.01" required>
            </div>
            <div class="form-group">
                <label for="inputDescription">Description</label>
                <input type="text" id="inputDescription" class="form-input" placeholder="What was this for?" required>
            </div>
            <div class="form-group">
                <label for="selectSplitType">Split Type</label>
                <select id="selectSplitType" class="form-select">
                    <option value="EQUAL">Split Equally</option>
                    <option value="EXACT">Exact Amounts</option>
                    <option value="PERCENTAGE">Percentages</option>
                </select>
            </div>
            <div id="splitDetailsContainer" class="split-details hidden">
                <!-- Dynamically populated -->
            </div>
            <div class="form-actions" style="margin-top: 20px;">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
                <button type="submit" class="btn btn-primary" id="btnSubmitExpense">Add Expense</button>
            </div>
        </form>
    `);

    const splitTypeSelect = document.getElementById('selectSplitType');
    const splitContainer = document.getElementById('splitDetailsContainer');
    const amountInput = document.getElementById('inputAmount');

    function renderSplitInputs() {
        const type = splitTypeSelect.value;
        if (type === 'EQUAL') {
            splitContainer.classList.add('hidden');
            return;
        }

        splitContainer.classList.remove('hidden');
        const symbol = type === 'PERCENTAGE' ? '%' : '₹';
        const rows = groupMembers.map(m => `
            <div class="split-row">
                <span>${escapeHtml(m.userName)}</span>
                <div class="split-input-wrapper">
                    ${symbol === '₹' ? `<span class="split-symbol">₹</span>` : ''}
                    <input type="number" class="form-input split-val" data-uid="${m.userId}" placeholder="0" min="0" step="0.01">
                    ${symbol === '%' ? `<span class="split-symbol">%</span>` : ''}
                </div>
            </div>
        `).join('');

        splitContainer.innerHTML = `
            ${rows}
            <div class="split-summary" id="splitSummary">
                <span>Total:</span>
                <span id="splitTotalVal">0</span>
            </div>
        `;

        document.querySelectorAll('.split-val').forEach(inp => {
            inp.addEventListener('input', updateSplitSummary);
        });
    }

    function updateSplitSummary() {
        const type = splitTypeSelect.value;
        const totalInput = parseFloat(amountInput.value) || 0;
        let currentSum = 0;
        document.querySelectorAll('.split-val').forEach(inp => {
            currentSum += parseFloat(inp.value) || 0;
        });

        const summaryEl = document.getElementById('splitSummary');
        const totalValEl = document.getElementById('splitTotalVal');
        
        let isValid = false;
        if (type === 'PERCENTAGE') {
            totalValEl.textContent = `${currentSum.toFixed(2)}% / 100%`;
            isValid = Math.abs(currentSum - 100) < 0.01;
        } else if (type === 'EXACT') {
            totalValEl.textContent = `₹${currentSum.toFixed(2)} / ₹${totalInput.toFixed(2)}`;
            isValid = Math.abs(currentSum - totalInput) < 0.01;
        }

        if (isValid) {
            summaryEl.className = 'split-summary success';
        } else {
            summaryEl.className = 'split-summary error';
        }
        
        document.getElementById('btnSubmitExpense').disabled = !isValid && type !== 'EQUAL';
    }

    splitTypeSelect.addEventListener('change', () => {
        renderSplitInputs();
        updateSplitSummary();
    });
    
    amountInput.addEventListener('input', () => {
        if (splitTypeSelect.value === 'EXACT') updateSplitSummary();
    });

    document.getElementById('formAddExpense').addEventListener('submit', async (e) => {
        e.preventDefault();
        const paidByUserId = parseInt(document.getElementById('selectPayer').value);
        const category = document.getElementById('selectCategory').value;
        const splitType = document.getElementById('selectSplitType').value;
        const amount = parseFloat(document.getElementById('inputAmount').value);
        const description = document.getElementById('inputDescription').value.trim();
        
        if (!paidByUserId || !amount || !description) return;
        
        const payload = { paidByUserId, amount, description, category, splitType };
        
        if (splitType !== 'EQUAL') {
            const splits = {};
            let total = 0;
            document.querySelectorAll('.split-val').forEach(inp => {
                const val = parseFloat(inp.value) || 0;
                splits[inp.dataset.uid] = val;
                total += val;
            });
            
            if (splitType === 'PERCENTAGE' && Math.abs(total - 100) > 0.01) {
                showToast('Percentages must sum to exactly 100', 'error');
                return;
            } else if (splitType === 'EXACT' && Math.abs(total - amount) > 0.01) {
                showToast('Exact amounts must sum to the total amount', 'error');
                return;
            }
            payload.splits = splits;
        }
        try {
            await api(`/api/groups/${currentGroupId}/expenses`, {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            showToast('Expense added!', 'success');
            closeModal();
            onGroupChange();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });
});

// --- Compare Algorithms ---
document.getElementById('btnCompareAlgorithms').addEventListener('click', async () => {
    if (!currentGroupId) {
        showToast('Please select a group first', 'error');
        return;
    }
    try {
        showToast('Running both algorithms...', 'info');
        const data = await api(`/api/groups/${currentGroupId}/settlements/compare`);
        renderComparison(data);
    } catch (err) {
        showToast('Failed to compare: ' + err.message, 'error');
    }
});

function renderComparison(data) {
    const panel = document.getElementById('algoComparison');
    panel.style.display = 'block';

    const summary = data.summary;
    const greedy = data.greedy;
    const exact = data.exact;

    // Verdict badge
    const verdictBadge = document.getElementById('verdictBadge');
    if (summary.isGreedyOptimal) {
        verdictBadge.className = 'verdict-badge optimal';
        verdictBadge.innerHTML = '✓ Greedy is Optimal!';
    } else if (summary.savings > 0) {
        verdictBadge.className = 'verdict-badge improved';
        verdictBadge.innerHTML = `⚡ DFS saves ${summary.savings} transaction(s)`;
    } else {
        verdictBadge.className = 'verdict-badge fallback';
        verdictBadge.innerHTML = '⚠ Group too large for exact';
    }

    // Greedy card
    document.getElementById('greedyCount').textContent = greedy.transactionCount;
    const greedyList = document.getElementById('greedyList');
    greedyList.innerHTML = (greedy.settlements || []).map(s => `
        <div class="algo-tx-item">
            <span>${escapeHtml(s.fromUserName)}</span>
            <span class="algo-tx-arrow">→</span>
            <span>${escapeHtml(s.toUserName)}</span>
            <span class="algo-tx-amount">${formatCurrency(s.amount)}</span>
        </div>
    `).join('');

    // Exact card
    const exactCount = exact.feasible ? exact.transactionCount : '—';
    document.getElementById('exactCount').textContent = exactCount;
    const exactList = document.getElementById('exactList');
    if (exact.feasible) {
        exactList.innerHTML = (exact.settlements || []).map(s => `
            <div class="algo-tx-item">
                <span>${escapeHtml(s.fromUserName)}</span>
                <span class="algo-tx-arrow">→</span>
                <span>${escapeHtml(s.toUserName)}</span>
                <span class="algo-tx-amount">${formatCurrency(s.amount)}</span>
            </div>
        `).join('');
    } else {
        exactList.innerHTML = `<div class="algo-card-desc" style="color: var(--accent-orange);">${exact.message}</div>`;
    }

    // Highlight winner
    const greedyCard = document.getElementById('greedyCard');
    const exactCard = document.getElementById('exactCard');
    greedyCard.classList.remove('winner');
    exactCard.classList.remove('winner');

    if (exact.feasible) {
        if (summary.isGreedyOptimal) {
            // Both are optimal — highlight both subtly
            greedyCard.classList.add('winner');
            exactCard.classList.add('winner');
        } else if (exact.transactionCount < greedy.transactionCount) {
            exactCard.classList.add('winner');
        }
    }

    // Add compute time if available
    if (exact.computeTimeMs !== undefined) {
        const timeInfo = document.createElement('div');
        timeInfo.className = 'algo-card-desc';
        timeInfo.style.color = 'var(--text-tertiary)';
        timeInfo.textContent = `Computed in ${exact.computeTimeMs.toFixed(2)}ms`;
        // Only add if not already present
        const existing = exactList.parentElement.querySelector('.compute-time');
        if (existing) existing.remove();
        timeInfo.classList.add('compute-time');
        exactList.parentElement.appendChild(timeInfo);
    }
}

// --- WhatsApp Share ---
document.getElementById('btnShareWhatsApp').addEventListener('click', async () => {
    if (!currentGroupId) return;
    try {
        showToast('Generating share link...', 'info');
        const shareData = await api(`/api/groups/${currentGroupId}/share/whatsapp`);
        window.open(shareData.whatsappUrl, '_blank');
    } catch (err) {
        showToast('Failed to generate share link: ' + err.message, 'error');
    }
});

// --- Analytics ---
document.getElementById('btnRefreshAnalytics').addEventListener('click', loadAnalytics);

async function loadAnalytics() {
    if (!currentGroupId) return;
    try {
        const analytics = await api(`/api/groups/${currentGroupId}/analytics`);
        availableCategories = analytics.availableCategories || [];
        
        const container = document.getElementById('analyticsContainer');
        if (analytics.totalExpenses === 0) {
            container.innerHTML = '<div class="empty-state"><p>No data to analyze. Add some expenses first!</p></div>';
            return;
        }

        const totalAmt = parseFloat(analytics.totalAmount);
        
        // Build Category Progress Bars
        const catHtml = analytics.categoryBreakdown.map(cat => {
            const pct = (parseFloat(cat.total) / totalAmt) * 100;
            return `
                <div class="progress-item">
                    <div class="progress-header">
                        <span class="progress-label">${escapeHtml(cat.displayName)}</span>
                        <span class="progress-value">${formatCurrency(cat.total)} (${pct.toFixed(1)}%)</span>
                    </div>
                    <div class="progress-bar-container">
                        <div class="progress-bar-fill" style="width: ${pct}%"></div>
                    </div>
                </div>
            `;
        }).join('');

        // Build Member Progress Bars
        const memHtml = analytics.memberBreakdown.map(mem => {
            const pct = (parseFloat(mem.totalPaid) / totalAmt) * 100;
            return `
                <div class="progress-item">
                    <div class="progress-header">
                        <span class="progress-label">${escapeHtml(mem.userName)}</span>
                        <span class="progress-value">${formatCurrency(mem.totalPaid)} (${pct.toFixed(1)}%)</span>
                    </div>
                    <div class="progress-bar-container">
                        <div class="progress-bar-fill" style="width: ${pct}%; background: linear-gradient(90deg, var(--accent-teal), var(--accent-teal-glow));"></div>
                    </div>
                </div>
            `;
        }).join('');

        container.innerHTML = `
            <div class="analytics-grid">
                <div class="analytics-card glass-card">
                    <h3>
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 2v10l5 5"/></svg>
                        Spending by Category
                    </h3>
                    <div class="progress-list">${catHtml}</div>
                </div>
                <div class="analytics-card glass-card">
                    <h3>
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
                        Who Paid the Most?
                    </h3>
                    <div class="progress-list">${memHtml}</div>
                </div>
            </div>
        `;
    } catch (err) {
        console.error('Analytics load error:', err);
    }
}
