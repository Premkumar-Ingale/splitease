<section class="tab-panel" id="panel-analytics" role="tabpanel">
    <div class="panel-header">
        <div>
            <h2>Spending Analytics</h2>
            <p class="subtitle">Category breakdown and payer insights</p>
        </div>
        <button class="btn btn-secondary" id="btnRefreshAnalytics" type="button" title="Refresh">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/><path d="M21 3v5h-5"/></svg>
        </button>
    </div>
    <div class="analytics-container" id="analyticsContainer">
        <div class="empty-state">
            <div class="empty-icon">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 3v18h18"/><path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/></svg>
            </div>
            <p>Loading analytics...</p>
        </div>
    </div>
</section>
