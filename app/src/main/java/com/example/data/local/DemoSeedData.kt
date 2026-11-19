package com.example.data.local

import com.example.data.model.SchoolBranding
import com.example.data.model.SubjectMark
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object DemoSeedData {
    const val DEMO_SERVER_URL = "demo://aanoor.onebook.school"
    const val DEMO_MOBILE = "9876543210"
    const val DEMO_OTP = "123456"

    suspend fun seed(dao: ParentDao, prefs: ParentPrefsStore) {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter<List<SubjectMark>>(
            Types.newParameterizedType(List::class.java, SubjectMark::class.java)
        )

        prefs.updateSchoolBranding(
            SchoolBranding(
                schoolName = "Aanoor Vidyalaya",
                schoolNameTamil = "ஆனூர் வித்யாலயா",
                appShortName = "Aanoor",
                feeOrderUrl = "https://aanoor.onebook.school/files/fee-committee-order-2027.pdf"
            )
        )

        dao.clearStudents()
        dao.upsertStudents(
            listOf(
                StudentEntity(
                    id = "STU-KAVIN",
                    name = "Kavin Sundaram",
                    classSection = "VI-A",
                    admissionNo = "AV-2021-0412",
                    classTeacherName = "Ms. Kavitha R",
                    teacherOfficeHours = "Mon–Fri, 3:30 PM – 5:00 PM",
                    todayStatus = "Present",
                    alertSent = false,
                    termPercentage = 94.0,
                    apaarConsent = "Pending"
                ),
                StudentEntity(
                    id = "STU-NILA",
                    name = "Nila Sundaram",
                    classSection = "II-B",
                    admissionNo = "AV-2024-0819",
                    classTeacherName = "Mrs. Priya S",
                    teacherOfficeHours = "Mon–Fri, 3:00 PM – 4:30 PM",
                    todayStatus = "Absent",
                    alertSent = true,
                    termPercentage = 89.0,
                    apaarConsent = "Pending"
                )
            )
        )

        val attendanceList = buildList {
            for (day in 1..30) {
                val dayStr = day.toString().padStart(2, '0')
                val date = "2027-06-$dayStr"
                val isWeekend = day % 7 == 0
                val kavinStatus = when {
                    isWeekend -> "Holiday"
                    day == 14 -> "Absent"
                    else -> "Present"
                }
                add(
                    AttendanceEntity(
                        id = "ATT-K-$dayStr",
                        studentId = "STU-KAVIN",
                        date = date,
                        status = kavinStatus,
                        reason = if (kavinStatus == "Absent") "Fever" else null,
                        arrivalTime = if (kavinStatus == "Present") "08:25 AM" else null
                    )
                )
                val nilaStatus = when {
                    isWeekend -> "Holiday"
                    day == 3 || day == 19 || day == 25 -> "Absent"
                    else -> "Present"
                }
                add(
                    AttendanceEntity(
                        id = "ATT-N-$dayStr",
                        studentId = "STU-NILA",
                        date = date,
                        status = nilaStatus,
                        reason = if (nilaStatus == "Absent") "Family travel / Sick leave" else null,
                        arrivalTime = if (nilaStatus == "Present") "08:30 AM" else null
                    )
                )
            }
        }
        dao.clearAttendance()
        dao.upsertAttendance(attendanceList)

        dao.clearInvoices()
        dao.upsertInvoices(
            listOf(
                FeeInvoiceEntity(
                    name = "INV-2027-0101",
                    studentId = "STU-KAVIN",
                    studentName = "Kavin Sundaram",
                    classSection = "VI-A",
                    title = "Term 1 Tuition Fee",
                    dueDate = "15 Jun 2027",
                    amount = 9000.0,
                    concessionNote = null,
                    feeOrderUrl = "https://aanoor.onebook.school/files/fee-committee-order-2027.pdf"
                ),
                FeeInvoiceEntity(
                    name = "INV-2027-0102",
                    studentId = "STU-KAVIN",
                    studentName = "Kavin Sundaram",
                    classSection = "VI-A",
                    title = "Books & Notebooks Kit",
                    dueDate = "15 Jun 2027",
                    amount = 1800.0,
                    concessionNote = null,
                    feeOrderUrl = "https://aanoor.onebook.school/files/fee-committee-order-2027.pdf"
                ),
                FeeInvoiceEntity(
                    name = "INV-2027-0103",
                    studentId = "STU-NILA",
                    studentName = "Nila Sundaram",
                    classSection = "II-B",
                    title = "Term 1 Tuition Fee",
                    dueDate = "15 Jun 2027",
                    amount = 7600.0,
                    concessionNote = "Sibling concession applied",
                    feeOrderUrl = "https://aanoor.onebook.school/files/fee-committee-order-2027.pdf"
                )
            )
        )

        dao.clearReceipts()
        dao.upsertReceipts(
            listOf(
                ReceiptEntity(
                    name = "FR-0012",
                    date = "12 Apr 2027",
                    amount = 6000.0,
                    mode = "UPI",
                    referenceNo = "UPI-4109827361",
                    allocationsSummary = "Kavin VI-A (₹3,200), Nila II-B (₹2,800)",
                    pdfUrl = null
                )
            )
        )

        dao.clearCirculars()
        dao.upsertCirculars(
            listOf(
                CircularEntity(
                    name = "CIR-2027-01",
                    studentId = null,
                    title = "PTM Saturday — 10:00 AM to 12:30 PM",
                    titleTa = "சனிக்கிழமை பெற்றோர்-ஆசிரியர் கூட்டம் (காலை 10:00)",
                    body = "Parents are requested to meet class teachers in their respective classrooms to review Term 1 readiness and sign the diary.",
                    bodyTa = "முதல் பருவ முன்னேற்றம் குறித்து விவாதிக்க சனிக்கிழமை காலை 10 மணிக்கு வகுப்பு ஆசிரியரைச் சந்திக்கவும்.",
                    publishedAt = "03 Jun 2027",
                    priority = "High",
                    isRead = false,
                    requiresAck = true,
                    isAcknowledged = false,
                    attachmentUrl = "https://aanoor.onebook.school/files/ptm-schedule-june.pdf"
                ),
                CircularEntity(
                    name = "CIR-2027-02",
                    studentId = "STU-NILA",
                    title = "Class II-B Story & Craft Week",
                    titleTa = "II-B வகுப்பு கதை மற்றும் கைவினை வாரம்",
                    body = "Children may bring coloured paper and blunt scissors on Thursday for the classroom craft activity.",
                    bodyTa = "வியாழக்கிழமை கைவினைப் பயிற்சிக்கு வண்ணக் காகிதம் கொண்டு வரவும்.",
                    publishedAt = "02 Jun 2027",
                    priority = "Normal",
                    isRead = false,
                    requiresAck = false,
                    isAcknowledged = false,
                    attachmentUrl = null
                ),
                CircularEntity(
                    name = "CIR-2027-03",
                    studentId = null,
                    title = "School Bus Route Timing Update",
                    titleTa = "பள்ளி பேருந்து நேர மாற்றம்",
                    body = "Morning Route 4 pickup at Gandhi Nagar stop is moved by 5 minutes earlier to 7:40 AM.",
                    bodyTa = "காலை வழித்தடம் 4 காந்தி நகர் நிறுத்தம் 7:40 மணிக்கு மாற்றப்பட்டுள்ளது.",
                    publishedAt = "28 May 2027",
                    priority = "Medium",
                    isRead = true,
                    requiresAck = true,
                    isAcknowledged = true,
                    attachmentUrl = null
                )
            )
        )

        dao.clearHomework()
        dao.upsertHomework(
            listOf(
                HomeworkEntity(
                    id = "HW-101",
                    studentId = "STU-KAVIN",
                    studentName = "Kavin Sundaram",
                    course = "Mathematics",
                    date = "03 Jun 2027",
                    text = "Exercise 3.2 (Fractions): Solve problems 1 to 8 neatly in the homework notebook.",
                    attachmentUrl = "https://aanoor.onebook.school/files/maths-ex-3-2.pdf",
                    postedBy = "Ms. Kavitha R"
                ),
                HomeworkEntity(
                    id = "HW-102",
                    studentId = "STU-KAVIN",
                    studentName = "Kavin Sundaram",
                    course = "Tamil · தமிழ்",
                    date = "03 Jun 2027",
                    text = "திருக்குறள் அதிகாரம் 1: முதல் 5 குறட்பாக்களைப் படித்து எழுதி வரவும்.",
                    attachmentUrl = null,
                    postedBy = "Mr. Arul M"
                ),
                HomeworkEntity(
                    id = "HW-103",
                    studentId = "STU-NILA",
                    studentName = "Nila Sundaram",
                    course = "English",
                    date = "03 Jun 2027",
                    text = "Read Unit 2 'The Little Sparrow' aloud twice and circle 5 new words.",
                    attachmentUrl = null,
                    postedBy = "Mrs. Priya S"
                )
            )
        )

        dao.clearReportCards()
        dao.upsertReportCards(
            listOf(
                ReportCardEntity(
                    name = "RC-2027-KAVIN-T1",
                    studentId = "STU-KAVIN",
                    studentName = "Kavin Sundaram",
                    classSection = "VI-A",
                    examName = "Annual Assessment 2026–27",
                    percentage = 88.4,
                    overallGrade = "A1",
                    publishedDate = "20 Apr 2027",
                    subjectsJson = adapter.toJson(
                        listOf(
                            SubjectMark("Tamil", 91.0, 100.0, "A1"),
                            SubjectMark("English", 86.0, 100.0, "A2"),
                            SubjectMark("Mathematics", 94.0, 100.0, "A1"),
                            SubjectMark("Science", 88.0, 100.0, "A2"),
                            SubjectMark("Social Science", 83.0, 100.0, "A2")
                        )
                    ),
                    remarks = "Very attentive in Mathematics and Science lab activities.",
                    pdfUrl = null
                ),
                ReportCardEntity(
                    name = "RC-2027-NILA-T1",
                    studentId = "STU-NILA",
                    studentName = "Nila Sundaram",
                    classSection = "II-B",
                    examName = "Annual Assessment 2026–27",
                    percentage = 91.0,
                    overallGrade = "A1",
                    publishedDate = "20 Apr 2027",
                    subjectsJson = adapter.toJson(
                        listOf(
                            SubjectMark("Tamil", 92.0, 100.0, "A1"),
                            SubjectMark("English", 90.0, 100.0, "A1"),
                            SubjectMark("Number Work", 93.0, 100.0, "A1"),
                            SubjectMark("EVS", 89.0, 100.0, "A2")
                        )
                    ),
                    remarks = "Cheerful participation in storytelling and reading.",
                    pdfUrl = null
                )
            )
        )

        dao.clearCertificates()
        dao.upsertCertificates(
            listOf(
                CertificateEntity(
                    name = "CERT-2027-001",
                    studentId = "STU-KAVIN",
                    studentName = "Kavin Sundaram",
                    certificateType = "Bonafide",
                    status = "Issued",
                    requestedDate = "15 May 2027",
                    verifyCode = "AV-BON-7741",
                    verifyUrl = "https://aanoor.onebook.school/verify/AV-BON-7741",
                    pdfUrl = null,
                    reason = "Passport / Bank account KYC"
                )
            )
        )

        dao.clearCalendarEvents()
        dao.upsertCalendarEvents(
            listOf(
                CalendarEventEntity(
                    id = "CAL-1",
                    date = "06 Jun 2027",
                    title = "Parent–Teacher Meeting (PTM)",
                    titleTa = "பெற்றோர்–ஆசிரியர் கூட்டம்",
                    category = "Event",
                    description = "10:00 AM – 12:30 PM in respective classrooms."
                ),
                CalendarEventEntity(
                    id = "CAL-2",
                    date = "17 Jun 2027",
                    title = "Muharram — School Holiday",
                    titleTa = "மொஹரம் — பள்ளி விடுமுறை",
                    category = "Holiday",
                    description = "School remains closed."
                ),
                CalendarEventEntity(
                    id = "CAL-3",
                    date = "21 Jul 2027",
                    title = "Periodic Test 1 Begins",
                    titleTa = "முதல் பருவ அலகுத் தேர்வு ஆரம்பம்",
                    category = "Exam",
                    description = "Timetable published in Circulars."
                )
            )
        )

        dao.clearTeacherMessages()
        dao.upsertTeacherMessages(
            listOf(
                TeacherMessageEntity(
                    id = "MSG-1",
                    studentId = "STU-KAVIN",
                    teacherName = "Ms. Kavitha R",
                    senderRole = "Teacher",
                    body = "Vanakkam! Kavin completed his Geometry model well today.",
                    sentAt = "Yesterday, 4:10 PM"
                ),
                TeacherMessageEntity(
                    id = "MSG-2",
                    studentId = "STU-NILA",
                    teacherName = "Mrs. Priya S",
                    senderRole = "Teacher",
                    body = "Nila was marked absent this morning. Hope she is feeling well—please share a note if she needs leave tomorrow.",
                    sentAt = "Today, 9:20 AM"
                )
            )
        )

        dao.clearAlerts()
        dao.upsertAlerts(
            listOf(
                InAppAlertEntity(
                    id = "ALR-1",
                    title = "Absence Alert: Nila Sundaram (II-B)",
                    body = "Marked Absent today (03 Jun). SMS/WhatsApp alert sent.",
                    category = "Absence",
                    createdAt = "Today, 09:15 AM",
                    isRead = false
                ),
                InAppAlertEntity(
                    id = "ALR-2",
                    title = "Term 1 Fee Due: ₹18,400",
                    body = "Due by 15 Jun 2027 for Kavin and Nila.",
                    category = "Fee",
                    createdAt = "Yesterday",
                    isRead = false
                ),
                InAppAlertEntity(
                    id = "ALR-3",
                    title = "Circular: PTM Saturday",
                    body = "Acknowledgement required before Friday.",
                    category = "Circular",
                    createdAt = "03 Jun",
                    isRead = false
                )
            )
        )

        prefs.updateLastSync()
    }
}
