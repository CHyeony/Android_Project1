package com.example.myapplication.diary

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DiaryManager private constructor(context: Context) { // 생성자를 private으로

    private val sharedPreferences = context.getSharedPreferences("DiaryPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val DIARY_ENTRIES_KEY = "diary_entries"

    fun addDiaryEntry(entry: DiaryEntry) {
        val entries = getAllDiaryEntries().toMutableList()
        entries.add(0, entry) // 최신 항목을 맨 앞에 추가
        saveDiaryEntries(entries)
    }

    fun getAllDiaryEntries(): List<DiaryEntry> {
        val json = sharedPreferences.getString(DIARY_ENTRIES_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<DiaryEntry>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    private fun saveDiaryEntries(entries: List<DiaryEntry>) {
        val json = gson.toJson(entries)
        sharedPreferences.edit().putString(DIARY_ENTRIES_KEY, json).apply()
    }

    // 싱글톤 구현 (Context를 사용하므로 Application Context 사용 권장)
    companion object {
        @Volatile
        private var INSTANCE: DiaryManager? = null

        fun getInstance(context: Context): DiaryManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DiaryManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}