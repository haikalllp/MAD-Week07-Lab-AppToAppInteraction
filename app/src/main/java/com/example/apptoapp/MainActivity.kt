package com.example.apptoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.apptoapp.ui.theme.ApptoAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApptoAppTheme {
               MainScreen()
            }
        }
    }
}


@Composable
fun MainScreen(){
    val context = LocalContext.current
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {

        Button(onClick = {openWeb(context)}) {
            Text("Open Webpage")
        }
        Button(onClick = { dialNumber(context) }) {
            Text("Dial Number")
        }

        Button(onClick = { sendSMS(context) }) {
            Text("Send SMS")
        }

        Button(onClick = { sendEmail(context) }) {
            Text("Send Email")
        }

        Button(onClick = { openMap(context) }) {
            Text("Open Map")
        }

        Button(onClick = {
            val intent = Intent(context, Thumbnail::class.java)
            context.startActivity(intent)
        }) {
            Text("Image Capture ")
        }

        Button(onClick = {
            val intent = Intent(context, ThumbnailCompose::class.java)
            context.startActivity(intent)
        }) {
            Text("Image Capture-Compose ")
        }

        Button(onClick = {
            val intent = Intent(context, PickContact::class.java)
            context.startActivity(intent)
        }) {
            Text("Pick Contact")
        }

        Button(onClick = {
            val intent = Intent(context, PickContactCompose::class.java)
            context.startActivity(intent)
        }) {
            Text("Pick Contact Compose")
        }
    }
}