package com.example.workmanager_jetpackcompose_playground.repository

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url

interface FileApi {

    @GET
    suspend fun downloadImage(@Url uriString: String): Response<ResponseBody>

    companion object{
        val instance by lazy {
            Retrofit.Builder()
                .build()
                .create(FileApi::class.java)
        }
    }

}