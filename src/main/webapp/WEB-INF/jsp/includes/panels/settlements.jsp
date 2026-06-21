<section class="tab-panel" id="panel-settlements" role="tabpanel">
    <div class="panel-header">
        <div>
            <h2>Settlement Plan</h2>
            <p class="subtitle">Minimum transactions to clear all debts</p>
        </div>
        <div class="header-actions">
            <button class="btn btn-secondary" id="btnComputeSettlements" type="button">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
                Compute
            </button>
            <button class="btn btn-accent" id="btnCompareAlgorithms" type="button">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 20V10"/><path d="M12 20V4"/><path d="M6 20v-6"/></svg>
                Compare
            </button>
            <button class="btn btn-whatsapp" id="btnShareWhatsApp" type="button">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347z"/><path d="M12 2C6.477 2 2 6.477 2 12c0 1.89.525 3.66 1.438 5.168L2 22l4.832-1.438A9.955 9.955 0 0012 22c5.523 0 10-4.477 10-10S17.523 2 12 2z"/></svg>
                Share
            </button>
            <button class="btn btn-primary" id="btnConfirmSettlements" type="button" style="display:none;">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                Confirm &amp; Save
            </button>
        </div>
    </div>
    <div class="settlement-info glass-card" id="settlementAlgoInfo">
        <div class="algo-badge">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>
            Greedy Min-Heap Algorithm
        </div>
        <p>Computes the <strong>minimum number of transactions</strong> to settle all debts. Uses two max-heaps to greedily match the largest creditor with the largest debtor.</p>
    </div>
    <div class="algo-comparison" id="algoComparison" style="display:none;">
        <div class="comparison-header">
            <h3>Algorithm Comparison</h3>
            <div class="verdict-badge" id="verdictBadge"></div>
        </div>
        <div class="comparison-grid">
            <div class="algo-card glass-card" id="greedyCard">
                <div class="algo-card-header">
                    <div class="algo-card-badge greedy">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
                        GREEDY HEAP
                    </div>
                    <span class="algo-complexity">O(n log n)</span>
                </div>
                <div class="algo-card-stat">
                    <span class="algo-stat-value" id="greedyCount">—</span>
                    <span class="algo-stat-label">Transactions</span>
                </div>
                <div class="algo-card-desc">Fast approximation. Pairs largest creditor with largest debtor greedily.</div>
                <div class="algo-card-list" id="greedyList"></div>
            </div>
            <div class="algo-vs"><span>VS</span></div>
            <div class="algo-card glass-card" id="exactCard">
                <div class="algo-card-header">
                    <div class="algo-card-badge exact">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                        EXACT DFS
                    </div>
                    <span class="algo-complexity">O(n!)</span>
                </div>
                <div class="algo-card-stat">
                    <span class="algo-stat-value" id="exactCount">—</span>
                    <span class="algo-stat-label">Transactions</span>
                </div>
                <div class="algo-card-desc">Provably optimal via DFS/backtracking. Explores all settlement orderings.</div>
                <div class="algo-card-list" id="exactList"></div>
            </div>
        </div>
    </div>
    <div class="settlement-list" id="settlementList">
        <div class="empty-state">
            <div class="empty-icon">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
            </div>
            <p>Click "Compute" to generate the settlement plan</p>
        </div>
    </div>
</section>
