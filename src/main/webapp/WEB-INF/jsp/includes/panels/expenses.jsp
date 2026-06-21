<section class="tab-panel" id="panel-expenses" role="tabpanel">
    <div class="panel-header">
        <div>
            <h2>Expenses</h2>
            <p class="subtitle">Track and search group spending</p>
        </div>
        <div class="header-actions">
            <button class="btn btn-secondary" id="btnExportCSV" type="button">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                Export CSV
            </button>
            <button class="btn btn-primary" id="btnAddExpense" type="button">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                Add Expense
            </button>
        </div>
    </div>
    <div class="search-container">
        <input type="text" id="searchExpenses" placeholder="Search expenses by description..." class="form-input">
    </div>
    <div class="expense-list" id="expenseList">
        <div class="empty-state">
            <div class="empty-icon">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="5" width="20" height="14" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/></svg>
            </div>
            <p>No expenses yet</p>
        </div>
    </div>
    <div class="pagination-controls" id="paginationControls">
        <div class="pagination-info">
            <span id="paginationInfo">Loading...</span>
        </div>
        <div class="pagination-buttons">
            <button class="btn btn-secondary btn-sm" id="prevPage" type="button" disabled>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
                Previous
            </button>
            <button class="btn btn-secondary btn-sm" id="nextPage" type="button" disabled>
                Next
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
            </button>
        </div>
    </div>
</section>
