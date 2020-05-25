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

        val uriString: String? = intent.getStringExtra("imageUri")
        val uri = Uri.parse(uriString)
        imageView.setImageURI(uri)
        val startImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap

        seekBarInvisible()
        progressBarInvisible()

        //--------поворот изображения
        rotateButton.setOnClickListener {
            rotate()
        }
        rotateText.setOnClickListener {
            rotate()
        }

        //--------сохранение изображения
        saveButton.setOnClickListener {
            save()
        }
        saveText.setOnClickListener {
            save()
        }

        //--------сброс изменений
        undoButton.setOnClickListener {
            undo(startImage)
        }
        undoText.setOnClickListener {
            undo(startImage)
        }

        //--------эффекты
        effectsButton.setOnClickListener {
            effects()
        }
        effectsText.setOnClickListener {
            effects()
        }

        //--------масштабирование
        scalingButton.setOnClickListener {
            scaling()
        }
        scalingText.setOnClickListener {
            scaling()
        }

        //--------ретушь
        retouchButton.setOnClickListener {
            retouch()
        }
        retouchText.setOnClickListener {
            retouch()
        }
    }

    //----------изменение активности SeekBar, ProgressBar и нижних кнопок
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
        degrees.visibility = View.GONE
        seekBar.visibility = View.GONE
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

    //----------поворот изображения
    private fun rotate() {
        buttonsInvisible()
        seekBarVisible()
        seekBar.max = 0
        seekBar.max = 360
        seekBar.progress = 180

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progess: Int, fromUser: Boolean) {
                degrees.text = (seek.progress - 180).toString() + "°"
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                progressBarVisible()
                seekBarInvisible()

                doAsync {
                    val rotatedImage = rotation(seekBar.progress - 180)

                    uiThread {
                        imageView.setImageBitmap(rotatedImage)
                        seekBarInvisible()
                        progressBarInvisible()
                        buttonsVisible()
                        Toast.makeText(
                            this@EditingActivity,
                            "Выполнен поворот на " + (seek.progress - 180) + "°",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }
    //алгоритм поворота
    private fun rotation(angleStart: Int): Bitmap {
        val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val width: Int = image.width
        val height: Int = image.height

        val pixelsArray = IntArray(width * height)
        image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
        val newPixelsArray = IntArray(width * height)

        val angle = Math.toRadians(angleStart.toDouble())
        val sin = sin(angle)
        val cos = cos(angle)

        val midX = 0.5 * (width - 1) // point to rotate about
        val midY = 0.5 * (height - 1) // center of image

        if (angleStart in -44 until 44) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val a = x - midX
                    val b = y - midY
                    val xx = (+a * cos - b * sin + midX).toInt()
                    val yy = (+a * sin + b * cos + midY).toInt()
                    if (xx in 0 until width && yy >= 0 && yy < height) {
                        newPixelsArray[y * width + x] = pixelsArray[yy * width + xx]
                    } else {
                        newPixelsArray[y * width + x] = Color.argb(100, 0, 0, 0)
                    }
                }
            }
            return Bitmap.createBitmap(newPixelsArray, width, height, image.config)
        }
        else {
            for (y in 0 until width) {
            for (x in 0 until height){
                val a = x - midY
                val b = y - midX
                val xx = (+a * cos - b * sin + midX).toInt()
                val yy = (+a * sin + b * cos + midY).toInt()
                if (xx in 0 until width && yy >= 0 && yy < height) {
                    newPixelsArray[y*height+x] = pixelsArray[yy*width+xx]
                } else {
                    newPixelsArray[y*height+x]  = Color.argb(100, 0, 0, 0)
                }
            }
        }
        return Bitmap.createBitmap(newPixelsArray, height, width, image.config)
        }
    }

    //----------сохранение изображения
    private fun save() {
        var temp = 0
        buttonsInvisible()

        doAsync {
            val mainImage: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val time = Time()
            time.setToNow()
            val externalStorageState = Environment.getExternalStorageState()
            if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
                val storageDirectory = Environment.getExternalStorageDirectory().toString()
                val file = File(
                    storageDirectory,
                    "new_image" + time.year.toString() + (time.month + 1).toString() +
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
                buttonsVisible()
                if (temp == 1) {
                    Toast.makeText(
                        this@EditingActivity,
                        "Изображение успешно сохранено",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@EditingActivity,
                        "Не удалось получить доступ к памяти",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    //----------сброс изменений
    private fun undo(startImage: Bitmap) {
        val image: Bitmap = startImage
        imageView.setImageBitmap(image)
        Toast.makeText(this@EditingActivity, "Изменения отменены", Toast.LENGTH_SHORT).show()
    }

    //----------эффекты
    private fun effects() {
        val menu = PopupMenu(this, effectsButton)
        menu.inflate(R.menu.effects_menu)

        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                // Неоновый розовый
                R.id.effect1 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                val pixelAlpha = Color.alpha(pixelsArray[y*width+x])
                                val pixelRed = Color.red(pixelsArray[y*width+x])
                                val pixelGreen = Color.green(pixelsArray[y*width+x])
                                val pixelBlue = Color.blue(pixelsArray[y*width+x])

                                newPixelsArray[y*width+x] = Color.argb(pixelAlpha, pixelBlue, pixelRed / 2, pixelGreen)
                            }
                        }
                        uiThread {
                            imageView.setImageBitmap(Bitmap.createBitmap(newPixelsArray, width, height, image.config))
                            progressBarInvisible()
                            buttonsVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Неоновый розовый",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                // Розовый
                R.id.effect2 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                var pixelAlpha = Color.alpha(pixelsArray[y*width+x])
                                val pixelRed = Color.red(pixelsArray[y*width+x])
                                var pixelGreen = Color.green(pixelsArray[y*width+x])
                                var pixelBlue = Color.blue(pixelsArray[y*width+x])

                                if (pixelAlpha >= 10) {
                                    pixelAlpha -= 10
                                }
                                if (pixelGreen >= 10) {
                                    pixelGreen -= 10
                                }
                                if (pixelBlue <= 10) {
                                    pixelBlue += 10
                                }

                                newPixelsArray[y*width+x] = Color.argb( pixelAlpha, pixelRed, pixelGreen, pixelBlue)
                            }
                        }
                        uiThread {
                            imageView.setImageBitmap(Bitmap.createBitmap(newPixelsArray, width, height, image.config))
                            progressBarInvisible()
                            buttonsVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Розовый",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                // Черно-белый 1
                R.id.effect3 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                val pixelAlpha = Color.alpha(pixelsArray[y*width+x])
                                val pixelRed = Color.red(pixelsArray[y*width+x])
                                val pixelGreen = Color.green(pixelsArray[y*width+x])
                                val pixelBlue = Color.blue(pixelsArray[y*width+x])

                                val grey = (pixelRed + pixelGreen + pixelBlue) / 3

                                newPixelsArray[y*width+x] = Color.argb(pixelAlpha, grey, grey, grey)
                            }
                        }
                        uiThread {
                            imageView.setImageBitmap(Bitmap.createBitmap(newPixelsArray, width, height, image.config))
                            progressBarInvisible()
                            buttonsVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Черно-белый 1",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                // Черно-белый 2
                R.id.effect4 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                val pixelAlpha = Color.alpha(pixelsArray[y*width+x])
                                val pixelRed = Color.red(pixelsArray[y*width+x])
                                val pixelGreen = Color.green(pixelsArray[y*width+x])
                                val pixelBlue = Color.blue(pixelsArray[y*width+x])

                                var mid = (pixelRed + pixelGreen + pixelBlue) / 3

                                if (mid in 0..115) {
                                    mid = 0
                                } else if (mid in 116..149) {
                                    mid = 134
                                } else if (mid in 155..255) {
                                    mid = 255
                                }

                                newPixelsArray[y*width+x] = Color.argb(pixelAlpha, mid, mid, mid)
                            }
                        }
                        uiThread {
                            imageView.setImageBitmap(Bitmap.createBitmap(newPixelsArray, width, height, image.config))
                            progressBarInvisible()
                            buttonsVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Черно-белый 2",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                // Негатив
                R.id.effect5 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                val pixelAlpha = Color.alpha(pixelsArray[y*width+x])
                                val pixelRed = Color.red(pixelsArray[y*width+x])
                                val pixelGreen = Color.green(pixelsArray[y*width+x])
                                val pixelBlue = Color.blue(pixelsArray[y*width+x])

                                newPixelsArray[y*width+x] = Color.argb(pixelAlpha, 255-pixelRed,
                                    255-pixelGreen, 255-pixelBlue)
                            }
                        }
                        uiThread {
                            imageView.setImageBitmap(Bitmap.createBitmap(newPixelsArray, width, height, image.config))
                            progressBarInvisible()
                            buttonsVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Негатив",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                // Сепия
                R.id.effect6 -> {
                    buttonsInvisible()
                    progressBarVisible()
                    val image = (imageView.drawable as BitmapDrawable).bitmap
                    val width = image.width
                    val height = image.height

                    val pixelsArray = IntArray(width * height)
                    image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
                    val newPixelsArray = IntArray(width * height)

                    doAsync {
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                val pixelAlpha = Color.alpha(pixelsArray[y*width+x])
                                var pixelRed = Color.red(pixelsArray[y*width+x])
                                var pixelGreen = Color.green(pixelsArray[y*width+x])
                                var pixelBlue = Color.blue(pixelsArray[y*width+x])

                                pixelRed =(pixelRed * 0.393 + pixelGreen * 0.769 + pixelBlue * 0.189).toInt()
                                pixelGreen =(pixelRed * 0.349 + pixelGreen * 0.686 + pixelBlue * 0.168).toInt()
                                pixelBlue =(pixelRed * 0.272 + pixelGreen * 0.534 + pixelBlue * 0.131).toInt()

                                if (pixelRed > 255) pixelRed = 255
                                if (pixelGreen > 255) pixelGreen = 255
                                if (pixelBlue > 255) pixelBlue = 255

                                newPixelsArray[y*width+x] = Color.argb(
                                        pixelAlpha, pixelRed, pixelGreen, pixelBlue)
                            }
                        }
                        uiThread {
                            imageView.setImageBitmap(Bitmap.createBitmap(newPixelsArray, width, height, image.config))
                            progressBarInvisible()
                            buttonsVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Сепия",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    true
                }
                else -> false
            }
        }
        menu.show()
    }

    //----------масштабирование
    private fun scaling() {
        buttonsInvisible()
        seekBarVisible()
        seekBar.max = 0
        seekBar.max = 200
        seekBar.progress = 100
        degrees.text = "100%"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progess: Int, fromUser: Boolean) {
                degrees.text = (seek.progress).toString() + "%"
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                progressBarVisible()
                seekBarInvisible()

                doAsync {
                    val scaledImage = bilinearInterpolation((seek.progress).toDouble() / 100)

                    uiThread {
                        //imageView.setImageBitmap(scaledImage)
                        imageView.setImageBitmap(scaledImage)
                        seekBarInvisible()
                        progressBarInvisible()
                        buttonsVisible()
                        if (seek.progress > 100) {
                            Toast.makeText(
                            this@EditingActivity,
                            "Изображение увеличено на " + (seek.progress - 100) + "%",
                            Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(
                                this@EditingActivity,
                                "Изображение уменьшено на " + (100 - seek.progress) + "%",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    //----------алгоритм масштабирования (билинейная интерполяция)
    private fun bilinearInterpolation(ratio: Double): Bitmap {
        val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
        // высота и ширина оригинала
        val width1 = image.width
        val height1 = image.height

        val pixelsArray = IntArray(width1 * height1)
        image.getPixels(pixelsArray, 0, width1, 0, 0, width1, height1)

        // новые ширина и высота с учет
        val width2 = (image.width * ratio).toInt()
        val height2 = (image.height * ratio).toInt()
        val newPixelsArray = IntArray(width2 * height2)

        // рассмотрим квадрат пикселей 2х2
        var a: Int   // верхний левый пиксель
        var b: Int   // верхий правый
        var c: Int   // нижний левый
        var d: Int   // нижний правый
        var x: Int
        var y: Int
        var index: Int

        val xRatio = (width1 - 1).toFloat() / width2
        val yRatio = (height1 - 1).toFloat() / height2
        var xDiff: Float
        var yDiff: Float
        var blue: Float
        var red: Float
        var green: Float
        var offset = 0

        for (i in 0 until height2) {
            for (j in 0 until width2) {
                x = (xRatio * j).toInt()
                y = (yRatio * i).toInt()
                xDiff = xRatio * j - x
                yDiff = yRatio * i - y
                index = y * width1 + x
                a = pixelsArray[index]
                b = pixelsArray[index + 1]
                c = pixelsArray[index + width1]
                d = pixelsArray[index + width1 + 1]

                blue = (a and 0xff) * (1 - xDiff) * (1 - yDiff) + (b and 0xff) *
                        xDiff * (1 - yDiff) + (c and 0xff) * yDiff *
                        (1 - xDiff) + (d and 0xff) * (xDiff * yDiff)

                green = (a shr 8 and 0xff) * (1 - xDiff) * (1 - yDiff) + (b shr 8 and 0xff) *
                        xDiff * (1 - yDiff) + (c shr 8 and 0xff) * yDiff * (1 - xDiff) +
                        (d shr 8 and 0xff) * (xDiff * yDiff)

                red = (a shr 16 and 0xff) * (1 - xDiff) * (1 - yDiff) + (b shr 16 and 0xff) *
                        xDiff * (1 - yDiff) + (c shr 16 and 0xff) * yDiff * (1 - xDiff) +
                        (d shr 16 and 0xff) * (xDiff * yDiff)

                newPixelsArray[offset++] = -0x1000000 or
                        (red.toInt() shl 16 and 0xff0000) or
                        (green.toInt() shl 8 and 0xff00) or
                        blue.toInt()
            }
        }
        return Bitmap.createBitmap(newPixelsArray, width2, height2, image.config)
    }

    //----------ретуширование
    private fun retouch() {
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







