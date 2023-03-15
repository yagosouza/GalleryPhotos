package com.yagosouza.galleryphotos

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import android.Manifest

class MainActivity : AppCompatActivity() {
    private lateinit var photoGallery: RecyclerView
    private lateinit var btnAddPhoto: Button
    private lateinit var photoAdapter: PhotoAdapter
    private val photos = mutableListOf<Photo>()

    // Variável de chacagem de permissão
    private var check = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicia pedido de permissão
        initPermissions()

        photoGallery = findViewById(R.id.photo_gallery)
        btnAddPhoto = findViewById(R.id.btn_add_photo)

        photoAdapter = PhotoAdapter(photos) { photo ->
            photos.remove(photo)
            photoAdapter.notifyDataSetChanged()
        }

        photoGallery.adapter = photoAdapter
        photoGallery.layoutManager = GridLayoutManager(this, 3)

        btnAddPhoto.setOnClickListener { addPhoto() }
    }

    private fun addPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_SELECT_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SELECT_PHOTO && resultCode == RESULT_OK) {
            val selectedPhotoUri = data?.data
            selectedPhotoUri?.let { uri ->
                val filePath = getRealPathFromUri(uri)
                filePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val photo = Photo(
                            id = System.currentTimeMillis(),
                            name = file.name,
                            path = path,
                            dateAdded = System.currentTimeMillis()
                        )
                        photos.add(photo)
                        photoAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    // Checa se existe ou não permissão
    private fun initPermissions(){
        if(!getPermission()) setPermission()
        else check = true
    }

    // Checa se existe permissão
    private fun getPermission(): Boolean =
        (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)

    // Pede permissão se não tiver
    private fun setPermission(){
        val permissionsList = listOf<String>(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(this, permissionsList.toTypedArray(), PERMISSION_CODE)
    }

    // Envia mensagem por não ter permissão
    private fun errorPermission(){
        Toast.makeText(this, "Sem permissão", Toast.LENGTH_SHORT).show()
    }

    // Recebe resultado do pedido de permissão
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    errorPermission()
                } else {
                    check = true
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            val path = it.getString(columnIndex)
            it.close()
            return path
        }
        return null
    }

    companion object {
        private const val REQUEST_SELECT_PHOTO = 1
        private const val PERMISSION_CODE = 1
    }
}





