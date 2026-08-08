package com.mattschoe.apptemplate.domain

typealias RootError = Error

/**
 * Return type for any operation that can fail in a way the caller must handle.
 *
 * Repositories return this instead of throwing so that failure is visible in the
 * signature and the UI layer is forced to deal with it. Read paths that expose a
 * stream (`Flow<List<T>>`) stay unwrapped — only fallible one-shot calls use this.
 */
sealed interface Result<out D, out E : RootError> {
    data class Success<out D, out E : RootError>(val data: D) : Result<D, E>
    data class Error<out D, out E : RootError>(val error: E) : Result<D, E>
}
