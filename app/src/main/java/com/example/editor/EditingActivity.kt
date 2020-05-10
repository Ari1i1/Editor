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

    private val editableImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing)

        val uriString:String? = intent.getStringExtra("imageUri")

        val uri = Uri.parse(uriString)
        imageView.setImageURI(uri)

        rotateButton.setOnClickListener{
            val width: Int = this.editableImage.getWidth()
            val height: Int = this.editableImage.getHeight()
            val angle = Math.toRadians(90.0)
            val sin = sin(angle)
            val cos = cos(angle)
            val rotationPoint = 0.5 * (width - 1) // point to rotate about
            val imageCenter = 0.5 * (height - 1) // center of image
            val editedImage = createBitmap(width, height, Bitmap.Config.ALPHA_8)
//        val pixel = IntArray(3)

            // rotation
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val a = x - rotationPoint
                    val b = y - imageCenter
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






