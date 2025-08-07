package com.example.myapplication.network // network 패키지 생성 권장

import com.example.myapplication.diary.FoodItem // 이전 단계에서 만든 FoodItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path // 바코드 검색 시
import retrofit2.http.Query

// Open Food Facts API 응답 모델 (필요한 부분만 단순화)
data class OpenFoodFactsProductResponse(
    val product: Product?,
    val status: Int,
    val status_verbose: String
)

data class Product(
    val product_name: String?,
    // val product_name_en: String?, // 영어 이름 등 필요에 따라 추가
    val nutriments: NutrimentsData?
    // val image_front_url: String? // 이미지 URL
)

data class NutrimentsData(
    // 칼로리는 'energy-kcal_100g' 또는 'energy_100g' (kJ) 등으로 제공될 수 있음. API 문서 확인 필요.
    // 여기서는 'energy-kcal_100g'로 가정 (100g당 kcal)
    // 필드명에 하이픈(-)이 있으면 @SerializedName 사용
    @com.google.gson.annotations.SerializedName("energy-kcal_100g")
    val energyKcal100g: Double?,

    @com.google.gson.annotations.SerializedName("energy-kcal") // 'energy-kcal' 필드도 있을 수 있음
    val energyKcal: Double?
    // 다른 영양 정보도 필요하면 추가 (단백질, 탄수화물, 지방 등)
)


interface FoodApiService {
    // 제품 이름으로 검색 (Open Food Facts는 검색 기능이 제한적일 수 있음)
    // https://documenter.getpostman.com/view/8470508/SVtZWq6chF#3535f694-e698-4504-8991-6020af9a1174
    // Text search (search.pl) is more powerful
    @GET("cgi/search.pl")
    fun searchFoodByName(
        @Query("search_terms") foodName: String,
        @Query("search_simple") searchSimple: Int = 1, // Simple search
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1, // JSON 응답 요청
        @Header("User-Agent") userAgent: String = "MyFoodDiaryApp - Android - Version 1.0 - JOCHAEYEON" // 권장 User-Agent
    ): Call<OpenFoodFactsSearchResponse> // 검색 결과는 다른 모델 필요

    // 바코드 기반 제품 조회 (더 정확함)
    @GET("api/v0/product/{barcode}.json")
    fun getFoodByBarcode(
        @Path("barcode") barcode: String,
        @Header("User-Agent") userAgent: String = "MyFoodDiaryApp - Android - Version 1.0 - JOCHAEYEON"
    ): Call<OpenFoodFactsProductResponse>
}

// 이름 검색 시 응답 모델 (필요한 부분만)
data class OpenFoodFactsSearchResponse(
    val products: List<Product>?,
    val count: Int
)
