package com.example.myapplication.diary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodItem(
    val name: String,
    val calorie: Int,
    val confidence: Float
) : Parcelable