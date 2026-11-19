package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.i18n.stringsFor
import com.example.ui.navigation.ParentRoutes
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CertificatesScreen
import com.example.ui.screens.CircularsScreen
import com.example.ui.screens.ConsentScreen
import com.example.ui.screens.ContactTeacherScreen
import com.example.ui.screens.FeesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HomeworkScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PaymentResultScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.theme.AanoorParentTheme
import com.example.ui.theme.CeruleanPrimary
import com.example.ui.theme.IceBackground
import com.example.ui.theme.InkMuted
import com.example.ui.theme.PureCardWhite
import com.example.ui.theme.SoftSkyTint
import com.example.ui.viewmodel.ParentViewModel
import com.example.util.PdfGenerator
import com.example.util.UpiPaymentHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ParentViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val authState by viewModel.authState.collectAsStateWithLifecycle()

            AanoorParentTheme(largeText = uiState.session.largeTextMode) {
                OneBookParentApp(
                    viewModel = viewModel,
                    uiState = uiState,
                    authState = authState
                )
            }
        }
    }
}

private data class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
private fun OneBookParentApp(
    viewModel: ParentViewModel,
    uiState: com.example.ui.viewmodel.MainUiState,
    authState: com.example.ui.viewmodel.AuthUiState
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val lang = uiState.session.language
    val strings = stringsFor(lang)

    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.onReturnedFromUpiApp {
            navController.navigate(ParentRoutes.PAYMENT_RESULT)
        }
    }

    LaunchedEffect(uiState.pendingUpiLaunchUri) {
        val upiUri = uiState.pendingUpiLaunchUri ?: return@LaunchedEffect
        viewModel.consumePendingUpiUri()
        if (UpiPaymentHelper.hasUpiApp(context, upiUri)) {
            upiLauncher.launch(UpiPaymentHelper.buildUpiIntent(upiUri))
        } else {
            viewModel.onNoUpiAppInstalled(strings.noUpiAppFound) {
                navController.navigate(ParentRoutes.PAYMENT_RESULT)
            }
        }
    }

    LaunchedEffect(uiState.bannerMessage) {
        val msg = uiState.bannerMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.dismissMessages()
    }

    val startDest = when {
        !uiState.session.isLoggedIn -> ParentRoutes.LOGIN
        !uiState.session.consent.accepted -> ParentRoutes.CONSENT
        else -> ParentRoutes.HOME
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelRoutes = setOf(
        ParentRoutes.HOME,
        ParentRoutes.FEES,
        ParentRoutes.CIRCULARS,
        ParentRoutes.MORE
    )
    val showMainNav = currentRoute in topLevelRoutes

    val navTabs = listOf(
        BottomNavTab(ParentRoutes.HOME, strings.navHome, Icons.Filled.Home, "nav_home"),
        BottomNavTab(ParentRoutes.FEES, strings.navFees, Icons.Filled.AccountBalanceWallet, "nav_fees"),
        BottomNavTab(ParentRoutes.CIRCULARS, strings.navCirculars, Icons.Filled.Campaign, "nav_circulars"),
        BottomNavTab(ParentRoutes.MORE, strings.navMore, Icons.Filled.GridView, "nav_more")
    )

    val navigateTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 680.dp

        Scaffold(
            containerColor = IceBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showMainNav && !isWideScreen) {
                    NavigationBar(
                        containerColor = PureCardWhite,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        navTabs.forEach { tab ->
                            val selected = navBackStackEntry?.destination?.hierarchy?.any {
                                it.route == tab.route
                            } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTab(tab.route) },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CeruleanPrimary,
                                    selectedTextColor = CeruleanPrimary,
                                    indicatorColor = SoftSkyTint,
                                    unselectedIconColor = InkMuted,
                                    unselectedTextColor = InkMuted
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showMainNav && isWideScreen) {
                    NavigationRail(containerColor = PureCardWhite) {
                        navTabs.forEach { tab ->
                            val selected = navBackStackEntry?.destination?.hierarchy?.any {
                                it.route == tab.route
                            } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTab(tab.route) },
                                icon = { Icon(tab.icon, contentDescription = tab.label) },
                                label = { Text(tab.label, maxLines = 1) },
                                modifier = Modifier.testTag("rail_${tab.testTag}")
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = startDest,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ParentRoutes.LOGIN) {
                        LoginScreen(
                            strings = strings,
                            currentLang = lang,
                            authState = authState,
                            onSelectLang = viewModel::setLanguage,
                            onServerUrlChange = viewModel::updateServerUrlInput,
                            onMobileChange = viewModel::updateMobileInput,
                            onOtpChange = viewModel::updateOtpInput,
                            onSendOtp = {
                                viewModel.requestOtp(strings.invalidUrlError, strings.invalidMobileError)
                            },
                            onVerifyOtp = {
                                viewModel.verifyOtp(strings.invalidOtpError) { needsConsent ->
                                    val target = if (needsConsent) ParentRoutes.CONSENT else ParentRoutes.HOME
                                    navController.navigate(target) {
                                        popUpTo(ParentRoutes.LOGIN) { inclusive = true }
                                    }
                                }
                            },
                            onChangeMobile = viewModel::resetOtpStep,
                            onUseDemoAccount = viewModel::fillDemoAndSendOtp
                        )
                    }

                    composable(ParentRoutes.CONSENT) {
                        ConsentScreen(
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onSubmitConsent = { alerts, optional, apaar ->
                                viewModel.submitConsent(alerts, optional, apaar) {
                                    navController.navigate(ParentRoutes.HOME) {
                                        popUpTo(ParentRoutes.CONSENT) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable(ParentRoutes.HOME) {
                        HomeScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onSelectStudent = viewModel::selectStudent,
                            onRefresh = viewModel::refreshHome,
                            onOpenAlerts = { navController.navigate(ParentRoutes.NOTIFICATIONS) },
                            onOpenAttendance = { navController.navigate(ParentRoutes.ATTENDANCE) },
                            onOpenFees = { navigateTab(ParentRoutes.FEES) },
                            onPayWithUpi = {
                                viewModel.initiateUpiPayment {
                                    navController.navigate(ParentRoutes.PAYMENT_RESULT)
                                }
                            },
                            onOpenCirculars = { navigateTab(ParentRoutes.CIRCULARS) },
                            onAcknowledgeCircular = viewModel::acknowledgeCircular,
                            onOpenHomework = { navController.navigate(ParentRoutes.HOMEWORK) },
                            onOpenResults = { navController.navigate(ParentRoutes.RESULTS) },
                            onOpenCertificates = { navController.navigate(ParentRoutes.CERTIFICATES) },
                            onOpenCalendar = { navController.navigate(ParentRoutes.CALENDAR) },
                            onOpenTeacher = { navController.navigate(ParentRoutes.CONTACT_TEACHER) },
                            onOpenProfile = { navController.navigate(ParentRoutes.PROFILE) }
                        )
                    }

                    composable(ParentRoutes.ATTENDANCE) {
                        AttendanceScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onSelectStudent = viewModel::selectStudent,
                            onRefresh = viewModel::loadAttendance,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(ParentRoutes.FEES) {
                        FeesScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onRefresh = viewModel::loadFees,
                            onToggleInvoice = viewModel::toggleInvoiceSelection,
                            onPaySelected = {
                                viewModel.initiateUpiPayment {
                                    navController.navigate(ParentRoutes.PAYMENT_RESULT)
                                }
                            },
                            onOpenReceiptPdf = { receipt ->
                                if (!receipt.pdfUrl.isNullOrBlank()) {
                                    UpiPaymentHelper.openExternalUrl(
                                        context = context,
                                        rawUrl = receipt.pdfUrl,
                                        serverBaseUrl = uiState.session.serverBaseUrl
                                    )
                                } else {
                                    val uri = viewModel.createReceiptPdf(receipt)
                                    PdfGenerator.openPdfUri(context, uri)
                                }
                            }
                        )
                    }

                    composable(ParentRoutes.PAYMENT_RESULT) {
                        PaymentResultScreen(
                            result = uiState.latestPaymentResult,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onDownloadPdf = { receipt ->
                                val uri = viewModel.createReceiptPdf(receipt)
                                PdfGenerator.openPdfUri(context, uri)
                            },
                            onShareWhatsApp = { receipt ->
                                val uri = viewModel.createReceiptPdf(receipt)
                                PdfGenerator.shareToWhatsAppOrChooser(
                                    context = context,
                                    uri = uri,
                                    text = "${strings.schoolName} — ${strings.receiptNoLabel}: ${receipt.name}"
                                )
                            },
                            onBackToFees = {
                                navController.popBackStack(ParentRoutes.FEES, inclusive = false)
                            }
                        )
                    }

                    composable(ParentRoutes.CIRCULARS) {
                        CircularsScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onRefresh = viewModel::loadCirculars,
                            onOpenCircular = viewModel::openCircular,
                            onAcknowledge = viewModel::acknowledgeCircular
                        )
                    }

                    composable(ParentRoutes.HOMEWORK) {
                        HomeworkScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onSelectStudent = viewModel::selectStudent,
                            onRefresh = viewModel::loadHomework,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(ParentRoutes.RESULTS) {
                        ResultsScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onSelectStudent = viewModel::selectStudent,
                            onRefresh = viewModel::loadReportCards,
                            onDownloadReportCardPdf = { card ->
                                if (!card.pdfUrl.isNullOrBlank()) {
                                    UpiPaymentHelper.openExternalUrl(
                                        context = context,
                                        rawUrl = card.pdfUrl,
                                        serverBaseUrl = uiState.session.serverBaseUrl
                                    )
                                } else {
                                    val uri = viewModel.createReportCardPdf(card)
                                    PdfGenerator.openPdfUri(context, uri)
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(ParentRoutes.CERTIFICATES) {
                        CertificatesScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onRefresh = viewModel::loadCertificates,
                            onRequestCertificate = viewModel::submitCertificateRequest,
                            onDownloadCertificatePdf = { cert ->
                                if (!cert.pdfUrl.isNullOrBlank()) {
                                    UpiPaymentHelper.openExternalUrl(
                                        context = context,
                                        rawUrl = cert.pdfUrl,
                                        serverBaseUrl = uiState.session.serverBaseUrl
                                    )
                                } else {
                                    val uri = viewModel.createCertificatePdf(cert)
                                    PdfGenerator.openPdfUri(context, uri)
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(ParentRoutes.CALENDAR) {
                        CalendarScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onRefresh = viewModel::loadCalendar,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(ParentRoutes.CONTACT_TEACHER) {
                        ContactTeacherScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onSelectStudent = viewModel::selectStudent,
                            onRefresh = viewModel::loadTeacherMessages,
                            onSendMessage = viewModel::sendTeacherMessage,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(ParentRoutes.MORE) {
                        MoreScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onOpenAttendance = { navController.navigate(ParentRoutes.ATTENDANCE) },
                            onOpenHomework = { navController.navigate(ParentRoutes.HOMEWORK) },
                            onOpenResults = { navController.navigate(ParentRoutes.RESULTS) },
                            onOpenCertificates = { navController.navigate(ParentRoutes.CERTIFICATES) },
                            onOpenCalendar = { navController.navigate(ParentRoutes.CALENDAR) },
                            onOpenTeacher = { navController.navigate(ParentRoutes.CONTACT_TEACHER) },
                            onOpenNotifications = { navController.navigate(ParentRoutes.NOTIFICATIONS) },
                            onOpenProfile = { navController.navigate(ParentRoutes.PROFILE) }
                        )
                    }

                    composable(ParentRoutes.NOTIFICATIONS) {
                        NotificationsScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onMarkAllRead = viewModel::markAllAlertsRead,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(ParentRoutes.PROFILE) {
                        ProfileScreen(
                            state = uiState,
                            strings = strings,
                            currentLang = lang,
                            onSelectLang = viewModel::setLanguage,
                            onToggleLargeText = viewModel::setLargeTextMode,
                            onUpdateNotificationPrefs = viewModel::updateNotificationPrefs,
                            onUpdateApaarConsent = viewModel::updateApaarConsent,
                            onWithdrawOptionalConsents = viewModel::withdrawOptionalConsents,
                            onExportData = {
                                viewModel.exportMyData { uri ->
                                    PdfGenerator.shareUri(
                                        context = context,
                                        uri = uri,
                                        mimeType = "application/json",
                                        title = strings.downloadMyData
                                    )
                                }
                            },
                            onRequestCorrection = viewModel::submitRecordCorrection,
                            onUpdateServerUrl = viewModel::updateServerUrlFromSettings,
                            onLogout = {
                                viewModel.logout {
                                    navController.navigate(ParentRoutes.LOGIN) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
