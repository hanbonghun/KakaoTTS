package com.example.cd2

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class RequestPermissionAcitivty : AppCompatActivity() {

    val PERMISSION_ALL = 1
    var PERMISSIONS =  arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.BLUETOOTH_CONNECT)
    var requestCount = 0;

    fun hasPermissions(context: Context?,  permissions: Array<String>?): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission!!.toString()
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        if(Build.VERSION.SDK_INT<=30){
            PERMISSIONS = arrayOf(Manifest.permission.RECORD_AUDIO)
        }

        var requestBtn: TextView = findViewById(R.id.request_btn)

        requestBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!hasPermissions(this, PERMISSIONS)) {
                    requestCount++
                    if(requestCount>2) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)

                    }else {ActivityCompat.requestPermissions(this, PERMISSIONS!!, PERMISSION_ALL)}
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(Build.VERSION.SDK_INT<=30 &&  grantResults[0]== PackageManager.PERMISSION_GRANTED )  {var intent = Intent( this, MainActivity::class.java)
            startActivity(intent)
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)}
        else if(grantResults.size > 0  && grantResults[0]== PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED) {
            var intent = Intent( this, MainActivity::class.java)
            startActivity(intent)
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onStart() {
        super.onStart()
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 23) {
            if (!notificationManager.isNotificationPolicyAccessGranted) { //알림 접근 권한이 허용 여부 확인
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                this.startActivity(intent)
            }
        }

        if (hasPermissions(this, PERMISSIONS)) {
            var intent = Intent( this, MainActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onBackPressed() {
        this.finishAffinity()
        super.onBackPressed()
    }
}