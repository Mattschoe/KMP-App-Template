package com.mattschoe.apptemplate

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    // `createAppContainer()` is the JVM composition root; it lives in
    // shared/jvmMain next to the platform database + DataStore builders.
    val appContainer = remember { createAppContainer() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "App Template",
    ) {
        App(appContainer)
    }
}
