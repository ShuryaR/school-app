package com.example.data.model

data class SchoolBranding(
    val schoolName: String = "Aanoor Vidyalaya",
    val schoolNameTamil: String = "ஆனூர் வித்யாலயா",
    val appShortName: String = "Aanoor",
    val logoUrl: String? = null,
    val feeOrderUrl: String? = null
)

data class GuardianProfile(
    val name: String = "",
    val mobile: String = "",
    val relation: String = "Parent",
    val obsLanguage: String = "en",
    val obsApaarConsent: String = "Pending" // Pending, Given, Refused
)

data class StudentInfo(
    val id: String,
    val name: String,
    val classSection: String,
    val admissionNo: String = "",
    val classTeacherName: String = "",
    val teacherOfficeHours: String = "Mon–Fri, 3:30 PM – 5:00 PM",
    val todayStatus: String = "Unknown", // Present, Absent, Holiday, Unknown
    val alertSent: Boolean = false,
    val termPercentage: Double = 0.0,
    val apaarConsent: String = "Pending"
)

data class AttendanceDay(
    val id: String,
    val studentId: String,
    val date: String, // YYYY-MM-DD
    val status: String, // Present, Absent, Holiday
    val reason: String? = null,
    val arrivalTime: String? = null
)

data class FeeInvoice(
    val name: String,
    val studentId: String,
    val studentName: String,
    val classSection: String,
    val title: String,
    val dueDate: String,
    val amount: Double,
    val concessionNote: String? = null,
    val feeOrderUrl: String? = null
)

data class FamilyReceipt(
    val name: String,
    val date: String,
    val amount: Double,
    val mode: String = "UPI",
    val referenceNo: String = "",
    val allocationsSummary: String = "",
    val pdfUrl: String? = null
)

data class PaymentInitiationResult(
    val status: String, // initiated, paid, pending, failed
    val upiUri: String? = null,
    val paymentRequestId: String = "",
    val receiptNo: String? = null,
    val amount: Double = 0.0,
    val allocationsSummary: String = ""
)

data class CircularItem(
    val name: String,
    val studentId: String? = null,
    val title: String,
    val titleTa: String? = null,
    val body: String,
    val bodyTa: String? = null,
    val publishedAt: String,
    val priority: String = "Normal", // High, Medium, Normal
    val isRead: Boolean = false,
    val requiresAck: Boolean = false,
    val isAcknowledged: Boolean = false,
    val attachmentUrl: String? = null
)

data class HomeworkItem(
    val id: String,
    val studentId: String,
    val studentName: String,
    val course: String,
    val date: String,
    val text: String,
    val attachmentUrl: String? = null,
    val postedBy: String = ""
)

data class SubjectMark(
    val subject: String,
    val marksObtained: Double,
    val maxMarks: Double,
    val grade: String
)

data class ReportCard(
    val name: String,
    val studentId: String,
    val studentName: String,
    val classSection: String,
    val examName: String,
    val percentage: Double,
    val overallGrade: String,
    val publishedDate: String,
    val subjects: List<SubjectMark>,
    val remarks: String = "",
    val pdfUrl: String? = null
)

data class CertificateTypeInfo(
    val name: String,
    val label: String,
    val labelTa: String? = null
)

data class CertificateItem(
    val name: String,
    val studentId: String,
    val studentName: String,
    val certificateType: String,
    val status: String, // Requested, Processing, Issued
    val requestedDate: String,
    val verifyCode: String? = null,
    val verifyUrl: String? = null,
    val pdfUrl: String? = null,
    val reason: String = ""
)

data class CalendarEventItem(
    val id: String,
    val date: String,
    val title: String,
    val titleTa: String? = null,
    val category: String, // Holiday, Exam, Event
    val description: String = ""
)

data class TeacherMessageItem(
    val id: String,
    val studentId: String,
    val teacherName: String,
    val senderRole: String, // Parent, Teacher
    val body: String,
    val sentAt: String
)

data class ConsentPreferences(
    val noticeVersion: String = "v1.0",
    val accepted: Boolean = false,
    val essentialAccepted: Boolean = true,
    val alertsAccepted: Boolean = true,
    val optionalAccepted: Boolean = false,
    val apaarConsent: String = "Pending"
)

data class NotificationSettings(
    val pushEnabled: Boolean = true,
    val whatsappEnabled: Boolean = true,
    val smsEnabled: Boolean = true
)

data class InAppAlert(
    val id: String,
    val title: String,
    val body: String,
    val category: String, // Absence, Fee, Circular, Result, Certificate, Teacher
    val createdAt: String,
    val isRead: Boolean = false
)
