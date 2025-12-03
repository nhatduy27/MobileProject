package com.example.foodapp.presentation.view.MainScreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.presentation.view.Component.Product.FoodCategory
import com.example.foodapp.presentation.view.Component.Product.Product
import com.example.foodapp.presentation.view.Component.Product.ProductAdapter
import com.example.foodapp.presentation.view.ProductDetailScreen.ProductDetailActivity
import com.example.foodapp.R

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Tìm RecyclerView từ layout
        recyclerView = findViewById(R.id.rvFoods)

        setupRecyclerView()

    }

    private fun setupRecyclerView() {

        val productList = createSampleProducts()

        productAdapter = ProductAdapter(
            productList = productList,
            onItemClick = { product ->
                val intent = Intent(this@MainActivity, ProductDetailActivity::class.java)
                startActivity(intent)
            },

            )


        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = productAdapter
        }
    }



    private fun createSampleProducts(): List<Product> {
        return listOf(
            Product(
                name = "Matcha Latte",
                description = "Full Topping",
                price = "20.000đ",
                priceValue = 12.0,
                imageRes = R.drawable.matchalatte,
                category = FoodCategory.DRINK
            ),
            Product(

                name = "Classic Pepporoni",
                description = "Medium | Cheese, hungarian pepper, paneer, capsicum and onion",
                price = "$12",
                priceValue = 12.0,
                imageRes = R.drawable.data_3,
                category = FoodCategory.FOOD
            ),
            Product(

                name = "Chicken Supreme",
                description = "Medium | Cheese, onion, and tomato pure",
                price = "$12",
                priceValue = 12.0,
                imageRes = R.drawable.data_2,
                category = FoodCategory.SNACK
            ),
            Product(

                name = "Veggie Paradise",
                description = "Medium | Corn, capsicum, onion, tomato",
                price = "$10",
                priceValue = 10.0,
                imageRes = R.drawable.data_3,
                category = FoodCategory.FOOD
            ),
            Product(

                name = "BBQ Chicken",
                description = "Medium | BBQ sauce, chicken, onion",
                price = "$14",
                priceValue = 14.0,
                imageRes = R.drawable.data_1,
                category = FoodCategory.DRINK

            )
        )
    }
}