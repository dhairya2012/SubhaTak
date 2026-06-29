package com.example.subhatak.data.datasource

import com.example.subhatak.data.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsDatasource {
    @GET("v2/top-headlines")
    suspend fun getNewsHeadlines(
        @Query("country") country: String,
    ): Response<NewsResponse>
}