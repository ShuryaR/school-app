package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object UpiPaymentHelper {

    fun buildUpiIntent(upiUri: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(upiUri))
    }

    fun hasUpiApp(context: Context, upiUri: String): Boolean {
        val intent = buildUpiIntent(upiUri)
        return intent.resolveActivity(context.packageManager) != null
    }

    fun openExternalUrl(context: Context, rawUrl: String, serverBaseUrl: String): Boolean {
        val resolved = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            rawUrl
        } else {
            val base = serverBaseUrl.trimEnd('/')
            val path = rawUrl.trimStart('/')
            if (base.isNotBlank()) "$base/$path" else rawUrl
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resolved)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
