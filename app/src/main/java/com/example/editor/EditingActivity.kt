package com.example.editor

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.format.Time
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_editing.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
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
        var finalImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap

        //поворот изображения
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
            finalImage = editedImage
        }

        //сохранение изображения
        saveButton.setOnClickListener{
            val time = Time()
            time.setToNow()
            val externalStorageState = Environment.getExternalStorageState()
            if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
                val storageDirectory = Environment.getExternalStorageDirectory().toString()
                val file = File(storageDirectory, "new_image" + time.year.toString() + (time.month + 1).toString() +
                        time.monthDay.toString() + time.hour.toString() + time.minute.toString() +
                        time.second.toString() + ".jpg")
                try {
                    val stream: OutputStream = FileOutputStream(file)
                    finalImage.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.flush()
                    stream.close()
                    Toast.makeText(this, "Изображение успешно сохранено", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else {
                Toast.makeText(this, "Не удалось получить доступ к памяти", Toast.LENGTH_SHORT).show()
            }
        }
    }


}






