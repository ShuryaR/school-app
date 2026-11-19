package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.i18n.AppLang
import com.example.i18n.AppStrings
import com.example.ui.components.ChildSwitcherRow
import com.example.ui.components.CurvedSchoolHeader
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.OfflineLastUpdatedStrip
import com.example.ui.components.QuickAccessTile
import com.example.ui.components.SectionCard
import com.example.ui.components.StatusPill
import com.example.ui.components.formatInr
import com.example.ui.theme.AbsentRose
import com.example.ui.theme.AbsentRoseBg
import com.example.ui.theme.CeruleanPrimary
import com.example.ui.theme.DeepSchoolNavy
import com.example.ui.theme.DividerLine
import com.example.ui.theme.FeeAmber
import com.example.ui.theme.FeeAmberBg
import com.example.ui.theme.GoldDarkText
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.IceBackground
import com.example.ui.theme.InfoAzure
import com.example.ui.theme.InfoAzureBg
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.PureCardWhite
import com.example.ui.theme.SkyHeaderAccent
import com.example.ui.viewmodel.MainUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onSelectStudent: (String?) -> Unit,
    onRefresh: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenFees: () -> Unit,
    onPayWithUpi: () -> Unit,
    onOpenCirculars: () -> Unit,
    onAcknowledgeCircular: (String) -> Unit,
    onOpenHomework: () -> Unit,
    onOpenResults: () -> Unit,
    onOpenCertificates: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenTeacher: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val schoolTitle = if (currentLang == AppLang.TA && state.session.schoolBranding.schoolNameTamil.isNotBlank()) {
        state.session.schoolBranding.schoolNameTamil
    } else {
        state.session.schoolBranding.schoolName.ifBlank { strings.schoolName }
    }
    val baseGreeting = if (state.session.guardian.name.isNotBlank()) {
        "${strings.greetingMorning}, ${state.session.guardian.name}"
    } else {
        strings.greetingMorning
    }
    val greetingSub = if (state.session.isDemoMode) "$baseGreeting · Demo" else baseGreeting

    val visibleStudents = state.students.filter {
        state.selectedStudentId == null || it.id == state.selectedStudentId
    }
    val visibleInvoices = state.invoices.filter {
        state.selectedStudentId == null || it.studentId == state.selectedStudentId
    }
    val totalDue = visibleInvoices.sumOf { it.amount }
    val earliestDue = visibleInvoices.minByOrNull { it.dueDate }?.dueDate

    val visibleCirculars = state.circulars.filter {
        state.selectedStudentId == null || it.studentId == null || it.studentId == state.selectedStudentId
    }
    val unreadCirculars = visibleCirculars.filter { !it.isRead }
    val topCircular = unreadCirculars.firstOrNull() ?: visibleCirculars.firstOrNull()

    val visibleHomework = state.homework.filter {
        state.selectedStudentId == null || it.studentId == state.selectedStudentId
    }
    val todayText = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = schoolTitle,
            subtitle = greetingSub,
            currentLang = currentLang,
            onSelectLang = onSelectLang,
            unreadAlertsCount = state.unreadAlertsCount,
            onAlertsClick = onOpenAlerts
        ) {
            ChildSwitcherRow(
                students = state.students,
                selectedStudentId = state.selectedStudentId,
                allLabel = strings.allChildren,
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
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                ) {
                    OfflineLastUpdatedStrip(
                        strings = strings,
                        lastUpdated = state.lastUpdatedFormatted,
                        syncError = state.syncError,
                        isSyncing = state.isSyncing,
                        onRefresh = onRefresh
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. TODAY'S ATTENDANCE SUMMARY CARD
                    SectionCard(
                        modifier = Modifier
                            .testTag("home_attendance_card")
                            .clickable(onClick = onOpenAttendance)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.todayLabel} · $todayText",
                                style = MaterialTheme.typography.labelLarge,
                                color = CeruleanPrimary
                            )
                            TextButton(
                                onClick = onOpenAttendance,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(strings.attendanceTitle)
                            }
                        }
                        if (visibleStudents.isEmpty()) {
                            Text(
                                text = strings.noDataTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkMuted
                            )
                        } else {
                            visibleStudents.forEachIndexed { index, student ->
                                if (index > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = DividerLine
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        val isPresent = student.todayStatus.equals("Present", ignoreCase = true)
                                        val isAbsent = student.todayStatus.equals("Absent", ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isPresent -> PresentGreen
                                                        isAbsent -> AbsentRose
                                                        else -> FeeAmber
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = InkPrimary
                                            )
                                            Text(
                                                text = student.classSection,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = InkMuted
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val isPresent = student.todayStatus.equals("Present", ignoreCase = true)
                                        val isAbsent = student.todayStatus.equals("Absent", ignoreCase = true)
                                        val label = when {
                                            isPresent -> strings.present
                                            isAbsent -> strings.absent
                                            else -> student.todayStatus
                                        }
                                        StatusPill(
                                            text = label,
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
                                        if (isAbsent && student.alertSent) {
                                            Text(
                                                text = "(${strings.alertSent})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AbsentRose
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. FEES DUE HERO CARD (inspired by reference sports/tuition gradient banner)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_fees_card")
                            .clickable(onClick = onOpenFees),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CeruleanPrimary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(DeepSchoolNavy, CeruleanPrimary, SkyHeaderAccent)
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.feesDueTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = PureCardWhite.copy(alpha = 0.85f)
                                    )
                                    if (!earliestDue.isNullOrBlank() && totalDue > 0) {
                                        Text(
                                            text = "${strings.dueByPrefix} $earliestDue",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = GoldHighlight
                                        )
                                    }
                                }
                                Text(
                                    text = if (totalDue > 0) formatInr(totalDue) else strings.noFeesDue,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PureCardWhite
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (totalDue > 0) {
                                        Button(
                                            onClick = onPayWithUpi,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = GoldHighlight,
                                                contentColor = GoldDarkText
                                            ),
                                            modifier = Modifier
                                                .height(48.dp)
                                                .testTag("home_pay_upi_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.AccountBalanceWallet,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = strings.payWithUpi,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = onOpenFees,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureCardWhite),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text(strings.feesTitle)
                                    }
                                }
                            }
                        }
                    }

                    // 3. CIRCULARS PREVIEW CARD
                    SectionCard(
                        modifier = Modifier
                            .testTag("home_circulars_card")
                            .clickable(onClick = onOpenCirculars)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Campaign,
                                    contentDescription = null,
                                    tint = InfoAzure
                                )
                                Text(
                                    text = strings.circularsTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = InfoAzure
                                )
                                if (unreadCirculars.isNotEmpty()) {
                                    StatusPill(
                                        text = "${unreadCirculars.size} ${strings.newBadge}",
                                        textColor = InfoAzure,
                                        bgColor = InfoAzureBg
                                    )
                                }
                            }
                            TextButton(onClick = onOpenCirculars) {
                                Text(strings.filterAll)
                            }
                        }

                        if (topCircular == null) {
                            Text(
                                text = strings.noCirculars,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkMuted
                            )
                        } else {
                            val title = if (currentLang == AppLang.TA && !topCircular.titleTa.isNullOrBlank()) {
                                topCircular.titleTa
                            } else {
                                topCircular.title
                            }
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = topCircular.publishedAt,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkMuted
                            )
                            if (topCircular.requiresAck && !topCircular.isAcknowledged) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onAcknowledgeCircular(topCircular.name) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CeruleanPrimary),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("home_ack_circular_button")
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.acknowledge)
                                }
                            }
                        }
                    }

                    // 4. HOMEWORK PREVIEW CARD
                    SectionCard(
                        modifier = Modifier
                            .testTag("home_homework_card")
                            .clickable(onClick = onOpenHomework)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = FeeAmber
                                )
                                Text(
                                    text = strings.homeworkTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = FeeAmber
                                )
                            }
                            TextButton(onClick = onOpenHomework) {
                                Text(strings.filterAll)
                            }
                        }

                        if (visibleHomework.isEmpty()) {
                            Text(
                                text = strings.noHomework,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkMuted
                            )
                        } else {
                            visibleHomework.take(2).forEachIndexed { idx, hw ->
                                if (idx > 0) Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${hw.studentName.substringBefore(" ")} · ${hw.course}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = InkPrimary
                                )
                                Text(
                                    text = hw.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // 5. QUICK ACCESS 2-COLUMN GRID (Reference 1 & 2 pattern)
                    Text(
                        text = strings.quickAccess,
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickAccessTile(
                            title = strings.attendanceTitle,
                            subtitle = strings.last30Days,
                            icon = Icons.Filled.EventAvailable,
                            iconTint = PresentGreen,
                            iconBg = PresentGreenBg,
                            testTag = "quick_attendance_tile",
                            onClick = onOpenAttendance,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessTile(
                            title = strings.resultsTitle,
                            subtitle = strings.overallScore,
                            icon = Icons.Filled.Grade,
                            iconTint = InfoAzure,
                            iconBg = InfoAzureBg,
                            testTag = "quick_results_tile",
                            onClick = onOpenResults,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickAccessTile(
                            title = strings.certificatesTitle,
                            subtitle = "Bonafide · TC",
                            icon = Icons.Filled.WorkspacePremium,
                            iconTint = FeeAmber,
                            iconBg = FeeAmberBg,
                            testTag = "quick_certificates_tile",
                            onClick = onOpenCertificates,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessTile(
                            title = strings.calendarTitle,
                            subtitle = "${strings.typeHoliday} · ${strings.typeExam}",
                            icon = Icons.Filled.CalendarMonth,
                            iconTint = AbsentRose,
                            iconBg = AbsentRoseBg,
                            testTag = "quick_calendar_tile",
                            onClick = onOpenCalendar,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickAccessTile(
                            title = strings.contactTeacherTitle,
                            subtitle = strings.officeHoursLabel,
                            icon = Icons.AutoMirrored.Filled.Chat,
                            iconTint = CeruleanPrimary,
                            iconBg = InfoAzureBg,
                            testTag = "quick_teacher_tile",
                            onClick = onOpenTeacher,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessTile(
                            title = strings.profileTitle,
                            subtitle = strings.languageSetting,
                            icon = Icons.Filled.Person,
                            iconTint = PresentGreen,
                            iconBg = PresentGreenBg,
                            testTag = "quick_profile_tile",
                            onClick = onOpenProfile,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
