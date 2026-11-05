package com.antigravity.todoapp.domain.model

enum class TodoFilter(val displayName: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    HIGH_PRIORITY("High Priority")
}
