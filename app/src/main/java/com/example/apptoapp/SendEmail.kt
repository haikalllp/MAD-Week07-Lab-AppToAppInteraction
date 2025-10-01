package com.example.apptoapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

fun sendEmail(context: Context){

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"   // MIME type for email apps
        putExtra(Intent.EXTRA_EMAIL, arrayOf("someone@example.com")) // recipients
        putExtra(Intent.EXTRA_SUBJECT, "Test Email")
        putExtra(Intent.EXTRA_TEXT, "Hello, this is the email body.")
    }

    try{
        context.startActivity(intent)
    }
    catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app can handle this action.",
            Toast.LENGTH_SHORT).show()
    }
}

