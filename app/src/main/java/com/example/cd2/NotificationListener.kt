package com.example.cd2

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationListener : NotificationListenerService() {


    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.i("NotificationListener", " onNotificationPosted() - $sbn")
        Log.i("NotificationListener", " PackageName:" + sbn.packageName)
        Log.i("NotificationListener", " PostTime:" + sbn.postTime)
        val notificatin: Notification = sbn.notification
        val extras: Bundle = notificatin.extras
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
        Log.i("NotificationListener", " Title:$title")
        Log.i("NotificationListener", " Text:$text")
        Log.i("NotificationListener", "Sub Text:$subText")
    }
}