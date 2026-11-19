package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val SchoolFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
    Font(R.font.noto_sans_tamil, FontWeight.Normal),
    Font(R.font.noto_sans_tamil, FontWeight.Medium),
    Font(R.font.noto_sans_tamil, FontWeight.Bold)
)

fun buildAppTypography(largeText: Boolean = false): Typography {
    val scale = if (largeText) 1.18f else 1.0f
    return Typography(
        headlineMedium = TextStyle(
            fontFamily = SchoolFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (24f * scale).sp,
            lineHeight = (32f * scale).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = SchoolFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (20f * scale).sp,
            lineHeight = (28f * scale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = SchoolFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (18f * scale).sp,
            lineHeight = (26f * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = SchoolFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16f * scale).sp,
            lineHeight = (24f * scale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = SchoolFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14f * scale).sp,
            lineHeight = (20f * scale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = SchoolFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16f * scale).sp,
            lineHeight = (24f * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = SchoolFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14f * scale).sp,
            lineHeight = (21f * scale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = SchoolFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12.5f * scale).sp,
            lineHeight = (18f * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = SchoolFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (15f * scale).sp,
            lineHeight = (20f * scale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = SchoolFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (13f * scale).sp,
            lineHeight = (18f * scale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = SchoolFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11.5f * scale).sp,
            lineHeight = (16f * scale).sp
        )
    )
}
