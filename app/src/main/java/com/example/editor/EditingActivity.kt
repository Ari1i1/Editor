package com.example.editor

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_editing.*
import kotlin.math.cos
import kotlin.math.sin

class EditingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing)

        val uriString:String? = intent.getStringExtra("imageUri")

        val uri = Uri.parse(uriString)
        imageView.setImageURI(uri)

        rotateButton.setOnClickListener{
            val editableImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap

            val width: Int = editableImage.width
            val height: Int = editableImage.height
            val angle = Math.toRadians(90.0)
            val sin = sin(angle)
            val cos = cos(angle)
            val rotationPoint = 0.5 * (width - 1) // point to rotate about
            val imageCenter = 0.5 * (height - 1) // center of image
            val editedImage = Bitmap.createBitmap(height, width, editableImage.config)

            //rotation
            for (x in 0 until height) {
                for (y in 0 until width) {
                    val a = x - imageCenter
                    val b = y - rotationPoint
                    val xx = (+a * cos - b * sin + rotationPoint).toInt()
                    val yy = (+a * sin + b * cos + imageCenter).toInt()
                    if (xx in 0 until width && yy >= 0 && yy < height) {
                        editedImage.setPixel(x, y, editableImage.getPixel(xx, yy))
                    }
                }
            }
            imageView.setImageBitmap(editedImage)
        }
    }
}







