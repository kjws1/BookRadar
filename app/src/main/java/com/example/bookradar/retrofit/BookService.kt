package com.example.bookradar.retrofit

import com.example.bookradar.retrofit.model.BookListModel
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BookService {
    @GET("/v3/search/book")
    suspend fun getBooks(
        @Header("Authorization") authorization: String,
        @Query("query") query: String
    ): BookListModel
}