package com.example.cd2

import android.Manifest
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton


class MainActivity : AppCompatActivity() {

    val bluetoothBroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceive(contxt: Context?, intent: Intent?) {
            val bleOnOffBtn:ToggleButton = findViewById(R.id.bluetooth_on_off_btn)

            var action = intent?.getAction()

            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                var state =
                    intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    System.out.println("블루투스 off")
                    bleOnOffBtn.isChecked = false

                }else if  (state == BluetoothAdapter.STATE_ON){
                    System.out.println("블루투스 on")
                    bleOnOffBtn.isChecked = true
                }
            }
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                val device: BluetoothDevice? = intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) //연결된 장치
                bleOnOffBtn.isChecked = true
                var bleDeviceTextView : TextView= findViewById(R.id.connectedDevice)
                    bleDeviceTextView.setText("connected")
                Toast.makeText(
                    baseContext,
                    "Device is now Connected",
                    Toast.LENGTH_SHORT
                ).show()
                System.out.println(device)
            }else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                bleOnOffBtn.isChecked = false

                System.out.println("해제")
                Toast.makeText(
                    baseContext,
                    "Device is disconnected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val filter = IntentFilter().apply{
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        registerReceiver(bluetoothBroadcastReceiver,filter)

    }


    fun isBluetoothEnabled(): Boolean {
        val myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(myBluetoothAdapter.isEnabled()){
            return true
        }else{
            return false
        }
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
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED||ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    // Request the user to grant permission to read SMS messages
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS,Manifest.permission.BLUETOOTH_CONNECT), 2);
                }

            }
        }
    }


}