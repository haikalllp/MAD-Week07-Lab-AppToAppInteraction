package com.example.apptoapp

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import java.io.IOException
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
// Import ContactDetails data class from the same package
import android.provider.ContactsContract

class PickContact : AppCompatActivity() {
    private lateinit var txtName: TextView
    private lateinit var txtNumber: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtDateOfBirth: TextView
    private lateinit var txtPostalAddress: TextView
    private lateinit var imgContact: ImageView
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
            if (contactUri != null) loadContactDetails(contactUri) else
                Toast.makeText(this,
                    "No contact selected", Toast.LENGTH_SHORT).show()
        }
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_contact)

        txtName = findViewById<TextView>(R.id.txtName)
        txtNumber = findViewById<TextView>(R.id.txtNumber)
        txtEmail = findViewById<TextView>(R.id.txtEmail)
        txtDateOfBirth = findViewById<TextView>(R.id.txtDateOfBirth)
        txtPostalAddress = findViewById<TextView>(R.id.txtPostalAddress)
        imgContact = findViewById<ImageView>(R.id.imgContact)

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

    // 3c) Safely load all contact details
    private fun loadContactDetails(contactUri: Uri) {
        val contactDetails = getContactDetails(contactUri)
        
        // Update UI with all contact details
        txtName.text = contactDetails.name ?: "No name"
        txtNumber.text = contactDetails.phoneNumber ?: "No number on file"
        txtEmail.text = contactDetails.email ?: "No email on file"
        txtDateOfBirth.text = contactDetails.dateOfBirth ?: "No date of birth on file"
        txtPostalAddress.text = contactDetails.postalAddress ?: "No postal address on file"
        
        // Set contact photo bitmap if available
        if (contactDetails.contactImageBitmap != null) {
            imgContact.setImageBitmap(contactDetails.contactImageBitmap)
        } else {
            // No photo available, set a default placeholder
            imgContact.setImageResource(R.mipmap.ic_launcher)
        }
    }
    
    // Function to retrieve all contact details
    private fun getContactDetails(contactUri: Uri): ContactDetails {
        var name: String? = null
        var phoneNumber: String? = null
        var email: String? = null
        var dateOfBirth: String? = null
        var postalAddress: String? = null
        var contactImageUri: String? = null
        
        // Query the Contacts table first to get CONTACT_ID, DISPLAY_NAME, and PHOTO_URI
        val contactsProjection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.PHOTO_ID
        )

        contentResolver.query(contactUri, contactsProjection,
            null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                // Get contact ID to use in subsequent queries
                val contactId = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))

                // Get name and photo URI
                name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                contactImageUri = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))

                // Query for phone number
                val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    phoneProjection,
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=?",
                    arrayOf(contactId),
                    null
                )
                
                phoneCursor?.use { pc ->
                    if (pc.moveToFirst()) {
                        phoneNumber = pc.getString(pc.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Phone.NUMBER))
                    }
                }
                
                // Query for email address
                val emailProjection = arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS)
                val emailCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    emailProjection,
                    "${ContactsContract.CommonDataKinds.Email.CONTACT_ID}=?",
                    arrayOf(contactId),
                    null
                )
                
                emailCursor?.use { ec ->
                    if (ec.moveToFirst()) {
                        email = ec.getString(ec.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Email.ADDRESS))
                    }
                }
                
                // Query for date of birth
                val eventProjection = arrayOf(
                    ContactsContract.CommonDataKinds.Event.START_DATE,
                    ContactsContract.CommonDataKinds.Event.TYPE
                )
                val eventCursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    eventProjection,
                    // This uses MIMETYPE instead to ensure it only get events of type birthday
                    "${ContactsContract.CommonDataKinds.Event.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=? AND ${ContactsContract.CommonDataKinds.Event.TYPE}=?",
                    arrayOf(contactId, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()),
                    null
                )
                
                eventCursor?.use { evc ->
                    if (evc.moveToFirst()) {
                        dateOfBirth = evc.getString(evc.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Event.START_DATE))
                    }
                }
                
                // Query for postal address
                val postalProjection = arrayOf(
                    ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
                )
                val postalCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                    postalProjection,
                    "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID}=?",
                    arrayOf(contactId),
                    null
                )
                
                postalCursor?.use { pc ->
                    if (pc.moveToFirst()) {
                        postalAddress = pc.getString(pc.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS))
                    }
                }
            }
        }
        
        // Load contact photo as Bitmap if URI is available
        val contactBitmap = contactImageUri?.let { uriString ->
            try {
                val uri = Uri.parse(uriString)
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } catch (e: IOException) {
                null
            }
        }

        // Return all details and set them in ContactDetails object
        return ContactDetails(name, phoneNumber, email, dateOfBirth, postalAddress, contactImageUri, contactBitmap)
    }
    
}