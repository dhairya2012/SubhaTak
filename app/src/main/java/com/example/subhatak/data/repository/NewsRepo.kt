package com.example.subhatak.data.repository

import com.example.subhatak.data.datasource.NewsDatasource
import com.example.subhatak.data.model.NewsResponse
import com.example.subhatak.utils.ResourceState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

@Singleton
class NewsRepo @Inject constructor(private val newsDatasource: NewsDatasource) {
    fun geNewsHeadlines(country: String): Flow<ResourceState<NewsResponse>> {
        return flow {
            emit(ResourceState.Loading())
            val response = newsDatasource.getNewsHeadlines(country)
            if (response.isSuccessful && response.body() != null) {
                emit(ResourceState.Success(response.body()!!))
            } else {
                emit(ResourceState.Error(response.message()))
            }
        }.catch { e ->
            emit(ResourceState.Error(e.localizedMessage ?: "Something went wrong"))
        }
    }
}