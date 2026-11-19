package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AanoorLightColorScheme = lightColorScheme(
    primary = CeruleanPrimary,
    onPrimary = PureCardWhite,
    primaryContainer = SoftSkyTint,
    onPrimaryContainer = DeepSchoolNavy,
    secondary = SkyHeaderAccent,
    onSecondary = PureCardWhite,
    secondaryContainer = InfoAzureBg,
    onSecondaryContainer = DeepSchoolNavy,
    tertiary = FeeAmber,
    onTertiary = PureCardWhite,
    tertiaryContainer = FeeAmberBg,
    onTertiaryContainer = GoldDarkText,
    background = IceBackground,
    onBackground = InkPrimary,
    surface = PureCardWhite,
    onSurface = InkPrimary,
    surfaceVariant = SubtleSurface,
    onSurfaceVariant = InkSecondary,
    outline = CardBorder,
    outlineVariant = DividerLine,
    error = AbsentRose,
    onError = PureCardWhite,
    errorContainer = AbsentRoseBg,
    onErrorContainer = AbsentRose
)

val AanoorShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AanoorParentTheme(
    largeText: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AanoorLightColorScheme,
        typography = buildAppTypography(largeText = largeText),
        shapes = AanoorShapes,
        content = content
    )
}
