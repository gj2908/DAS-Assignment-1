package com.example.core_sdk.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException
import java.util.concurrent.TimeUnit

internal data class PayloadRequest(val encryptedData: String)
internal data class ApiResponse(val status: String, val message: String)

internal interface SdkApi {
    @POST("api/v1/track")
    suspend fun sendData(@Body request: PayloadRequest): ApiResponse
}

// Interceptor for Retry Logic
internal class RetryInterceptor(private val maxRetries: Int) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var request = chain.request()
        var response: Response? = null
        var exception: IOException? = null

        while (attempt < maxRetries && (response == null || !response.isSuccessful)) {
            try {
                response?.close() // Close previous failed response
                response = chain.proceed(request)
            } catch (e: IOException) {
                exception = e
            }
            attempt++
        }
        return response ?: throw exception ?: IOException("Unknown network error")
    }
}

internal object ApiClient {
    // We'll use a public mock API service for testing
    private const val BASE_URL = "https://mockapi.example.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(RetryInterceptor(maxRetries = 3)) // Network failure retry logic
        .build()

    val api: SdkApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SdkApi::class.java)
    }
}