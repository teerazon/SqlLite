package com.nonthakorn.productmanagement

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nonthakorn.productmanagement.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: ProductDbHelper
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ตั้งค่า Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "จัดการสินค้า"

        // เริ่มต้นคลาสตัวช่วยฐานข้อมูล
        dbHelper = ProductDbHelper(this)

        // ตั้งค่า RecyclerView
        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(
            emptyList(),
            onItemClick = { product -> showProductDetail(product) },
            onEditClick = { product -> editProduct(product) },
            onDeleteClick = { product -> deleteProduct(product) }
        )
        binding.recyclerViewProducts.adapter = productAdapter

        // ปุ่มเพิ่มสินค้า (FAB)
        binding.fabAddProduct.setOnClickListener {
            openAddProductActivity()
        }

        // โหลดข้อมูลสินค้า
        loadProducts()
    }

    private fun openAddProductActivity() {
        val intent = Intent(this, AddEditProductActivity::class.java)
        startActivityForResult(intent, REQUEST_ADD_PRODUCT)
    }

    private fun loadProducts() {
        val products = dbHelper.getAllProducts()
        productAdapter.updateProducts(products)

        // แสดงข้อความเมื่อไม่มีสินค้า
        if (products.isEmpty()) {
            // คุณสามารถเพิ่ม TextView สำหรับแสดงข้อความ "ไม่มีสินค้า" ได้ที่นี่
        }
    }

    private fun showProductDetail(product: Product) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("product_id", product.id)
        startActivity(intent)
    }

    private fun editProduct(product: Product) {
        val intent = Intent(this, AddEditProductActivity::class.java)
        intent.putExtra("product_id", product.id)
        startActivityForResult(intent, REQUEST_EDIT_PRODUCT)
    }

    private fun deleteProduct(product: Product) {
        // แสดง dialog ยืนยันการลบ
        android.app.AlertDialog.Builder(this)
            .setTitle("ลบสินค้า")
            .setMessage("คุณแน่ใจว่าต้องการลบ ${product.name}?")
            .setPositiveButton("ลบ") { dialog, which ->
                dbHelper.deleteProduct(product.id)
                loadProducts() // โหลดข้อมูลใหม่
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            loadProducts() // โหลดข้อมูลใหม่หลังจากเพิ่มหรือแก้ไข
        }
    }

    companion object {
        const val REQUEST_ADD_PRODUCT = 1
        const val REQUEST_EDIT_PRODUCT = 2
    }
}