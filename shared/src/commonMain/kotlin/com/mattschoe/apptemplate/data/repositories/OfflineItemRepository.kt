package com.mattschoe.apptemplate.data.repositories

import com.mattschoe.apptemplate.data.local.ItemDao
import com.mattschoe.apptemplate.data.local.ItemEntity
import com.mattschoe.apptemplate.domain.DataError
import com.mattschoe.apptemplate.domain.Item
import com.mattschoe.apptemplate.domain.Result
import com.mattschoe.apptemplate.domain.repositories.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Room-backed [ItemRepository]. Owns the entity <-> domain mapping and converts
 * thrown storage failures into [Result.Error] so callers never see an exception.
 */
@OptIn(ExperimentalTime::class)
class OfflineItemRepository(
    private val _itemDao: ItemDao
) : ItemRepository {

    override fun observeItems(): Flow<List<Item>> =
        _itemDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addItem(name: String): Result<Unit, DataError> {
        if (name.isBlank()) return Result.Error(DataError.WRITE_FAILED)
        return try {
            val now = Clock.System.now().toEpochMilliseconds()
            _itemDao.insert(ItemEntity(name = name.trim(), createdAt = now))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.WRITE_FAILED)
        }
    }

    override suspend fun deleteItem(id: Long): Result<Unit, DataError> {
        return try {
            val deleted = _itemDao.deleteById(id)
            if (deleted == 0) Result.Error(DataError.ITEM_NOT_FOUND) else Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.UNKNOWN)
        }
    }
}

private fun ItemEntity.toDomain() = Item(id = id, name = name, createdAt = createdAt)
