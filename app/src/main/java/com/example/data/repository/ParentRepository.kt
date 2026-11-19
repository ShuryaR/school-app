package com.example.data.repository

import com.example.data.local.AttendanceEntity
import com.example.data.local.CalendarEventEntity
import com.example.data.local.CertificateEntity
import com.example.data.local.CircularEntity
import com.example.data.local.DemoSeedData
import com.example.data.local.FeeInvoiceEntity
import com.example.data.local.HomeworkEntity
import com.example.data.local.InAppAlertEntity
import com.example.data.local.ParentDao
import com.example.data.local.ParentDatabase
import com.example.data.local.ParentPrefsStore
import com.example.data.local.PersistedSessionState
import com.example.data.local.ReceiptEntity
import com.example.data.local.ReportCardEntity
import com.example.data.local.StudentEntity
import com.example.data.local.TeacherMessageEntity
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
import com.example.data.model.SchoolBranding
import com.example.data.model.StudentInfo
import com.example.data.model.SubjectMark
import com.example.data.model.TeacherMessageItem
import com.example.data.remote.AuthResponseDto
import com.example.data.remote.CorrectionPayload
import com.example.data.remote.FrappeApiService
import com.example.data.remote.FrappeEnvelope
import com.example.data.remote.GenericAckPayload
import com.example.data.remote.LanguagePayload
import com.example.data.remote.PayFamilyRequest
import com.example.data.remote.PersistedCookieJar
import com.example.data.remote.RequestCertificatePayload
import com.example.data.remote.SendOtpRequest
import com.example.data.remote.SendTeacherMessagePayload
import com.example.data.remote.SetConsentPayload
import com.example.data.remote.VerifyOtpRequest
import com.example.i18n.AppLang
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import retrofit2.Response

