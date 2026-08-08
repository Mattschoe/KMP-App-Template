package com.mattschoe.apptemplate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/**
 * Typography is built by copying the Material baseline and overriding only the
 * font family on each slot — that keeps Material's tuned sizes/line-heights and
 * means a font swap is a one-line change.
 *
 * To use real fonts: drop the files into `shared/src/commonMain/composeResources/font/`,
 * then replace the two families below with e.g.
 *
 *     val displayFamily = FontFamily(Font(Res.font.PlayfairDisplay))
 *
 * (`appTypography()` is already `@Composable` so `Font(Res.font.X)` works directly.)
 */
@Composable
fun appTypography(): Typography {
    val displayFamily = FontFamily.Default
    val bodyFamily = FontFamily.Default

    val baseline = Typography()
    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = displayFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = displayFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = displayFamily),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFamily),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFamily),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFamily),
        titleLarge = baseline.titleLarge.copy(fontFamily = displayFamily),
        titleMedium = baseline.titleMedium.copy(fontFamily = displayFamily),
        titleSmall = baseline.titleSmall.copy(fontFamily = displayFamily),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFamily),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFamily),
        bodySmall = baseline.bodySmall.copy(fontFamily = bodyFamily),
        labelLarge = baseline.labelLarge.copy(fontFamily = bodyFamily),
        labelMedium = baseline.labelMedium.copy(fontFamily = bodyFamily),
        labelSmall = baseline.labelSmall.copy(fontFamily = bodyFamily)
    )
}
