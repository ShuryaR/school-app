package com.example.data.remote

import com.example.data.local.ParentPrefsStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class FrappeEnvelope<T>(
    val message: T? = null,
    val exc: String? = null,
    val _server_messages: String? = null
)

data class SendOtpRequest(val mobile: String)
data class VerifyOtpRequest(val mobile: String, val otp: String)

data class AuthResponseDto(
    val ok: Boolean = true,
    val user: String? = null,
    val guardian_name: String? = null,
    val mobile: String? = null,
    val relation: String? = null,
    val obs_language: String? = null,
    val consent_required: Boolean? = null,
    val message: String? = null
)

data class HomePayloadDto(
    val school_name: String? = null,
    val school_name_tamil: String? = null,
    val fee_order_url: String? = null,
    val guardian: GuardianDto? = null,
    val students: List<StudentDto> = emptyList(),
    val attendance_today: List<AttendanceSummaryDto> = emptyList(),
    val fees_due: List<InvoiceDto> = emptyList(),
    val recent_receipts: List<ReceiptDto> = emptyList(),
    val circulars: List<CircularDto> = emptyList(),
    val homework: List<HomeworkDto> = emptyList(),
    val alerts: List<AlertDto> = emptyList()
)

data class GuardianDto(
    val name: String? = null,
    val mobile: String? = null,
    val relation: String? = null,
    val obs_language: String? = null,
    val obs_apaar_consent: String? = null
)

data class StudentDto(
    val name: String,
    val student_name: String? = null,
    val class_section: String? = null,
    val program: String? = null,
    val student_batch_name: String? = null,
    val obs_admission_no: String? = null,
    val obs_class_teacher: String? = null,
    val class_teacher_name: String? = null,
    val office_hours: String? = null,
    val today_status: String? = null,
    val alert_sent: Boolean? = null,
    val term_percentage: Double? = null,
    val obs_apaar_consent: String? = null
)

data class AttendanceSummaryDto(
    val student: String,
    val status: String? = null,
    val alert_sent: Boolean? = null,
    val term_percentage: Double? = null
)

data class AttendanceDayDto(
    val name: String? = null,
    val student: String? = null,
    val date: String,
    val status: String,
    val reason: String? = null,
    val arrival_time: String? = null
)

data class FamilyDuesDto(
    val total_due: Double = 0.0,
    val total_paid: Double = 0.0,
    val fee_order_url: String? = null,
    val invoices: List<InvoiceDto> = emptyList(),
    val receipts: List<ReceiptDto> = emptyList()
)

data class InvoiceDto(
    val name: String,
    val student: String? = null,
    val student_name: String? = null,
    val class_section: String? = null,
    val title: String? = null,
    val due_date: String? = null,
    val outstanding_amount: Double? = null,
    val grand_total: Double? = null,
    val concession_note: String? = null,
    val fee_order_url: String? = null
)

data class ReceiptDto(
    val name: String,
    val posting_date: String? = null,
    val paid_amount: Double? = null,
    val total_amount: Double? = null,
    val mode_of_payment: String? = null,
    val reference_no: String? = null,
    val allocations_summary: String? = null,
    val pdf_url: String? = null
)

data class PayFamilyRequest(val invoices: List<String>)

data class PayFamilyResponseDto(
    val status: String? = null,
    val upi_uri: String? = null,
    val payment_request: String? = null,
    val receipt_no: String? = null,
    val amount: Double? = null,
    val allocations_summary: String? = null
)

data class CircularDto(
    val name: String,
    val student: String? = null,
    val title: String? = null,
    val subject: String? = null,
    val title_ta: String? = null,
    val body: String? = null,
    val content: String? = null,
    val body_ta: String? = null,
    val published_on: String? = null,
    val creation: String? = null,
    val priority: String? = null,
    val is_read: Boolean? = null,
    val requires_acknowledgement: Boolean? = null,
    val acknowledged: Boolean? = null,
    val attachment: String? = null
)

data class HomeworkDto(
    val name: String,
    val student: String? = null,
    val student_name: String? = null,
    val course: String? = null,
    val date: String? = null,
    val text: String? = null,
    val description: String? = null,
    val file: String? = null,
    val posted_by: String? = null
)

