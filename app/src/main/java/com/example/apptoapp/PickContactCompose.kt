package com.example.apptoapp

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.apptoapp.ui.theme.ApptoAppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class PickContactCompose : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApptoAppTheme {
                PickContactScreen()
            }
        }
    }
}

@Composable
fun PickContactScreen() {
    val context = LocalContext.current

    // UI state
    var displayName by remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf<String?>(null) }


    // Launcher: pick a contact (returns a Uri)
    val pickContact = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri: Uri? ->
            if (contactUri == null) {
                Toast.makeText(context, "No contact selected", Toast.LENGTH_SHORT).show()
            } else {
                // Two-step query: Contacts → Phone
                val (name, number) = loadNameAndNumber(context.contentResolver, contactUri)
                displayName = name
                phoneNumber = number
                if (name == null && number == null) {
                    Toast.makeText(context, "No details found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
    // Launcher: request READ_CONTACTS permission
    val requestContactsPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                pickContact.launch(null)
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )


    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                pickContact.launch(null)
            } else {
                requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
            }
        }) {
            Text("Pick a Contact")
        }

        Spacer(Modifier.height(24.dp))

        Text(text = "Name: ${displayName ?: "-"}")
        Text(text = "Number: ${phoneNumber ?: "-"}")
    }
}

/**
 * Queries the Contacts provider for display name and the first phone number for a contact Uri.
 * Returns Pair<name, number>, either may be null.
 */
private fun loadNameAndNumber(
    resolver: android.content.ContentResolver,
    contactUri: Uri
): Pair<String?, String?> {
    var id: String? = null
    var name: String? = null

    // 1) Query Contacts table for _ID and DISPLAY_NAME
    val contactsProjection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME
    )

    resolver.query(contactUri, contactsProjection, null, null, null)?.use { c: Cursor ->
        if (c.moveToFirst()) {
            id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
        }
    }

    // 2) Query Phone table for a number (if we have an ID)
    var number: String? = null
    if (id != null) {
        val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            phoneProjection,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=?",
            arrayOf(id),
            null
        )?.use { pc ->
            if (pc.moveToFirst()) {
                number = pc.getString(
                    pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
            }
        }
    }
    return name to number
}