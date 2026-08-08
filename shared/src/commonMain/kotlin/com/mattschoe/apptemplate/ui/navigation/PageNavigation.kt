package com.mattschoe.apptemplate.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Every destination in the app. Type-safe Compose Navigation — routes are real
 * types, not strings, so arguments are checked at compile time.
 *
 * Adding a page: add the entry here, then a `composable<PageNavigation.X>` block
 * in [ApplicationNavigationHost], then the page + ViewModel under `ui/pages/<name>/`.
 */
@Serializable
sealed class PageNavigation {
    @Serializable
    object Home : PageNavigation()

    @Serializable
    object Settings : PageNavigation()

    // Routes carrying arguments are data classes; read them in the nav host with
    // `backStackEntry.toRoute<PageNavigation.Detail>()`.
    //
    // @Serializable
    // data class Detail(val itemId: Long) : PageNavigation()
}