data class ReportCardDto(
    val name: String,
    val student: String? = null,
    val student_name: String? = null,
    val class_section: String? = null,
    val exam_name: String? = null,
    val percentage: Double? = null,
    val grade: String? = null,
    val published_date: String? = null,
    val remarks: String? = null,
    val pdf_url: String? = null,
    val subjects: List<SubjectMarkDto> = emptyList()
)

data class SubjectMarkDto(
    val subject: String? = null,
    val course: String? = null,
    val marks_obtained: Double? = null,
    val maximum_marks: Double? = null,
    val grade: String? = null
)

data class CertificateTypeDto(
    val name: String,
    val title: String? = null,
    val title_ta: String? = null
)

data class CertificateRequestDto(
    val name: String,
    val student: String? = null,
    val student_name: String? = null,
    val certificate_type: String? = null,
    val status: String? = null,
    val creation: String? = null,
    val verify_code: String? = null,
    val verify_url: String? = null,
    val pdf_url: String? = null,
    val reason: String? = null
)

data class RequestCertificatePayload(
    val student: String,
    val certificate_type: String,
    val reason: String
)

data class CalendarEventDto(
    val name: String? = null,
    val date: String? = null,
    val starts_on: String? = null,
    val title: String? = null,
    val subject: String? = null,
    val title_ta: String? = null,
    val category: String? = null,
    val event_type: String? = null,
    val description: String? = null
)

data class TeacherMessageDto(
    val name: String? = null,
    val student: String? = null,
    val teacher_name: String? = null,
    val sender_role: String? = null,
    val message: String? = null,
    val body: String? = null,
    val sent_at: String? = null,
    val creation: String? = null
)

data class SendTeacherMessagePayload(
    val student: String,
    val message: String
)

data class ConsentDto(
    val notice_version: String? = null,
    val accepted: Boolean? = null,
    val alerts_accepted: Boolean? = null,
    val optional_accepted: Boolean? = null,
    val obs_apaar_consent: String? = null
)

data class SetConsentPayload(
    val notice_version: String,
    val alerts_accepted: Boolean,
    val optional_accepted: Boolean,
    val obs_apaar_consent: String
)

data class AlertDto(
    val name: String? = null,
    val title: String? = null,
    val body: String? = null,
    val category: String? = null,
    val creation: String? = null,
    val is_read: Boolean? = null
)

data class GenericAckPayload(val name: String)
data class LanguagePayload(val obs_language: String)
data class CorrectionPayload(val student: String, val field_name: String, val details: String)

interface FrappeApiService {
    @POST("api/method/onebook_school.parent.auth.send_otp")
    suspend fun sendOtp(@Body body: SendOtpRequest): Response<FrappeEnvelope<AuthResponseDto>>

