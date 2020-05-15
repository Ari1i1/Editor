package com.example.editor

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.format.Time
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_editing.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.cos
import kotlin.math.sin

class EditingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing)

        val uriString:String? = intent.getStringExtra("imageUri")
        val uri = Uri.parse(uriString)
        imageView.setImageURI(uri)

        seekBar.visibility = View.GONE

        //поворот изображения
        rotateButton.setOnClickListener{
            rotation()
        }
        rotateText.setOnClickListener{
            rotation()
        }

        //сохранение изображения
        saveButton.setOnClickListener{
            save()
        }
        saveText.setOnClickListener{
            save()
        }

        //сброс изменений
        undoButton.setOnClickListener{
            undo()
        }
        undoText.setOnClickListener{
            undo()
        }

        //эффекты
        effectsButton.setOnClickListener{
            effects()
        }
        effectsText.setOnClickListener{
            effects()
        }

        //масштабирование
        scalingButton.setOnClickListener{
            scaling()
        }
        scalingText.setOnClickListener{
            scaling()
        }
    }

    //поворот изображения
    private fun rotation(){
        var mainImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val width: Int = mainImage.width
        val height: Int = mainImage.height
        val angle = Math.toRadians(90.0)
        val sin = sin(angle)
        val cos = cos(angle)
        val rotationPoint = 0.5 * (width - 1) // point to rotate about
        val imageCenter = 0.5 * (height - 1) // center of image
        val editedImage = Bitmap.createBitmap(height, width, mainImage.config)

        for (x in 0 until height) {
            for (y in 0 until width) {
                val a = x - imageCenter
                val b = y - rotationPoint
                val xx = (+a * cos - b * sin + rotationPoint).toInt()
                val yy = (+a * sin + b * cos + imageCenter).toInt()
                if (xx in 0 until width && yy >= 0 && yy < height) {
                    editedImage.setPixel(x, y, mainImage.getPixel(xx, yy))
                }
            }
        }
        imageView.setImageBitmap(editedImage)
        mainImage = editedImage
        Toast.makeText(this, "Выполнен поворот 90°", Toast.LENGTH_SHORT).show()
    }

    //сохранение изображения
    private fun save(){
        val mainImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
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
                mainImage.compress(Bitmap.CompressFormat.JPEG, 100, stream)
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

    //сброс изменений
    private fun undo(){
        val mainImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
        imageView.setImageBitmap(mainImage)
        Toast.makeText(this, "Изменения отменены", Toast.LENGTH_SHORT).show()
    }

    //ползунок + изменение активности нижних кнопок
    private fun seekBarVisible(){
        seekBar.visibility = View.VISIBLE

        effectsButton.visibility = View.GONE
        rotateButton.visibility = View.GONE
        scalingButton.visibility = View.GONE
        retouchButton.visibility = View.GONE
        segmentationButton.visibility = View.GONE
        filtrationButton.visibility = View.GONE
        saveButton.visibility = View.GONE
        undoButton.visibility = View.GONE

        effectsText.visibility = View.GONE
        rotateText.visibility = View.GONE
        scalingText.visibility = View.GONE
        retouchText.visibility = View.GONE
        segmentationText.visibility = View.GONE
        filtrationText.visibility = View.GONE
        saveText.visibility = View.GONE
        undoText.visibility = View.GONE
    }
    private fun seekBarInvisible(){
        seekBar.visibility = View.GONE

        effectsButton.visibility = View.VISIBLE
        rotateButton.visibility = View.VISIBLE
        scalingButton.visibility = View.VISIBLE
        retouchButton.visibility = View.VISIBLE
        segmentationButton.visibility = View.VISIBLE
        filtrationButton.visibility = View.VISIBLE
        saveButton.visibility = View.VISIBLE
        undoButton.visibility = View.VISIBLE

        effectsText.visibility = View.VISIBLE
        rotateText.visibility = View.VISIBLE
        scalingText.visibility = View.VISIBLE
        retouchText.visibility = View.VISIBLE
        segmentationText.visibility = View.VISIBLE
        filtrationText.visibility = View.VISIBLE
        saveText.visibility = View.VISIBLE
        undoText.visibility = View.VISIBLE
    }

    //эффекты
    private fun effects(){
        var mainImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val menu = PopupMenu(this, effectsButton)
        menu.inflate(R.menu.effects_menu)
        menu.setOnMenuItemClickListener{
            when (it.itemId) {
                R.id.effect1 -> {
                    //тело эффекта
                    Toast.makeText(this, "Применен эффект 1", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.effect2 -> {
                    //тело эффекта
                    Toast.makeText(this, "Применен эффект 2", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        menu.show()
    }

    //масштабирование
    private fun scaling(){
        seekBarVisible()
        //алгоритм
        seekBarInvisible()
        Toast.makeText(this, "Выполнено масштабирование", Toast.LENGTH_SHORT).show()
    }
}







