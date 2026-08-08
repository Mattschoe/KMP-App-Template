package com.mattschoe.apptemplate.ui.pages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import apptemplate.shared.generated.resources.Res
import apptemplate.shared.generated.resources.a11y_navigate_back
import apptemplate.shared.generated.resources.settings_theme_dark
import apptemplate.shared.generated.resources.settings_theme_light
import apptemplate.shared.generated.resources.settings_theme_system
import apptemplate.shared.generated.resources.settings_theme_title
import apptemplate.shared.generated.resources.settings_title
import com.mattschoe.apptemplate.ui.components.PageShell
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    navController: NavHostController,
    viewModel: SettingsViewModel
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    PageShell(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.a11y_navigate_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = stringResource(Res.string.settings_theme_title))

            Column(Modifier.selectableGroup()) {
                ThemeOption(Res.string.settings_theme_system, isDarkMode == null) {
                    viewModel.setDarkMode(null)
                }
                ThemeOption(Res.string.settings_theme_light, isDarkMode == false) {
                    viewModel.setDarkMode(false)
                }
                ThemeOption(Res.string.settings_theme_dark, isDarkMode == true) {
                    viewModel.setDarkMode(true)
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: StringResource,
    selected: Boolean,
    onSelect: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = stringResource(label))
    }
}
