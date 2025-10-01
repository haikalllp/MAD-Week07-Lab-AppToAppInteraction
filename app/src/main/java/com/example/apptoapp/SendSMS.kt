package com.example.apptoapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toUri

fun sendSMS(context: Context){
    val smsUri = "smsto:1234567890".toUri()
    val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
        putExtra("sms_body", "Hello from my app!")
    }
    try{
        context.startActivity(intent)
    }
    catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app can handle this action.",
            Toast.LENGTH_SHORT).show()
    }

}

