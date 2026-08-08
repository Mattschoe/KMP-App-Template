package com.mattschoe.apptemplate.domain

/**
 * Domain model. Deliberately separate from `ItemEntity` (the Room row) so the
 * database schema can change without dragging the UI along — `OfflineItemRepository`
 * owns the mapping between the two.
 */
data class Item(
    val id: Long,
    val name: String,
    val createdAt: Long
)
