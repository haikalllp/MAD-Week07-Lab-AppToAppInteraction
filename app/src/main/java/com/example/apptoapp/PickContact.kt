package com.example.apptoapp

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.provider.ContactsContract

class PickContact : AppCompatActivity() {
    private lateinit var txtName: TextView
    private lateinit var txtNumber: TextView
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
        txtNumber = findViewById<TextView>(R.id.txtNumber)

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

    // 3c) Safely load display name + first phone number
    private fun loadNameAndNumber(contactUri: Uri) {
        // Query the Contacts table first to get CONTACT_ID and DISPLAY_NAME
        val contactsProjection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )

        contentResolver.query(contactUri, contactsProjection,
            null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val contactId = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val displayName =
                    c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                // Now query the Phone table for any numbers with this CONTACT_ID
                val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val phoneCursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    phoneProjection,
                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=?",
                    arrayOf(contactId),
                    null
                )

                var number: String? = null
                phoneCursor?.use { pc ->
                    if (pc.moveToFirst()) {
                        number =
                            pc.getString(pc.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.NUMBER))
                    }
                }

                // Update UI
                txtName.text = "Name: ${displayName.orEmpty()}"
                txtNumber.text = "Number: ${number ?: "No number on file"}"
            }
        }
    }
}