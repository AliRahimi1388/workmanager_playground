package com.example.workmanager_jetpackcompose_playground.repository

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET

interface FileApi {

    @GET("/pictures/2024/05/22/B/lsofyvjz.jpeg")
    suspend fun downloadImage(): Response<ResponseBody>

    companion object{
        val instance by lazy {
            Retrofit.Builder()
                .baseUrl("https://news-cdn.varzesh3.com")
                .build()
                .create(FileApi::class.java)
        }
    }

}