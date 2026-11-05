package com.antigravity.todoapp.domain.model

enum class Priority(val label: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3)
}
