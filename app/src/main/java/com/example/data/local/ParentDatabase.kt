package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val classSection: String,
    val admissionNo: String,
    val classTeacherName: String,
    val teacherOfficeHours: String,
    val todayStatus: String,
    val alertSent: Boolean,
    val termPercentage: Double,
    val apaarConsent: String
)

@Entity(tableName = "attendance_days")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val date: String,
    val status: String,
    val reason: String?,
    val arrivalTime: String?
)

@Entity(tableName = "fee_invoices")
data class FeeInvoiceEntity(
    @PrimaryKey val name: String,
    val studentId: String,
    val studentName: String,
    val classSection: String,
    val title: String,
    val dueDate: String,
    val amount: Double,
    val concessionNote: String?,
    val feeOrderUrl: String?
)

@Entity(tableName = "family_receipts")
data class ReceiptEntity(
    @PrimaryKey val name: String,
    val date: String,
    val amount: Double,
    val mode: String,
    val referenceNo: String,
    val allocationsSummary: String,
    val pdfUrl: String?
)

@Entity(tableName = "circulars")
data class CircularEntity(
    @PrimaryKey val name: String,
    val studentId: String?,
    val title: String,
    val titleTa: String?,
    val body: String,
    val bodyTa: String?,
    val publishedAt: String,
    val priority: String,
    val isRead: Boolean,
    val requiresAck: Boolean,
    val isAcknowledged: Boolean,
    val attachmentUrl: String?
)

@Entity(tableName = "homework")
data class HomeworkEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val course: String,
    val date: String,
    val text: String,
    val attachmentUrl: String?,
    val postedBy: String
)

@Entity(tableName = "report_cards")
data class ReportCardEntity(
    @PrimaryKey val name: String,
    val studentId: String,
    val studentName: String,
    val classSection: String,
    val examName: String,
    val percentage: Double,
    val overallGrade: String,
    val publishedDate: String,
    val subjectsJson: String,
    val remarks: String,
    val pdfUrl: String?
)

@Entity(tableName = "certificates")
data class CertificateEntity(
    @PrimaryKey val name: String,
    val studentId: String,
    val studentName: String,
    val certificateType: String,
    val status: String,
    val requestedDate: String,
    val verifyCode: String?,
    val verifyUrl: String?,
    val pdfUrl: String?,
    val reason: String
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val date: String,
    val title: String,
    val titleTa: String?,
    val category: String,
    val description: String
)

@Entity(tableName = "teacher_messages")
data class TeacherMessageEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val teacherName: String,
    val senderRole: String,
    val body: String,
    val sentAt: String
)

@Entity(tableName = "in_app_alerts")
data class InAppAlertEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val category: String,
    val createdAt: String,
    val isRead: Boolean
)

@Dao
interface ParentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun observeStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudents(items: List<StudentEntity>)

    @Query("DELETE FROM students")
    suspend fun clearStudents()

    @Query("SELECT * FROM attendance_days ORDER BY date DESC")
    fun observeAttendance(): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttendance(items: List<AttendanceEntity>)

    @Query("DELETE FROM attendance_days")
    suspend fun clearAttendance()

    @Query("SELECT * FROM fee_invoices ORDER BY dueDate ASC")
    fun observeInvoices(): Flow<List<FeeInvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInvoices(items: List<FeeInvoiceEntity>)

    @Query("DELETE FROM fee_invoices")
    suspend fun clearInvoices()

    @Query("SELECT * FROM family_receipts ORDER BY date DESC")
    fun observeReceipts(): Flow<List<ReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReceipts(items: List<ReceiptEntity>)

    @Query("DELETE FROM family_receipts")
    suspend fun clearReceipts()

    @Query("SELECT * FROM circulars ORDER BY publishedAt DESC")
    fun observeCirculars(): Flow<List<CircularEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCirculars(items: List<CircularEntity>)

    @Query("UPDATE circulars SET isRead = 1 WHERE name = :name")
    suspend fun markCircularRead(name: String)

    @Query("UPDATE circulars SET isRead = 1, isAcknowledged = 1 WHERE name = :name")
    suspend fun markCircularAcknowledged(name: String)

    @Query("DELETE FROM circulars")
    suspend fun clearCirculars()

    @Query("SELECT * FROM homework ORDER BY date DESC")
    fun observeHomework(): Flow<List<HomeworkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHomework(items: List<HomeworkEntity>)

    @Query("DELETE FROM homework")
    suspend fun clearHomework()

    @Query("SELECT * FROM report_cards ORDER BY publishedDate DESC")
    fun observeReportCards(): Flow<List<ReportCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReportCards(items: List<ReportCardEntity>)

    @Query("DELETE FROM report_cards")
    suspend fun clearReportCards()

    @Query("SELECT * FROM certificates ORDER BY requestedDate DESC")
    fun observeCertificates(): Flow<List<CertificateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCertificates(items: List<CertificateEntity>)

    @Query("DELETE FROM certificates")
    suspend fun clearCertificates()

    @Query("SELECT * FROM calendar_events ORDER BY date ASC")
    fun observeCalendarEvents(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCalendarEvents(items: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events")
    suspend fun clearCalendarEvents()

    @Query("SELECT * FROM teacher_messages ORDER BY sentAt ASC")
    fun observeTeacherMessages(): Flow<List<TeacherMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTeacherMessages(items: List<TeacherMessageEntity>)

    @Query("DELETE FROM teacher_messages")
    suspend fun clearTeacherMessages()

    @Query("SELECT * FROM in_app_alerts ORDER BY createdAt DESC")
    fun observeAlerts(): Flow<List<InAppAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlerts(items: List<InAppAlertEntity>)

    @Query("UPDATE in_app_alerts SET isRead = 1 WHERE category = :category")
    suspend fun markAlertsReadByCategory(category: String)

    @Query("UPDATE in_app_alerts SET isRead = 1")
    suspend fun markAllAlertsRead()

    @Query("DELETE FROM in_app_alerts")
    suspend fun clearAlerts()
}

@Database(
    entities = [
        StudentEntity::class,
        AttendanceEntity::class,
        FeeInvoiceEntity::class,
        ReceiptEntity::class,
        CircularEntity::class,
        HomeworkEntity::class,
        ReportCardEntity::class,
        CertificateEntity::class,
        CalendarEventEntity::class,
        TeacherMessageEntity::class,
        InAppAlertEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ParentDatabase : RoomDatabase() {
    abstract fun parentDao(): ParentDao

    suspend fun clearAllTablesData() {
        val dao = parentDao()
        dao.clearStudents()
        dao.clearAttendance()
        dao.clearInvoices()
        dao.clearReceipts()
        dao.clearCirculars()
        dao.clearHomework()
        dao.clearReportCards()
        dao.clearCertificates()
        dao.clearCalendarEvents()
        dao.clearTeacherMessages()
        dao.clearAlerts()
    }

    companion object {
        @Volatile
        private var INSTANCE: ParentDatabase? = null

        fun getInstance(context: Context): ParentDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ParentDatabase::class.java,
                    "aanoor_parent_cache.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
