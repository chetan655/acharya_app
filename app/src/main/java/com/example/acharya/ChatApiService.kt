//package com.example.acharya
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import java.util.concurrent.TimeUnit
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.Multipart
//import retrofit2.http.POST
//import retrofit2.http.Part
////import com.google.gson.annotations.SerializedName
//
//import com.google.gson.annotations.SerializedName // Make sure to add this import!
//
//data class ChatResponse(
//    val reply: String,
//    @SerializedName("thread_id")
//    val threadId: String // Changed to camelCase to make Android Studio happy
//)
//
//interface ChatApiService {
//    @Multipart
//    @POST("/chat")
//    suspend fun sendMessage(
//        @Part("question") question: RequestBody,
//        @Part("lat") lat: RequestBody,
//        @Part("long") long: RequestBody,
//        @Part("thread_id") threadId: RequestBody,
//        @Part image: MultipartBody.Part?
//    ): ChatResponse
//}
//
////object RetrofitClient {
////    private const val BASE_URL = "https://healthcare-agentic-chatbot.onrender.com/"
////
////    val api: ChatApiService by lazy {
////        Retrofit.Builder()
////            .baseUrl(BASE_URL)
////            .addConverterFactory(GsonConverterFactory.create())
////            .build()
////            .create(ChatApiService::class.java)
////    }
////}
//
//object RetrofitClient {
//    private const val BASE_URL = "https://healthcare-agentic-chatbot.onrender.com/"
//
//    // 1. Create the logger
//    private val loggingInterceptor = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY // This tells it to print the whole response
//    }
//
//    // 2. Add the logger to our client
//    private val okHttpClient = OkHttpClient.Builder()
//        .addInterceptor(loggingInterceptor) // <-- Added here
//        .connectTimeout(90, TimeUnit.SECONDS)
//        .readTimeout(90, TimeUnit.SECONDS)
//        .writeTimeout(90, TimeUnit.SECONDS)
//        .build()
//
//    val api: ChatApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ChatApiService::class.java)
//    }
//}

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory // Add this import
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

// You can delete the ChatResponse data class entirely since we aren't using it anymore!

interface ChatApiService {
    @Multipart
    @POST("/chat")
    suspend fun sendMessage(
        @Part("question") question: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("long") long: RequestBody,
        @Part("thread_id") threadId: RequestBody,
        @Part image: MultipartBody.Part?
    ): String // CHANGED: Now we expect a raw String back instead of a JSON object
}

object RetrofitClient {
    private const val BASE_URL = "https://healthcare-agentic-chatbot.onrender.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    val api: ChatApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) // MUST BE ADDED BEFORE GSON
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApiService::class.java)
    }
}