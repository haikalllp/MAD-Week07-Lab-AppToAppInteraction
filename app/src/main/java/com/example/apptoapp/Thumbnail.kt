package com.example.apptoapp

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.apptoapp.ui.theme.ApptoAppTheme

class Thumbnail : AppCompatActivity() {
    private lateinit var imgThumb: ImageView
    // Pre-declare: contract (what we want) + callback (what to do with result)
    private val takeThumbnail = registerForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        callback = { bitmap: Bitmap? ->
            if (bitmap != null) {
                imgThumb.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "No image captured", Toast.LENGTH_SHORT).show()
            }
        }
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thumbnail)

        imgThumb = findViewById(R.id.imgThumb)

        // Launch the contract when button is clicked
        findViewById<Button>(R.id.btnTakeThumb).setOnClickListener {
            takeThumbnail.launch(null) // No input required
        }
    }
}
