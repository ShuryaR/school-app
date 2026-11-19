package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CertificateItem
import com.example.data.model.ReportCard
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
import com.example.ui.theme.DeepSchoolNavy
import com.example.ui.theme.DividerLine
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
import com.example.ui.theme.SoftSkyTint
import com.example.ui.viewmodel.MainUiState

@Composable
fun ResultsScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onSelectStudent: (String?) -> Unit,
    onRefresh: (String?) -> Unit,
    onDownloadReportCardPdf: (ReportCard) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(state.selectedStudentId) {
        onRefresh(state.selectedStudentId)
    }

    val cards = state.reportCards.filter {
        state.selectedStudentId == null || it.studentId == state.selectedStudentId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.resultsTitle,
            subtitle = strings.schoolName,
            currentLang = currentLang,
            onSelectLang = onSelectLang,
            onBackClick = onBack
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
                Column(modifier = Modifier.widthIn(max = 600.dp)) {
                    OfflineLastUpdatedStrip(
                        strings = strings,
                        lastUpdated = state.lastUpdatedFormatted,
                        syncError = state.syncError,
                        isSyncing = state.isSyncing,
                        onRefresh = { onRefresh(state.selectedStudentId) }
                    )
                }
            }

            if (cards.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        EmptyStateCard(
                            icon = Icons.Filled.Grade,
                            title = strings.noResults,
                            actionLabel = strings.retry,
                            onAction = { onRefresh(state.selectedStudentId) }
                        )
                    }
                }
            } else {
                items(cards, key = { it.name }) { card ->
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        SectionCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = card.examName,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = DeepSchoolNavy
                                    )
                                    Text(
                                        text = "${card.studentName} · ${card.classSection}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkMuted
                                    )
                                }
                                StatusPill(
                                    text = "${"%.1f".format(card.percentage)}% (${card.overallGrade})",
                                    textColor = PresentGreen,
                                    bgColor = PresentGreenBg
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = strings.subjectLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InkMuted,
                                    modifier = Modifier.weight(1.5f)
                                )
                                Text(
                                    text = strings.marksLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InkMuted,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = strings.gradeLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = InkMuted
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = DividerLine)

                            card.subjects.forEach { sub ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sub.subject,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = InkPrimary,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                    Text(
                                        text = "${sub.marksObtained.toInt()}/${sub.maxMarks.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = InkSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = sub.grade,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = CeruleanPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onDownloadReportCardPdf(card) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CeruleanPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("download_report_card_${card.name}")
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.downloadReportCard)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificatesScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onRefresh: () -> Unit,
    onRequestCertificate: (studentId: String, type: String, reason: String) -> Unit,
    onDownloadCertificatePdf: (CertificateItem) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedStudentId by remember(state.students, state.selectedStudentId) {
        mutableStateOf(state.selectedStudentId ?: state.students.firstOrNull()?.id.orEmpty())
    }
    var selectedType by remember(state.certificateTypes) {
        mutableStateOf(state.certificateTypes.firstOrNull()?.name ?: "Bonafide")
    }
    var reason by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.certificatesTitle,
            subtitle = strings.schoolName,
            currentLang = currentLang,
            onSelectLang = onSelectLang,
            onBackClick = onBack
        )

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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Madras HC compliance notice (unpaid dues never block TC)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SoftSkyTint
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = CeruleanPrimary)
                            Text(
                                text = strings.tcPolicyNotice,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkPrimary
                            )
                        }
                    }

                    Button(
                        onClick = { showDialog = true },
                        enabled = state.students.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CeruleanPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("request_certificate_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.requestCertificate, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            if (state.certificates.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        EmptyStateCard(
                            icon = Icons.Filled.WorkspacePremium,
                            title = strings.noCertificates,
                            actionLabel = strings.retry,
                            onAction = onRefresh
                        )
                    }
                }
            } else {
                items(state.certificates, key = { it.name }) { cert ->
                    val isIssued = cert.status.equals("Issued", ignoreCase = true) ||
                        cert.status.equals("Ready", ignoreCase = true)
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        SectionCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cert.certificateType,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = InkPrimary
                                    )
                                    Text(
                                        text = "${cert.studentName} · ${cert.requestedDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkMuted
                                    )
                                }
                                StatusPill(
                                    text = cert.status,
                                    textColor = if (isIssued) PresentGreen else FeeAmber,
                                    bgColor = if (isIssued) PresentGreenBg else FeeAmberBg
                                )
                            }
                            if (!cert.verifyCode.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.QrCode2,
                                        contentDescription = null,
                                        tint = CeruleanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${strings.verifyCodePrefix} ${cert.verifyCode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CeruleanPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { onDownloadCertificatePdf(cert) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("download_cert_pdf_${cert.name}")
                            ) {
                                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.downloadQrPdf)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(strings.requestCertificate) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.students.size > 1) {
                        ChildSwitcherRow(
                            students = state.students,
                            selectedStudentId = selectedStudentId,
                            allLabel = strings.allChildren,
                            includeAllOption = false,
                            onSelectStudent = { if (it != null) selectedStudentId = it },
                            darkBackground = false
                        )
                    }
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.certificateTypeLabel) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            state.certificateTypes.forEach { t ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (currentLang == AppLang.TA && !t.labelTa.isNullOrBlank()) t.labelTa else t.label
                                        )
                                    },
                                    onClick = {
                                        selectedType = t.name
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text(strings.reasonLabel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cert_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedStudentId.isNotBlank()) {
                            onRequestCertificate(selectedStudentId, selectedType, reason)
                            showDialog = false
                            reason = ""
                        }
                    },
                    modifier = Modifier.testTag("confirm_cert_request_button")
                ) {
                    Text(strings.submitRequest)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
fun CalendarScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.calendarTitle,
            subtitle = strings.schoolName,
            currentLang = currentLang,
            onSelectLang = onSelectLang,
            onBackClick = onBack
        )

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
                        onRefresh = onRefresh
                    )
                }
            }

            if (state.calendarEvents.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        EmptyStateCard(
                            icon = Icons.Filled.CalendarMonth,
                            title = strings.noEvents,
                            actionLabel = strings.retry,
                            onAction = onRefresh
                        )
                    }
                }
            } else {
                items(state.calendarEvents, key = { it.id }) { event ->
                    val isHoliday = event.category.equals("Holiday", ignoreCase = true)
                    val isExam = event.category.equals("Exam", ignoreCase = true)
                    val title = if (currentLang == AppLang.TA && !event.titleTa.isNullOrBlank()) {
                        event.titleTa
                    } else event.title
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        SectionCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = event.date,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CeruleanPrimary
                                )
                                StatusPill(
                                    text = when {
                                        isHoliday -> strings.typeHoliday
                                        isExam -> strings.typeExam
                                        else -> strings.typeEvent
                                    },
                                    textColor = when {
                                        isHoliday -> AbsentRose
                                        isExam -> FeeAmber
                                        else -> InfoAzure
                                    },
                                    bgColor = when {
                                        isHoliday -> AbsentRoseBg
                                        isExam -> FeeAmberBg
                                        else -> InfoAzureBg
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkPrimary
                            )
                            if (event.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = event.description,
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
}
