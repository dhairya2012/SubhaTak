package com.example.subhatak.data.api

import com.example.subhatak.data.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {
    @GET("v2/top-headlines")
    suspend fun getNewsHeadlines(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String = "34a08f7411ee4a34bbfd4edf78914fe0"
    ): Response<NewsResponse>
}