package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.i18n.AppLang
import com.example.i18n.AppStrings
import com.example.ui.components.CurvedSchoolHeader
import com.example.ui.components.SectionCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.CeruleanPrimary
import com.example.ui.theme.IceBackground
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.SoftSkyTint

@Composable
fun ConsentScreen(
    strings: AppStrings,
    currentLang: AppLang,
    onSelectLang: (AppLang) -> Unit,
    onSubmitConsent: (alerts: Boolean, optional: Boolean, apaarStatus: String) -> Unit
) {
    var alertsEnabled by remember { mutableStateOf(true) }
    var optionalEnabled by remember { mutableStateOf(true) }
    var apaarEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IceBackground)
    ) {
        CurvedSchoolHeader(
            title = strings.consentTitle,
            subtitle = strings.schoolName,
            currentLang = currentLang,
            onSelectLang = onSelectLang
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftSkyTint
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = strings.consentTitle,
                            tint = CeruleanPrimary
                        )
                        Text(
                            text = strings.consentFiduciaryNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkPrimary
                        )
                    }
                }

                // Mandatory Essential Purpose
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.consentEssentialTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = InkPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.consentEssentialDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkSecondary
                            )
                        }
                        StatusPill(
                            text = strings.consentMandatoryTag,
                            textColor = PresentGreen,
                            bgColor = PresentGreenBg
                        )
                    }
                }

                // Alerts Purpose
                ConsentToggleRow(
                    title = strings.consentAlertsTitle,
                    description = strings.consentAlertsDesc,
                    checked = alertsEnabled,
                    onCheckedChange = { alertsEnabled = it },
                    testTag = "consent_alerts_switch"
                )

                // Optional Events Purpose
                ConsentToggleRow(
                    title = strings.consentOptionalTitle,
                    description = strings.consentOptionalDesc,
                    checked = optionalEnabled,
                    onCheckedChange = { optionalEnabled = it },
                    testTag = "consent_optional_switch"
                )

                // Optional APAAR Consent
                ConsentToggleRow(
                    title = strings.consentApaarTitle,
                    description = strings.consentApaarDesc,
                    checked = apaarEnabled,
                    onCheckedChange = { apaarEnabled = it },
                    testTag = "consent_apaar_switch"
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        onSubmitConsent(
                            alertsEnabled,
                            optionalEnabled,
                            if (apaarEnabled) "Given" else "Refused"
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CeruleanPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("consent_accept_button")
                ) {
                    Text(strings.consentAcceptAll, style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = {
                        onSubmitConsent(true, false, "Refused")
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("consent_decline_optional_button")
                ) {
                    Text(strings.consentDeclineOptional, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun ConsentToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = InkPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
