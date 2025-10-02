package com.example.apptoapp

import android.graphics.Bitmap

/**
 * Data class to hold all contact information
 */
data class ContactDetails(
    val name: String?,
    val phoneNumber: String?,
    val email: String?,
    val dateOfBirth: String?,
    val postalAddress: String?,
    val contactImageUri: String?,
    val contactImageBitmap: Bitmap? = null
)