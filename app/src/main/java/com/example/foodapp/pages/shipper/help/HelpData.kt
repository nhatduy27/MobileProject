package com.example.foodapp.pages.shipper.help

data class HelpCategory(
    val id: String,
    val icon: String,
    val title: String,
    val description: String,
    val articles: List<HelpArticle>
)

data class HelpArticle(
    val id: String,
    val title: String,
    val content: String
)

data class FAQ(
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)
