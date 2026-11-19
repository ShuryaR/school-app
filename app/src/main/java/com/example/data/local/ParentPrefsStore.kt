package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ConsentPreferences
import com.example.data.model.GuardianProfile
import com.example.data.model.NotificationSettings
import com.example.data.model.SchoolBranding
import com.example.i18n.AppLang
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.parentDataStore: DataStore<Preferences> by preferencesDataStore(name = "aanoor_parent_prefs")

data class PersistedSessionState(
    val serverBaseUrl: String = "",
    val sessionCookie: String = "",
    val isLoggedIn: Boolean = false,
    val isDemoMode: Boolean = false,
    val language: AppLang = AppLang.EN,
    val largeTextMode: Boolean = false,
    val lastSyncMillis: Long = 0L,
    val schoolBranding: SchoolBranding = SchoolBranding(),
    val guardian: GuardianProfile = GuardianProfile(),
    val consent: ConsentPreferences = ConsentPreferences(),
    val notifications: NotificationSettings = NotificationSettings()
)

class ParentPrefsStore(private val context: Context) {

    private object Keys {
        val SERVER_URL = stringPreferencesKey("server_url")
        val SESSION_COOKIE = stringPreferencesKey("session_cookie")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_DEMO_MODE = booleanPreferencesKey("is_demo_mode")
        val LANGUAGE = stringPreferencesKey("obs_language")
        val LARGE_TEXT = booleanPreferencesKey("large_text_mode")
        val LAST_SYNC_MILLIS = longPreferencesKey("last_sync_millis")
        val SCHOOL_NAME = stringPreferencesKey("school_name")
        val SCHOOL_NAME_TA = stringPreferencesKey("school_name_ta")
        val FEE_ORDER_URL = stringPreferencesKey("fee_order_url")
        val GUARDIAN_NAME = stringPreferencesKey("guardian_name")
        val GUARDIAN_MOBILE = stringPreferencesKey("guardian_mobile")
        val GUARDIAN_RELATION = stringPreferencesKey("guardian_relation")
        val APAAR_CONSENT = stringPreferencesKey("obs_apaar_consent")
        val CONSENT_ACCEPTED = booleanPreferencesKey("consent_accepted")
        val CONSENT_VERSION = stringPreferencesKey("consent_version")
        val CONSENT_ALERTS = booleanPreferencesKey("consent_alerts")
        val CONSENT_OPTIONAL = booleanPreferencesKey("consent_optional")
        val PUSH_ENABLED = booleanPreferencesKey("push_enabled")
        val WHATSAPP_ENABLED = booleanPreferencesKey("whatsapp_enabled")
        val SMS_ENABLED = booleanPreferencesKey("sms_enabled")
    }

    val stateFlow: Flow<PersistedSessionState> = context.parentDataStore.data.map { prefs ->
        val langCode = prefs[Keys.LANGUAGE] ?: "en"
        PersistedSessionState(
            serverBaseUrl = prefs[Keys.SERVER_URL] ?: "",
            sessionCookie = prefs[Keys.SESSION_COOKIE] ?: "",
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            isDemoMode = prefs[Keys.IS_DEMO_MODE] ?: false,
            language = if (langCode == "ta") AppLang.TA else AppLang.EN,
            largeTextMode = prefs[Keys.LARGE_TEXT] ?: false,
            lastSyncMillis = prefs[Keys.LAST_SYNC_MILLIS] ?: 0L,
            schoolBranding = SchoolBranding(
                schoolName = prefs[Keys.SCHOOL_NAME] ?: "Aanoor Vidyalaya",
                schoolNameTamil = prefs[Keys.SCHOOL_NAME_TA] ?: "ஆனூர் வித்யாலயா",
                feeOrderUrl = prefs[Keys.FEE_ORDER_URL]
            ),
            guardian = GuardianProfile(
                name = prefs[Keys.GUARDIAN_NAME] ?: "",
                mobile = prefs[Keys.GUARDIAN_MOBILE] ?: "",
                relation = prefs[Keys.GUARDIAN_RELATION] ?: "Parent",
                obsLanguage = langCode,
                obsApaarConsent = prefs[Keys.APAAR_CONSENT] ?: "Pending"
            ),
            consent = ConsentPreferences(
                noticeVersion = prefs[Keys.CONSENT_VERSION] ?: "v1.0",
                accepted = prefs[Keys.CONSENT_ACCEPTED] ?: false,
                essentialAccepted = true,
                alertsAccepted = prefs[Keys.CONSENT_ALERTS] ?: true,
                optionalAccepted = prefs[Keys.CONSENT_OPTIONAL] ?: false,
                apaarConsent = prefs[Keys.APAAR_CONSENT] ?: "Pending"
            ),
            notifications = NotificationSettings(
                pushEnabled = prefs[Keys.PUSH_ENABLED] ?: true,
                whatsappEnabled = prefs[Keys.WHATSAPP_ENABLED] ?: true,
                smsEnabled = prefs[Keys.SMS_ENABLED] ?: true
            )
        )
    }

