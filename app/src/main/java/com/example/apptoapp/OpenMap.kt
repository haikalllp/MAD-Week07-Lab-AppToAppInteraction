package com.example.apptoapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

fun openMap(context: Context){
    val intent = Intent(Intent.ACTION_VIEW,
        "geo:0,0?q=-32.00521012889965, 115.8920247119055(Curtin University)".toUri())
    try{
        context.startActivity(intent)
    }
    catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app can handle this action.",
            Toast.LENGTH_SHORT).show()
    }
}

