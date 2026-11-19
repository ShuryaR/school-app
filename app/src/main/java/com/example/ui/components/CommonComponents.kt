package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.StudentInfo
import com.example.i18n.AppLang
import com.example.i18n.AppStrings
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CeruleanPrimary
import com.example.ui.theme.DeepSchoolNavy
import com.example.ui.theme.FeeAmber
import com.example.ui.theme.FeeAmberBg
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PureCardWhite
import com.example.ui.theme.SkyHeaderAccent
import com.example.ui.theme.SoftSkyTint
import java.text.NumberFormat
import java.util.Locale

fun formatInr(amount: Double): String {
    val locale = Locale.Builder().setLanguage("en").setRegion("IN").build()
    val formatter = NumberFormat.getNumberInstance(locale).apply {
        maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
    }
    return "₹${formatter.format(amount)}"
}

@Composable
fun LanguageTogglePill(
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    modifier: Modifier = Modifier,
    darkSurface: Boolean = true
) {
    val containerColor = if (darkSurface) Color.White.copy(alpha = 0.18f) else SoftSkyTint
    Surface(
        modifier = modifier
            .testTag("language_toggle_pill")
            .height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppLang.entries.forEach { lang ->
                val selected = lang == currentLang
                val pillBg = when {
                    selected && darkSurface -> PureCardWhite
                    selected && !darkSurface -> CeruleanPrimary
                    else -> Color.Transparent
                }
                val textColor = when {
                    selected && darkSurface -> DeepSchoolNavy
                    selected && !darkSurface -> PureCardWhite
                    darkSurface -> PureCardWhite
                    else -> DeepSchoolNavy
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(pillBg)
                        .clickable { onSelectLang(lang) }
                        .padding(horizontal = 11.dp, vertical = 6.dp)
                        .testTag("lang_option_${lang.code}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lang.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun CurvedSchoolHeader(
    title: String,
    subtitle: String? = null,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onBackClick: (() -> Unit)? = null,
    unreadAlertsCount: Int = 0,
    onAlertsClick: (() -> Unit)? = null,
    bottomContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SkyHeaderAccent, CeruleanPrimary, DeepSchoolNavy)
                )
            )
    ) {
        // Subtle decorative school-backdrop rings matching reference header
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = size.width * 0.35f,
                center = Offset(size.width * 0.92f, size.height * 0.15f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = size.width * 0.22f,
                center = Offset(size.width * 0.10f, size.height * 0.80f)
            )
            val wavePath = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, size.height - 18f)
                quadraticTo(size.width * 0.5f, size.height + 10f, size.width, size.height - 18f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(wavePath, color = Color(0xFFF4F7FB))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("header_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PureCardWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = PureCardWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = PureCardWhite.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageTogglePill(
                        currentLang = currentLang,
                        onSelectLang = onSelectLang,
                        darkSurface = true
                    )
                    if (onAlertsClick != null) {
                        IconButton(
                            onClick = onAlertsClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("header_alerts_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadAlertsCount > 0) {
                                        Badge {
                                            Text(unreadAlertsCount.coerceAtMost(99).toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "School Alerts",
                                    tint = PureCardWhite
                                )
                            }
                        }
                    }
                }
            }

            if (bottomContent != null) {
                Spacer(modifier = Modifier.height(12.dp))
                bottomContent()
            }
        }
    }
}

@Composable
fun ChildSwitcherRow(
    students: List<StudentInfo>,
    selectedStudentId: String?,
    allLabel: String,
    includeAllOption: Boolean = true,
    onSelectStudent: (String?) -> Unit,
    darkBackground: Boolean = true
) {
    if (students.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (includeAllOption) {
            ChildChip(
                label = allLabel,
                selected = selectedStudentId == null,
                darkBackground = darkBackground,
                testTag = "child_chip_all",
                onClick = { onSelectStudent(null) }
            )
        }
        students.forEach { student ->
            val shortLabel = "${student.name.substringBefore(" ")} · ${student.classSection}"
            ChildChip(
                label = shortLabel,
                initial = student.name.firstOrNull()?.uppercaseChar()?.toString(),
                selected = selectedStudentId == student.id,
                darkBackground = darkBackground,
                testTag = "child_chip_${student.id}",
                onClick = { onSelectStudent(student.id) }
            )
        }
    }
}

@Composable
private fun ChildChip(
    label: String,
    initial: String? = null,
    selected: Boolean,
    darkBackground: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val bg = when {
        selected && darkBackground -> PureCardWhite
        selected && !darkBackground -> CeruleanPrimary
        darkBackground -> Color.White.copy(alpha = 0.16f)
        else -> PureCardWhite
    }
    val fg = when {
        selected && darkBackground -> DeepSchoolNavy
        selected && !darkBackground -> PureCardWhite
        darkBackground -> PureCardWhite
        else -> InkPrimary
    }
    val border = if (!selected && !darkBackground) BorderStroke(1.dp, CardBorder) else null

    Surface(
        modifier = Modifier
            .height(48.dp)
            .testTag(testTag)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = bg,
        border = border
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (initial != null) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (selected) CeruleanPrimary.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = fg
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = fg,
                maxLines = 1
            )
        }
    }
}

@Composable
fun OfflineLastUpdatedStrip(
    strings: AppStrings,
    lastUpdated: String?,
    syncError: String?,
    isSyncing: Boolean,
    onRefresh: () -> Unit
) {
    if (syncError == null && lastUpdated == null && !isSyncing) return
    val showWarning = syncError != null
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (showWarning) FeeAmberBg else SoftSkyTint,
        border = BorderStroke(1.dp, if (showWarning) FeeAmber.copy(alpha = 0.35f) else CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (showWarning) Icons.Filled.CloudOff else Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = if (showWarning) FeeAmber else CeruleanPrimary,
                    modifier = Modifier.size(18.dp)
                )
                val msg = buildString {
                    if (showWarning) {
                        append(strings.offlineBanner)
                        if (lastUpdated != null) append(" · ${strings.lastUpdatedPrefix} $lastUpdated")
                    } else if (isSyncing) {
                        append(strings.loading)
                    } else if (lastUpdated != null) {
                        append("${strings.lastUpdatedPrefix} $lastUpdated")
                    }
                }
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkPrimary
                )
            }
            TextButton(
                onClick = onRefresh,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = CeruleanPrimary),
                modifier = Modifier.testTag("sync_refresh_button")
            ) {
                Text(strings.syncNow, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureCardWhite),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun StatusPill(
    text: String,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun QuickAccessTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureCardWhite),
        border = BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    SectionCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SoftSkyTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = CeruleanPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = InkPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary
                )
            }
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}