    suspend fun current(): PersistedSessionState = stateFlow.first()

    suspend fun setServerUrl(url: String) {
        context.parentDataStore.edit { it[Keys.SERVER_URL] = url.trim().trimEnd('/') }
    }

    suspend fun setDemoMode(enabled: Boolean) {
        context.parentDataStore.edit { it[Keys.IS_DEMO_MODE] = enabled }
    }

    suspend fun saveCookie(cookie: String) {
        context.parentDataStore.edit { it[Keys.SESSION_COOKIE] = cookie }
    }

    suspend fun saveLoginSession(mobile: String, guardianName: String, relation: String) {
        context.parentDataStore.edit {
            it[Keys.IS_LOGGED_IN] = true
            it[Keys.GUARDIAN_MOBILE] = mobile
            it[Keys.GUARDIAN_NAME] = guardianName
            it[Keys.GUARDIAN_RELATION] = relation
        }
    }

    suspend fun setLanguage(lang: AppLang) {
        context.parentDataStore.edit { it[Keys.LANGUAGE] = lang.code }
    }

    suspend fun setLargeTextMode(enabled: Boolean) {
        context.parentDataStore.edit { it[Keys.LARGE_TEXT] = enabled }
    }

    suspend fun saveConsent(
        accepted: Boolean,
        alerts: Boolean,
        optional: Boolean,
        apaarStatus: String,
        version: String = "v1.0"
    ) {
        context.parentDataStore.edit {
            it[Keys.CONSENT_ACCEPTED] = accepted
            it[Keys.CONSENT_ALERTS] = alerts
            it[Keys.CONSENT_OPTIONAL] = optional
            it[Keys.APAAR_CONSENT] = apaarStatus
            it[Keys.CONSENT_VERSION] = version
        }
    }

    suspend fun setApaarConsent(status: String) {
        context.parentDataStore.edit { it[Keys.APAAR_CONSENT] = status }
    }

    suspend fun setNotificationSettings(push: Boolean, whatsapp: Boolean, sms: Boolean) {
        context.parentDataStore.edit {
            it[Keys.PUSH_ENABLED] = push
            it[Keys.WHATSAPP_ENABLED] = whatsapp
            it[Keys.SMS_ENABLED] = sms
        }
    }

    suspend fun updateSchoolBranding(branding: SchoolBranding) {
        context.parentDataStore.edit {
            it[Keys.SCHOOL_NAME] = branding.schoolName
            it[Keys.SCHOOL_NAME_TA] = branding.schoolNameTamil
            if (!branding.feeOrderUrl.isNullOrBlank()) {
                it[Keys.FEE_ORDER_URL] = branding.feeOrderUrl
            }
        }
    }

    suspend fun updateLastSync(millis: Long = System.currentTimeMillis()) {
        context.parentDataStore.edit { it[Keys.LAST_SYNC_MILLIS] = millis }
    }

    suspend fun clearSession() {
        context.parentDataStore.edit {
            val preservedUrl = it[Keys.SERVER_URL] ?: ""
            val preservedLang = it[Keys.LANGUAGE] ?: "en"
            val preservedLarge = it[Keys.LARGE_TEXT] ?: false
            it.clear()
            if (!preservedUrl.startsWith("demo://")) {
                it[Keys.SERVER_URL] = preservedUrl
            }
            it[Keys.LANGUAGE] = preservedLang
            it[Keys.LARGE_TEXT] = preservedLarge
        }
    }
}
