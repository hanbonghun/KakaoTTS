package com.example.cd2

import android.Manifest
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener

class MainActivity : AppCompatActivity() {

    var speedRate:Float = 1.0F

    // SharedPreference : 스토리지 파일에 key-value쌍으로 저장하여 다른 액티비티에서도 사용할 수 있음
    //블루투스 리시버
    //http://jinyongjeong.github.io/2018/09/27/bluetoothpairing/
    //https://jung-max.github.io/2019/08/27/Android-Bluetooth/


    private val volumeChangeReceiver = object: BroadcastReceiver(){

        override fun onReceive(contxt: Context?, intent: Intent?) {
            val volumeSeekBar: SeekBar = findViewById(R.id.volumeSeeBar)

            var action = intent?.action //입력된 action

            if(action.equals("android.media.VOLUME_CHANGED_ACTION")) {
                val audio = getSystemService(AppCompatActivity.AUDIO_SERVICE) as AudioManager
                val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                volumeSeekBar.progress=currentVolume
            }
        }
    }

    //FIXME : 블루투스 기기 연결된 상태로 앱 켰을 때 출력 안되는 것 수정해야함
    private val bluetoothChangeReceiver = object : BroadcastReceiver() {

        override fun onReceive(contxt: Context?, intent: Intent?) {
            val bleOnOffBtn:ToggleButton = findViewById(R.id.bluetooth_on_off_btn)

            var action = intent?.action //입력된 action

            //블루투스 on/off 변화
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                var state =
                    intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                if (state == BluetoothAdapter.STATE_OFF) {
                    System.out.println("블루투스 off")
                    bleOnOffBtn.isChecked = false

                }else if  (state == BluetoothAdapter.STATE_ON){
                    System.out.println("블루투스 on")
                    bleOnOffBtn.isChecked = true
                }
            }

            //블루투스 연결 장치 변화
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                bleOnOffBtn.isChecked = true

                val device: BluetoothDevice? = intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) //연결된 장치
                var bleDeviceTextView : TextView= findViewById(R.id.connectedDevice)

                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //       ActivityCompat#requestPermissions
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT),1)
                    return
                }
                if (device != null) {
                    bleDeviceTextView.text = device.name
                    //bleDeviceTextView.text = device.name

                    Toast.makeText(
                        baseContext,
                        "Device is now Connected",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{

                    Toast.makeText(
                        baseContext,
                        "No Device is Connected",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                //Device와 TTS audio 연결
                device.createBond()

            }else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                bleOnOffBtn.isChecked = false
                var bleDeviceTextView : TextView= findViewById(R.id.connectedDevice)

                bleDeviceTextView.text = ""

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

        // SharedPreference : 스토리지 파일에 key-value쌍으로 저장하여 다른 액티비티에서도 사용할 수 있음
        val sharedPreference = getSharedPreferences("userSetting", MODE_PRIVATE ) //기능 사용 on off 를 위한 변수
        val editor = sharedPreference.edit() // SharedPreference 수정을 위한 변수

        editor.putString("func","ON") // 최초 기능 사용 on 으로 설정
        editor.putString("from","ON") // 발신자 on 으로 설정
        editor.putString("time","ON") // 시간 on 으로 설정
        editor.putString("content","ON") // 내용 on 으로 설정

        editor.commit() //commit 까지해야 반영

        val audioManager = this.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        var currentVolume =audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)  //현재 음량
        var maxVolume =audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        var funcOnOff : Switch = findViewById(R.id.func_on_off)
        var fromOnOff : Switch = findViewById(R.id.from_on_off)
        var timeOnOff  : Switch= findViewById(R.id.time_on_off)
        var contentOnOff : Switch = findViewById(R.id.content_on_off)


        val volumeSeekBar:SeekBar = findViewById(R.id.volumeSeeBar)
        volumeSeekBar.max =maxVolume
        volumeSeekBar.progress = currentVolume

        val bluetoothFilter = IntentFilter().apply{
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)//블루투스상태변화액션
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)//연결확인
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)//
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)//연결끊김확인
        }

        var volumeFilter =IntentFilter().apply {
            addAction("android.media.EXTRA_VOLUME_STREAM_TYPE")
            addAction("android.media.VOLUME_CHANGED_ACTION")
        }

        //TODO:
        //TODO: 반복 제거
        funcOnOff.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                System.out.println("스위치 on")
                editor.putString("func","ON")
            } else {
                System.out.println("스위치 off")
                editor.putString("func","OFF")
            }
            editor.commit()
        }
        fromOnOff.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                System.out.println("스위치 on")
                editor.putString("from","ON")
            } else {
                System.out.println("스위치 off")
                editor.putString("from","OFF")
            }
            editor.commit()
        }
        timeOnOff.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                System.out.println("스위치 on")
                editor.putString("time","ON")
            } else {
                System.out.println("스위치 off")
                editor.putString("time","OFF")
            }
            editor.commit()
        }
        contentOnOff.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                System.out.println("스위치 on")
                editor.putString("content","ON")
            } else {
                System.out.println("스위치 off")
                editor.putString("content","OFF")
            }
            editor.commit()
        }


        volumeSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })


        registerReceiver(bluetoothChangeReceiver,bluetoothFilter)//reiver와 filter연결
        registerReceiver(volumeChangeReceiver,volumeFilter)


        //https://www.geeksforgeeks.org/spinner-in-kotlin/
        initSpeedRateSpinner()

        //볼륨 변화 감지를 위해 contentobserver를 사용할 수 있음
