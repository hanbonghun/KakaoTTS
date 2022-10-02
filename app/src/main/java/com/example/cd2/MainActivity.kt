package com.example.cd2

import android.Manifest
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View


class MainActivity : AppCompatActivity() {

    private fun isNotificationServiceEnabled(c: Context): Boolean {
        val pkgName: String = c.getPackageName()
        val flat = Settings.Secure.getString(
            c.getContentResolver(),
            "enabled_notification_listeners"
        )
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //알림 접근 허용
//        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if(Build.VERSION.SDK_INT >= 23){
//            if(!notificationManager.isNotificationPolicyAccessGranted){
//                this.startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
//            }
//        }

//        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
//            startActivity(intent)


        // See if the user has not granted permission to read his or her text messages


        // request permission 은 오직 한 번만
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
//            // Request the user to grant permission to read SMS messages
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_SMS), 2);
//            System.out.println("Permission Denied")
//        }
        // See if the user has not granted permission to read his or her text messages




    }

    //블루투스 연결 설정 버튼 클릭 시
    fun onClickBluetoothBtn(view: View) {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intent); // 블루투스 연결 설정 화면으로 이동
    }

    //알림 설정 버튼 클릭 시
    fun onClickNotiSettingBtn(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName) //해당 앱 알림 설정 화면으로 이동
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
//                Log.d("태그" val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= 23){
            if(!notificationManager.isNotificationPolicyAccessGranted){ //알림 접근 권한이 허용 여부 확인
                this.startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }else{
//      request permission 은 오직 한 번만
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED) {
            // Request the user to grant permission to read SMS messages
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS), 2);
        }
            }
        }
    }

}