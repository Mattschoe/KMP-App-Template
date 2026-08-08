package com.mattschoe.apptemplate.domain.repositories

import com.mattschoe.apptemplate.domain.DataError
import com.mattschoe.apptemplate.domain.Item
import com.mattschoe.apptemplate.domain.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interfaces live in `domain/` and know nothing about Room, the network,
 * or any platform type. Implementations live in `data/repositories/` and are named
 * after their backing store — `OfflineItemRepository`, `KtorItemRepository`, ...
 */
interface ItemRepository {
    /** Streams every item, newest first. Emits again on every write. */
    fun observeItems(): Flow<List<Item>>

    suspend fun addItem(name: String): Result<Unit, DataError>

    suspend fun deleteItem(id: Long): Result<Unit, DataError>
}
