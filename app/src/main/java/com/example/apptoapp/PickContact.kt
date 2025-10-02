package com.example.apptoapp

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.provider.ContactsContract
import android.graphics.BitmapFactory
import android.util.Base64

// Data structures for enhanced contact information
data class PhoneNumber(
    val number: String,
    val type: String
)

data class Email(
    val address: String,
    val type: String
)

data class PostalAddress(
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String,
    val type: String
)

data class ContactDetails(
    val id: String,
    val name: String,
    val phoneNumbers: List<PhoneNumber>,
    val emails: List<Email>,
    val dateOfBirth: String?,
    val postalAddresses: List<PostalAddress>,
    val photoUri: String?
)

class PickContact : AppCompatActivity() {
    private lateinit var txtName: TextView
    private lateinit var txtNumber: TextView
    private lateinit var contactImage: ImageView
    private lateinit var emailSection: LinearLayout
    private lateinit var phoneSection: LinearLayout
    private lateinit var addressSection: LinearLayout
    private lateinit var dobSection: LinearLayout
    private lateinit var emailHeader: TextView
    private lateinit var phoneHeader: TextView
    private lateinit var addressHeader: TextView
    private lateinit var dobHeader: TextView
    
    // 3a) Permission launcher
    private val requestPermission = registerForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        callback = { granted ->
            if (granted) pickContact.launch(null)
            else Toast.makeText(this, "Permission denied",
                Toast.LENGTH_SHORT).show()
        }
    )
    // 3b) Contact picker launcher
    private val pickContact = registerForActivityResult(
        contract = ActivityResultContracts.PickContact(),
        callback = { contactUri: Uri? ->
            if (contactUri != null) loadNameAndNumber(contactUri) else
                Toast.makeText(this,
                    "No contact selected", Toast.LENGTH_SHORT).show()
        }
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_contact)

        txtName = findViewById<TextView>(R.id.txtName)
        contactImage = findViewById<ImageView>(R.id.contactImage)
        phoneSection = findViewById<LinearLayout>(R.id.phoneSection)
        emailSection = findViewById<LinearLayout>(R.id.emailSection)
        addressSection = findViewById<LinearLayout>(R.id.addressSection)
        dobSection = findViewById<LinearLayout>(R.id.dobSection)
        phoneHeader = findViewById<TextView>(R.id.phoneHeader)
        emailHeader = findViewById<TextView>(R.id.emailHeader)
        addressHeader = findViewById<TextView>(R.id.addressHeader)
        dobHeader = findViewById<TextView>(R.id.dobHeader)

        findViewById<Button>(R.id.btnPickContact).setOnClickListener {
            // If already granted, skip the prompt
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickContact.launch(null)
            } else {
                requestPermission.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    // 3c) Load all contact details
    private fun loadNameAndNumber(contactUri: Uri) {
        val contactDetails = loadContactDetails(contactUri)
        if (contactDetails != null) {
            displayContactDetails(contactDetails)
        }
    }
    
    private fun loadContactDetails(contactUri: Uri): ContactDetails? {
        // Query the Contacts table first to get CONTACT_ID and DISPLAY_NAME
        val contactsProjection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI
        )

        return contentResolver.query(contactUri, contactsProjection,
            null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val contactId = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val displayName = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val photoUri = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))

                // Load phone numbers
                val phoneNumbers = loadPhoneNumbers(contactId)
                
                // Load emails
                val emails = loadEmails(contactId)
                
                // Load date of birth
                val dateOfBirth = loadDateOfBirth(contactId)
                
                // Load postal addresses
                val postalAddresses = loadPostalAddresses(contactId)

                ContactDetails(
                    id = contactId,
                    name = displayName,
                    phoneNumbers = phoneNumbers,
                    emails = emails,
                    dateOfBirth = dateOfBirth,
                    postalAddresses = postalAddresses,
                    photoUri = photoUri
                )
            } else null
        }
    }
    
    private fun loadPhoneNumbers(contactId: String): List<PhoneNumber> {
        val phoneProjection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE
        )
        
        val phoneNumbers = mutableListOf<PhoneNumber>()
        
        contentResolver.query(
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
    
    private fun loadEmails(contactId: String): List<Email> {
        val emailProjection = arrayOf(
            ContactsContract.CommonDataKinds.Email.DATA,
            ContactsContract.CommonDataKinds.Email.TYPE
        )
        
        val emails = mutableListOf<Email>()
        
        contentResolver.query(
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
    
    private fun loadDateOfBirth(contactId: String): String? {
        val eventProjection = arrayOf(
            ContactsContract.CommonDataKinds.Event.START_DATE,
            ContactsContract.CommonDataKinds.Event.TYPE
        )
        
        contentResolver.query(
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
    
    private fun loadPostalAddresses(contactId: String): List<PostalAddress> {
        val addressProjection = arrayOf(
            ContactsContract.CommonDataKinds.StructuredPostal.STREET,
            ContactsContract.CommonDataKinds.StructuredPostal.CITY,
            ContactsContract.CommonDataKinds.StructuredPostal.REGION,
            ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
            ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE
        )
        
        val addresses = mutableListOf<PostalAddress>()
        
        contentResolver.query(
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
    
    private fun getPhoneTypeLabel(type: Int): String {
        return when (type) {
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "Mobile"
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
            else -> "Other"
        }
    }
    
    private fun getEmailTypeLabel(type: Int): String {
        return when (type) {
            ContactsContract.CommonDataKinds.Email.TYPE_HOME -> "Home"
            ContactsContract.CommonDataKinds.Email.TYPE_WORK -> "Work"
            ContactsContract.CommonDataKinds.Email.TYPE_MOBILE -> "Mobile"
            else -> "Other"
        }
    }
    
    private fun getAddressTypeLabel(type: Int): String {
        return when (type) {
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME -> "Home"
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK -> "Work"
            else -> "Other"
        }
    }
    
    private fun displayContactDetails(contactDetails: ContactDetails) {
        // Clear previous data
        clearPreviousData()
        
        // Display basic info
        txtName.text = "Name: ${contactDetails.name}"
        
        // Display contact photo if available
        if (contactDetails.photoUri != null) {
            try {
                val photoUri = Uri.parse(contactDetails.photoUri)
                contactImage.setImageURI(photoUri)
                contactImage.visibility = ImageView.VISIBLE
            } catch (e: Exception) {
                contactImage.visibility = ImageView.GONE
            }
        } else {
            contactImage.visibility = ImageView.GONE
        }
        
        // Display phone numbers
        if (contactDetails.phoneNumbers.isNotEmpty()) {
            phoneSection.visibility = LinearLayout.VISIBLE
            phoneHeader.text = "Phone Numbers (${contactDetails.phoneNumbers.size})"
            
            for (phone in contactDetails.phoneNumbers) {
                val phoneView = TextView(this).apply {
                    text = "- ${phone.type}: ${phone.number}"
                    setPadding(0, 8, 0, 8)
                    textSize = 16f
                }
                phoneSection.addView(phoneView)
            }
            
            // Add expand/collapse functionality if multiple phones
            if (contactDetails.phoneNumbers.size > 1) {
                setupExpandCollapse(phoneHeader, phoneSection, contactDetails.phoneNumbers.size)
            }
        } else {
            txtNumber.text = "Number: No number on file"
            phoneSection.visibility = LinearLayout.GONE
        }
        
        // Display emails
        if (contactDetails.emails.isNotEmpty()) {
            emailSection.visibility = LinearLayout.VISIBLE
            emailHeader.text = "Email Addresses (${contactDetails.emails.size})"
            
            for (email in contactDetails.emails) {
                val emailView = TextView(this).apply {
                    text = "- ${email.type}: ${email.address}"
                    setPadding(0, 8, 0, 8)
                    textSize = 16f
                }
                emailSection.addView(emailView)
            }
            
            // Add expand/collapse functionality if multiple emails
            if (contactDetails.emails.size > 1) {
                setupExpandCollapse(emailHeader, emailSection, contactDetails.emails.size)
            }
        } else {
            emailSection.visibility = LinearLayout.GONE
        }
        
        // Display date of birth
        if (contactDetails.dateOfBirth != null) {
            dobSection.visibility = LinearLayout.VISIBLE
            dobHeader.text = "Date of Birth"
            
            val dobView = TextView(this).apply {
                text = "- ${contactDetails.dateOfBirth}"
                setPadding(0, 8, 0, 8)
                textSize = 16f
            }
            dobSection.addView(dobView)
        } else {
            dobSection.visibility = LinearLayout.GONE
        }
        
        // Display postal addresses
        if (contactDetails.postalAddresses.isNotEmpty()) {
            addressSection.visibility = LinearLayout.VISIBLE
            addressHeader.text = "Addresses (${contactDetails.postalAddresses.size})"
            
            for (address in contactDetails.postalAddresses) {
                val addressView = TextView(this).apply {
                    val fullAddress = "- ${address.type}:\n${address.street}\n${address.city}, ${address.state} ${address.postalCode}\n${address.country}"
                    text = fullAddress
                    setPadding(0, 8, 0, 8)
                    textSize = 16f
                }
                addressSection.addView(addressView)
            }
            
            // Add expand/collapse functionality if multiple addresses
            if (contactDetails.postalAddresses.size > 1) {
                setupExpandCollapse(addressHeader, addressSection, contactDetails.postalAddresses.size)
            }
        } else {
            addressSection.visibility = LinearLayout.GONE
        }
    }
    
    private fun setupExpandCollapse(header: TextView, section: LinearLayout, itemCount: Int) {
        var isExpanded = true
        
        header.setOnClickListener {
            isExpanded = !isExpanded
            
            // Show/hide all child views except the header
            for (i in 1 until section.childCount) {
                section.getChildAt(i).visibility = if (isExpanded) android.view.View.VISIBLE else android.view.View.GONE
            }
            
            // Update header text to indicate expand/collapse state
            val originalText = header.text.toString().substringBeforeLast(" (")
            header.text = if (isExpanded) {
                "$originalText ($itemCount) ▼"
            } else {
                "$originalText ($itemCount) ▶"
            }
        }
    }
    
    private fun clearPreviousData() {
        // Clear all sections except headers
        clearSectionViews(phoneSection)
        clearSectionViews(emailSection)
        clearSectionViews(addressSection)
        clearSectionViews(dobSection)
        
        // Hide all sections initially
        phoneSection.visibility = LinearLayout.GONE
        emailSection.visibility = LinearLayout.GONE
        addressSection.visibility = LinearLayout.GONE
        dobSection.visibility = LinearLayout.GONE
        contactImage.visibility = ImageView.GONE
    }
    
    private fun clearSectionViews(section: LinearLayout) {
        // Remove all views except the header (first view)
        while (section.childCount > 1) {
            section.removeViewAt(1)
        }
    }
}