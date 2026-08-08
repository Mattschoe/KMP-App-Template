package com.mattschoe.apptemplate.domain

import apptemplate.shared.generated.resources.Res
import apptemplate.shared.generated.resources.error_generic
import apptemplate.shared.generated.resources.error_item_not_found
import apptemplate.shared.generated.resources.error_write_failed
import org.jetbrains.compose.resources.StringResource

/**
 * Marker for every error type in the app. Implement it per domain area
 * (one enum per area) rather than growing a single catch-all enum.
 */
sealed interface Error

enum class DataError : Error {
    ITEM_NOT_FOUND,
    WRITE_FAILED,
    UNKNOWN;

    companion object {
        /**
         * Maps the user-relevant errors onto user-readable messages. Anything the
         * user can neither understand nor act on collapses to a generic message.
         */
        fun DataError.getResource(): StringResource {
            return when (this) {
                ITEM_NOT_FOUND -> Res.string.error_item_not_found
                WRITE_FAILED -> Res.string.error_write_failed
                UNKNOWN -> Res.string.error_generic
            }
        }
    }
}
