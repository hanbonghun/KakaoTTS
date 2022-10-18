package com.example.cd2

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar


//환경 설정 변화를 observe 하는 객체 (볼륨)
class SettingsContentObserver(var context: Context, handler: Handler?, activity: MainActivity) :
    ContentObserver(handler) {
    var previousVolume: Int
    val volumeSeekBar: SeekBar = activity.findViewById(R.id.volumeSeeBar)

    init {
        val audio = context.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC) //현재 볼륨
    }


    override fun deliverSelfNotifications(): Boolean {
        return super.deliverSelfNotifications()
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        System.out.println("변함")
        val audio = context.getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
        val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        val delta = previousVolume - currentVolume

        if (delta > 0) {
            System.out.println("볼륨 감소")
            previousVolume = currentVolume
            System.out.println(previousVolume)
            volumeSeekBar.progress=previousVolume
        } else if (delta < 0) {
            System.out.println("볼륨 증가")
            previousVolume = currentVolume
            System.out.println(previousVolume)
            volumeSeekBar.progress=previousVolume


        }
    }
}