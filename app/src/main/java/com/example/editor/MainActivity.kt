package com.example.editor

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val PERMISSION_CODE_CAMERA = 999
    private val PERMISSION_CODE_GALLERY = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private val IMAGE_PICK_CODE = 1002
    var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //----------нажатие кнопки куба
        cubeButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "Функция в стадии разработки", Toast.LENGTH_SHORT).show()
        }

        //----------нажатие кнопки камеры
        cameraButton.setOnClickListener {
            // версия android >= Marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED
                ) {
                    val permission = arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    requestPermissions(permission, PERMISSION_CODE_CAMERA)
                } else {
                    openCamera()
                }
            }
            else {
                // версия android < Marshmallow
                openCamera()
            }
        }

        //----------нажатие кнопки галереи
        galleryButton.setOnClickListener {
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    // запрос отклонен
                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                    requestPermissions(permissions, PERMISSION_CODE_GALLERY);
                }
                else{
                    // запрос уже принят
                    pickImageFromGallery();
                }
            }
            else{
                // версия android < Marshmallow
                pickImageFromGallery();
            }
        }
    }

    //вызов камеры
    private fun openCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    //вызов галереи
    private fun pickImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // вызывается, когда пользователь принимает/отклоняет запрос
        when (requestCode) {
            PERMISSION_CODE_CAMERA -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //запрос принят
                    openCamera()
                }
                else {
                    //запрос отклонен
                    Toast.makeText(this, "Запрос отклонен", Toast.LENGTH_SHORT).show()
                }
            }
            PERMISSION_CODE_GALLERY -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //запрос принят
                    pickImageFromGallery()
                }
                else {
                    //запрос отклонен
                    Toast.makeText(this, "Запрос отклонен", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //вызывается, когда фото захвачено с камеры/взято из галереи
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_CAPTURE_CODE) {
            //создание нового активити и переход к нему
            val newActivityIntent = Intent(this, EditingActivity::class.java)
            newActivityIntent.putExtra("imageUri", imageUri.toString())
            startActivity(newActivityIntent)
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data?.data
            val newActivityIntent = Intent(this, EditingActivity::class.java)
            newActivityIntent.putExtra("imageUri", imageUri.toString())
            startActivity(newActivityIntent)
        }
    }
}

