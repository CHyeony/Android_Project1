package com.example.myapplication.diary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date


// 스프링의 Entity / DTO / VO 와 같은 역할
@Parcelize
data class DiaryEntry(
    val id: Long = System.currentTimeMillis(), // 고유 ID
    val title: String,                         // 일기 제목
    val date: String,                          // 작성 날짜 (예: "yyyy-MM-dd")
    val foodItems: List<FoodItem>,             // 음식 목록
    val totalCalorie: Int,                     // 총 칼로리
    val imagePath: String? = null
    // 이건 쓸 때 var diaryEntry = DiaryEntry("id","title sample","content ~~"...)
): Parcelable

//
//class DiaryEntry{
//    val id: Long = 0
//    val title: String
//    val content: String
//    val date: Date = Date()
//    val mood: String = "😊"
//
//    // 이건 쓸 때 val DiaryEntry = DiaryEntry() / DiaryEntry.id = "id"
//}