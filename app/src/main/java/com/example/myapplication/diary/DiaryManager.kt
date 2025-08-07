package com.example.myapplication.diary


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

object DiaryManager {
    private val gson = Gson()

    fun toJson(entries: List<DiaryEntry>): String {
        return gson.toJson(entries)
    }

    fun fromJson(json: String): List<DiaryEntry> {
        val type = object : TypeToken<List<DiaryEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    fun generateId(): Long {
        return System.currentTimeMillis()
    }
}