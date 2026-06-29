package com.example.subhatak.data.datasource

import com.example.subhatak.data.api.ApiServices
import com.example.subhatak.data.model.NewsResponse
import retrofit2.Response
import javax.inject.Inject

class NewsDatasourceImpl @Inject constructor(private val apiServices: ApiServices) :
    NewsDatasource {
    override suspend fun getNewsHeadlines(country: String): Response<NewsResponse> {
        return apiServices.getNewsHeadlines(country)

    }

}