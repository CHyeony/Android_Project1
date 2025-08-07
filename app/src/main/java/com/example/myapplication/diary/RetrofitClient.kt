package com.example.myapplication.network

// 1. BuildConfig import (실제 앱의 applicationId에 맞게 수정)
import com.example.myapplication.BuildConfig

// 2. OkHttp 및 HttpLoggingInterceptor import
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor // ***** 여기 import 확인 *****

// 3. Retrofit 관련 import (이미 있어야 함)
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val OPEN_FOOD_FACTS_BASE_URL = "https://world.openfoodfacts.org/"

    // FoodApiService 인터페이스가 동일 패키지 또는 올바르게 import 되었다고 가정
    // 예: import com.example.myapplication.network.FoodApiService 또는 동일 패키지 내 정의

    val openFoodFactsInstance: FoodApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply { // ***** HttpLoggingInterceptor 사용 *****
            // BuildConfig.DEBUG와 HttpLoggingInterceptor.Level 사용
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(OPEN_FOOD_FACTS_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FoodApiService::class.java) // FoodApiService 인터페이스가 여기에 필요
    }
}
