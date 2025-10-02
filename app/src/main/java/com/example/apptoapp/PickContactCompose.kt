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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.apptoapp.ui.theme.ApptoAppTheme
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

// Note: Data structures are defined in PickContact.kt to avoid redeclaration

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
    var contactDetails by remember { mutableStateOf<ContactDetails?>(null) }
    
    // State for expandable sections
    var phoneExpanded by remember { mutableStateOf(true) }
    var emailExpanded by remember { mutableStateOf(true) }
    var addressExpanded by remember { mutableStateOf(true) }

    // Launcher: pick a contact (returns a Uri)
    val pickContact = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        onResult = { contactUri: Uri? ->
            if (contactUri == null) {
                Toast.makeText(context, "No contact selected", Toast.LENGTH_SHORT).show()
            } else {
                // Load all contact details
                val details = loadContactDetails(context.contentResolver, contactUri)
                if (details != null) {
                    contactDetails = details
                } else {
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
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            // Display contact details if available
            contactDetails?.let { details ->
                ContactDetailsCard(
                    details = details,
                    phoneExpanded = phoneExpanded,
                    emailExpanded = emailExpanded,
                    addressExpanded = addressExpanded,
                    onPhoneToggle = { phoneExpanded = !phoneExpanded },
                    onEmailToggle = { emailExpanded = !emailExpanded },
                    onAddressToggle = { addressExpanded = !addressExpanded }
                )
            }
        }
    }
}

