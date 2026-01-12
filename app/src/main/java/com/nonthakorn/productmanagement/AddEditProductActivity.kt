package com.nonthakorn.productmanagement

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.nonthakorn.productmanagement.databinding.ActivityAddEditProductBinding
import java.io.File
import java.io.FileOutputStream

class AddEditProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditProductBinding
    private lateinit var dbHelper: ProductDbHelper
    private var productId: Int = -1
    private var imagePath: String? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = ProductDbHelper(this)

        // ตรวจสอบว่าเป็นโหมดแก้ไขหรือเพิ่มใหม่
        productId = intent.getIntExtra("product_id", -1)
        if (productId != -1) {
            val product = dbHelper.getProductById(productId)
            product?.let {
                binding.editTextName.setText(it.name)
                binding.editTextDescription.setText(it.description)
                binding.editTextPrice.setText(it.price.toString())
                binding.editTextQuantity.setText(it.quantity.toString())
                imagePath = it.imagePath

                if (!imagePath.isNullOrEmpty()) {
                    binding.imageViewProduct.setImageURI(Uri.parse(imagePath))
                }
            }
        }

        binding.buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.buttonSave.setOnClickListener {
            saveProduct()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            selectedImageUri?.let { uri ->
                imagePath = saveImageToInternalStorage(uri)
                binding.imageViewProduct.setImageURI(uri)
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(filesDir, "product_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    private fun saveProduct() {
        val name = binding.editTextName.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val priceText = binding.editTextPrice.text.toString().trim()
        val quantityText = binding.editTextQuantity.text.toString().trim()

        if (name.isEmpty()) {
            binding.editTextName.error = "กรุณากรอกชื่อสินค้า"
            return
        }
        if (priceText.isEmpty()) {
            binding.editTextPrice.error = "กรุณากรอกราคา"
            return
        }
        if (quantityText.isEmpty()) {
            binding.editTextQuantity.error = "กรุณากรอกจำนวน"
            return
        }

        val price = priceText.toDoubleOrNull() ?: 0.0
        val quantity = quantityText.toIntOrNull() ?: 0
        val product = Product(productId, name, description, price, quantity, imagePath)

        if (productId == -1) {
            dbHelper.addProduct(product)
        } else {
            dbHelper.updateProduct(product)
        }

        setResult(RESULT_OK)
        finish()
    }
}