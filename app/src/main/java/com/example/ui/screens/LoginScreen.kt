package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.DemoSeedData
import com.example.i18n.AppLang
import com.example.i18n.AppStrings
import com.example.ui.components.LanguageTogglePill
import com.example.ui.theme.AbsentRose
import com.example.ui.theme.AbsentRoseBg
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CeruleanPrimary
import com.example.ui.theme.DeepSchoolNavy
import com.example.ui.theme.FeeAmber
import com.example.ui.theme.FeeAmberBg
import com.example.ui.theme.GoldHighlight
import com.example.ui.theme.InkMuted
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.InkSecondary
import com.example.ui.theme.LoginSkyBackdrop
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenBg
import com.example.ui.theme.PureCardWhite
import com.example.ui.theme.SkyHeaderAccent
import com.example.ui.theme.SoftSkyTint
import com.example.ui.viewmodel.AuthUiState

@Composable
fun LoginScreen(
    strings: AppStrings,
    currentLang: AppLang,
    authState: AuthUiState,
    onSelectLang: (AppLang) -> Unit,
    onServerUrlChange: (String) -> Unit,
    onMobileChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onChangeMobile: () -> Unit,
    onUseDemoAccount: () -> Unit
) {
    var showServerEditor by rememberSaveable {
        mutableStateOf(true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(LoginSkyBackdrop, Color(0xFFEAF5FD), PureCardWhite)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: School Brand + Language Pill
            Row(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(listOf(SkyHeaderAccent, DeepSchoolNavy))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = strings.schoolName,
                            tint = PureCardWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.schoolName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepSchoolNavy
                        )
                        Text(
                            text = "OneBook School",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }
                LanguageTogglePill(
                    currentLang = currentLang,
                    onSelectLang = onSelectLang,
                    darkSurface = false
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Friendly Family & School Illustration Banner (inspired by reference 3)
            FamilyWelcomeIllustration(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .height(145.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main White Login Card
            Card(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PureCardWhite),
                border = BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = strings.loginWelcomeTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = DeepSchoolNavy
                    )
                    Text(
                        text = strings.loginSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary
                    )

                    // Collapsible Frappe Server URL Configuration
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SoftSkyTint.copy(alpha = 0.65f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudQueue,
                                        contentDescription = strings.serverConfigToggle,
                                        tint = CeruleanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = authState.serverUrl.ifBlank { strings.serverConfigToggle },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = DeepSchoolNavy
                                    )
                                }
                                TextButton(
                                    onClick = { showServerEditor = !showServerEditor },
                                    modifier = Modifier.testTag("toggle_server_url_button")
                                ) {
                                    Text(if (showServerEditor) strings.close else "Edit")
                                }
                            }
                            if (showServerEditor) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = authState.serverUrl,
                                    onValueChange = onServerUrlChange,
                                    label = { Text(strings.serverUrlLabel) },
                                    placeholder = { Text(strings.serverUrlHint) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("server_url_input")
                                )
                            }
                        }
                    }

                    if (!authState.otpSent) {
                        OutlinedTextField(
                            value = authState.mobile,
                            onValueChange = onMobileChange,
                            label = { Text(strings.mobileLabel) },
                            placeholder = { Text(strings.mobileHint) },
                            leadingIcon = {
                                Icon(Icons.Filled.PhoneAndroid, contentDescription = null)
                            },
                            prefix = { Text("+91 ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mobile_input")
                        )

                        Button(
                            onClick = onSendOtp,
                            enabled = !authState.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CeruleanPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("send_otp_button")
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(
                                    color = PureCardWhite,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(strings.sendOtp, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PresentGreenBg
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${strings.otpSentTo} +91 ${authState.mobile}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PresentGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(
                                    onClick = onChangeMobile,
                                    modifier = Modifier.testTag("change_mobile_button")
                                ) {
                                    Text(strings.changeMobile)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = authState.otp,
                            onValueChange = onOtpChange,
                            label = { Text(strings.otpLabel) },
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = null)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input")
                        )

                        Button(
                            onClick = onVerifyOtp,
                            enabled = !authState.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CeruleanPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("verify_otp_button")
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(
                                    color = PureCardWhite,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(strings.verifyOtp, style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        OutlinedButton(
                            onClick = onSendOtp,
                            enabled = authState.resendSeconds == 0 && !authState.isLoading,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("resend_otp_button")
                        ) {
                            val label = if (authState.resendSeconds > 0) {
                                "${strings.resendInSeconds} ${authState.resendSeconds}s"
                            } else {
                                strings.resendOtp
                            }
                            Text(label)
                        }
                    }

                    if (!authState.statusMessage.isNullOrBlank() && authState.otpSent) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftSkyTint
                        ) {
                            Text(
                                text = authState.statusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepSchoolNavy,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (!authState.error.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AbsentRoseBg
                        ) {
                            Text(
                                text = authState.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = AbsentRose,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Demo Version Quick Access Banner
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = FeeAmberBg,
                        border = BorderStroke(1.dp, FeeAmber.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (currentLang == AppLang.TA) {
                                    "மாதிரிப் பதிப்பு (Demo Mode)"
                                } else {
                                    "Try Interactive Demo Mode"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = InkPrimary
                            )
                            Text(
                                text = if (currentLang == AppLang.TA) {
                                    "கைபேசி: ${DemoSeedData.DEMO_MOBILE} · OTP: ${DemoSeedData.DEMO_OTP}"
                                } else {
                                    "Demo Mobile: ${DemoSeedData.DEMO_MOBILE}  ·  Demo OTP: ${DemoSeedData.DEMO_OTP}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                            OutlinedButton(
                                onClick = onUseDemoAccount,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("demo_login_button")
                            ) {
                                Text(
                                    text = if (currentLang == AppLang.TA) {
                                        "மாதிரி கணக்கை நிரப்புக (${DemoSeedData.DEMO_MOBILE})"
                                    } else {
                                        "Fill Demo Credentials & OTP"
                                    }
                                )
                            }
                        }
                    }

                    Text(
                        text = strings.connectServerHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun FamilyWelcomeIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(CeruleanPrimary, SkyHeaderAccent)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = h * 0.65f,
                center = Offset(w * 0.85f, h * 0.25f)
            )
            drawCircle(
                color = GoldHighlight.copy(alpha = 0.22f),
                radius = h * 0.35f,
                center = Offset(w * 0.16f, h * 0.78f)
            )
            // Stylized family trio silhouettes (parent + guardian + child)
            // Parent left
            drawCircle(color = PureCardWhite, radius = 16.dp.toPx(), center = Offset(w * 0.38f, h * 0.36f))
            drawRoundRect(
                color = PureCardWhite.copy(alpha = 0.92f),
                topLeft = Offset(w * 0.38f - 18.dp.toPx(), h * 0.50f),
                size = androidx.compose.ui.geometry.Size(36.dp.toPx(), 54.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )
            // Child center
            drawCircle(color = GoldHighlight, radius = 13.dp.toPx(), center = Offset(w * 0.50f, h * 0.48f))
            drawRoundRect(
                color = Color(0xFFFFE082),
                topLeft = Offset(w * 0.50f - 14.dp.toPx(), h * 0.59f),
                size = androidx.compose.ui.geometry.Size(28.dp.toPx(), 42.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            // Parent right
            drawCircle(color = PureCardWhite, radius = 16.dp.toPx(), center = Offset(w * 0.62f, h * 0.38f))
            drawRoundRect(
                color = Color(0xFFBBDEFB),
                topLeft = Offset(w * 0.62f - 18.dp.toPx(), h * 0.52f),
                size = androidx.compose.ui.geometry.Size(36.dp.toPx(), 52.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )
        }
    }
}
