package com.example.editor

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.format.Time
import android.view.View
import android.widget.PopupMenu
import android.widget.SeekBar
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
        val startImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap

        seekBar.visibility = View.GONE
        degrees.visibility = View.GONE

        //поворот изображения
        rotateButton.setOnClickListener {
            rotate()
        }
        rotateText.setOnClickListener {
            rotate()
        }

        //сохранение изображения
        saveButton.setOnClickListener {
            save()
        }
        saveText.setOnClickListener {
            save()
        }

        //сброс изменений
        undoButton.setOnClickListener {
            undo(startImage)
        }
        undoText.setOnClickListener {
            undo(startImage)
        }

        //эффекты
        effectsButton.setOnClickListener {
            effects()
        }
        effectsText.setOnClickListener {
            effects()
        }

        //масштабирование
        scalingButton.setOnClickListener {
            scaling()
        }
        scalingText.setOnClickListener {
            scaling()
        }
    }

    //ползунок + изменение активности нижних кнопок
    private fun seekBarVisible() {
        seekBar.visibility = View.VISIBLE
        degrees.visibility = View.VISIBLE

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

    private fun seekBarInvisible() {
        seekBar.visibility = View.GONE
        degrees.visibility = View.GONE

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

    //поворот изображения
    private fun rotate() {
        seekBarVisible()
        seekBar.max = 0
        seekBar.max = 360
        seekBar.progress = 180

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progess: Int , fromUser: Boolean) {
                degrees.text = (seek.progress-180).toString() + "°"
            }
            override fun onStartTrackingTouch(seek: SeekBar) {
            }
            override fun onStopTrackingTouch(seek: SeekBar) {
                val image = (imageView.drawable as BitmapDrawable).bitmap
                val editedImage = rotation(seekBar.progress - 180, image)
                imageView.setImageBitmap(editedImage)
                Toast.makeText(this@EditingActivity, "Выполнен поворот на " + (seek.progress-180) + "°", Toast.LENGTH_SHORT).show()
                seekBarInvisible()
            }
        })
    }
    //алгоритм поворота
    private fun rotation(angleStart: Int, Image: Bitmap) : Bitmap {
        val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val width: Int = image.width
        val height: Int = image.height
        val angle = Math.toRadians(angleStart.toDouble())
        val sin = sin(angle)
        val cos = cos(angle)
        val midX = 0.5 * (width - 1) // point to rotate about
        val midY = 0.5 * (height - 1) // center of image
        val editedImage = Bitmap.createBitmap(height, width, image.config)

        for (x in 0 until height) {
            for (y in 0 until width) {
                val a = x - midY
                val b = y - midX
                val xx = (+a * cos - b * sin + midX).toInt()
                val yy = (+a * sin + b * cos + midY).toInt()
                if (xx in 0 until width && yy >= 0 && yy < height) {
                    editedImage.setPixel(x, y, image.getPixel(xx, yy))
                }
                else {
                    editedImage.setPixel(x, y, Color.argb(100, 0, 0, 0))
                }
            }
        }
        return editedImage
    }

    //сохранение изображения
    private fun save() {
        val mainImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val time = Time()
        time.setToNow()
        val externalStorageState = Environment.getExternalStorageState()
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
            val storageDirectory = Environment.getExternalStorageDirectory().toString()
            val file = File(
                storageDirectory, "new_image" + time.year.toString() + (time.month + 1).toString() +
                        time.monthDay.toString() + time.hour.toString() + time.minute.toString() +
                        time.second.toString() + ".jpg"
            )
            try {
                val stream: OutputStream = FileOutputStream(file)
                mainImage.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
                stream.close()
                Toast.makeText(this, "Изображение успешно сохранено", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Не удалось получить доступ к памяти", Toast.LENGTH_SHORT).show()
        }
    }

    //сброс изменений
    private fun undo(startImage: Bitmap) {
        val image: Bitmap = startImage
        imageView.setImageBitmap(image)
        Toast.makeText(this, "Изменения отменены", Toast.LENGTH_SHORT).show()
    }

    //эффекты
    private fun effects() {
        val menu = PopupMenu(this, effectsButton)
        menu.inflate(R.menu.effects_menu)

        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.effect1 -> {
                    //неоновый розовый
                    val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val editedImage = Bitmap.createBitmap(width, height, image.config)

                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            val pixelColor = image.getPixel(x, y)
                            val pixelAlpha = Color.alpha(pixelColor)
                            val pixelRed = Color.red(pixelColor)
                            val pixelGreen = Color.green(pixelColor)
                            val pixelBlue = Color.blue(pixelColor)

                            editedImage.setPixel(x, y, Color.argb(pixelAlpha, pixelBlue, pixelRed/2, pixelGreen))
                        }
                    }
                    imageView.setImageBitmap(editedImage)

                    Toast.makeText(this, "Применен эффект 1", Toast.LENGTH_SHORT).show()
                    true
                }
                //розовый
                R.id.effect2 -> {
                    val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val editedImage = Bitmap.createBitmap(width, height, image.config)

                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            val pixelColor = image.getPixel(x, y)
                            var pixelAlpha = Color.alpha(pixelColor)
                            var pixelRed = Color.red(pixelColor)
                            var pixelGreen = Color.green(pixelColor)
                            var pixelBlue = Color.blue(pixelColor)

                            if (pixelAlpha >= 10) {
                                pixelAlpha -= 10
                            }
                            if (pixelRed <= 200) {
                                pixelRed += 10
                            }
                            if (pixelGreen >= 20) {
                                pixelGreen -= 20
                            }
                            if (pixelBlue <= 200) {
                                pixelBlue += 10
                            }

                            editedImage.setPixel(x, y, Color.argb(pixelAlpha, pixelRed,
                                pixelGreen, pixelBlue))
                        }
                    }
                    imageView.setImageBitmap(editedImage)

                    Toast.makeText(this, "Применен эффект 2", Toast.LENGTH_SHORT).show()
                    true
                }
                //черно-белый
                R.id.effect3 -> {
                    val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val editedImage = Bitmap.createBitmap(width, height, image.config)

                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            val pixelColor = image.getPixel(x, y)
                            val pixelAlpha = Color.alpha(pixelColor)
                            val pixelRed = Color.red(pixelColor)
                            val pixelGreen = Color.green(pixelColor)
                            val pixelBlue = Color.blue(pixelColor)

                            val grey = (pixelRed + pixelGreen + pixelBlue)/3

                            editedImage.setPixel(x, y, Color.argb(pixelAlpha, grey, grey, grey))
                        }
                    }
                    imageView.setImageBitmap(editedImage)

                    Toast.makeText(this, "Применен эффект 3", Toast.LENGTH_SHORT).show()
                    true
                }
                //случайный эффект
                R.id.effect4 -> {
                    val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val editedImage = Bitmap.createBitmap(width, height, image.config)

                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            val pixelColor = image.getPixel(x, y)
                            val pixelAlpha = Color.alpha(pixelColor)
                            val pixelRed = Color.red(pixelColor)
                            val pixelGreen = Color.green(pixelColor)
                            val pixelBlue = Color.blue(pixelColor)

                            val rand1 = (-50..50).random()
                            val rand2 = (-50..50).random()
                            val rand3 = (-50..50).random()

                            editedImage.setPixel(x, y, Color.argb(pixelAlpha, pixelRed + rand1,
                                pixelGreen + rand2, pixelBlue + rand3))
                        }
                    }
                    imageView.setImageBitmap(editedImage)

                    Toast.makeText(this, "Применен случайны эффект", Toast.LENGTH_SHORT).show()
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







