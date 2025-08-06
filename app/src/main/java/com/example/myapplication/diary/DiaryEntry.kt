package com.example.myapplication.diary

import java.util.Date


// 스프링의 Entity / DTO / VO 와 같은 역할
data class DiaryEntry(
    val id: Long = 0,
    val title: String,
    val content: String,
    val date: Date = Date(),
    val mood: String = "😊"
    // 이건 쓸 때 var diaryEntry = DiaryEntry("id","title sample","content ~~"...)
)

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