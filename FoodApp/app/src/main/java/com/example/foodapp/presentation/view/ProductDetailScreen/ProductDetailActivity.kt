package com.example.foodapp.presentation.view.ProductDetailScreen

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.R
import com.google.android.material.button.MaterialButton

class ProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val ivBack =  findViewById<ImageButton>(R.id.ivBack)
        val ivProductDetail = findViewById<ImageView>(R.id.ivProductDetail)
        val btnDecrease = findViewById<MaterialButton>(R.id.btnDecrease)
        val btnIncrease = findViewById<MaterialButton>(R.id.btnIncrease)
        val tvQuantity = findViewById<TextView>(R.id.tvQuantity)
        ivProductDetail.setImageResource(R.drawable.matchalatte)

        ivBack.setOnClickListener {
            finish()
        }
        setSumProduct(btnIncrease,btnDecrease,tvQuantity)
    }

    fun setSumProduct(btnIncrease : MaterialButton, btnDecrease : MaterialButton, tvQuantity : TextView) {

        btnDecrease.setOnClickListener {
            var value = tvQuantity.text.toString().toInt()
            if(value > 1){
                value--
                tvQuantity.text = value.toString()
            }
        }

        btnIncrease.setOnClickListener {
            var value = tvQuantity.text.toString().toInt()
            value++
            tvQuantity.text = value.toString()
        }

    }

}