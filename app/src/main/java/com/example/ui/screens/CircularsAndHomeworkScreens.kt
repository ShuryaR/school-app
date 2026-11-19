package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.InfoAzure
import com.example.ui.theme.InfoAzureBg
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.viewmodel.MainUiState
import com.example.util.UpiPaymentHelper

@Composable
fun CircularsScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onRefresh: () -> Unit,
    onOpenCircular: (String) -> Unit,
    onAcknowledge: (String) -> Unit
) {
    val context = LocalContext.current
    var filterTab by remember { mutableIntStateOf(0) } // 0 = All, 1 = Unread
    var expandedName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        onRefresh()
    }

    val filtered = state.circulars.filter { item ->
        val matchesStudent = state.selectedStudentId == null || item.studentId == null || item.studentId == state.selectedStudentId
        val matchesFilter = filterTab == 0 || !item.isRead
        matchesStudent && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.circularsTitle,
            subtitle = "${state.unreadCircularCount} ${strings.newBadge}",
            currentLang = currentLang,
            onSelectLang = onSelectLang
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
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        SegmentedButton(
                            selected = filterTab == 0,
                            onClick = { filterTab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                            modifier = Modifier.testTag("circulars_all_tab")
                        ) {
                            Text(strings.filterAll)
                        }
                        SegmentedButton(
                            selected = filterTab == 1,
                            onClick = { filterTab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                            modifier = Modifier.testTag("circulars_unread_tab")
                        ) {
                            Text("${strings.filterUnread} (${state.unreadCircularCount})")
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        EmptyStateCard(
                            icon = Icons.Filled.Campaign,
                            title = strings.noCirculars,
                            actionLabel = strings.retry,
                            onAction = onRefresh
                        )
                    }
                }
            } else {
                items(filtered, key = { it.name }) { circular ->
                    val title = if (currentLang == AppLang.TA && !circular.titleTa.isNullOrBlank()) {
                        circular.titleTa
                    } else circular.title
                    val body = if (currentLang == AppLang.TA && !circular.bodyTa.isNullOrBlank()) {
                        circular.bodyTa
                    } else circular.body
                    val isExpanded = expandedName == circular.name

                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        SectionCard(
                            modifier = Modifier
                                .testTag("circular_card_${circular.name}")
                                .clickable {
                                    expandedName = if (isExpanded) null else circular.name
                                    if (!circular.isRead) onOpenCircular(circular.name)
                                }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (!circular.isRead) {
                                        StatusPill(
                                            text = strings.newBadge,
                                            textColor = InfoAzure,
                                            bgColor = InfoAzureBg
                                        )
                                    }
                                    if (circular.priority.equals("High", ignoreCase = true)) {
                                        StatusPill(
                                            text = "High",
                                            textColor = AbsentRose,
                                            bgColor = AbsentRoseBg
                                        )
                                    }
                                    if (circular.requiresAck && !circular.isAcknowledged) {
                                        StatusPill(
                                            text = strings.requiresAckBadge,
                                            textColor = FeeAmber,
                                            bgColor = FeeAmberBg
                                        )
                                    }
                                }
                                Text(
                                    text = circular.publishedAt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkSecondary,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 3
                            )

                            if (!circular.attachmentUrl.isNullOrBlank() || circular.requiresAck) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (circular.requiresAck) {
                                        if (circular.isAcknowledged) {
                                            StatusPill(
                                                text = strings.acknowledged,
                                                textColor = PresentGreen,
                                                bgColor = PresentGreenBg
                                            )
                                        } else {
                                            Button(
                                                onClick = { onAcknowledge(circular.name) },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = CeruleanPrimary),
                                                modifier = Modifier
                                                    .height(48.dp)
                                                    .testTag("ack_button_${circular.name}")
                                            ) {
                                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(strings.acknowledge)
                                            }
                                        }
                                    }
                                    if (!circular.attachmentUrl.isNullOrBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                UpiPaymentHelper.openExternalUrl(
                                                    context = context,
                                                    rawUrl = circular.attachmentUrl,
                                                    serverBaseUrl = state.session.serverBaseUrl
                                                )
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .height(48.dp)
                                                .testTag("circular_attachment_${circular.name}")
                                        ) {
                                            Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(strings.openAttachment)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeworkScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onSelectStudent: (String?) -> Unit,
    onRefresh: (String?) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(state.selectedStudentId) {
        onRefresh(state.selectedStudentId)
    }

    val items = state.homework.filter {
        state.selectedStudentId == null || it.studentId == state.selectedStudentId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.homeworkScreenTitle,
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

            if (items.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        EmptyStateCard(
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            title = strings.noHomework,
                            actionLabel = strings.retry,
                            onAction = { onRefresh(state.selectedStudentId) }
                        )
                    }
                }
            } else {
                items(items, key = { it.id }) { hw ->
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
                                StatusPill(
                                    text = hw.course,
                                    textColor = FeeAmber,
                                    bgColor = FeeAmberBg
                                )
                                Text(
                                    text = hw.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkMuted
                                )
                            }
                            if (hw.studentName.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = hw.studentName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CeruleanPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = hw.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = InkPrimary
                            )
                            if (hw.postedBy.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = hw.postedBy,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkMuted
                                )
                            }
                            if (!hw.attachmentUrl.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        UpiPaymentHelper.openExternalUrl(
                                            context = context,
                                            rawUrl = hw.attachmentUrl,
                                            serverBaseUrl = state.session.serverBaseUrl
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("homework_attachment_${hw.id}")
                                ) {
                                    Icon(Icons.Filled.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.openAttachment)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
