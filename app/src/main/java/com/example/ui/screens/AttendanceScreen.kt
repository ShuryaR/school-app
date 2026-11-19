package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.i18n.AppLang
import com.example.i18n.AppStrings
import com.example.ui.components.ChildSwitcherRow
import com.example.ui.components.CurvedSchoolHeader
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.OfflineLastUpdatedStrip
import com.example.ui.components.SectionCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.AbsentRose
import com.example.ui.theme.AbsentRoseBg
import com.example.ui.theme.CeruleanPrimary
import com.example.ui.theme.FeeAmber
import com.example.ui.theme.FeeAmberBg
import com.example.ui.theme.IceBackground
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.PureCardWhite
import com.example.ui.viewmodel.MainUiState

@Composable
fun AttendanceScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onSelectStudent: (String?) -> Unit,
    onRefresh: (String?) -> Unit,
    onBack: () -> Unit
) {
    val activeStudent = state.students.firstOrNull { it.id == state.selectedStudentId }
        ?: state.students.firstOrNull()

    LaunchedEffect(activeStudent?.id) {
        if (activeStudent != null) {
            onRefresh(activeStudent.id)
        }
    }

    var viewMode by remember { mutableIntStateOf(1) } // 0 = Weekly (7d), 1 = Monthly (30d)
    val studentDays = state.attendanceDays.filter {
        activeStudent == null || it.studentId == activeStudent.id
    }
    val displayedDays = if (viewMode == 0) studentDays.take(7) else studentDays.take(30)
    val presents = displayedDays.count { it.status.equals("Present", ignoreCase = true) }
    val absents = displayedDays.count { it.status.equals("Absent", ignoreCase = true) }
    val pct = when {
        displayedDays.isNotEmpty() && (presents + absents) > 0 ->
            (presents * 100.0) / (presents + absents)
        activeStudent != null -> activeStudent.termPercentage
        else -> 0.0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.attendanceTitle,
            subtitle = activeStudent?.let { "${it.name} · ${it.classSection}" },
            currentLang = currentLang,
            onSelectLang = onSelectLang,
            onBackClick = onBack
        ) {
            ChildSwitcherRow(
                students = state.students,
                selectedStudentId = activeStudent?.id,
                allLabel = strings.allChildren,
                includeAllOption = false,
                onSelectStudent = onSelectStudent,
                darkBackground = true
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(modifier = Modifier.widthIn(max = 600.dp)) {
                    OfflineLastUpdatedStrip(
                        strings = strings,
                        lastUpdated = state.lastUpdatedFormatted,
                        syncError = state.syncError,
                        isSyncing = state.isSyncing,
                        onRefresh = { onRefresh(activeStudent?.id) }
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = viewMode == 0,
                            onClick = { viewMode = 0 },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                            modifier = Modifier.testTag("attendance_weekly_tab")
                        ) {
                            Text(strings.weeklyView)
                        }
                        SegmentedButton(
                            selected = viewMode == 1,
                            onClick = { viewMode = 1 },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                            modifier = Modifier.testTag("attendance_monthly_tab")
                        ) {
                            Text(strings.monthlyView)
                        }
                    }

                    // Reference-1 Ring Gauge + Presents/Absents Summary Card
                    SectionCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AttendanceRingGauge(
                                percentage = pct,
                                label = strings.termPercent
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = PresentGreenBg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = strings.presentsCount,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = PresentGreen
                                        )
                                        Text(
                                            text = presents.toString().padStart(2, '0'),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = PresentGreen
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = AbsentRoseBg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = strings.absentsCount,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = AbsentRose
                                        )
                                        Text(
                                            text = absents.toString().padStart(2, '0'),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = AbsentRose
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 30-day color dot strip (matching reference 2 calendar grid aesthetic)
                    if (displayedDays.isNotEmpty()) {
                        SectionCard {
                            Text(
                                text = if (viewMode == 0) strings.weeklyView else strings.last30Days,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            val rows = displayedDays.chunked(7)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                rows.forEach { week ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        week.forEach { day ->
                                            val isP = day.status.equals("Present", ignoreCase = true)
                                            val isA = day.status.equals("Absent", ignoreCase = true)
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isP -> PresentGreenBg
                                                            isA -> AbsentRoseBg
                                                            else -> FeeAmberBg
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = day.date.takeLast(2),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = when {
                                                        isP -> PresentGreen
                                                        isA -> AbsentRose
                                                        else -> FeeAmber
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (displayedDays.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        EmptyStateCard(
                            icon = Icons.Filled.EventAvailable,
                            title = strings.noDataTitle,
                            actionLabel = strings.retry,
                            onAction = { onRefresh(activeStudent?.id) }
                        )
                    }
                }
            } else {
                items(displayedDays, key = { it.id }) { day ->
                    val isPresent = day.status.equals("Present", ignoreCase = true)
                    val isAbsent = day.status.equals("Absent", ignoreCase = true)
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        SectionCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = day.date,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = InkPrimary
                                    )
                                    if (!day.arrivalTime.isNullOrBlank()) {
                                        Text(
                                            text = day.arrivalTime,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkMuted
                                        )
                                    }
                                    if (!day.reason.isNullOrBlank()) {
                                        Text(
                                            text = day.reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkSecondary
                                        )
                                    }
                                }
                                StatusPill(
                                    text = when {
                                        isPresent -> strings.present
                                        isAbsent -> strings.absent
                                        else -> strings.holidayLabel
                                    },
                                    textColor = when {
                                        isPresent -> PresentGreen
                                        isAbsent -> AbsentRose
                                        else -> FeeAmber
                                    },
                                    bgColor = when {
                                        isPresent -> PresentGreenBg
                                        isAbsent -> AbsentRoseBg
                                        else -> FeeAmberBg
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRingGauge(percentage: Double, label: String) {
    val clamped = percentage.coerceIn(0.0, 100.0).toFloat()
    Box(
        modifier = Modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 11.dp.toPx()
            drawArc(
                color = Color(0xFFE3ECF6),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = CeruleanPrimary,
                startAngle = -90f,
                sweepAngle = (clamped / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${clamped.toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = InkMuted
            )
        }
    }
}
