package com.splitease.model;

public enum ExpenseCategory {
    FOOD("Food & Groceries"),
    BILLS("Bills & Utilities"),
    TRAVEL("Travel & Transport"),
    ENTERTAINMENT("Entertainment"),
    RENT("Rent & Housing"),
    SHOPPING("Shopping"),
    OTHER("Other");

    private final String displayName;

    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
