package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.myapplication.data.models.UserProfile
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class UserProfileStorage(context: Context) {

    companion object {
        private const val PREF_NAME = "user_profile_data"
        private const val KEY_PROFILE = "profile"
        private const val TAG = "UserProfileStorage"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder().create()

    fun saveProfile(profile: UserProfile) {
        try {
            val json = gson.toJson(profile)
            prefs.edit().putString(KEY_PROFILE, json).apply()
            Log.d(TAG, "Saved user profile (level=${profile.level}, xp=${profile.totalXp})")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile: ${e.message}", e)
        }
    }

    fun loadProfile(): UserProfile {
        return try {
            val json = prefs.getString(KEY_PROFILE, null) ?: return UserProfile()
            gson.fromJson(json, UserProfile::class.java) ?: UserProfile()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user profile: ${e.message}", e)
            UserProfile()
        }
    }
}
