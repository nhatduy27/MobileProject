package com.example.foodapp.presentation.view.Component.Product

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.R

class ProductAdapter(
    private var productList: List<Product>, private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {


    // ViewHolder chỉ hiển thị, không xử lý click
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductDescription: TextView = itemView.findViewById(R.id.tvProductDescription)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvProductCategory: TextView = itemView.findViewById(R.id.tvProductCategory)

        fun bind(product: Product) {
            // Chỉ hiển thị dữ liệu, không xử lý sự kiện
            tvProductName.text = product.name
            tvProductDescription.text = product.description
            tvProductPrice.text = product.price
            ivProductImage.setImageResource(product.imageRes)

            when(product.category) {

                FoodCategory.FOOD -> {tvProductCategory.text = "Thức ăn"}
                FoodCategory.DRINK -> {tvProductCategory.text = "Đồ uống"}
                FoodCategory.SNACK -> {tvProductCategory.text = "Thức ăn vặt"}
            }

            itemView.setOnClickListener {
                onItemClick(product)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    override fun getItemCount(): Int = productList.size

    fun updateProducts(newProducts: List<Product>) {
        productList = newProducts
        notifyDataSetChanged()
    }
}