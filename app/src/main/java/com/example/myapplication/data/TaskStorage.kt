package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.myapplication.data.models.Task
import com.example.myapplication.data.models.TaskCategory
import com.example.myapplication.data.models.TaskDifficulty
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * A storage class that persists tasks to SharedPreferences using JSON serialization
 */
class TaskStorage(context: Context) {
    
    companion object {
        private const val PREF_NAME = "task_data"
        private const val KEY_TASKS = "tasks"
        private const val TAG = "TaskStorage"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson: Gson
    
    init {
        // Create Gson instance with custom type adapters for LocalDate and LocalTime
        gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            .create()
    }
    
    /**
     * Save the list of tasks to SharedPreferences
     */
    fun saveTasks(tasks: List<Task>) {
        try {
            val json = gson.toJson(tasks)
            prefs.edit().putString(KEY_TASKS, json).apply()
            Log.d(TAG, "Saved ${tasks.size} tasks to storage")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving tasks: ${e.message}", e)
        }
    }
    
    /**
     * Load the list of tasks from SharedPreferences
     */
    fun loadTasks(): List<Task> {
        try {
            val json = prefs.getString(KEY_TASKS, null) ?: return emptyList()
            val type = object : TypeToken<List<Task>>() {}.type
            val tasks = gson.fromJson<List<Task>>(json, type)
            Log.d(TAG, "Loaded ${tasks.size} tasks from storage")
            return tasks
        } catch (e: Exception) {
            Log.e(TAG, "Error loading tasks: ${e.message}", e)
            return emptyList()
        }
    }
    
    /**
     * Clear all stored tasks
     */
    fun clearTasks() {
        prefs.edit().remove(KEY_TASKS).apply()
        Log.d(TAG, "Cleared all tasks from storage")
    }
}

/**
 * Custom JSON adapter for LocalDate
 */
class LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    override fun serialize(src: LocalDate, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDate {
        return LocalDate.parse(json.asString)
    }
}

/**
 * Custom JSON adapter for LocalTime
 */
class LocalTimeAdapter : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    override fun serialize(src: LocalTime, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalTime {
        return LocalTime.parse(json.asString)
    }
} 