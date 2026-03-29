package com.example.myapplication.ui.theme

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.example.myapplication.R

/**
 * Lightweight SoundPool wrapper for UI sound effects.
 *
 * Sound files live in res/raw/:
 *   task_complete.ogg  — chime / success ding
 *   button_tap.ogg     — short pop
 *   swipe_action.ogg   — soft swoosh
 *   level_up.ogg       — fanfare / ascending tone
 *
 * All sounds are optional — missing resources are handled gracefully.
 */
class SoundManager(context: Context) {

    private val tag = "SoundManager"

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var soundComplete: Int = 0
    private var soundTap:      Int = 0
    private var soundSwipe:    Int = 0
    private var soundLevelUp:  Int = 0

    private var loaded = false

    init {
        pool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) loaded = true
        }
        soundComplete = loadSafe(context, R.raw.task_complete)
        soundTap      = loadSafe(context, R.raw.button_tap)
        soundSwipe    = loadSafe(context, R.raw.swipe_action)
        soundLevelUp  = loadSafe(context, R.raw.level_up)
    }

    private fun loadSafe(context: Context, resId: Int): Int {
        return try {
            pool.load(context, resId, 1)
        } catch (e: Exception) {
            Log.w(tag, "Sound resource not found (id=$resId), skipping: ${e.message}")
            0
        }
    }

    private fun play(soundId: Int, volume: Float = 0.8f) {
        if (soundId != 0) {
            pool.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    fun playComplete() = play(soundComplete, 0.9f)
    fun playTap()      = play(soundTap,      0.6f)
    fun playSwipe()    = play(soundSwipe,    0.7f)
    fun playLevelUp()  = play(soundLevelUp,  1.0f)

    fun release() {
        pool.release()
    }
}