class ParentRepository(
    private val api: FrappeApiService,
    private val cookieJar: PersistedCookieJar,
    private val db: ParentDatabase,
    private val prefs: ParentPrefsStore
) {
    private val dao: ParentDao = db.parentDao()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val subjectListType = Types.newParameterizedType(List::class.java, SubjectMark::class.java)
    private val subjectListAdapter = moshi.adapter<List<SubjectMark>>(subjectListType)

    val sessionFlow: Flow<PersistedSessionState> = prefs.stateFlow

    val studentsFlow: Flow<List<StudentInfo>> = dao.observeStudents().map { list ->
        list.map {
            StudentInfo(
                id = it.id,
                name = it.name,
                classSection = it.classSection,
                admissionNo = it.admissionNo,
                classTeacherName = it.classTeacherName,
                teacherOfficeHours = it.teacherOfficeHours,
                todayStatus = it.todayStatus,
                alertSent = it.alertSent,
                termPercentage = it.termPercentage,
                apaarConsent = it.apaarConsent
            )
        }
    }

    val attendanceFlow: Flow<List<AttendanceDay>> = dao.observeAttendance().map { list ->
        list.map {
            AttendanceDay(
                id = it.id,
                studentId = it.studentId,
                date = it.date,
                status = it.status,
                reason = it.reason,
                arrivalTime = it.arrivalTime
            )
        }
    }

    val invoicesFlow: Flow<List<FeeInvoice>> = dao.observeInvoices().map { list ->
        list.map {
            FeeInvoice(
                name = it.name,
                studentId = it.studentId,
                studentName = it.studentName,
                classSection = it.classSection,
                title = it.title,
                dueDate = it.dueDate,
                amount = it.amount,
                concessionNote = it.concessionNote,
                feeOrderUrl = it.feeOrderUrl
            )
        }
    }

    val receiptsFlow: Flow<List<FamilyReceipt>> = dao.observeReceipts().map { list ->
        list.map {
            FamilyReceipt(
                name = it.name,
                date = it.date,
                amount = it.amount,
                mode = it.mode,
                referenceNo = it.referenceNo,
                allocationsSummary = it.allocationsSummary,
                pdfUrl = it.pdfUrl
            )
        }
    }

    val circularsFlow: Flow<List<CircularItem>> = dao.observeCirculars().map { list ->
        list.map {
            CircularItem(
                name = it.name,
                studentId = it.studentId,
                title = it.title,
                titleTa = it.titleTa,
                body = it.body,
                bodyTa = it.bodyTa,
                publishedAt = it.publishedAt,
                priority = it.priority,
                isRead = it.isRead,
                requiresAck = it.requiresAck,
                isAcknowledged = it.isAcknowledged,
                attachmentUrl = it.attachmentUrl
            )
        }
    }

    val homeworkFlow: Flow<List<HomeworkItem>> = dao.observeHomework().map { list ->
        list.map {
            HomeworkItem(
                id = it.id,
                studentId = it.studentId,
                studentName = it.studentName,
                course = it.course,
                date = it.date,
                text = it.text,
                attachmentUrl = it.attachmentUrl,
                postedBy = it.postedBy
            )
        }
    }

    val reportCardsFlow: Flow<List<ReportCard>> = dao.observeReportCards().map { list ->
        list.map {
            val subjects = runCatching { subjectListAdapter.fromJson(it.subjectsJson) }.getOrNull().orEmpty()
            ReportCard(
                name = it.name,
                studentId = it.studentId,
                studentName = it.studentName,
                classSection = it.classSection,
                examName = it.examName,
                percentage = it.percentage,
                overallGrade = it.overallGrade,
                publishedDate = it.publishedDate,
                subjects = subjects,
                remarks = it.remarks,
                pdfUrl = it.pdfUrl
            )
        }
    }

    val certificatesFlow: Flow<List<CertificateItem>> = dao.observeCertificates().map { list ->
        list.map {
            CertificateItem(
                name = it.name,
                studentId = it.studentId,
                studentName = it.studentName,
                certificateType = it.certificateType,
                status = it.status,
                requestedDate = it.requestedDate,
                verifyCode = it.verifyCode,
                verifyUrl = it.verifyUrl,
                pdfUrl = it.pdfUrl,
                reason = it.reason
            )
        }
    }

    val calendarEventsFlow: Flow<List<CalendarEventItem>> = dao.observeCalendarEvents().map { list ->
        list.map {
            CalendarEventItem(
                id = it.id,
                date = it.date,
                title = it.title,
                titleTa = it.titleTa,
                category = it.category,
                description = it.description
            )
        }
    }

    val teacherMessagesFlow: Flow<List<TeacherMessageItem>> = dao.observeTeacherMessages().map { list ->
        list.map {
            TeacherMessageItem(
                id = it.id,
                studentId = it.studentId,
                teacherName = it.teacherName,
                senderRole = it.senderRole,
                body = it.body,
                sentAt = it.sentAt
            )
        }
    }

    val alertsFlow: Flow<List<InAppAlert>> = dao.observeAlerts().map { list ->
        list.map {
            InAppAlert(
                id = it.id,
                title = it.title,
                body = it.body,
                category = it.category,
                createdAt = it.createdAt,
                isRead = it.isRead
            )
        }
    }

    suspend fun setServerUrl(url: String) {
        prefs.setServerUrl(url)
    }

    private fun isDemoServer(url: String, mobile: String = ""): Boolean {
        val trimmed = url.trim()
        return trimmed.startsWith("demo://", ignoreCase = true) ||
            trimmed.equals("demo", ignoreCase = true) ||
            (trimmed.isBlank() && mobile == DemoSeedData.DEMO_MOBILE)
    }

    suspend fun setLanguage(lang: AppLang, syncRemote: Boolean = true) {
        prefs.setLanguage(lang)
        val state = prefs.current()
        if (syncRemote && state.isLoggedIn && !state.isDemoMode && state.serverBaseUrl.isNotBlank()) {
            runCatching { api.updateLanguage(LanguagePayload(lang.code)) }
        }
    }

    suspend fun setLargeTextMode(enabled: Boolean) {
        prefs.setLargeTextMode(enabled)
    }

    suspend fun sendOtp(serverUrl: String, mobile: String): Result<String> {
        val effectiveUrl = if (isDemoServer(serverUrl, mobile)) DemoSeedData.DEMO_SERVER_URL else serverUrl.trim()
        prefs.setServerUrl(effectiveUrl)
        if (isDemoServer(effectiveUrl, mobile)) {
            return if (mobile == DemoSeedData.DEMO_MOBILE) {
                Result.success("Demo OTP: ${DemoSeedData.DEMO_OTP}")
            } else {
                Result.failure(Exception("For Demo Mode, use mobile ${DemoSeedData.DEMO_MOBILE} (OTP: ${DemoSeedData.DEMO_OTP})."))
            }
        }
        return try {
            val response = api.sendOtp(SendOtpRequest(mobile))
            val dto = unwrap(response)
            Result.success(dto.message ?: "OTP sent")
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun verifyOtp(serverUrl: String, mobile: String, otp: String): Result<AuthResponseDto> {
        val effectiveUrl = if (isDemoServer(serverUrl, mobile)) DemoSeedData.DEMO_SERVER_URL else serverUrl.trim()
        prefs.setServerUrl(effectiveUrl)
        if (isDemoServer(effectiveUrl, mobile)) {
            if (mobile != DemoSeedData.DEMO_MOBILE || otp != DemoSeedData.DEMO_OTP) {
                return Result.failure(
                    Exception("Invalid demo credentials. Use Mobile ${DemoSeedData.DEMO_MOBILE} and OTP ${DemoSeedData.DEMO_OTP}.")
                )
            }
            prefs.setDemoMode(true)
            prefs.saveLoginSession(
                mobile = DemoSeedData.DEMO_MOBILE,
                guardianName = "Meenakshi Sundaram",
                relation = "Mother / Guardian"
            )
            DemoSeedData.seed(dao, prefs)
            return Result.success(
                AuthResponseDto(
                    ok = true,
                    user = "demo_parent@aanoor.edu",
                    guardian_name = "Meenakshi Sundaram",
                    mobile = DemoSeedData.DEMO_MOBILE,
                    relation = "Mother / Guardian",
                    obs_language = prefs.current().language.code,
                    consent_required = !prefs.current().consent.accepted
                )
            )
        }
        prefs.setDemoMode(false)
        return try {
            val response = api.verifyOtp(VerifyOtpRequest(mobile, otp))
            val dto = unwrap(response)
            prefs.saveLoginSession(
                mobile = dto.mobile ?: mobile,
                guardianName = dto.guardian_name ?: "Parent",
                relation = dto.relation ?: "Guardian"
            )
            dto.obs_language?.let { langCode ->
                prefs.setLanguage(if (langCode == "ta") AppLang.TA else AppLang.EN)
            }
            // Fetch current consent state if available
            runCatching {
                val consentRes = unwrap(api.getMyConsents())
                prefs.saveConsent(
                    accepted = consentRes.accepted ?: false,
                    alerts = consentRes.alerts_accepted ?: true,
                    optional = consentRes.optional_accepted ?: false,
                    apaarStatus = consentRes.obs_apaar_consent ?: "Pending",
                    version = consentRes.notice_version ?: "v1.0"
                )
            }
            Result.success(dto)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun submitConsent(
        alertsAccepted: Boolean,
        optionalAccepted: Boolean,
        apaarStatus: String
    ): Result<Unit> {
        prefs.saveConsent(
            accepted = true,
            alerts = alertsAccepted,
            optional = optionalAccepted,
            apaarStatus = apaarStatus,
            version = "v1.0"
        )
        val state = prefs.current()
        if (state.isDemoMode) return Result.success(Unit)
        return try {
            if (state.serverBaseUrl.isNotBlank()) {
                unwrap(
                    api.setConsent(
                        SetConsentPayload(
                            notice_version = "v1.0",
                            alerts_accepted = alertsAccepted,
                            optional_accepted = optionalAccepted,
                            obs_apaar_consent = apaarStatus
                        )
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun updateApaarConsent(status: String): Result<Unit> {
        val currentState = prefs.current()
        val current = currentState.consent
        prefs.setApaarConsent(status)
        if (currentState.isDemoMode) return Result.success(Unit)
        return try {
            unwrap(
                api.setConsent(
                    SetConsentPayload(
                        notice_version = current.noticeVersion,
                        alerts_accepted = current.alertsAccepted,
                        optional_accepted = current.optionalAccepted,
                        obs_apaar_consent = status
                    )
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun updateNotificationPreferences(push: Boolean, whatsapp: Boolean, sms: Boolean) {
        prefs.setNotificationSettings(push, whatsapp, sms)
    }

    suspend fun refreshHome(): Result<Unit> {
        val state = prefs.current()
        if (state.isDemoMode) {
            if (studentsFlow.first().isEmpty()) {
                DemoSeedData.seed(dao, prefs)
            } else {
                prefs.updateLastSync()
            }
            return Result.success(Unit)
        }
        val url = state.serverBaseUrl
        if (url.isBlank()) return Result.failure(Exception("Frappe server URL is not configured."))
        return try {
            val payload = unwrap(api.getHome())
            prefs.updateSchoolBranding(
                SchoolBranding(
                    schoolName = payload.school_name ?: "Aanoor Vidyalaya",
                    schoolNameTamil = payload.school_name_tamil ?: "ஆனூர் வித்யாலயா",
                    feeOrderUrl = payload.fee_order_url
                )
            )
            payload.guardian?.let { g ->
                prefs.saveLoginSession(
                    mobile = g.mobile ?: prefs.current().guardian.mobile,
                    guardianName = g.name ?: prefs.current().guardian.name,
                    relation = g.relation ?: "Guardian"
                )
                g.obs_apaar_consent?.let { prefs.setApaarConsent(it) }
            }

            val attendanceByStudent = payload.attendance_today.associateBy { it.student }
            val studentEntities = payload.students.map { s ->
                val att = attendanceByStudent[s.name]
                StudentEntity(
                    id = s.name,
                    name = s.student_name ?: s.name,
                    classSection = s.class_section ?: listOfNotNull(s.program, s.student_batch_name).joinToString(" ").ifBlank { "Student" },
                    admissionNo = s.obs_admission_no ?: "",
                    classTeacherName = s.class_teacher_name ?: s.obs_class_teacher ?: "Class Teacher",
                    teacherOfficeHours = s.office_hours ?: "Mon–Fri, 3:30 PM – 5:00 PM",
                    todayStatus = att?.status ?: s.today_status ?: "Unknown",
                    alertSent = att?.alert_sent ?: s.alert_sent ?: false,
                    termPercentage = att?.term_percentage ?: s.term_percentage ?: 0.0,
                    apaarConsent = s.obs_apaar_consent ?: "Pending"
                )
            }
            dao.clearStudents()
            dao.upsertStudents(studentEntities)

            val invoiceEntities = payload.fees_due.map { inv ->
                FeeInvoiceEntity(
                    name = inv.name,
                    studentId = inv.student ?: "",
                    studentName = inv.student_name ?: "",
                    classSection = inv.class_section ?: "",
                    title = inv.title ?: inv.name,
                    dueDate = inv.due_date ?: "",
                    amount = inv.outstanding_amount ?: inv.grand_total ?: 0.0,
                    concessionNote = inv.concession_note,
                    feeOrderUrl = inv.fee_order_url ?: payload.fee_order_url
                )
            }
            dao.clearInvoices()
            dao.upsertInvoices(invoiceEntities)

            val receiptEntities = payload.recent_receipts.map { rec ->
                ReceiptEntity(
                    name = rec.name,
                    date = rec.posting_date ?: "",
                    amount = rec.paid_amount ?: rec.total_amount ?: 0.0,
                    mode = rec.mode_of_payment ?: "UPI",
                    referenceNo = rec.reference_no ?: "",
                    allocationsSummary = rec.allocations_summary ?: "",
                    pdfUrl = rec.pdf_url
                )
            }
            dao.upsertReceipts(receiptEntities)

            val circularEntities = payload.circulars.map { c ->
                CircularEntity(
                    name = c.name,
                    studentId = c.student,
                    title = c.title ?: c.subject ?: c.name,
                    titleTa = c.title_ta,
                    body = c.body ?: c.content ?: "",
                    bodyTa = c.body_ta,
                    publishedAt = c.published_on ?: c.creation ?: "",
                    priority = c.priority ?: "Normal",
                    isRead = c.is_read ?: false,
                    requiresAck = c.requires_acknowledgement ?: false,
                    isAcknowledged = c.acknowledged ?: false,
                    attachmentUrl = c.attachment
                )
            }
            dao.upsertCirculars(circularEntities)

            val homeworkEntities = payload.homework.map { h ->
                HomeworkEntity(
                    id = h.name,
                    studentId = h.student ?: "",
                    studentName = h.student_name ?: "",
                    course = h.course ?: "General",
                    date = h.date ?: "",
                    text = h.text ?: h.description ?: "",
                    attachmentUrl = h.file,
                    postedBy = h.posted_by ?: ""
                )
            }
            dao.upsertHomework(homeworkEntities)

            val alertEntities = payload.alerts.mapIndexed { idx, a ->
                InAppAlertEntity(
                    id = a.name ?: "alert_$idx",
                    title = a.title ?: "School Alert",
                    body = a.body ?: "",
                    category = a.category ?: "Circular",
                    createdAt = a.creation ?: "",
                    isRead = a.is_read ?: false
                )
            }
            if (alertEntities.isNotEmpty()) {
                dao.upsertAlerts(alertEntities)
            }

            prefs.updateLastSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun refreshAttendance(studentId: String): Result<Unit> {
        if (prefs.current().isDemoMode) {
            prefs.updateLastSync()
            return Result.success(Unit)
        }
        return try {
            val days = unwrap(api.getAttendance(studentId))
            dao.upsertAttendance(
                days.map { d ->
                    AttendanceEntity(
                        id = d.name ?: "${studentId}_${d.date}",
                        studentId = d.student ?: studentId,
                        date = d.date,
                        status = d.status,
                        reason = d.reason,
                        arrivalTime = d.arrival_time
                    )
                }
            )
            prefs.updateLastSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun refreshFamilyDues(): Result<Unit> {
        if (prefs.current().isDemoMode) {
            prefs.updateLastSync()
            return Result.success(Unit)
        }
        return try {
            val dues = unwrap(api.getFamilyDues())
            dao.clearInvoices()
            dao.upsertInvoices(
                dues.invoices.map { inv ->
                    FeeInvoiceEntity(
                        name = inv.name,
                        studentId = inv.student ?: "",
                        studentName = inv.student_name ?: "",
                        classSection = inv.class_section ?: "",
                        title = inv.title ?: inv.name,
                        dueDate = inv.due_date ?: "",
                        amount = inv.outstanding_amount ?: inv.grand_total ?: 0.0,
                        concessionNote = inv.concession_note,
                        feeOrderUrl = inv.fee_order_url ?: dues.fee_order_url
                    )
                }
            )
            dao.upsertReceipts(
                dues.receipts.map { rec ->
                    ReceiptEntity(
                        name = rec.name,
                        date = rec.posting_date ?: "",
                        amount = rec.paid_amount ?: rec.total_amount ?: 0.0,
                        mode = rec.mode_of_payment ?: "UPI",
                        referenceNo = rec.reference_no ?: "",
                        allocationsSummary = rec.allocations_summary ?: "",
                        pdfUrl = rec.pdf_url
                    )
                }
            )
            prefs.updateLastSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun initiateFamilyPayment(invoiceIds: List<String>): Result<PaymentInitiationResult> {
        if (prefs.current().isDemoMode) {
            val currentInvoices = invoicesFlow.first()
            val paidItems = currentInvoices.filter { it.name in invoiceIds }
            val remaining = currentInvoices.filter { it.name !in invoiceIds }
            val paidTotal = paidItems.sumOf { it.amount }
            val receiptNo = "FR-00${13 + receiptsFlow.first().size}"
            val summary = paidItems.joinToString(", ") {
                "${it.studentName.substringBefore(" ")} — ${it.title} (₹${it.amount.toInt()})"
            }
            val dateStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                .format(java.util.Date())
            dao.clearInvoices()
            if (remaining.isNotEmpty()) {
                dao.upsertInvoices(
                    remaining.map {
                        FeeInvoiceEntity(
                            name = it.name,
                            studentId = it.studentId,
                            studentName = it.studentName,
                            classSection = it.classSection,
                            title = it.title,
                            dueDate = it.dueDate,
                            amount = it.amount,
                            concessionNote = it.concessionNote,
                            feeOrderUrl = it.feeOrderUrl
                        )
                    }
                )
            }
            dao.upsertReceipts(
                listOf(
                    ReceiptEntity(
                        name = receiptNo,
                        date = dateStr,
                        amount = paidTotal,
                        mode = "UPI (Demo)",
                        referenceNo = "UPI-DEMO-${System.currentTimeMillis().toString().takeLast(6)}",
                        allocationsSummary = summary,
                        pdfUrl = null
                    )
                )
            )
            return Result.success(
                PaymentInitiationResult(
                    status = "paid",
                    upiUri = null,
                    paymentRequestId = "PR-DEMO-$receiptNo",
                    receiptNo = receiptNo,
                    amount = paidTotal,
                    allocationsSummary = summary
                )
            )
        }
        return try {
            val res = unwrap(api.payFamily(PayFamilyRequest(invoiceIds)))
            Result.success(
                PaymentInitiationResult(
                    status = res.status ?: "initiated",
                    upiUri = res.upi_uri,
                    paymentRequestId = res.payment_request ?: "",
                    receiptNo = res.receipt_no,
                    amount = res.amount ?: 0.0,
                    allocationsSummary = res.allocations_summary ?: ""
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun refreshCirculars(): Result<Unit> {
        if (prefs.current().isDemoMode) {
            prefs.updateLastSync()
            return Result.success(Unit)
        }
        return try {
            val items = unwrap(api.getCirculars())
            dao.upsertCirculars(
                items.map { c ->
                    CircularEntity(
                        name = c.name,
                        studentId = c.student,
                        title = c.title ?: c.subject ?: c.name,
                        titleTa = c.title_ta,
                        body = c.body ?: c.content ?: "",
                        bodyTa = c.body_ta,
                        publishedAt = c.published_on ?: c.creation ?: "",
                        priority = c.priority ?: "Normal",
                        isRead = c.is_read ?: false,
                        requiresAck = c.requires_acknowledgement ?: false,
                        isAcknowledged = c.acknowledged ?: false,
                        attachmentUrl = c.attachment
                    )
                }
            )
            prefs.updateLastSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun markCircularReadLocally(name: String) {
        dao.markCircularRead(name)
    }

    suspend fun acknowledgeCircular(name: String): Result<Unit> {
        dao.markCircularAcknowledged(name)
        if (prefs.current().isDemoMode) return Result.success(Unit)
        return try {
            unwrap(api.acknowledgeCircular(GenericAckPayload(name)))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun refreshHomework(studentId: String? = null): Result<Unit> {
        if (prefs.current().isDemoMode) {
            prefs.updateLastSync()
            return Result.success(Unit)
        }
        return try {
            val list = unwrap(api.getHomework(studentId))
            dao.upsertHomework(
                list.map { h ->
                    HomeworkEntity(
                        id = h.name,
                        studentId = h.student ?: "",
                        studentName = h.student_name ?: "",
                        course = h.course ?: "General",
                        date = h.date ?: "",
                        text = h.text ?: h.description ?: "",
                        attachmentUrl = h.file,
                        postedBy = h.posted_by ?: ""
                    )
                }
            )
            prefs.updateLastSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun refreshReportCards(studentId: String? = null): Result<Unit> {
        if (prefs.current().isDemoMode) {
            prefs.updateLastSync()
            return Result.success(Unit)
        }
        return try {
            val cards = unwrap(api.getReportCards(studentId))
            dao.upsertReportCards(
                cards.map { rc ->
                    val marks = rc.subjects.map { m ->
                        SubjectMark(
                            subject = m.subject ?: m.course ?: "Subject",
                            marksObtained = m.marks_obtained ?: 0.0,
                            maxMarks = m.maximum_marks ?: 100.0,
                            grade = m.grade ?: "-"
                        )
                    }
                    ReportCardEntity(
                        name = rc.name,
                        studentId = rc.student ?: "",
                        studentName = rc.student_name ?: "",
                        classSection = rc.class_section ?: "",
                        examName = rc.exam_name ?: rc.name,
                        percentage = rc.percentage ?: 0.0,
                        overallGrade = rc.grade ?: "-",
                        publishedDate = rc.published_date ?: "",
                        subjectsJson = subjectListAdapter.toJson(marks),
                        remarks = rc.remarks ?: "",
                        pdfUrl = rc.pdf_url
                    )
                }
            )
            prefs.updateLastSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun fetchCertificateTypes(): Result<List<CertificateTypeInfo>> {
        if (prefs.current().isDemoMode) {
            return Result.success(
                listOf(
                    CertificateTypeInfo("Bonafide", "Bonafide Certificate", "நன்னடத்தைச் சான்றிதழ்"),
                    CertificateTypeInfo("Transfer Certificate", "Transfer Certificate (TC)", "மாற்றுச் சான்றிதழ் (TC)")
                )
            )
        }
        return try {
            val list = unwrap(api.getCertificateTypes())
            Result.success(
                list.map {
                    CertificateTypeInfo(
                        name = it.name,
                        label = it.title ?: it.name,
                        labelTa = it.title_ta
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun refreshCertificates(): Result<Unit> {
        if (prefs.current().isDemoMode) {
            prefs.updateLastSync()
            return Result.success(Unit)
        }
        return try {
            val list = unwrap(api.getMyCertificates())
            dao.upsertCertificates(
                list.map { c ->
                    CertificateEntity(
                        name = c.name,
                        studentId = c.student ?: "",
                        studentName = c.student_name ?: "",
                        certificateType = c.certificate_type ?: "Bonafide",
                        status = c.status ?: "Requested",
                        requestedDate = c.creation ?: "",
                        verifyCode = c.verify_code,
                        verifyUrl = c.verify_url,
                        pdfUrl = c.pdf_url,
                        reason = c.reason ?: ""
                    )
                }
            )
            prefs.updateLastSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun requestCertificate(studentId: String, type: String, reason: String): Result<Unit> {
        if (prefs.current().isDemoMode) {
            val studentName = studentsFlow.first().firstOrNull { it.id == studentId }?.name ?: "Student"
            val id = "CERT-2027-${100 + certificatesFlow.first().size}"
            val verifyCode = "AV-${type.take(3).uppercase()}-${(1000..9999).random()}"
            val dateStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                .format(java.util.Date())
            dao.upsertCertificates(
                listOf(
                    CertificateEntity(
                        name = id,
                        studentId = studentId,
                        studentName = studentName,
                        certificateType = type,
                        status = "Requested",
                        requestedDate = dateStr,
                        verifyCode = verifyCode,
                        verifyUrl = "https://aanoor.onebook.school/verify/$verifyCode",
                        pdfUrl = null,
                        reason = reason
                    )
                )
            )
            return Result.success(Unit)
        }
        return try {
            val created = unwrap(api.requestCertificate(RequestCertificatePayload(studentId, type, reason)))
            dao.upsertCertificates(
                listOf(
                    CertificateEntity(
                        name = created.name,
                        studentId = created.student ?: studentId,
                        studentName = created.student_name ?: "",
                        certificateType = created.certificate_type ?: type,
                        status = created.status ?: "Requested",
                        requestedDate = created.creation ?: "",
                        verifyCode = created.verify_code,
                        verifyUrl = created.verify_url,
                        pdfUrl = created.pdf_url,
                        reason = created.reason ?: reason
                    )
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun refreshCalendar(): Result<Unit> {
        if (prefs.current().isDemoMode) {
            prefs.updateLastSync()
            return Result.success(Unit)
        }
        return try {
            val events = unwrap(api.getCalendar())
            dao.upsertCalendarEvents(
                events.mapIndexed { idx, ev ->
                    CalendarEventEntity(
                        id = ev.name ?: "cal_$idx",
                        date = ev.date ?: ev.starts_on ?: "",
                        title = ev.title ?: ev.subject ?: "School Event",
                        titleTa = ev.title_ta,
                        category = ev.category ?: ev.event_type ?: "Event",
                        description = ev.description ?: ""
                    )
                }
            )
            prefs.updateLastSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun refreshTeacherMessages(studentId: String? = null): Result<Unit> {
        if (prefs.current().isDemoMode) {
            prefs.updateLastSync()
            return Result.success(Unit)
        }
        return try {
            val messages = unwrap(api.getTeacherMessages(studentId))
            dao.upsertTeacherMessages(
                messages.mapIndexed { idx, m ->
                    TeacherMessageEntity(
                        id = m.name ?: "msg_$idx",
                        studentId = m.student ?: studentId ?: "",
                        teacherName = m.teacher_name ?: "Class Teacher",
                        senderRole = m.sender_role ?: "Teacher",
                        body = m.message ?: m.body ?: "",
                        sentAt = m.sent_at ?: m.creation ?: ""
                    )
                }
            )
            prefs.updateLastSync()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun sendTeacherMessage(studentId: String, message: String): Result<Unit> {
        if (prefs.current().isDemoMode) {
            val student = studentsFlow.first().firstOrNull { it.id == studentId }
            val teacherName = student?.classTeacherName ?: "Class Teacher"
            val now = System.currentTimeMillis()
            dao.upsertTeacherMessages(
                listOf(
                    TeacherMessageEntity(
                        id = "msg_p_$now",
                        studentId = studentId,
                        teacherName = teacherName,
                        senderRole = "Parent",
                        body = message,
                        sentAt = "Just now"
                    ),
                    TeacherMessageEntity(
                        id = "msg_t_${now + 1}",
                        studentId = studentId,
                        teacherName = teacherName,
                        senderRole = "Teacher",
                        body = "Thank you for your message. $teacherName will reply during office hours (${student?.teacherOfficeHours ?: "3:30–5:00 PM"}).",
                        sentAt = "Auto-ack"
                    )
                )
            )
            return Result.success(Unit)
        }
        return try {
            val sent = unwrap(api.sendTeacherMessage(SendTeacherMessagePayload(studentId, message)))
            dao.upsertTeacherMessages(
                listOf(
                    TeacherMessageEntity(
                        id = sent.name ?: "local_${System.currentTimeMillis()}",
                        studentId = sent.student ?: studentId,
                        teacherName = sent.teacher_name ?: "Class Teacher",
                        senderRole = "Parent",
                        body = sent.message ?: sent.body ?: message,
                        sentAt = sent.sent_at ?: sent.creation ?: "Just now"
                    )
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun markAlertsReadForCategory(category: String) {
        dao.markAlertsReadByCategory(category)
    }

    suspend fun markAllAlertsRead() {
        dao.markAllAlertsRead()
    }

    suspend fun requestRecordCorrection(studentId: String, fieldName: String, details: String): Result<Unit> {
        if (prefs.current().isDemoMode) return Result.success(Unit)
        return try {
            unwrap(api.requestCorrection(CorrectionPayload(studentId, fieldName, details)))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(readableError(e)))
        }
    }

    suspend fun buildDpdpDataExportJson(): String {
        val state = prefs.current()
        val students = studentsFlow.first()
        val receipts = receiptsFlow.first()
        val certificates = certificatesFlow.first()
        return JSONObject().apply {
            put("school", state.schoolBranding.schoolName)
            put("demo_mode", state.isDemoMode)
            put("guardian_name", state.guardian.name)
            put("guardian_mobile", state.guardian.mobile)
            put("language", state.language.code)
            put("apaar_consent", state.consent.apaarConsent)
            put("consent_version", state.consent.noticeVersion)
            put("students_count", students.size)
            put("receipts_count", receipts.size)
            put("certificates_count", certificates.size)
            put("exported_at_epoch_ms", System.currentTimeMillis())
        }.toString(2)
    }

    suspend fun logout() {
        if (!prefs.current().isDemoMode) {
            runCatching { api.logout() }
        }
        cookieJar.clear()
        db.clearAllTablesData()
        prefs.clearSession()
    }

    private fun <T> unwrap(response: Response<FrappeEnvelope<T>>): T {
        if (!response.isSuccessful) {
            val rawErr = response.errorBody()?.string().orEmpty()
            val parsedMsg = runCatching {
                val obj = JSONObject(rawErr)
                obj.optString("message").ifBlank { obj.optString("_server_messages") }
            }.getOrNull()
            throw IllegalStateException(
                parsedMsg?.takeIf { it.isNotBlank() } ?: "Server returned HTTP ${response.code()}"
            )
        }
        val body = response.body() ?: throw IllegalStateException("Empty response from Frappe server")
        return body.message ?: throw IllegalStateException("Missing payload in Frappe response")
    }

    private fun readableError(e: Exception): String {
        val msg = e.message ?: "Network error"
        return when {
            msg.contains("Unable to resolve host", ignoreCase = true) ->
                "Cannot reach Frappe server. Check URL and internet connection."
            msg.contains("timeout", ignoreCase = true) ->
                "School server took too long to respond. Showing cached data if available."
            else -> msg
        }
    }
}
