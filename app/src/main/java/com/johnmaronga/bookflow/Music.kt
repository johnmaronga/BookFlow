package com.johnmaronga.bookflow

import android.app.Application
import android.media.MediaPlayer
import com.johnmaronga.bookflow.workers.WorkManagerScheduler

class Music : Application() {
    private var backgroundMusic: MediaPlayer? = null
    private var isMuted = false

    override fun onCreate() {
        super.onCreate()
        setupBackgroundMusic()
        setupBackgroundTasks()
    }

    private fun setupBackgroundTasks() {
        // Schedule periodic book sync
        WorkManagerScheduler.scheduleSyncWork(this)
        
        // Schedule reading reminders (optional - can be enabled by user preference)
        // WorkManagerScheduler.scheduleReadingReminders(this)
    }

    private fun setupBackgroundMusic() {
        backgroundMusic = MediaPlayer.create(this, R.raw.background_music)
        backgroundMusic?.apply {
            isLooping = true
            setVolume(0.3f, 0.3f) // 30% volume for background
            start()
        }
    }

    fun toggleMusic() {
        isMuted = !isMuted
        backgroundMusic?.let { mediaPlayer ->
            if (isMuted) {
                mediaPlayer.setVolume(0f, 0f) // Mute
            } else {
                mediaPlayer.setVolume(0.3f, 0.3f) // Unmute
            }
        }
    }

    fun isMuted(): Boolean = isMuted

    override fun onTerminate() {
        super.onTerminate()
        backgroundMusic?.release()
        backgroundMusic = null
    }
}