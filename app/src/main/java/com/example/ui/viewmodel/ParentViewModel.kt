package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ParentDatabase
import com.example.data.local.ParentPrefsStore
import com.example.data.local.PersistedSessionState
import com.example.data.model.AttendanceDay
import com.example.data.model.CalendarEventItem
import com.example.data.model.CertificateItem
import com.example.data.model.CertificateTypeInfo
import com.example.data.model.CircularItem
import com.example.data.model.FamilyReceipt
import com.example.data.model.FeeInvoice
import com.example.data.model.HomeworkItem
import com.example.data.model.InAppAlert
import com.example.data.model.PaymentInitiationResult
import com.example.data.model.ReportCard
import com.example.data.model.StudentInfo
import com.example.data.model.TeacherMessageItem
import com.example.data.remote.FrappeClientProvider
import com.example.data.repository.ParentRepository
import com.example.i18n.AppLang
import com.example.util.PdfGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AuthUiState(
    val serverUrl: String = "",
    val mobile: String = "",
    val otp: String = "",
    val otpSent: Boolean = false,
    val resendSeconds: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val statusMessage: String? = null
)

data class MainUiState(
    val session: PersistedSessionState = PersistedSessionState(),
    val selectedStudentId: String? = null, // null means "All"
    val students: List<StudentInfo> = emptyList(),
    val attendanceDays: List<AttendanceDay> = emptyList(),
    val invoices: List<FeeInvoice> = emptyList(),
    val selectedInvoiceIds: Set<String> = emptySet(),
    val receipts: List<FamilyReceipt> = emptyList(),
    val circulars: List<CircularItem> = emptyList(),
    val homework: List<HomeworkItem> = emptyList(),
    val reportCards: List<ReportCard> = emptyList(),
    val certificates: List<CertificateItem> = emptyList(),
    val certificateTypes: List<CertificateTypeInfo> = listOf(
        CertificateTypeInfo("Bonafide", "Bonafide Certificate", "நன்னடத்தைச் சான்றிதழ்"),
        CertificateTypeInfo("Transfer Certificate", "Transfer Certificate (TC)", "மாற்றுச் சான்றிதழ் (TC)")
    ),
    val calendarEvents: List<CalendarEventItem> = emptyList(),
    val teacherMessages: List<TeacherMessageItem> = emptyList(),
    val alerts: List<InAppAlert> = emptyList(),
    val latestPaymentResult: PaymentInitiationResult? = null,
    val pendingUpiLaunchUri: String? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val bannerMessage: String? = null
) {
    val lastUpdatedFormatted: String?
        get() = if (session.lastSyncMillis > 0L) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(session.lastSyncMillis))
        } else null

    val unreadCircularCount: Int
        get() = circulars.count { !it.isRead }

    val unreadAlertsCount: Int
        get() = alerts.count { !it.isRead }

    val totalFeesDue: Double
        get() = invoices
            .filter { selectedStudentId == null || it.studentId == selectedStudentId }
            .sumOf { it.amount }

    val selectedFeesAmount: Double
        get() = invoices
            .filter { it.name in selectedInvoiceIds }
            .sumOf { it.amount }
}

class ParentViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = ParentPrefsStore(application)
    private val db = ParentDatabase.getInstance(application)
    private val clientPair = FrappeClientProvider.create(prefs)
    private val repository = ParentRepository(clientPair.first, clientPair.second, db, prefs)

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState

    private val _selectedStudentId = MutableStateFlow<String?>(null)
    private val _selectedInvoiceIds = MutableStateFlow<Set<String>>(emptySet())
    private val _certificateTypes = MutableStateFlow(
        listOf(
            CertificateTypeInfo("Bonafide", "Bonafide Certificate", "நன்னடத்தைச் சான்றிதழ்"),
            CertificateTypeInfo("Transfer Certificate", "Transfer Certificate (TC)", "மாற்றுச் சான்றிதழ் (TC)")
        )
    )
    private val _paymentResult = MutableStateFlow<PaymentInitiationResult?>(null)
    private val _pendingUpiUri = MutableStateFlow<String?>(null)
    private val _isSyncing = MutableStateFlow(false)
    private val _syncError = MutableStateFlow<String?>(null)
    private val _bannerMessage = MutableStateFlow<String?>(null)

    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            val current = prefs.current()
            _authState.update {
                it.copy(
                    serverUrl = current.serverBaseUrl,
                    mobile = current.guardian.mobile
                )
            }
            if (current.isLoggedIn && current.serverBaseUrl.isNotBlank()) {
                refreshHome()
            }
        }
    }

    private data class CoreFlows(
        val session: PersistedSessionState,
        val students: List<StudentInfo>,
        val attendance: List<AttendanceDay>,
        val invoices: List<FeeInvoice>,
        val receipts: List<FamilyReceipt>
    )

    private data class ContentFlows(
        val circulars: List<CircularItem>,
        val homework: List<HomeworkItem>,
        val reportCards: List<ReportCard>,
        val certificates: List<CertificateItem>,
        val calendarEvents: List<CalendarEventItem>
    )

    private data class AuxFlows(
        val messages: List<TeacherMessageItem>,
        val alerts: List<InAppAlert>,
        val selectedStudentId: String?,
        val selectedInvoices: Set<String>,
        val certTypes: List<CertificateTypeInfo>
    )

    private data class EphemeralFlows(
        val paymentResult: PaymentInitiationResult?,
        val pendingUpiUri: String?,
        val isSyncing: Boolean,
        val syncError: String?,
        val bannerMessage: String?
    )

    private val coreFlow = combine(
        repository.sessionFlow,
        repository.studentsFlow,
        repository.attendanceFlow,
        repository.invoicesFlow,
        repository.receiptsFlow
    ) { session, students, attendance, invoices, receipts ->
        CoreFlows(session, students, attendance, invoices, receipts)
    }

    private val contentFlow = combine(
        repository.circularsFlow,
        repository.homeworkFlow,
        repository.reportCardsFlow,
        repository.certificatesFlow,
        repository.calendarEventsFlow
    ) { circulars, homework, reportCards, certificates, calendar ->
        ContentFlows(circulars, homework, reportCards, certificates, calendar)
    }

    private val auxFlow = combine(
        repository.teacherMessagesFlow,
        repository.alertsFlow,
        _selectedStudentId,
        _selectedInvoiceIds,
        _certificateTypes
    ) { messages, alerts, studentId, selectedInvoices, certTypes ->
        AuxFlows(messages, alerts, studentId, selectedInvoices, certTypes)
    }

    private val ephemeralFlow = combine(
        _paymentResult,
        _pendingUpiUri,
        _isSyncing,
        _syncError,
        _bannerMessage
    ) { paymentResult, upiUri, syncing, syncError, banner ->
        EphemeralFlows(paymentResult, upiUri, syncing, syncError, banner)
    }

    val uiState: StateFlow<MainUiState> = combine(
        coreFlow,
        contentFlow,
        auxFlow,
        ephemeralFlow
    ) { core, content, aux, eph ->
        val defaultInvoices = if (aux.selectedInvoices.isEmpty() && core.invoices.isNotEmpty()) {
            core.invoices.map { it.name }.toSet()
        } else {
            aux.selectedInvoices
        }
        MainUiState(
            session = core.session,
            selectedStudentId = aux.selectedStudentId,
            students = core.students,
            attendanceDays = core.attendance,
            invoices = core.invoices,
            selectedInvoiceIds = defaultInvoices,
            receipts = core.receipts,
            circulars = content.circulars,
            homework = content.homework,
            reportCards = content.reportCards,
            certificates = content.certificates,
            certificateTypes = aux.certTypes,
            calendarEvents = content.calendarEvents,
            teacherMessages = aux.messages,
            alerts = aux.alerts,
            latestPaymentResult = eph.paymentResult,
            pendingUpiLaunchUri = eph.pendingUpiUri,
            isSyncing = eph.isSyncing,
            syncError = eph.syncError,
            bannerMessage = eph.bannerMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun updateServerUrlInput(url: String) {
        _authState.update { it.copy(serverUrl = url, error = null) }
    }

    fun updateMobileInput(mobile: String) {
        val cleaned = mobile.filter { it.isDigit() }.take(10)
        _authState.update { it.copy(mobile = cleaned, error = null) }
    }

    fun updateOtpInput(otp: String) {
        val cleaned = otp.filter { it.isDigit() }.take(6)
        _authState.update { it.copy(otp = cleaned, error = null) }
    }

    fun resetOtpStep() {
        countdownJob?.cancel()
        _authState.update { it.copy(otpSent = false, otp = "", resendSeconds = 0, error = null) }
    }

    fun fillDemoAndSendOtp() {
        _authState.update {
            it.copy(
                serverUrl = com.example.data.local.DemoSeedData.DEMO_SERVER_URL,
                mobile = com.example.data.local.DemoSeedData.DEMO_MOBILE,
                otp = com.example.data.local.DemoSeedData.DEMO_OTP,
                otpSent = true,
                statusMessage = "Demo OTP: ${com.example.data.local.DemoSeedData.DEMO_OTP} (Mobile: ${com.example.data.local.DemoSeedData.DEMO_MOBILE})",
                error = null
            )
        }
    }

    fun requestOtp(invalidUrlMsg: String, invalidMobileMsg: String) {
        val state = _authState.value
        val url = state.serverUrl.trim()
        val isDemoAttempt = url.startsWith("demo://", ignoreCase = true) ||
            url.equals("demo", ignoreCase = true) ||
            (url.isBlank() && state.mobile == com.example.data.local.DemoSeedData.DEMO_MOBILE)
        if (!isDemoAttempt && !url.startsWith("http://") && !url.startsWith("https://")) {
            _authState.update { it.copy(error = invalidUrlMsg) }
            return
        }
        if (state.mobile.length != 10) {
            _authState.update { it.copy(error = invalidMobileMsg) }
            return
        }
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = repository.sendOtp(url, state.mobile)
            result.onSuccess { msg ->
                _authState.update {
                    it.copy(
                        isLoading = false,
                        serverUrl = if (isDemoAttempt) com.example.data.local.DemoSeedData.DEMO_SERVER_URL else url,
                        otpSent = true,
                        statusMessage = msg,
                        error = null
                    )
                }
                startResendCountdown()
            }.onFailure { err ->
                _authState.update {
                    it.copy(isLoading = false, error = err.message)
                }
            }
        }
    }

    private fun startResendCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (sec in 60 downTo 0) {
                _authState.update { it.copy(resendSeconds = sec) }
                if (sec > 0) delay(1000)
            }
        }
    }

    fun verifyOtp(invalidOtpMsg: String, onAuthenticated: (needsConsent: Boolean) -> Unit) {
        val state = _authState.value
        if (state.otp.length != 6) {
            _authState.update { it.copy(error = invalidOtpMsg) }
            return
        }
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            val result = repository.verifyOtp(state.serverUrl.trim(), state.mobile, state.otp)
            result.onSuccess { dto ->
                _authState.update { it.copy(isLoading = false, error = null) }
                refreshHome()
                val needsConsent = dto.consent_required ?: !uiState.value.session.consent.accepted
                onAuthenticated(needsConsent)
            }.onFailure { err ->
                _authState.update { it.copy(isLoading = false, error = err.message) }
            }
        }
    }

    fun submitConsent(alerts: Boolean, optional: Boolean, apaarStatus: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.submitConsent(alerts, optional, apaarStatus)
            _isSyncing.value = false
            onComplete()
            refreshHome()
        }
    }

    fun setLanguage(lang: AppLang) {
        viewModelScope.launch {
            repository.setLanguage(lang, syncRemote = true)
        }
    }

    fun setLargeTextMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setLargeTextMode(enabled)
        }
    }

    fun selectStudent(studentId: String?) {
        _selectedStudentId.value = studentId
    }

    fun toggleInvoiceSelection(invoiceName: String) {
        _selectedInvoiceIds.update { current ->
            val base = if (current.isEmpty()) uiState.value.invoices.map { it.name }.toSet() else current
            if (invoiceName in base) base - invoiceName else base + invoiceName
        }
    }

    fun refreshHome() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            val res = repository.refreshHome()
            res.onFailure { _syncError.value = it.message }
            _isSyncing.value = false
        }
    }

    fun loadAttendance(studentId: String?) {
        val targetId = studentId ?: uiState.value.students.firstOrNull()?.id ?: return
        viewModelScope.launch {
            _isSyncing.value = true
            repository.markAlertsReadForCategory("Absence")
            val res = repository.refreshAttendance(targetId)
            res.onFailure { _syncError.value = it.message }
            _isSyncing.value = false
        }
    }

    fun loadFees() {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.markAlertsReadForCategory("Fee")
            val res = repository.refreshFamilyDues()
            res.onFailure { _syncError.value = it.message }
            _isSyncing.value = false
        }
    }

    fun initiateUpiPayment(onNavigateToResult: () -> Unit) {
        val selected = uiState.value.selectedInvoiceIds.toList()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            val res = repository.initiateFamilyPayment(selected)
            _isSyncing.value = false
            res.onSuccess { payment ->
                _paymentResult.value = payment
                if (!payment.upiUri.isNullOrBlank()) {
                    _pendingUpiUri.value = payment.upiUri
                } else {
                    onNavigateToResult()
                }
            }.onFailure { err ->
                _syncError.value = err.message
            }
        }
    }

    fun consumePendingUpiUri() {
        _pendingUpiUri.value = null
    }

    fun onReturnedFromUpiApp(onNavigateToResult: () -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.refreshFamilyDues()
            _isSyncing.value = false
            onNavigateToResult()
        }
    }

    fun onNoUpiAppInstalled(message: String, onNavigateToResult: () -> Unit) {
        _bannerMessage.value = message
        onNavigateToResult()
    }

    fun loadCirculars() {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.markAlertsReadForCategory("Circular")
            val res = repository.refreshCirculars()
            res.onFailure { _syncError.value = it.message }
            _isSyncing.value = false
        }
    }

    fun openCircular(name: String) {
        viewModelScope.launch {
            repository.markCircularReadLocally(name)
        }
    }

    fun acknowledgeCircular(name: String) {
        viewModelScope.launch {
            val res = repository.acknowledgeCircular(name)
            res.onFailure { _syncError.value = it.message }
        }
    }

    fun loadHomework(studentId: String? = _selectedStudentId.value) {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.refreshHomework(studentId)
            res.onFailure { _syncError.value = it.message }
            _isSyncing.value = false
        }
    }

    fun loadReportCards(studentId: String? = _selectedStudentId.value) {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.markAlertsReadForCategory("Result")
            val res = repository.refreshReportCards(studentId)
            res.onFailure { _syncError.value = it.message }
            _isSyncing.value = false
        }
    }

    fun loadCertificates() {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.markAlertsReadForCategory("Certificate")
            repository.fetchCertificateTypes().onSuccess { types ->
                if (types.isNotEmpty()) _certificateTypes.value = types
            }
            val res = repository.refreshCertificates()
            res.onFailure { _syncError.value = it.message }
            _isSyncing.value = false
        }
    }

    fun submitCertificateRequest(studentId: String, type: String, reason: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.requestCertificate(studentId, type, reason)
            res.onSuccess {
                _bannerMessage.value = "Certificate request submitted."
            }.onFailure {
                _syncError.value = it.message
            }
            _isSyncing.value = false
        }
    }

    fun loadCalendar() {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.refreshCalendar()
            res.onFailure { _syncError.value = it.message }
            _isSyncing.value = false
        }
    }

    fun loadTeacherMessages(studentId: String? = _selectedStudentId.value) {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.markAlertsReadForCategory("Teacher")
            val res = repository.refreshTeacherMessages(studentId)
            res.onFailure { _syncError.value = it.message }
            _isSyncing.value = false
        }
    }

    fun sendTeacherMessage(studentId: String, message: String) {
        val trimmed = message.trim().take(500)
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.sendTeacherMessage(studentId, trimmed)
            res.onFailure { _syncError.value = it.message }
            _isSyncing.value = false
        }
    }

    fun markAllAlertsRead() {
        viewModelScope.launch {
            repository.markAllAlertsRead()
        }
    }

    fun updateApaarConsent(status: String) {
        viewModelScope.launch {
            val res = repository.updateApaarConsent(status)
            res.onFailure { _syncError.value = it.message }
        }
    }

    fun updateNotificationPrefs(push: Boolean, whatsapp: Boolean, sms: Boolean) {
        viewModelScope.launch {
            repository.updateNotificationPreferences(push, whatsapp, sms)
        }
    }

    fun withdrawOptionalConsents() {
        viewModelScope.launch {
            repository.submitConsent(
                alertsAccepted = true,
                optionalAccepted = false,
                apaarStatus = "Refused"
            )
            _bannerMessage.value = "Optional consents withdrawn."
        }
    }

    fun updateServerUrlFromSettings(url: String) {
        viewModelScope.launch {
            repository.setServerUrl(url)
            _authState.update { it.copy(serverUrl = url.trim().trimEnd('/')) }
            refreshHome()
        }
    }

    fun submitRecordCorrection(studentId: String, field: String, details: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.requestRecordCorrection(studentId, field, details)
            res.onSuccess {
                _bannerMessage.value = "Correction request sent to school office."
            }.onFailure {
                _syncError.value = it.message
            }
            _isSyncing.value = false
        }
    }

    fun exportMyData(onUriReady: (Uri) -> Unit) {
        viewModelScope.launch {
            val json = repository.buildDpdpDataExportJson()
            val uri = PdfGenerator.exportDpdpJsonFile(getApplication(), json)
            onUriReady(uri)
        }
    }

    fun createReceiptPdf(receipt: FamilyReceipt): Uri {
        val state = uiState.value.session
        return PdfGenerator.generateReceiptPdf(
            context = getApplication(),
            schoolName = state.schoolBranding.schoolName,
            guardianName = state.guardian.name.ifBlank { "Parent" },
            receipt = receipt
        )
    }

    fun createReportCardPdf(card: ReportCard): Uri {
        val state = uiState.value.session
        return PdfGenerator.generateReportCardPdf(
            context = getApplication(),
            schoolName = state.schoolBranding.schoolName,
            card = card
        )
    }

    fun createCertificatePdf(cert: CertificateItem): Uri {
        val state = uiState.value.session
        return PdfGenerator.generateCertificatePdf(
            context = getApplication(),
            schoolName = state.schoolBranding.schoolName,
            certificate = cert
        )
    }

    fun dismissMessages() {
        _syncError.value = null
        _bannerMessage.value = null
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            _authState.update {
                AuthUiState(serverUrl = it.serverUrl)
            }
            onLoggedOut()
        }
    }
}
