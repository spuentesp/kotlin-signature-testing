package com.antigravity.todoapp.domain.model

import java.util.UUID

/**
 * Core business entity representing a single Todo item.
 * Pure Kotlin data class with zero Android dependencies.
 */
data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val createdAt: Long = System.currentTimeMillis()
)
