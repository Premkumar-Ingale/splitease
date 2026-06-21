<div id="appContainer" class="app-container hidden">
    <header class="app-header glass-panel">
        <div class="header-left">
            <div class="logo">
                <div class="logo-icon">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="12" y1="1" x2="12" y2="23"></line>
                        <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                    </svg>
                </div>
                <h1 class="logo-text">Split<span class="accent">Ease</span></h1>
            </div>
        </div>
        <div class="header-center">
            <div class="group-selector" id="groupSelector">
                <label for="groupDropdown" class="sr-only">Select group</label>
                <select id="groupDropdown" class="glass-select">
                    <option value="">Select a Group</option>
                </select>
            </div>
        </div>
        <div class="header-right">
            <button class="btn btn-ghost" id="btnNewUser" type="button" title="Add User">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/></svg>
                <span>Add User</span>
            </button>
            <button class="btn btn-ghost" id="btnNewGroup" type="button" title="New Group">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                <span>New Group</span>
            </button>
            <div class="user-menu glass-card" id="userMenu">
                <div class="user-avatar" id="userAvatar">?</div>
                <div class="user-info">
                    <span class="user-name" id="currentUserDisplay">Guest</span>
                    <span class="user-role">Member</span>
                </div>
                <button class="btn btn-ghost btn-icon" id="btnLogout" type="button" title="Sign out">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                </button>
            </div>
        </div>
    </header>

    <nav class="tab-nav glass-panel" id="tabNav" role="tablist" aria-label="Main navigation">
        <button class="tab-btn active" data-tab="dashboard" type="button" role="tab" aria-selected="true">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
            Dashboard
        </button>
        <button class="tab-btn" data-tab="expenses" type="button" role="tab" aria-selected="false">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
            Expenses
        </button>
        <button class="tab-btn" data-tab="settlements" type="button" role="tab" aria-selected="false">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
            Settlements
        </button>
        <button class="tab-btn" data-tab="members" type="button" role="tab" aria-selected="false">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
            Members
        </button>
        <button class="tab-btn" data-tab="analytics" type="button" role="tab" aria-selected="false">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 20V10"/><path d="M12 20V4"/><path d="M6 20v-6"/></svg>
            Analytics
        </button>
        <button class="tab-btn" data-tab="activity" type="button" role="tab" aria-selected="false">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            Activity
        </button>
    </nav>

    <main class="tab-content">
        <%@ include file="panels/dashboard.jsp" %>
        <%@ include file="panels/expenses.jsp" %>
        <%@ include file="panels/settlements.jsp" %>
        <%@ include file="panels/members.jsp" %>
        <%@ include file="panels/analytics.jsp" %>
        <%@ include file="panels/activity.jsp" %>
    </main>

    <nav class="mobile-nav glass-panel" id="mobileNav" aria-label="Mobile navigation">
        <button class="mobile-nav-btn active" data-tab="dashboard" type="button" aria-label="Dashboard">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
        </button>
        <button class="mobile-nav-btn" data-tab="expenses" type="button" aria-label="Expenses">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
        </button>
        <button class="mobile-nav-btn" data-tab="settlements" type="button" aria-label="Settlements">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
        </button>
        <button class="mobile-nav-btn" data-tab="members" type="button" aria-label="Members">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
        </button>
        <button class="mobile-nav-btn" data-tab="analytics" type="button" aria-label="Analytics">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 20V10"/><path d="M12 20V4"/><path d="M6 20v-6"/></svg>
        </button>
        <button class="mobile-nav-btn" data-tab="activity" type="button" aria-label="Activity">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
        </button>
    </nav>
</div>
