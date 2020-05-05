package com.example.editor

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_editing.*

class EditingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing)

        val uriString:String? = intent.getStringExtra("imageUri")

        val uri = Uri.parse(uriString)
        imageView.setImageURI(uri)
    }
}
