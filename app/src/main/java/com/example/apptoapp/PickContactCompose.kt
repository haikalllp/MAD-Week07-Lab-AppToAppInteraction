package com.example.apptoapp

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.apptoapp.ui.theme.ApptoAppTheme
import java.io.IOException

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

    // UI state contactDetails for the contact details object
    var contactDetails by remember { mutableStateOf<ContactDetails?>(null) }

    // Launcher: pick a contact (returns a Uri)
    val pickContact = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri: Uri? ->
            if (contactUri == null) {
                Toast.makeText(context, "No contact selected", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    // Query for all contact details
                    // Load contact details using the provided contactUri and then set the state
                    val details = loadContactDetails(context.contentResolver, contactUri)
                    
                    // Directly update the state with the loaded details
                    contactDetails = details
                    
                    if (details.name == null && details.phoneNumber == null) {
                        Toast.makeText(context, "No details found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error loading contact: ${e.message}", Toast.LENGTH_SHORT).show()
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

        // make a local copy of contactDetails for null-safety
        val details = contactDetails

        Spacer(Modifier.height(24.dp))

        // Contact Photo Section
        // Show the contact photo if available, otherwise show a placeholder
        if (details?.contactImageBitmap != null) {
            Image(
                bitmap = details.contactImageBitmap.asImageBitmap(),
                contentDescription = "Contact Photo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RectangleShape)
            )
        } else {
            // Placeholder when no photo is available
            Spacer(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RectangleShape)
                    .background(Color.LightGray)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Name Section
        Text(
            text = "Name",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = details?.name ?: "Name will appear here",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // Number Section
        Text(
            text = "Number",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = details?.phoneNumber ?: "Number will appear here",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // Email Section
        Text(
            text = "Email",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = details?.email ?: "Email will appear here",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(16.dp))

        // Date of Birth Section
        Text(
            text = "Date of Birth",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = details?.dateOfBirth ?: "Date of Birth will appear here",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // Postal Address Section
        Text(
            text = "Postal Address",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = details?.postalAddress ?: "Postal Address will appear here",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Queries the Contacts provider for all contact details for a contact Uri.
 * Returns ContactDetails object with all available information.
 */
private fun loadContactDetails(
    resolver: android.content.ContentResolver,
    contactUri: Uri
): ContactDetails {
    var id: String? = null
    var name: String? = null
    var photoUri: String? = null

    // 1) Query Contacts table for _ID, DISPLAY_NAME, and PHOTO_URI
    val contactsProjection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.PHOTO_URI,
    )

    resolver.query(contactUri, contactsProjection, null, null, null)?.use { c: Cursor ->
        if (c.moveToFirst()) {
            // Get contact ID to use in subsequent queries
            id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))

            // Get name and photo URI
            name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
            photoUri = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))
        }
    }

    // Initialize all fields to null
    var phoneNumber: String? = null
    var email: String? = null
    var dateOfBirth: String? = null
    var postalAddress: String? = null

    // 2) Query Phone table for a number (if we have an ID)
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
                phoneNumber = pc.getString(
                    pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
            }
        }

        // 3) Query Email table for email address
        val emailProjection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)
        resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            emailProjection,
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID}=?",
            arrayOf(id),
            null
        )?.use { ec ->
            if (ec.moveToFirst()) {
                email = ec.getString(
                    ec.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)
                )
            }
        }

        // 4) Query Event table for birthday
        val eventProjection = arrayOf(
            ContactsContract.CommonDataKinds.Event.START_DATE,
            ContactsContract.CommonDataKinds.Event.TYPE
        )
        resolver.query(
            ContactsContract.Data.CONTENT_URI,
            eventProjection,
            "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=? AND ${ContactsContract.CommonDataKinds.Event.TYPE}=?",
            arrayOf(id, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()),
            null
        )?.use { evc ->
            if (evc.moveToFirst()) {
                dateOfBirth = evc.getString(
                    evc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE)
                )
            }
        }

        // 5) Query Postal Address table
        val postalProjection = arrayOf(
            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
        )
        resolver.query(
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
            postalProjection,
            "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID}=?",
            arrayOf(id),
            null
        )?.use { pac ->
            if (pac.moveToFirst()) {
                postalAddress = pac.getString(
                    pac.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
                )
            }
        }
    }

    // Load contact photo as Bitmap if URI is available
    val contactBitmap: Bitmap? = photoUri?.let { uriString ->
        try {
            val uri = uriString.toUri()
            // Convert URI to Bitmap
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: IOException) {
            null
        }
    }

    // Return all details and set them in ContactDetails object
    return ContactDetails(
        name = name,
        phoneNumber = phoneNumber,
        email = email,
        dateOfBirth = dateOfBirth,
        postalAddress = postalAddress,
        contactImageUri = photoUri,
        contactImageBitmap = contactBitmap
    )
}

@Preview(showBackground = true)
@Composable
fun PickContactScreenPreview() {
    ApptoAppTheme {
        PickContactScreen()
    }
}
