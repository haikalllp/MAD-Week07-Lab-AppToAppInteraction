package com.example.apptoapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

fun openMap(context: Context){
    val intent = Intent(Intent.ACTION_VIEW,
        "geo:37.7749,-122.4194".toUri())
    try{
        context.startActivity(intent)
    }
    catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app can handle this action.",
            Toast.LENGTH_SHORT).show()
    }
}

