package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.i18n.AppLang
import com.example.i18n.AppStrings
import com.example.ui.components.ChildSwitcherRow
import com.example.ui.components.CurvedSchoolHeader
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.OfflineLastUpdatedStrip
import com.example.ui.components.QuickAccessTile
import com.example.ui.components.SectionCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.AbsentRose
import com.example.ui.theme.AbsentRoseBg
import com.example.ui.theme.CeruleanPrimary
import com.example.ui.theme.DeepSchoolNavy
import com.example.ui.theme.FeeAmber
import com.example.ui.theme.FeeAmberBg
import com.example.ui.theme.IceBackground
import com.example.ui.theme.InfoAzure
import com.example.ui.theme.InfoAzureBg
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.PureCardWhite
import com.example.ui.theme.SoftSkyTint
import com.example.ui.viewmodel.MainUiState

@Composable
fun ContactTeacherScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onSelectStudent: (String?) -> Unit,
    onRefresh: (String?) -> Unit,
    onSendMessage: (studentId: String, message: String) -> Unit,
    onBack: () -> Unit
) {
    val activeStudent = state.students.firstOrNull { it.id == state.selectedStudentId }
        ?: state.students.firstOrNull()

    LaunchedEffect(activeStudent?.id) {
        onRefresh(activeStudent?.id)
    }

    var draft by remember { mutableStateOf("") }
    val thread = state.teacherMessages.filter {
        activeStudent == null || it.studentId.isBlank() || it.studentId == activeStudent.id
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
            .imePadding()
    ) {
        CurvedSchoolHeader(
            title = strings.contactTeacherTitle,
            subtitle = activeStudent?.classTeacherName?.ifBlank { strings.schoolName } ?: strings.schoolName,
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
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    if (activeStudent != null) {
                        SectionCard {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(SoftSkyTint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = CeruleanPrimary
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeStudent.classTeacherName.ifBlank { "Class Teacher" },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = InkPrimary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AccessTime,
                                            contentDescription = null,
                                            tint = InkMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "${strings.officeHoursLabel}: ${activeStudent.teacherOfficeHours}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (thread.isEmpty()) {
                item {
                    Column(modifier = Modifier.widthIn(max = 600.dp)) {
                        EmptyStateCard(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            title = strings.noMessages,
                            actionLabel = strings.retry,
                            onAction = { onRefresh(activeStudent?.id) }
                        )
                    }
                }
            } else {
                items(thread, key = { it.id }) { msg ->
                    val isParent = msg.senderRole.equals("Parent", ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = if (isParent) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isParent) CeruleanPrimary else PureCardWhite,
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.body,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isParent) PureCardWhite else InkPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.sentAt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isParent) PureCardWhite.copy(alpha = 0.8f) else InkMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // 500-character message composer
        Surface(
            color = PureCardWhite,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { if (it.length <= 500) draft = it },
                        placeholder = { Text(strings.messageHint) },
                        supportingText = {
                            Text("${draft.length} ${strings.charLimitSuffix}")
                        },
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("teacher_message_input")
                    )
                    IconButton(
                        onClick = {
                            val studentId = activeStudent?.id.orEmpty()
                            if (studentId.isNotBlank() && draft.isNotBlank()) {
                                onSendMessage(studentId, draft)
                                draft = ""
                            }
                        },
                        enabled = draft.isNotBlank() && activeStudent != null,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (draft.isNotBlank()) CeruleanPrimary else SoftSkyTint)
                            .testTag("send_teacher_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = strings.sendMessage,
                            tint = if (draft.isNotBlank()) PureCardWhite else CeruleanPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoreScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenHomework: () -> Unit,
    onOpenResults: () -> Unit,
    onOpenCertificates: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenTeacher: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.moreTitle,
            subtitle = strings.schoolName,
            currentLang = currentLang,
            onSelectLang = onSelectLang,
            unreadAlertsCount = state.unreadAlertsCount,
            onAlertsClick = onOpenNotifications
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.widthIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickAccessTile(
                            title = strings.attendanceTitle,
                            subtitle = strings.last30Days,
                            icon = Icons.Filled.EventAvailable,
                            iconTint = PresentGreen,
                            iconBg = PresentGreenBg,
                            testTag = "more_attendance_tile",
                            onClick = onOpenAttendance,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessTile(
                            title = strings.homeworkScreenTitle,
                            subtitle = strings.homeworkTitle,
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            iconTint = FeeAmber,
                            iconBg = FeeAmberBg,
                            testTag = "more_homework_tile",
                            onClick = onOpenHomework,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickAccessTile(
                            title = strings.resultsTitle,
                            subtitle = strings.overallScore,
                            icon = Icons.Filled.Grade,
                            iconTint = InfoAzure,
                            iconBg = InfoAzureBg,
                            testTag = "more_results_tile",
                            onClick = onOpenResults,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessTile(
                            title = strings.certificatesTitle,
                            subtitle = "Bonafide · TC",
                            icon = Icons.Filled.WorkspacePremium,
                            iconTint = FeeAmber,
                            iconBg = FeeAmberBg,
                            testTag = "more_certificates_tile",
                            onClick = onOpenCertificates,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickAccessTile(
                            title = strings.calendarTitle,
                            subtitle = "${strings.typeHoliday} · ${strings.typeExam}",
                            icon = Icons.Filled.CalendarMonth,
                            iconTint = AbsentRose,
                            iconBg = AbsentRoseBg,
                            testTag = "more_calendar_tile",
                            onClick = onOpenCalendar,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessTile(
                            title = strings.contactTeacherTitle,
                            subtitle = strings.officeHoursLabel,
                            icon = Icons.AutoMirrored.Filled.Chat,
                            iconTint = CeruleanPrimary,
                            iconBg = InfoAzureBg,
                            testTag = "more_teacher_tile",
                            onClick = onOpenTeacher,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickAccessTile(
                            title = strings.notificationsTitle,
                            subtitle = "${state.unreadAlertsCount} ${strings.newBadge}",
                            icon = Icons.Filled.Notifications,
                            iconTint = InfoAzure,
                            iconBg = InfoAzureBg,
                            testTag = "more_notifications_tile",
                            onClick = onOpenNotifications,
                            modifier = Modifier.weight(1f)
                        )
                        QuickAccessTile(
                            title = strings.profileTitle,
                            subtitle = strings.guardianDetails,
                            icon = Icons.Filled.Person,
                            iconTint = PresentGreen,
                            iconBg = PresentGreenBg,
                            testTag = "more_profile_tile",
                            onClick = onOpenProfile,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onMarkAllRead: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        onMarkAllRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.notificationsTitle,
            subtitle = strings.schoolName,
            currentLang = currentLang,
            onSelectLang = onSelectLang,
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.alerts.isEmpty()) {
                item {
                    Column(modifier = Modifier.widthIn(max = 600.dp)) {
                        EmptyStateCard(
                            icon = Icons.Filled.Notifications,
                            title = strings.noNotifications
                        )
                    }
                }
            } else {
                items(state.alerts, key = { it.id }) { alert ->
                    Column(modifier = Modifier.widthIn(max = 600.dp)) {
                        SectionCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusPill(
                                    text = alert.category,
                                    textColor = InfoAzure,
                                    bgColor = InfoAzureBg
                                )
                                Text(
                                    text = alert.createdAt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkPrimary
                            )
                            Text(
                                text = alert.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onToggleLargeText: (Boolean) -> Unit,
    onUpdateNotificationPrefs: (push: Boolean, whatsapp: Boolean, sms: Boolean) -> Unit,
    onUpdateApaarConsent: (String) -> Unit,
    onWithdrawOptionalConsents: () -> Unit,
    onExportData: () -> Unit,
    onRequestCorrection: (studentId: String, field: String, details: String) -> Unit,
    onUpdateServerUrl: (String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var showCorrectionDialog by remember { mutableStateOf(false) }
    var correctionField by remember { mutableStateOf("") }
    var correctionDetails by remember { mutableStateOf("") }
    var serverUrlDraft by remember(state.session.serverBaseUrl) {
        mutableStateOf(state.session.serverBaseUrl)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.profileTitle,
            subtitle = state.session.guardian.mobile,
            currentLang = currentLang,
            onSelectLang = onSelectLang,
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.widthIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Guardian & Children
                    SectionCard {
                        Text(
                            text = strings.guardianDetails,
                            style = MaterialTheme.typography.labelLarge,
                            color = CeruleanPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.session.guardian.name.ifBlank { "Parent / Guardian" },
                            style = MaterialTheme.typography.titleLarge,
                            color = InkPrimary
                        )
                        Text(
                            text = "${state.session.guardian.relation} · ${state.session.guardian.mobile}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkSecondary
                        )
                        if (state.students.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = strings.linkedChildren,
                                style = MaterialTheme.typography.labelMedium,
                                color = InkMuted
                            )
                            state.students.forEach { s ->
                                Text(
                                    text = "• ${s.name} (${s.classSection})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkPrimary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Language & Large Text Accessibility
                    SectionCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.languageSetting,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            LanguageTogglePill(
                                currentLang = currentLang,
                                onSelectLang = onSelectLang,
                                darkSurface = false
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = strings.largeTextSetting,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = InkPrimary
                                )
                                Text(
                                    text = strings.largeTextSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkMuted
                                )
                            }
                            Switch(
                                checked = state.session.largeTextMode,
                                onCheckedChange = onToggleLargeText,
                                modifier = Modifier.testTag("large_text_switch")
                            )
                        }
                    }

                    // Notification Preferences
                    SectionCard {
                        Text(
                            text = strings.notificationSettings,
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepSchoolNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val n = state.session.notifications
                        NotificationToggleRow(
                            label = strings.pushAlerts,
                            checked = n.pushEnabled,
                            onChecked = { onUpdateNotificationPrefs(it, n.whatsappEnabled, n.smsEnabled) },
                            testTag = "notif_push_switch"
                        )
                        NotificationToggleRow(
                            label = strings.whatsappFallback,
                            checked = n.whatsappEnabled,
                            onChecked = { onUpdateNotificationPrefs(n.pushEnabled, it, n.smsEnabled) },
                            testTag = "notif_whatsapp_switch"
                        )
                        NotificationToggleRow(
                            label = strings.smsFallback,
                            checked = n.smsEnabled,
                            onChecked = { onUpdateNotificationPrefs(n.pushEnabled, n.whatsappEnabled, it) },
                            testTag = "notif_sms_switch"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.quietHoursNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted
                        )
                    }

                    // APAAR Consent & DPDP Rights
                    SectionCard {
                        Text(
                            text = strings.apaarConsentLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepSchoolNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val apaarOptions = listOf(
                            "Pending" to strings.apaarPending,
                            "Given" to strings.apaarGiven,
                            "Refused" to strings.apaarRefused
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            apaarOptions.forEachIndexed { idx, (value, label) ->
                                SegmentedButton(
                                    selected = state.session.consent.apaarConsent.equals(value, ignoreCase = true),
                                    onClick = { onUpdateApaarConsent(value) },
                                    shape = SegmentedButtonDefaults.itemShape(idx, apaarOptions.size),
                                    modifier = Modifier.testTag("apaar_option_$value")
                                ) {
                                    Text(label)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = strings.dpdpRightsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepSchoolNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onExportData,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("download_my_data_button")
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.downloadMyData)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showCorrectionDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("request_correction_button")
                        ) {
                            Icon(Icons.Filled.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.requestCorrection)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onWithdrawOptionalConsents,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdraw_consent_button")
                        ) {
                            Text(strings.withdrawOptionalConsent, color = AbsentRose)
                        }
                    }

                    // Frappe Server URL Configuration
                    SectionCard {
                        Text(
                            text = strings.serverUrlLabel,
                            style = MaterialTheme.typography.titleSmall,
                            color = InkPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = serverUrlDraft,
                            onValueChange = { serverUrlDraft = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onUpdateServerUrl(serverUrlDraft) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(strings.syncNow)
                        }
                    }

                    Button(
                        onClick = onLogout,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AbsentRose),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("logout_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.logOut, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    if (showCorrectionDialog) {
        val defaultStudentId = state.selectedStudentId ?: state.students.firstOrNull()?.id.orEmpty()
        AlertDialog(
            onDismissRequest = { showCorrectionDialog = false },
            title = { Text(strings.requestCorrection) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = correctionField,
                        onValueChange = { correctionField = it },
                        label = { Text(strings.correctionFieldLabel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = correctionDetails,
                        onValueChange = { correctionDetails = it },
                        label = { Text(strings.correctionNoteLabel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (correctionField.isNotBlank() && correctionDetails.isNotBlank()) {
                            onRequestCorrection(defaultStudentId, correctionField, correctionDetails)
                            showCorrectionDialog = false
                            correctionField = ""
                            correctionDetails = ""
                        }
                    }
                ) {
                    Text(strings.submit)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCorrectionDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun NotificationToggleRow(
    label: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChecked(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = InkPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            modifier = Modifier.testTag(testTag)
        )
    }
}