//        var mSettingsContentObserver = SettingsContentObserver(this, Handler(),this)
//        applicationContext.contentResolver.registerContentObserver(
//            Settings.System.CONTENT_URI,
//            true,
//            mSettingsContentObserver
//        )

    }


    private fun initSpeedRateSpinner(){
        val speedList:Array<Float> = arrayOf(1.0F,2.0F,3.0F)
        val speedRateSpinner = findViewById<Spinner>(R.id.spinner)
        val speedAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,speedList)
        speedRateSpinner.adapter=speedAdapter

        //https://jeongupark-study-house.tistory.com/11
        speedRateSpinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                //set speedRate of TTS
                when(position){
                    0->speedRate = 1.0F
                    1->speedRate = 1.5F
                    2->speedRate = 2.0F
                }

                //TODO: send speedRate to NotificationListener
//                val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
//                intent.putExtra("SpeedRate",speedRate)
//                startActivity(intent)

                //show user speedRate
                Toast.makeText(this@MainActivity,
                    "speed set : $speedRate",Toast.LENGTH_SHORT).show()

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        val audioManager = this.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        val volumeSeekBar:SeekBar = findViewById(R.id.volumeSeeBar)
//        var currentVolume =audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)  //현재 음량
//
//        when (keyCode) {
//            KeyEvent.KEYCODE_VOLUME_DOWN,KeyEvent.KEYCODE_VOLUME_UP -> {
//                volumeSeekBar.setProgress(currentVolume)
//            }
//        }
//        return super.onKeyDown(keyCode, event)
//    }



    //TODO: 한번켜지면 꺼지지 않음
    fun isBluetoothEnabled(): Boolean {
        val myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return myBluetoothAdapter.isEnabled
    }

    //블루투스 연결 설정 버튼 클릭 시
    fun onClickBluetoothBtn(view: View) {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        startActivity(intent) // 블루투스 연결 설정 화면으로 이동
    }

    //TODO: 한번켜지면 꺼지지 않음
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
        val bleOnOffBtn:ToggleButton = findViewById(R.id.bluetooth_on_off_btn)

        super.onStart()

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //https://kobbi-reply.tistory.com/17
        if(Build.VERSION.SDK_INT >= 23){
            if(!notificationManager.isNotificationPolicyAccessGranted){ //알림 접근 권한이 허용 여부 확인
                val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                this.startActivity(intent)
            }else{
                //request permission 은 오직 한 번만
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED||ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    // Request the user to grant permission to read SMS messages
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS,Manifest.permission.BLUETOOTH_CONNECT), 2)
                }

            }
        }

        //블루투스 켜짐/꺼짐에 따라 버튼 on/off
        /*if(!isBluetoothEnabled()){
            bleOnOffBtn.isChecked = false
        }else{
            bleOnOffBtn.isChecked = true
        }*/
        bleOnOffBtn.isChecked = isBluetoothEnabled()

        //초기 볼륨
        val audioManager = this.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volumeSeekBar:SeekBar = findViewById(R.id.volumeSeeBar)
        var currentVolume =audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)  //현재 음량
        var maxVolume =audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        volumeSeekBar.max =maxVolume
        volumeSeekBar.progress = currentVolume

        //TODO: 핸드폰 옆의 볼륨을 높힐때 바로 media 볼륨이 잡히지 않는다


    }

}