package com.mattschoe.apptemplate.ui.pages.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import apptemplate.shared.generated.resources.Res
import apptemplate.shared.generated.resources.a11y_add_item
import apptemplate.shared.generated.resources.a11y_delete_item
import apptemplate.shared.generated.resources.a11y_open_settings
import apptemplate.shared.generated.resources.home_add_dialog_label
import apptemplate.shared.generated.resources.home_add_dialog_title
import apptemplate.shared.generated.resources.home_empty
import apptemplate.shared.generated.resources.home_title
import com.mattschoe.apptemplate.domain.DataError.Companion.getResource
import com.mattschoe.apptemplate.ui.components.PageShell
import com.mattschoe.apptemplate.ui.components.TextInputDialog
import com.mattschoe.apptemplate.ui.navigation.PageNavigation
import org.jetbrains.compose.resources.stringResource

/**
 * Pages take `navController` and `viewModel` as named parameters and own no state
 * of their own beyond pure UI state (dialog open/closed). Everything else comes
 * from the ViewModel as a `StateFlow` read with `collectAsStateWithLifecycle()`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val error by viewModel.errorState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage = error?.let { stringResource(it.getResource()) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.consumeError()
        }
    }

    PageShell(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.home_title)) },
                actions = {
                    IconButton(onClick = { navController.navigate(PageNavigation.Settings) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(Res.string.a11y_open_settings)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.a11y_add_item)
                )
            }
        },
        bottomBar = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            uiState.items.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.home_empty),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f).padding(vertical = 16.dp)
                            )
                            IconButton(onClick = { viewModel.deleteItem(item.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(Res.string.a11y_delete_item)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TextInputDialog(
            title = stringResource(Res.string.home_add_dialog_title),
            label = stringResource(Res.string.home_add_dialog_label),
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addItem(name)
                showAddDialog = false
            }
        )
    }
}
