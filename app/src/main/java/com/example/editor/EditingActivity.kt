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
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
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

        seekBarInvisible()
        progressBarInvisible()

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

        //ретушь
        retouchButton.setOnClickListener {
            retouch()
        }
        retouchText.setOnClickListener {
            retouch()
        }
    }

    //изменение активности SeekBar, ProgressBar и нижних кнопок
    private fun seekBarVisible() {
        seekBar.visibility = View.VISIBLE
        degrees.visibility = View.VISIBLE
    }
    private fun buttonsInvisible() {
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
    }
    private fun buttonsVisible() {
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
    private fun progressBarVisible() {
        progressBar.visibility = View.VISIBLE
    }
    private fun progressBarInvisible() {
        progressBar.visibility = View.GONE
    }

    //поворот изображения
    private fun rotate() {
        seekBarVisible()
        buttonsInvisible()
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
                progressBarVisible()
                val image = (imageView.drawable as BitmapDrawable).bitmap
                var editedImage = image

                doAsync {
                    editedImage = rotation(seekBar.progress - 180, image)

                    uiThread {
                        imageView.setImageBitmap(editedImage)
                        progressBarInvisible()
                        seekBarInvisible()
                        buttonsVisible()
                        Toast.makeText(this@EditingActivity, "Выполнен поворот на " + (seek.progress-180) + "°", Toast.LENGTH_SHORT).show()
                    }
                }
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
        val rotatedImage = Bitmap.createBitmap(height, width, image.config)

        for (x in 0 until height) {
            for (y in 0 until width) {
                val a = x - midY
                val b = y - midX
                val xx = (+a * cos - b * sin + midX).toInt()
                val yy = (+a * sin + b * cos + midY).toInt()
                if (xx in 0 until width && yy >= 0 && yy < height) {
                    rotatedImage.setPixel(x, y, image.getPixel(xx, yy))
                }
                else {
                    rotatedImage.setPixel(x, y, Color.argb(100, 0, 0, 0))
                }
            }
        }
        return rotatedImage
    }

    //сохранение изображения
    private fun save() {
        var temp = 0

        doAsync {
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
                    temp = 1
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                temp = 0
            }

            uiThread {
                progressBarInvisible()
                if (temp == 1) {
                    Toast.makeText(this@EditingActivity, "Изображение успешно сохранено", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this@EditingActivity, "Не удалось получить доступ к памяти", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //сброс изменений
    private fun undo(startImage: Bitmap) {
        val image: Bitmap = startImage
        imageView.setImageBitmap(image)
        Toast.makeText(this@EditingActivity, "Изменения отменены", Toast.LENGTH_SHORT).show()
    }

    //эффекты
    private fun effects() {
        val menu = PopupMenu(this, effectsButton)
        menu.inflate(R.menu.effects_menu)

        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                //розовый неон
                R.id.effect1 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height
                    val editedImage = Bitmap.createBitmap(width, height, image.config)

                    doAsync {
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
                        uiThread {
                            imageView.setImageBitmap(editedImage)
                            progressBarInvisible()
                            buttonsVisible()
                            Toast.makeText(this@EditingActivity, "Применен эффект Розовый неон", Toast.LENGTH_SHORT).show()
                        }
                    }
                    buttonsVisible()
                    true
                }
                //розовый
                R.id.effect2 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height
                    val editedImage = Bitmap.createBitmap(width, height, image.config)

                    doAsync {
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
                                if (pixelGreen >= 10) {
                                    pixelGreen -= 10
                                }
                                if (pixelBlue <= 10) {
                                    pixelBlue += 10
                                }

                                editedImage.setPixel(x, y, Color.argb(pixelAlpha, pixelRed,
                                    pixelGreen, pixelBlue))
                            }
                        }
                        uiThread {
                            imageView.setImageBitmap(editedImage)
                            progressBarInvisible()
                            buttonsVisible()
                            Toast.makeText(this@EditingActivity, "Применен эффект Розовый", Toast.LENGTH_SHORT).show()
                        }
                    }
                    buttonsVisible()
                    true

                }
                //черно-белый 1
                R.id.effect3 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height
                    val editedImage = Bitmap.createBitmap(width, height, image.config)

                    doAsync {
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
                        uiThread {
                            imageView.setImageBitmap(editedImage)
                            progressBarInvisible()
                            buttonsVisible()
                            Toast.makeText(this@EditingActivity, "Применен эффект Черно-белый 1", Toast.LENGTH_SHORT).show()
                        }
                    }
                    buttonsVisible()
                    true
                }
                //черно-белый 2
                R.id.effect4 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height
                    val editedImage = Bitmap.createBitmap(width, height, image.config)

                    doAsync {
                        for (x in 0 until width) {
                            for (y in 0 until height) {
                                val pixelColor = image.getPixel(x, y)
                                val pixelAlpha = Color.alpha(pixelColor)
                                val pixelRed = Color.red(pixelColor)
                                val pixelGreen = Color.green(pixelColor)
                                val pixelBlue = Color.blue(pixelColor)

                                var mid = (pixelRed + pixelGreen + pixelBlue)/3

                                if (mid in 0..115) { mid = 0 }
                                else if (mid in 116..149) { mid = 134 }
                                else if (mid in 155..255) { mid= 255 }

                                editedImage.setPixel(x, y, Color.argb(pixelAlpha, mid,
                                    mid, mid))
                            }
                        }
                        uiThread {
                            imageView.setImageBitmap(editedImage)
                            progressBarInvisible()
                            buttonsVisible()
                            Toast.makeText(this@EditingActivity, "Применен эффект Черно-белый 2", Toast.LENGTH_SHORT).show()
                        }
                    }
                    buttonsVisible()
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
        val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val width = image.width
        val height = image.height

        seekBarInvisible()
        Toast.makeText(this, "Выполнено масштабирование", Toast.LENGTH_SHORT).show()
    }

    //ретуширование
    private fun retouch(){
        val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
        var pixelAlpha: Int = 0
        var pixelRed: Int = 0
        var pixelGreen: Int = 0
        var pixelBlue: Int = 0
        var pixelColor: Int
        val width = image.width
        val height = image.height
        val editedImage = Bitmap.createBitmap(width, height, image.config)
        val size = width * height
        for (x in 0 until width) {
            for (y in 0 until height) {
                pixelColor = image.getPixel(x, y)
                pixelAlpha += Color.alpha(pixelColor)
                pixelRed += Color.red(pixelColor)
                pixelGreen += Color.green(pixelColor)
                pixelBlue += Color.blue(pixelColor)
            }
        }
        pixelAlpha /= size
        pixelRed /= size
        pixelGreen /= size
        pixelBlue /= size
        for (x in 0 until width) {
            for (y in 0 until height) {
                editedImage.setPixel(x, y, Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue))
            }
        }
        imageView.setImageBitmap(editedImage)
        Toast.makeText(this, "Выполнена ретушь", Toast.LENGTH_SHORT).show()
        true
    }
}






