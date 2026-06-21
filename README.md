# SplitEase

## Problem
In any hostel room, PG, or flat-share, expenses pile up fast — groceries, rent splits, electricity bills, food orders. Everyone pays for everyone else at different times, and by month-end nobody knows **who owes whom and how much**. Manual settlement via WhatsApp math is almost always wrong or incomplete.

## What it does
- **Create users and groups** — set up your hostel room or flat-share
- **Add expenses** — record who paid and how much; equal splits are auto-calculated
- **View net balances** — see who's a creditor and who's a debtor in the group
- **Minimum settlement plan** — uses a greedy min-heap algorithm to compute the **fewest transactions** needed to clear all debts
- **Mark settlements as paid** — track which debts are settled

## Why it's interesting technically
Implements a **greedy two-max-heap algorithm** to compute the minimum number of transactions needed to settle all debts in a group, rather than naive pairwise settlement. The algorithm pairs the largest creditor with the largest debtor each iteration, clearing at least one balance per step. Runs in **O(n log n)** for n members.

> **The honest caveat:** finding the mathematically minimum number of transactions is NP-hard (LeetCode 465 / Optimal Account Balancing). The greedy heap approach is a fast, near-optimal approximation that's perfect for real-world group sizes.

## Tech stack
Java 21, Spring Boot 3.5, Spring Data JPA, H2 (in-memory), Vanilla HTML/CSS/JS

## How to run
1. Clone the repo
2. `cd splitease`
3. `./mvnw spring-boot:run` (or `mvnw.cmd spring-boot:run` on Windows)
4. Visit [http://localhost:8080](http://localhost:8080)

The app comes pre-loaded with demo data: 4 users, 1 group ("Room 404"), and 3 expenses.

## What I'd build next
- Unequal/percentage-based splits
- Exact-minimum settlement via DFS/backtracking for small groups (≤8 members)
- Expense categories + spend analytics per member
- Recurring expenses (e.g., monthly rent auto-added)
- Export settlement summary as a shareable WhatsApp message
