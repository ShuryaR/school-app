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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FamilyReceipt
import com.example.i18n.AppLang
import com.example.i18n.AppStrings
import com.example.ui.components.CurvedSchoolHeader
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.OfflineLastUpdatedStrip
import com.example.ui.components.SectionCard
import com.example.ui.components.StatusPill
import com.example.ui.components.formatInr
import com.example.ui.theme.CeruleanPrimary
import com.example.ui.theme.DeepSchoolNavy
import com.example.ui.theme.FeeAmber
import com.example.ui.theme.FeeAmberBg
import com.example.ui.theme.IceBackground
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.PureCardWhite
import com.example.ui.theme.SkyHeaderAccent
import com.example.ui.viewmodel.MainUiState
import com.example.util.UpiPaymentHelper

@Composable
fun FeesScreen(
    state: MainUiState,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onRefresh: () -> Unit,
    onToggleInvoice: (String) -> Unit,
    onPaySelected: () -> Unit,
    onOpenReceiptPdf: (FamilyReceipt) -> Unit
) {
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) } // 0 = Dues, 1 = Receipts

    LaunchedEffect(Unit) {
        onRefresh()
    }

    val totalDue = state.invoices.sumOf { it.amount }
    val totalPaid = state.receipts.sumOf { it.amount }
    val feeOrderUrl = state.invoices.firstNotNullOfOrNull { it.feeOrderUrl }
        ?: state.session.schoolBranding.feeOrderUrl
    val groupedInvoices = state.invoices.groupBy {
        it.studentName.ifBlank { it.studentId }.ifBlank { strings.schoolName }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.feesTitle,
            subtitle = "${strings.totalDueLabel}: ${formatInr(totalDue)}",
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
                }
            }

            // Reference-1 Gradient Tuition Summary Card
            item {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CeruleanPrimary)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(DeepSchoolNavy, CeruleanPrimary, SkyHeaderAccent)
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = strings.totalDueLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PureCardWhite.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = formatInr(totalDue),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PureCardWhite
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = strings.totalPaidLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PureCardWhite.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = formatInr(totalPaid),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PureCardWhite
                                    )
                                }
                            }
                        }
                    }

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = tab == 0,
                            onClick = { tab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                            modifier = Modifier.testTag("fees_dues_tab")
                        ) {
                            Text(strings.overviewTab)
                        }
                        SegmentedButton(
                            selected = tab == 1,
                            onClick = { tab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                            modifier = Modifier.testTag("fees_receipts_tab")
                        ) {
                            Text(strings.receiptsTab)
                        }
                    }
                }
            }

            if (tab == 0) {
                if (state.invoices.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 600.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            EmptyStateCard(
                                icon = Icons.Filled.AccountBalanceWallet,
                                title = strings.noFeesDue,
                                actionLabel = strings.retry,
                                onAction = onRefresh
                            )
                        }
                    }
                } else {
                    groupedInvoices.forEach { (studentLabel, studentInvoices) ->
                        item {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 600.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                SectionCard {
                                    val classLabel = studentInvoices.firstOrNull()?.classSection.orEmpty()
                                    Text(
                                        text = if (classLabel.isNotBlank()) "$studentLabel · $classLabel" else studentLabel,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = DeepSchoolNavy
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    studentInvoices.forEach { inv ->
                                        val checked = inv.name in state.selectedInvoiceIds
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onToggleInvoice(inv.name) }
                                                .padding(vertical = 6.dp)
                                                .testTag("invoice_row_${inv.name}"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Checkbox(
                                                    checked = checked,
                                                    onCheckedChange = { onToggleInvoice(inv.name) },
                                                    modifier = Modifier.testTag("invoice_checkbox_${inv.name}")
                                                )
                                                Column {
                                                    Text(
                                                        text = inv.title,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = InkPrimary
                                                    )
                                                    if (inv.dueDate.isNotBlank()) {
                                                        Text(
                                                            text = "${strings.dueByPrefix} ${inv.dueDate}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = InkMuted
                                                        )
                                                    }
                                                    if (!inv.concessionNote.isNullOrBlank()) {
                                                        StatusPill(
                                                            text = inv.concessionNote,
                                                            textColor = PresentGreen,
                                                            bgColor = PresentGreenBg,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = formatInr(inv.amount),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = InkPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 600.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val selectedTotal = state.selectedFeesAmount
                            Button(
                                onClick = onPaySelected,
                                enabled = selectedTotal > 0 && !state.isSyncing,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CeruleanPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("pay_selected_upi_button")
                            ) {
                                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${strings.paySelectedWithUpi} (${formatInr(selectedTotal)})",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            if (!feeOrderUrl.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        UpiPaymentHelper.openExternalUrl(
                                            context = context,
                                            rawUrl = feeOrderUrl,
                                            serverBaseUrl = state.session.serverBaseUrl
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("approved_fee_order_button")
                                ) {
                                    Icon(Icons.Filled.Description, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.approvedFeeOrder)
                                }
                            }
                        }
                    }
                }
            } else {
                if (state.receipts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 600.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            EmptyStateCard(
                                icon = Icons.Filled.ReceiptLong,
                                title = strings.noDataTitle,
                                actionLabel = strings.retry,
                                onAction = onRefresh
                            )
                        }
                    }
                } else {
                    items(state.receipts, key = { it.name }) { receipt ->
                        Column(
                            modifier = Modifier
                                .widthIn(max = 600.dp)
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                        ) {
                            SectionCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = receipt.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = InkPrimary
                                        )
                                        Text(
                                            text = "${receipt.date} · ${receipt.mode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = InkMuted
                                        )
                                        if (receipt.allocationsSummary.isNotBlank()) {
                                            Text(
                                                text = receipt.allocationsSummary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = InkSecondary
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatInr(receipt.amount),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PresentGreen
                                        )
                                        TextButton(
                                            onClick = { onOpenReceiptPdf(receipt) },
                                            modifier = Modifier.testTag("receipt_pdf_${receipt.name}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.PictureAsPdf,
                                                contentDescription = strings.downloadPdf,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(strings.downloadPdf)
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