    @POST("api/method/onebook_school.parent.auth.verify_otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): Response<FrappeEnvelope<AuthResponseDto>>

    @GET("api/method/onebook_school.parent.api.home")
    suspend fun getHome(): Response<FrappeEnvelope<HomePayloadDto>>

    @GET("api/method/onebook_school.parent.api.get_attendance")
    suspend fun getAttendance(
        @Query("student") studentId: String
    ): Response<FrappeEnvelope<List<AttendanceDayDto>>>

    @GET("api/method/onebook_school.parent.api.family_dues")
    suspend fun getFamilyDues(): Response<FrappeEnvelope<FamilyDuesDto>>

    @POST("api/method/onebook_school.parent.api.pay_family")
    suspend fun payFamily(@Body body: PayFamilyRequest): Response<FrappeEnvelope<PayFamilyResponseDto>>

    @GET("api/method/onebook_school.circulars.get_my_circulars")
    suspend fun getCirculars(): Response<FrappeEnvelope<List<CircularDto>>>

    @POST("api/method/onebook_school.circulars.acknowledge")
    suspend fun acknowledgeCircular(@Body body: GenericAckPayload): Response<FrappeEnvelope<Map<String, Any>>>

    @GET("api/method/onebook_school.parent.api.homework")
    suspend fun getHomework(
        @Query("student") studentId: String? = null
    ): Response<FrappeEnvelope<List<HomeworkDto>>>

    @GET("api/method/onebook_school.parent.api.report_cards")
    suspend fun getReportCards(
        @Query("student") studentId: String? = null
    ): Response<FrappeEnvelope<List<ReportCardDto>>>

    @GET("api/method/onebook_school.portal.certificate_types")
    suspend fun getCertificateTypes(): Response<FrappeEnvelope<List<CertificateTypeDto>>>

    @GET("api/method/onebook_school.portal.my_certificates")
    suspend fun getMyCertificates(): Response<FrappeEnvelope<List<CertificateRequestDto>>>

    @POST("api/method/onebook_school.portal.request_certificate")
    suspend fun requestCertificate(@Body body: RequestCertificatePayload): Response<FrappeEnvelope<CertificateRequestDto>>

    @GET("api/method/onebook_school.parent.api.calendar")
    suspend fun getCalendar(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<FrappeEnvelope<List<CalendarEventDto>>>

    @GET("api/method/onebook_school.parent.api.my_messages")
    suspend fun getTeacherMessages(
        @Query("student") studentId: String? = null
    ): Response<FrappeEnvelope<List<TeacherMessageDto>>>

    @POST("api/method/onebook_school.parent.api.send_teacher_message")
    suspend fun sendTeacherMessage(@Body body: SendTeacherMessagePayload): Response<FrappeEnvelope<TeacherMessageDto>>

    @GET("api/method/onebook_school.parent.api.my_consents")
    suspend fun getMyConsents(): Response<FrappeEnvelope<ConsentDto>>

    @POST("api/method/onebook_school.parent.api.set_consent")
    suspend fun setConsent(@Body body: SetConsentPayload): Response<FrappeEnvelope<ConsentDto>>

    @POST("api/method/onebook_school.parent.api.update_language")
    suspend fun updateLanguage(@Body body: LanguagePayload): Response<FrappeEnvelope<Map<String, Any>>>

    @POST("api/method/onebook_school.parent.api.request_correction")
    suspend fun requestCorrection(@Body body: CorrectionPayload): Response<FrappeEnvelope<Map<String, Any>>>

    @POST("api/method/logout")
    suspend fun logout(): Response<ResponseBody>
}

class PersistedCookieJar(private val prefsStore: ParentPrefsStore) : CookieJar {
    private val memoryCookies = ConcurrentHashMap<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val list = memoryCookies.getOrPut(url.host) { mutableListOf() }
        cookies.forEach { newCookie ->
            list.removeAll { it.name == newCookie.name }
            list.add(newCookie)
        }
        val sidCookie = list.firstOrNull { it.name == "sid" }?.value
        if (!sidCookie.isNullOrBlank()) {
            runBlocking { prefsStore.saveCookie(sidCookie) }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val list = memoryCookies[url.host]?.toMutableList() ?: mutableListOf()
        if (list.none { it.name == "sid" }) {
            val savedSid = runBlocking { prefsStore.current().sessionCookie }
            if (savedSid.isNotBlank()) {
                list.add(
                    Cookie.Builder()
                        .name("sid")
                        .value(savedSid)
                        .domain(url.host)
                        .path("/")
                        .build()
                )
            }
        }
        return list
    }

    fun clear() {
        memoryCookies.clear()
    }
}

class DynamicBaseUrlInterceptor(private val prefsStore: ParentPrefsStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        val configuredUrl = runBlocking { prefsStore.current().serverBaseUrl }
        val targetBase = configuredUrl.toHttpUrlOrNull()
        val updatedRequest = if (targetBase != null) {
            val newUrl = original.url.newBuilder()
                .scheme(targetBase.scheme)
                .host(targetBase.host)
                .port(targetBase.port)
                .build()
            original.newBuilder()
                .url(newUrl)
                .header("Accept", "application/json")
                .build()
        } else {
            original.newBuilder()
                .header("Accept", "application/json")
                .build()
        }
        return chain.proceed(updatedRequest)
    }
}

object FrappeClientProvider {
    fun create(prefsStore: ParentPrefsStore): Pair<FrappeApiService, PersistedCookieJar> {
        val cookieJar = PersistedCookieJar(prefsStore)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val okHttp = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(DynamicBaseUrlInterceptor(prefsStore))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://placeholder.onebook.school/")
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(FrappeApiService::class.java) to cookieJar
    }
}
