package com.mattschoe.apptemplate.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mattschoe.apptemplate.AppContainer
import com.mattschoe.apptemplate.ui.pages.home.HomePage
import com.mattschoe.apptemplate.ui.pages.home.HomeViewModel
import com.mattschoe.apptemplate.ui.pages.settings.SettingsPage
import com.mattschoe.apptemplate.ui.pages.settings.SettingsViewModel

/**
 * The single place ViewModels are constructed. Each `composable<T>` block pulls
 * exactly the dependencies its ViewModel needs out of [appContainer] — no
 * ViewModelProvider.Factory, no service locator inside the ViewModel.
 */
@Composable
fun ApplicationNavigationHost(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController(),
    startPageRoute: PageNavigation = PageNavigation.Home
) {
    NavHost(
        navController = navController,
        startDestination = startPageRoute,
        modifier = Modifier.fillMaxSize()
    ) {
        //Main screen
        composable<PageNavigation.Home> {
            val viewModel = viewModel<HomeViewModel> {
                HomeViewModel(appContainer.itemRepository)
            }
            HomePage(
                navController = navController,
                viewModel = viewModel
            )
        }

        //Settings
        composable<PageNavigation.Settings> {
            val viewModel = viewModel<SettingsViewModel> {
                SettingsViewModel(appContainer.preferencesRepository)
            }
            SettingsPage(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}
