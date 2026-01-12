package com.nonthakorn.productmanagement

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.nonthakorn.productmanagement.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use ViewBinding
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getIntExtra("product_id", -1)
        if (productId != -1) {
            val dbHelper = ProductDbHelper(this)
            val product = dbHelper.getProductById(productId)

            product?.let {
                // Show Image
                if (!it.imagePath.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(it.imagePath)
                        .into(binding.imageViewProduct)
                }

                binding.textViewName.text = it.name
                binding.textViewDescription.text = it.description
                binding.textViewPrice.text = "ราคา: ${it.price} บาท"
                binding.textViewQuantity.text = "จำนวน: ${it.quantity} ชิ้น"
            }
        }
    }
}