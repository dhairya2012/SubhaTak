package com.example.subhatak.data.model

data class NewsResponse(
    val status: String,
    val totalResults: String,
    val articles: List<Articles>
)

data class Articles(
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?,
    val source: Source?
)
data class Source(
    val id: String?,
    val name: String
)