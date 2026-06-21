<section class="tab-panel active" id="panel-dashboard" role="tabpanel">
    <div class="panel-header">
        <div>
            <h2>Group Overview</h2>
            <p class="subtitle" id="dashGroupName">Select a group to get started</p>
        </div>
    </div>
    <div class="stats-row" id="statsRow">
        <div class="stat-card glass-card">
            <div class="stat-icon" style="--accent: #6c63ff;">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>
            </div>
            <div class="stat-info">
                <span class="stat-value" id="statMembers">0</span>
                <span class="stat-label">Members</span>
            </div>
        </div>
        <div class="stat-card glass-card">
            <div class="stat-icon" style="--accent: #00c9a7;">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
            </div>
            <div class="stat-info">
                <span class="stat-value" id="statTotalExpenses">₹0</span>
                <span class="stat-label">Total Expenses</span>
            </div>
        </div>
        <div class="stat-card glass-card">
            <div class="stat-icon" style="--accent: #ff6b6b;">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
            </div>
            <div class="stat-info">
                <span class="stat-value" id="statSettlements">0</span>
                <span class="stat-label">Settlements Needed</span>
            </div>
        </div>
    </div>
    <div class="balance-section glass-panel">
        <h3>Member Balances</h3>
        <div class="balance-grid" id="balanceGrid">
            <div class="empty-state">
                <div class="empty-icon">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>
                </div>
                <p>Select a group to view balances</p>
            </div>
        </div>
    </div>
</section>