@Composable
fun ContactDetailsCard(
    details: ContactDetails,
    phoneExpanded: Boolean,
    emailExpanded: Boolean,
    addressExpanded: Boolean,
    onPhoneToggle: () -> Unit,
    onEmailToggle: () -> Unit,
    onAddressToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Contact name and photo
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contact photo
                if (details.photoUri != null) {
                    ContactPhoto(photoUri = details.photoUri)
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = details.name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Contact name
                Text(
                    text = details.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider()
            
            // Phone numbers section
            if (details.phoneNumbers.isNotEmpty()) {
                ExpandableSection(
                    title = "Phone Numbers (${details.phoneNumbers.size})",
                    expanded = phoneExpanded,
                    onToggle = onPhoneToggle
                ) {
                    Column {
                        details.phoneNumbers.forEach { phone ->
                            Text(
                                text = "${phone.type}: ${phone.number}",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Email addresses section
            if (details.emails.isNotEmpty()) {
                ExpandableSection(
                    title = "Email Addresses (${details.emails.size})",
                    expanded = emailExpanded,
                    onToggle = onEmailToggle
                ) {
                    Column {
                        details.emails.forEach { email ->
                            Text(
                                text = "${email.type}: ${email.address}",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Date of birth section
            details.dateOfBirth?.let { dob ->
                Text(
                    text = "Date of Birth",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = dob,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            // Postal addresses section
            if (details.postalAddresses.isNotEmpty()) {
                ExpandableSection(
                    title = "Addresses (${details.postalAddresses.size})",
                    expanded = addressExpanded,
                    onToggle = onAddressToggle
                ) {
                    Column {
                        details.postalAddresses.forEach { address ->
                            Text(
                                text = "${address.type}:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = address.street,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Text(
                                text = "${address.city}, ${address.state} ${address.postalCode}",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Text(
                                text = address.country,
                                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
        
        if (expanded) {
            content()
        }
        
        Divider()
    }
}

@Composable
fun ContactPhoto(photoUri: String) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    LaunchedEffect(photoUri) {
        try {
            val uri = Uri.parse(photoUri)
            bitmap = BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri)
            )
        } catch (e: Exception) {
            bitmap = null
        }
    }
    
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Contact Photo",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        // Default avatar
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Loads all contact details from the Contacts provider.
 */
private fun loadContactDetails(
    resolver: android.content.ContentResolver,
    contactUri: Uri
): ContactDetails? {
    var id: String? = null
    var name: String? = null
    var photoUri: String? = null

    // 1) Query Contacts table for _ID, DISPLAY_NAME, and PHOTO_URI
    val contactsProjection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Contacts.PHOTO_URI
    )

    resolver.query(contactUri, contactsProjection, null, null, null)?.use { c: Cursor ->
        if (c.moveToFirst()) {
            id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
            photoUri = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))
        }
    }

    // If we have an ID, load the rest of the details
    if (id != null && name != null) {
        // Load phone numbers
        val phoneNumbers = loadPhoneNumbers(resolver, id)
        
        // Load emails
        val emails = loadEmails(resolver, id)
        
        // Load date of birth
        val dateOfBirth = loadDateOfBirth(resolver, id)
        
        // Load postal addresses
        val postalAddresses = loadPostalAddresses(resolver, id)

        return ContactDetails(
            id = id,
            name = name,
            phoneNumbers = phoneNumbers,
            emails = emails,
            dateOfBirth = dateOfBirth,
            postalAddresses = postalAddresses,
            photoUri = photoUri
        )
    }
    
    return null
}

/**
 * Loads phone numbers for a contact.
 */
private fun loadPhoneNumbers(
    resolver: android.content.ContentResolver,
    contactId: String
): List<PhoneNumber> {
    val phoneProjection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.TYPE
    )
    
    val phoneNumbers = mutableListOf<PhoneNumber>()
    
    resolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        phoneProjection,
        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=?",
        arrayOf(contactId),
        null
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            val number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val type = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE))
            val typeLabel = getPhoneTypeLabel(type)
            
            phoneNumbers.add(PhoneNumber(number, typeLabel))
        }
    }
    
    return phoneNumbers
}

/**
 * Loads email addresses for a contact.
 */
private fun loadEmails(
    resolver: android.content.ContentResolver,
    contactId: String
): List<Email> {
    val emailProjection = arrayOf(
        ContactsContract.CommonDataKinds.Email.DATA,
        ContactsContract.CommonDataKinds.Email.TYPE
    )
    
    val emails = mutableListOf<Email>()
    
    resolver.query(
        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
        emailProjection,
        "${ContactsContract.CommonDataKinds.Email.CONTACT_ID}=?",
        arrayOf(contactId),
        null
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            val address = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA))
            val type = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.TYPE))
            val typeLabel = getEmailTypeLabel(type)
            
            emails.add(Email(address, typeLabel))
        }
    }
    
    return emails
}

/**
 * Loads date of birth for a contact.
 */
private fun loadDateOfBirth(
    resolver: android.content.ContentResolver,
    contactId: String
): String? {
    val eventProjection = arrayOf(
        ContactsContract.CommonDataKinds.Event.START_DATE,
        ContactsContract.CommonDataKinds.Event.TYPE
    )
    
    resolver.query(
        ContactsContract.Data.CONTENT_URI,
        eventProjection,
        "${ContactsContract.CommonDataKinds.Event.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=? AND ${ContactsContract.CommonDataKinds.Event.TYPE}=?",
        arrayOf(contactId, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()),
        null
    )?.use { cursor ->
        if (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE))
        }
    }
    
    return null
}

/**
 * Loads postal addresses for a contact.
 */
private fun loadPostalAddresses(
    resolver: android.content.ContentResolver,
    contactId: String
): List<PostalAddress> {
    val addressProjection = arrayOf(
        ContactsContract.CommonDataKinds.StructuredPostal.STREET,
        ContactsContract.CommonDataKinds.StructuredPostal.CITY,
        ContactsContract.CommonDataKinds.StructuredPostal.REGION,
        ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
        ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
        ContactsContract.CommonDataKinds.StructuredPostal.TYPE
    )
    
    val addresses = mutableListOf<PostalAddress>()
    
    resolver.query(
        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
        addressProjection,
        "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID}=?",
        arrayOf(contactId),
        null
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            val street = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.STREET)) ?: ""
            val city = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.CITY)) ?: ""
            val state = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.REGION)) ?: ""
            val postalCode = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)) ?: ""
            val country = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)) ?: ""
            val type = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.TYPE))
            val typeLabel = getAddressTypeLabel(type)
            
            addresses.add(PostalAddress(street, city, state, postalCode, country, typeLabel))
        }
    }
    
    return addresses
}

/**
 * Returns a human-readable label for a phone type.
 */
private fun getPhoneTypeLabel(type: Int): String {
    return when (type) {
        ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "Mobile"
        ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
        else -> "Other"
    }
}

/**
 * Returns a human-readable label for an email type.
 */
private fun getEmailTypeLabel(type: Int): String {
    return when (type) {
        ContactsContract.CommonDataKinds.Email.TYPE_HOME -> "Home"
        ContactsContract.CommonDataKinds.Email.TYPE_WORK -> "Work"
        ContactsContract.CommonDataKinds.Email.TYPE_MOBILE -> "Mobile"
        else -> "Other"
    }
}

/**
 * Returns a human-readable label for an address type.
 */
private fun getAddressTypeLabel(type: Int): String {
    return when (type) {
        ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME -> "Home"
        ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK -> "Work"
        else -> "Other"
    }
}

@Preview(showBackground = true)
@Composable
fun PickContactScreenPreview() {
    ApptoAppTheme {
        PickContactScreen()
    }
}
