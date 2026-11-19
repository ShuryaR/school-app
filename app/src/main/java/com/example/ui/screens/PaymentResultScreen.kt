package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FamilyReceipt
import com.example.data.model.PaymentInitiationResult
import com.example.i18n.AppLang
import com.example.i18n.AppStrings
import com.example.ui.components.CurvedSchoolHeader
import com.example.ui.components.SectionCard
import com.example.ui.components.formatInr
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
import com.example.ui.theme.WhatsAppGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentResultScreen(
    result: PaymentInitiationResult?,
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onDownloadPdf: (FamilyReceipt) -> Unit,
    onShareWhatsApp: (FamilyReceipt) -> Unit,
    onBackToFees: () -> Unit
) {
    val status = result?.status?.lowercase().orEmpty()
    val isPaid = status == "paid" || status == "success" || status == "settled"
    val isFailed = status == "failed" || status == "cancelled"
    val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

    val receipt = FamilyReceipt(
        name = result?.receiptNo ?: result?.paymentRequestId?.ifBlank { "FR-PENDING" } ?: "FR-PENDING",
        date = today,
        amount = result?.amount ?: 0.0,
        mode = "UPI",
        referenceNo = result?.paymentRequestId ?: "",
        allocationsSummary = result?.allocationsSummary ?: ""
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.paymentResultTitle,
            subtitle = strings.schoolName,
            currentLang = currentLang,
            onSelectLang = onSelectLang,
            onBackClick = onBackToFees
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val icon = when {
                            isPaid -> Icons.Filled.CheckCircle
                            isFailed -> Icons.Filled.ErrorOutline
                            else -> Icons.Filled.HourglassTop
                        }
                        val tint = when {
                            isPaid -> PresentGreen
                            isFailed -> AbsentRose
                            else -> FeeAmber
                        }
                        val bg = when {
                            isPaid -> PresentGreenBg
                            isFailed -> AbsentRoseBg
                            else -> FeeAmberBg
                        }
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(bg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        Text(
                            text = when {
                                isPaid -> strings.paymentSuccess
                                isFailed -> strings.paymentFailed
                                else -> strings.paymentPending
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = tint
                        )
                        Text(
                            text = formatInr(receipt.amount),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = "${strings.receiptNoLabel}: ${receipt.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkSecondary
                        )
                        if (receipt.allocationsSummary.isNotBlank()) {
                            Text(
                                text = receipt.allocationsSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = InkMuted
                            )
                        }
                    }
                }

                Button(
                    onClick = { onDownloadPdf(receipt) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CeruleanPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("download_receipt_pdf_button")
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.downloadPdf, style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = { onShareWhatsApp(receipt) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("share_whatsapp_button")
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.shareToWhatsApp, style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = onBackToFees,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("back_to_fees_button")
                ) {
                    Text(strings.backToFees)
                }
            }
        }
    }
}
