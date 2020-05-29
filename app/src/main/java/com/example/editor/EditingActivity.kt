package com.example.editor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.format.Time
import android.view.MotionEvent
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
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.cos
import kotlin.math.sin


class EditingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editing)

        val uriString: String? = intent.getStringExtra("imageUri")
        val uri = Uri.parse(uriString)

        val `is`: InputStream? = contentResolver.openInputStream(uri)
        val startOriginal = BitmapFactory.decodeStream(`is`)
        `is`!!.close()
        val startCompressed: Bitmap = bilinearInterpolation(startOriginal, 0.5)

        var editableOriginal: Bitmap = startOriginal

        imageView.setImageBitmap(startCompressed)

        seekBarInvisible()
        progressBarInvisible()
        allowInvisible()
        cancelInvisible()

        //--------поворот изображения
        rotateButton.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            editableOriginal = rotate(image, editableOriginal)
        }
        rotateText.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            editableOriginal = rotate(image, editableOriginal)
        }

        //--------сохранение изображения
        saveButton.setOnClickListener {
            save(editableOriginal)
        }
        saveText.setOnClickListener {
            save(editableOriginal)
        }

        //--------сброс изменений
        undoButton.setOnClickListener {
            editableOriginal = undo(startOriginal, startCompressed)
        }
        undoText.setOnClickListener {
            editableOriginal = undo(startOriginal, startCompressed)
        }

        //--------эффекты
        effectsButton.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            editableOriginal = effects(image, editableOriginal)
        }
        effectsText.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            editableOriginal = effects(image, editableOriginal)
        }

        //--------масштабирование
        scalingButton.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            editableOriginal = scaling(image, editableOriginal)
        }
        scalingText.setOnClickListener {
            val image: Bitmap = (imageView.drawable as BitmapDrawable).bitmap
            editableOriginal = scaling(image, editableOriginal)
        }

        //--------ретушь
        retouchButton.setOnClickListener {
            retouch()
            Toast.makeText(this@EditingActivity, "Функция в стадии разработки", Toast.LENGTH_SHORT).show()
        }
        retouchText.setOnClickListener {
            retouch()
            Toast.makeText(this@EditingActivity, "Функция в стадии разработки", Toast.LENGTH_SHORT).show()
        }

        //--------сегментация
        segmentationButton.setOnClickListener {
            Toast.makeText(this@EditingActivity, "Функция в стадии разработки", Toast.LENGTH_SHORT).show()
        }
        segmentationText.setOnClickListener {
            Toast.makeText(this@EditingActivity, "Функция в стадии разработки", Toast.LENGTH_SHORT).show()
        }

        //--------билин., трилин. фильтрация
        filtrationButton.setOnClickListener {
            Toast.makeText(this@EditingActivity, "Функция в стадии разработки", Toast.LENGTH_SHORT).show()
        }
        filtrationText.setOnClickListener {
            Toast.makeText(this@EditingActivity, "Функция в стадии разработки", Toast.LENGTH_SHORT).show()
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

    // активность кнопок подтверждения/отмены
    private fun allowVisible() {
        allowButton.visibility = View.VISIBLE
    }
    private fun cancelVisible() {
        cancelButton.visibility = View.VISIBLE
    }

    private fun allowInvisible() {
        allowButton.visibility = View.GONE
    }
    private fun cancelInvisible() {
        cancelButton.visibility = View.GONE
    }

    private fun bitmapToArray (image: Bitmap, width: Int, height: Int): IntArray {
        val pixelsArray = IntArray(width * height)
        image.getPixels(pixelsArray, 0, width, 0, 0, width, height)
        return pixelsArray
    }

    //----------поворот изображения
    private fun rotate(compressed: Bitmap, original: Bitmap): Bitmap {
        var rotatedOriginal: Bitmap = original
        var rotatedCompressed: Bitmap = compressed

        cancelVisible()
        buttonsInvisible()
        seekBarVisible()
        seekBar.max = 0
        seekBar.max = 360
        seekBar.progress = 180
        degrees.text = "0°"

        cancelButton.setOnClickListener {
            imageView.setImageBitmap(compressed)
            seekBarInvisible()
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }
        allowButton.setOnClickListener {
            imageView.setImageBitmap(rotatedCompressed)
            seekBarInvisible()
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }

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
                    cancelInvisible()
                    rotatedCompressed = rotation(rotatedCompressed, seekBar.progress - 180)
                    rotatedOriginal = rotation(rotatedOriginal, seekBar.progress - 180)

                    uiThread {
                        imageView.setImageBitmap(rotatedCompressed)
                        progressBarInvisible()
                        allowVisible()
                        cancelVisible()
                        seekBarVisible()
                        Toast.makeText(
                            this@EditingActivity,
                            "Выполнен поворот на " + (seekBar.progress - 180) + "°",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
        return rotatedOriginal
    }
    // алгоритм поворота
    private fun rotation(image: Bitmap, angleStart: Int): Bitmap {
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
    private fun save(image: Bitmap) {
        var temp = 0
        buttonsInvisible()
        progressBarVisible()

        doAsync {
            val time = Time()
            time.setToNow()
            val externalStorageState = Environment.getExternalStorageState()
            if (externalStorageState == Environment.MEDIA_MOUNTED) {
                val storageDirectory = Environment.getExternalStorageDirectory().toString()
                val file = File(
                    storageDirectory,
                    "new_image" + time.year.toString() + (time.month + 1).toString() +
                            time.monthDay.toString() + time.hour.toString() + time.minute.toString() +
                            time.second.toString() + ".jpg"
                )
                try {
                    val stream: OutputStream = FileOutputStream(file)
                    image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
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
    private fun undo(startOriginal: Bitmap, startCompressed: Bitmap): Bitmap {
        imageView.setImageBitmap(startCompressed)
        Toast.makeText(this@EditingActivity, "Изменения отменены", Toast.LENGTH_SHORT).show()
        return startOriginal
    }

    //----------эффекты
    private fun effects(compressed: Bitmap, original: Bitmap): Bitmap {
        val menu = PopupMenu(this, effectsButton)
        menu.inflate(R.menu.effects_menu)

        var editedCompressed: Bitmap = compressed
        var editedOriginal: Bitmap = original

        cancelButton.setOnClickListener {
            imageView.setImageBitmap(compressed)
            editedOriginal = original
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }
        allowButton.setOnClickListener {
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }

        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                // Неоновый розовый
                R.id.effect1 -> {
                    buttonsInvisible()
                    progressBarVisible()

                    doAsync {
                        val width1 = editedCompressed.width
                        val height1 = editedCompressed.height
                        val pixelsArrayCompressed = bitmapToArray(editedCompressed, width1, height1)
                        val newPixelsCompressed = IntArray(width1 * height1)

                        val width2 = editedOriginal.width
                        val height2 = editedOriginal.height
                        val pixelsArrayOriginal: IntArray = bitmapToArray(editedOriginal, width2, height2)
                        val newPixelsOriginal = IntArray(width2 * height2)

                        for (y in 0 until height1) {
                            for (x in 0 until width1) {
                                val pixelAlpha = Color.alpha(pixelsArrayCompressed[y*width1+x])
                                val pixelRed = Color.red(pixelsArrayCompressed[y*width1+x])
                                val pixelGreen = Color.green(pixelsArrayCompressed[y*width1+x])
                                val pixelBlue = Color.blue(pixelsArrayCompressed[y*width1+x])

                                newPixelsCompressed[y*width1+x] = Color.argb(pixelAlpha, pixelBlue, pixelRed / 2, pixelGreen)
                            }
                        }
                        for (y in 0 until height2) {
                            for (x in 0 until width2) {
                                val pixelAlpha = Color.alpha(pixelsArrayOriginal[y*width2+x])
                                val pixelRed = Color.red(pixelsArrayOriginal[y*width2+x])
                                val pixelGreen = Color.green(pixelsArrayOriginal[y*width2+x])
                                val pixelBlue = Color.blue(pixelsArrayOriginal[y*width2+x])

                                newPixelsOriginal[y*width2+x] = Color.argb(pixelAlpha, pixelBlue, pixelRed / 2, pixelGreen)
                            }
                        }
                        editedOriginal = Bitmap.createBitmap(newPixelsOriginal, width2, height2, editedCompressed.config)
                        uiThread {
                            editedCompressed = Bitmap.createBitmap(newPixelsCompressed, width1, height1, editedCompressed.config)
                            imageView.setImageBitmap(editedCompressed)
                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Неоновый розовый",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
                // Розовый
                R.id.effect2 -> {
                    buttonsInvisible()
                    progressBarVisible()

                    doAsync {
                        val width1 = editedCompressed.width
                        val height1 = editedCompressed.height
                        val pixelsArrayCompressed: IntArray = bitmapToArray(editedCompressed, width1, height1)
                        val newPixelsCompressed = IntArray(width1 * height1)

                        val width2 = editedOriginal.width
                        val height2 = editedOriginal.height
                        val pixelsArrayOriginal: IntArray = bitmapToArray(editedOriginal, width2, height2)
                        val newPixelsOriginal = IntArray(width2 * height2)

                        for (y in 0 until height1) {
                            for (x in 0 until width1) {
                                var pixelAlpha = Color.alpha(pixelsArrayCompressed[y*width1+x])
                                val pixelRed = Color.red(pixelsArrayCompressed[y*width1+x])
                                var pixelGreen = Color.green(pixelsArrayCompressed[y*width1+x])
                                var pixelBlue = Color.blue(pixelsArrayCompressed[y*width1+x])

                                if (pixelAlpha >= 10) {
                                    pixelAlpha -= 10
                                }
                                if (pixelGreen >= 10) {
                                    pixelGreen -= 10
                                }
                                if (pixelBlue <= 10) {
                                    pixelBlue += 10
                                }

                                newPixelsCompressed[y*width1+x] = Color.argb( pixelAlpha, pixelRed, pixelGreen, pixelBlue)
                            }
                        }
                        for (y in 0 until height2) {
                            for (x in 0 until width2) {
                                var pixelAlpha = Color.alpha(pixelsArrayOriginal[y*width2+x])
                                val pixelRed = Color.red(pixelsArrayOriginal[y*width2+x])
                                var pixelGreen = Color.green(pixelsArrayOriginal[y*width2+x])
                                var pixelBlue = Color.blue(pixelsArrayOriginal[y*width2+x])

                                if (pixelAlpha >= 10) {
                                    pixelAlpha -= 10
                                }
                                if (pixelGreen >= 10) {
                                    pixelGreen -= 10
                                }
                                if (pixelBlue <= 10) {
                                    pixelBlue += 10
                                }

                                newPixelsOriginal[y*width2+x] = Color.argb( pixelAlpha, pixelRed, pixelGreen, pixelBlue)
                            }
                        }
                        editedOriginal = Bitmap.createBitmap(newPixelsOriginal, width2, height2, editedCompressed.config)
                        uiThread {
                            editedCompressed = Bitmap.createBitmap(newPixelsCompressed, width1, height1, editedCompressed.config)
                            imageView.setImageBitmap(editedCompressed)
                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Розовый",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
                // Черно-белый 1
                R.id.effect3 -> {
                    buttonsInvisible()
                    progressBarVisible()

                    doAsync {
                        val width1 = editedCompressed.width
                        val height1 = editedCompressed.height
                        val pixelsArrayCompressed: IntArray = bitmapToArray(editedCompressed, width1, height1)
                        val newPixelsCompressed = IntArray(width1 * height1)

                        val width2 = editedOriginal.width
                        val height2 = editedOriginal.height
                        val pixelsArrayOriginal: IntArray = bitmapToArray(editedOriginal, width2, height2)
                        val newPixelsOriginal = IntArray(width2 * height2)

                        for (y in 0 until height1) {
                            for (x in 0 until width1) {
                                val pixelAlpha = Color.alpha(pixelsArrayCompressed[y*width1+x])
                                val pixelRed = Color.red(pixelsArrayCompressed[y*width1+x])
                                val pixelGreen = Color.green(pixelsArrayCompressed[y*width1+x])
                                val pixelBlue = Color.blue(pixelsArrayCompressed[y*width1+x])

                                val grey = (pixelRed + pixelGreen + pixelBlue) / 3

                                newPixelsCompressed[y*width1+x] = Color.argb(pixelAlpha, grey, grey, grey)
                            }
                        }
                        for (y in 0 until height2) {
                            for (x in 0 until width2) {
                                val pixelAlpha = Color.alpha(pixelsArrayOriginal[y*width2+x])
                                val pixelRed = Color.red(pixelsArrayOriginal[y*width2+x])
                                val pixelGreen = Color.green(pixelsArrayOriginal[y*width2+x])
                                val pixelBlue = Color.blue(pixelsArrayOriginal[y*width2+x])

                                val grey = (pixelRed + pixelGreen + pixelBlue) / 3

                                newPixelsOriginal[y*width2+x] = Color.argb(pixelAlpha, grey, grey, grey)
                            }
                        }
                        editedOriginal = Bitmap.createBitmap(newPixelsOriginal, width2, height2, editedCompressed.config)
                        uiThread {
                            editedCompressed = Bitmap.createBitmap(newPixelsCompressed, width1, height1, editedCompressed.config)
                            imageView.setImageBitmap(editedCompressed)
                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Черно-белый 1",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
                // Черно-белый 2
                R.id.effect4 -> {
                    buttonsInvisible()
                    progressBarVisible()

                    doAsync {
                        val width1 = editedCompressed.width
                        val height1 = editedCompressed.height
                        val pixelsArrayCompressed: IntArray = bitmapToArray(editedCompressed, width1, height1)
                        val newPixelsCompressed = IntArray(width1 * height1)

                        val width2 = editedOriginal.width
                        val height2 = editedOriginal.height
                        val pixelsArrayOriginal: IntArray = bitmapToArray(editedOriginal, width2, height2)
                        val newPixelsOriginal = IntArray(width2 * height2)

                        for (y in 0 until height1) {
                            for (x in 0 until width1) {
                                val pixelAlpha = Color.alpha(pixelsArrayCompressed[y*width1+x])
                                val pixelRed = Color.red(pixelsArrayCompressed[y*width1+x])
                                val pixelGreen = Color.green(pixelsArrayCompressed[y*width1+x])
                                val pixelBlue = Color.blue(pixelsArrayCompressed[y*width1+x])

                                var mid = (pixelRed + pixelGreen + pixelBlue) / 3
                                when (mid) {
                                    in 0..115 -> {
                                        mid = 0
                                    }
                                    in 116..149 -> {
                                        mid = 134
                                    }
                                    in 155..255 -> {
                                        mid = 255
                                    }
                                }

                                newPixelsCompressed[y*width1+x] = Color.argb(pixelAlpha, mid, mid, mid)
                            }
                        }
                        for (y in 0 until height2) {
                            for (x in 0 until width2) {
                                val pixelAlpha = Color.alpha(pixelsArrayOriginal[y*width2+x])
                                val pixelRed = Color.red(pixelsArrayOriginal[y*width2+x])
                                val pixelGreen = Color.green(pixelsArrayOriginal[y*width2+x])
                                val pixelBlue = Color.blue(pixelsArrayOriginal[y*width2+x])

                                var mid = (pixelRed + pixelGreen + pixelBlue) / 3
                                when (mid) {
                                    in 0..115 -> {
                                        mid = 0
                                    }
                                    in 116..149 -> {
                                        mid = 134
                                    }
                                    in 155..255 -> {
                                        mid = 255
                                    }
                                }

                                newPixelsOriginal[y*width2+x] = Color.argb(pixelAlpha, mid, mid, mid)
                            }
                        }
                        editedOriginal = Bitmap.createBitmap(newPixelsOriginal, width2, height2, editedCompressed.config)
                        uiThread {
                            editedCompressed = Bitmap.createBitmap(newPixelsCompressed, width1, height1, editedCompressed.config)
                            imageView.setImageBitmap(editedCompressed)
                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Черно-белый 2",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
                // Негатив
                R.id.effect5 -> {
                    buttonsInvisible()
                    progressBarVisible()

                    doAsync {
                        val width1 = editedCompressed.width
                        val height1 = editedCompressed.height
                        val pixelsArrayCompressed: IntArray = bitmapToArray(editedCompressed, width1, height1)
                        val newPixelsCompressed = IntArray(width1 * height1)

                        val width2 = editedOriginal.width
                        val height2 = editedOriginal.height
                        val pixelsArrayOriginal: IntArray = bitmapToArray(editedOriginal, width2, height2)
                        val newPixelsOriginal = IntArray(width2 * height2)

                        for (y in 0 until height1) {
                            for (x in 0 until width1) {
                                val pixelAlpha = Color.alpha(pixelsArrayCompressed[y*width1+x])
                                val pixelRed = Color.red(pixelsArrayCompressed[y*width1+x])
                                val pixelGreen = Color.green(pixelsArrayCompressed[y*width1+x])
                                val pixelBlue = Color.blue(pixelsArrayCompressed[y*width1+x])

                                newPixelsCompressed[y*width1+x] = Color.argb(pixelAlpha, 255-pixelRed,
                                    255-pixelGreen, 255-pixelBlue)
                            }
                        }
                        for (y in 0 until height2) {
                            for (x in 0 until width2) {
                                val pixelAlpha = Color.alpha(pixelsArrayOriginal[y*width2+x])
                                val pixelRed = Color.red(pixelsArrayOriginal[y*width2+x])
                                val pixelGreen = Color.green(pixelsArrayOriginal[y*width2+x])
                                val pixelBlue = Color.blue(pixelsArrayOriginal[y*width2+x])

                                newPixelsOriginal[y*width2+x] = Color.argb(pixelAlpha, 255-pixelRed,
                                    255-pixelGreen, 255-pixelBlue)
                            }
                        }
                        editedOriginal = Bitmap.createBitmap(newPixelsOriginal, width2, height2, editedCompressed.config)
                        uiThread {
                            editedCompressed = Bitmap.createBitmap(newPixelsCompressed, width1, height1, editedCompressed.config)
                            imageView.setImageBitmap(editedCompressed)
                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Негатив",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
                // Сепия
                R.id.effect6 -> {
                    buttonsInvisible()
                    progressBarVisible()

                    doAsync {
                        val width1 = editedCompressed.width
                        val height1 = editedCompressed.height
                        val pixelsArrayCompressed: IntArray = bitmapToArray(editedCompressed, width1, height1)
                        val newPixelsCompressed = IntArray(width1 * height1)

                        val width2 = editedOriginal.width
                        val height2 = editedOriginal.height
                        val pixelsArrayOriginal: IntArray = bitmapToArray(editedOriginal, width2, height2)
                        val newPixelsOriginal = IntArray(width2 * height2)

                        for (y in 0 until height1) {
                            for (x in 0 until width1) {
                                val pixelAlpha = Color.alpha(pixelsArrayCompressed[y*width1+x])
                                var pixelRed = Color.red(pixelsArrayCompressed[y*width1+x])
                                var pixelGreen = Color.green(pixelsArrayCompressed[y*width1+x])
                                var pixelBlue = Color.blue(pixelsArrayCompressed[y*width1+x])

                                pixelRed =(pixelRed * 0.393 + pixelGreen * 0.769 + pixelBlue * 0.189).toInt()
                                pixelGreen =(pixelRed * 0.349 + pixelGreen * 0.686 + pixelBlue * 0.168).toInt()
                                pixelBlue =(pixelRed * 0.272 + pixelGreen * 0.534 + pixelBlue * 0.131).toInt()

                                if (pixelRed > 255) pixelRed = 255
                                if (pixelGreen > 255) pixelGreen = 255
                                if (pixelBlue > 255) pixelBlue = 255

                                newPixelsCompressed[y*width1+x] = Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue)
                            }
                        }
                        for (y in 0 until height2) {
                            for (x in 0 until width2) {
                                val pixelAlpha = Color.alpha(pixelsArrayOriginal[y*width2+x])
                                var pixelRed = Color.red(pixelsArrayOriginal[y*width2+x])
                                var pixelGreen = Color.green(pixelsArrayOriginal[y*width2+x])
                                var pixelBlue = Color.blue(pixelsArrayOriginal[y*width2+x])

                                if (pixelRed > 255) pixelRed = 255
                                if (pixelGreen > 255) pixelGreen = 255
                                if (pixelBlue > 255) pixelBlue = 255

                                newPixelsOriginal[y*width2+x] = Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue)
                            }
                        }
                        editedOriginal = Bitmap.createBitmap(newPixelsOriginal, width2, height2, editedCompressed.config)
                        uiThread {
                            editedCompressed = Bitmap.createBitmap(newPixelsCompressed, width1, height1, editedCompressed.config)
                            imageView.setImageBitmap(editedCompressed)

                            progressBarInvisible()
                            effectsButton.visibility = View.VISIBLE
                            cancelVisible()
                            allowVisible()
                            Toast.makeText(
                                this@EditingActivity,
                                "Применен эффект Сепия",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
                else -> false
            }
        }
        menu.show()

        return editedOriginal
    }

    //----------масштабирование
    private fun scaling(compressed: Bitmap, original: Bitmap): Bitmap {
        var scaledOriginal: Bitmap = original
        var scaledCompressed: Bitmap = compressed

        buttonsInvisible()
        seekBarVisible()
        cancelVisible()
        seekBar.max = 0
        seekBar.max = 200
        seekBar.progress = 100
        degrees.text = "100%"

        cancelButton.setOnClickListener {
            imageView.setImageBitmap(compressed)
            scaledOriginal = original
            seekBarInvisible()
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }
        allowButton.setOnClickListener {
            imageView.setImageBitmap(scaledCompressed)
            seekBarInvisible()
            progressBarInvisible()
            buttonsVisible()
            cancelInvisible()
            allowInvisible()
        }

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
                    cancelInvisible()
                    scaledCompressed = bilinearInterpolation(scaledCompressed, (seek.progress).toDouble() / 100)
                    scaledOriginal = bilinearInterpolation(scaledOriginal, (seek.progress).toDouble() / 100)

                    uiThread {
                        imageView.setImageBitmap(scaledCompressed)
                        progressBarInvisible()
                        allowVisible()
                        cancelVisible()
                        seekBarVisible()
                        if (seekBar.progress > 100) {
                            Toast.makeText(
                                this@EditingActivity,
                                "Изображение увеличено на " + (seekBar.progress - 100) + "%",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@EditingActivity,
                                "Изображение уменьшено на " + (100 - seekBar.progress) + "%",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        })
        return scaledOriginal
    }

    //----------алгоритм масштабирования (билинейная интерполяция)
    private fun bilinearInterpolation(image: Bitmap, ratio: Double): Bitmap {
        // высота и ширина оригинала
        val width1 = image.width
        val height1 = image.height

        val pixelsArray = IntArray(width1 * height1)
        image.getPixels(pixelsArray, 0, width1, 0, 0, width1, height1)

        // новые ширина и высота с учетом коэффициента
        val width2 = (image.width * ratio).toInt()
        val height2 = (image.height * ratio).toInt()
        val newPixelsArray = IntArray(width2 * height2)

        var x: Int
        var y: Int
        var index: Int
        // квадрат пикселей 2х2
        var a: Int   // верхний левый пиксель
        var b: Int   // верхий правый
        var c: Int   // нижний левый
        var d: Int   // нижний правый

        val xRatio = (width1 - 1).toFloat() / width2
        val yRatio = (height1 - 1).toFloat() / height2
        var xDif: Float
        var yDif: Float
        var blue: Float
        var red: Float
        var green: Float
        var offset = 0

        for (i in 0 until height2) {
            for (j in 0 until width2) {
                x = (xRatio * j).toInt()
                y = (yRatio * i).toInt()
                xDif = xRatio * j - x
                yDif = yRatio * i - y
                index = y * width1 + x
                a = pixelsArray[index]
                b = pixelsArray[index + 1]
                c = pixelsArray[index + width1]
                d = pixelsArray[index + width1 + 1]

                blue = (a and 0xff) * (1 - xDif) * (1 - yDif) + (b and 0xff) *
                        xDif * (1 - yDif) + (c and 0xff) * yDif *
                        (1 - xDif) + (d and 0xff) * (xDif * yDif)

                green = (a shr 8 and 0xff) * (1 - xDif) * (1 - yDif) + (b shr 8 and 0xff) *
                        xDif * (1 - yDif) + (c shr 8 and 0xff) * yDif * (1 - xDif) +
                        (d shr 8 and 0xff) * (xDif * yDif)

                red = (a shr 16 and 0xff) * (1 - xDif) * (1 - yDif) + (b shr 16 and 0xff) *
                        xDif * (1 - yDif) + (c shr 16 and 0xff) * yDif * (1 - xDif) +
                        (d shr 16 and 0xff) * (xDif * yDif)

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
        val s = 10000
        val xCoordinates = IntArray(s)
        val yCoordinates = IntArray(s)
        val pixelValue = IntArray(s)
        var count = 0
        var editingImage: Bitmap = image
        buttonsInvisible()

        doAsync {
            imageView.setOnTouchListener { imageView, event ->
                ///алгоритм
                var x = event.x.toInt()
                var y = event.y.toInt()
                if (x >= 0 && x < image.width) {
                    if (y >= 0 && y < image.height) {
                        for (i in 0 until 21) {
                            for (j in 0 until 21) {
                                x = x - 10 + i
                                y = y - 10 + j
                                xCoordinates[count] = x
                                yCoordinates[count] = y
                                pixelValue[count] = image.getPixel(x, y)
                                count++
                            }
                        }
                    }
                }
                when (event!!.action) {
                    MotionEvent.ACTION_UP -> {
                        editingImage = retouchAlg(image, pixelValue, count, xCoordinates, yCoordinates)
                    }
                    else -> {
                    }
                }
                imageView.onTouchEvent(event)
            }
            uiThread {
                imageView.setImageBitmap(editingImage)
                progressBarInvisible()
                buttonsVisible()
                Toast.makeText(this@EditingActivity, "Выполнена ретушь", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retouchAlg(image: Bitmap, pixelValue: IntArray, count: Int,
                           xCoordinates: IntArray, yCoordinates: IntArray): Bitmap {
        var pixelAlpha = 0
        var pixelRed = 0
        var pixelGreen = 0
        var pixelBlue = 0
        var pixelColor: Int

        for (i in 0 until count) {
            pixelColor = pixelValue[i]
            pixelAlpha += Color.alpha(pixelColor)
            pixelRed += Color.red(pixelColor)
            pixelGreen += Color.green(pixelColor)
            pixelBlue += Color.blue(pixelColor)
        }

        pixelAlpha /= count
        pixelRed /= count
        pixelGreen /= count
        pixelBlue /= count

        for (i in 0 until count) {
            image.setPixel(xCoordinates[i], yCoordinates[i],
                Color.argb(pixelAlpha, pixelRed, pixelGreen, pixelBlue))
        }
        return image
    }
}






