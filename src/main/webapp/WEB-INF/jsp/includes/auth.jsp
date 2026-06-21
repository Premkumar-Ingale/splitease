<div id="authContainer" class="auth-container">
    <div class="auth-layout">
        <section class="auth-hero glass-panel">
            <div class="hero-badge">
                <span class="pulse-dot"></span>
                Smart settlement engine
            </div>
            <h1 class="hero-title">Split expenses.<br><span class="gradient-text">Settle smarter.</span></h1>
            <p class="hero-desc">Minimum-transaction settlement for hostel rooms, PGs, and flat-shares. Know exactly who owes whom — in the fewest payments possible.</p>
            <ul class="hero-features">
                <li>
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                    Greedy min-heap algorithm
                </li>
                <li>
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                    Equal, exact &amp; percentage splits
                </li>
                <li>
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                    WhatsApp share &amp; CSV export
                </li>
            </ul>
            <div class="hero-stats">
                <div class="hero-stat">
                    <span class="hero-stat-value">O(n log n)</span>
                    <span class="hero-stat-label">Settlement speed</span>
                </div>
                <div class="hero-stat">
                    <span class="hero-stat-value">3</span>
                    <span class="hero-stat-label">Split modes</span>
                </div>
            </div>
        </section>

        <section class="auth-card glass-panel">
            <div class="auth-brand">
                <div class="logo-icon">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="12" y1="1" x2="12" y2="23"></line>
                        <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                    </svg>
                </div>
                <h2 class="auth-logo">Split<span class="accent">Ease</span></h2>
            </div>
            <p class="auth-subtitle">Welcome back — sign in to manage your groups</p>

            <div class="auth-tabs" role="tablist">
                <button class="auth-tab active" id="tabLogin" type="button" onclick="switchAuthTab('login')" role="tab" aria-selected="true">Sign In</button>
                <button class="auth-tab" id="tabRegister" type="button" onclick="switchAuthTab('register')" role="tab" aria-selected="false">Create Account</button>
            </div>

            <form id="formLogin" class="auth-form active" novalidate>
                <div class="form-group">
                    <label for="loginEmail">Email</label>
                    <input type="email" id="loginEmail" class="form-input" placeholder="you@example.com" autocomplete="email" required>
                </div>
                <div class="form-group">
                    <label for="loginPassword">Password</label>
                    <input type="password" id="loginPassword" class="form-input" placeholder="••••••••" autocomplete="current-password" required>
                </div>
                <button type="submit" class="btn btn-primary w-100 btn-lg">
                    <span>Sign In</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
                </button>
            </form>

            <form id="formRegister" class="auth-form hidden" novalidate>
                <div class="form-group">
                    <label for="registerName">Full Name</label>
                    <input type="text" id="registerName" class="form-input" placeholder="Your name" autocomplete="name" required>
                </div>
                <div class="form-group">
                    <label for="registerEmail">Email</label>
                    <input type="email" id="registerEmail" class="form-input" placeholder="you@example.com" autocomplete="email" required>
                </div>
                <div class="form-group">
                    <label for="registerPassword">Password</label>
                    <input type="password" id="registerPassword" class="form-input" placeholder="Min. 6 characters" autocomplete="new-password" required minlength="6">
                </div>
                <button type="submit" class="btn btn-primary w-100 btn-lg">
                    <span>Create Account</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/></svg>
                </button>
            </form>
        </section>
    </div>
</div>
