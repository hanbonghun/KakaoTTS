package com.example.cd2


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val sharedPreference =
            getSharedPreferences("userSetting", MODE_PRIVATE) //기능 사용 on off 를 위한 변수
        val editor = sharedPreference.edit() // SharedPreference 수정을 위한 변수

        var customButton: Button = findViewById(R.id.custom_button)

        val customSwitch : Switch = findViewById(R.id.custom_switch)
        customSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            customButton.isEnabled = isChecked
            val s = if(isChecked) "ON" else "OFF"
            editor.putString("func",s)
            editor.commit()
        }

        val funcState = sharedPreference?.getString("func","none")
        if(funcState =="none"){
            //Btn Setting
            editor.putString("func", "ON")
            editor.commit()
            customButton.isEnabled = true
            customSwitch.isChecked = true
        }else{
            customButton.isEnabled = funcState == "ON"
            customSwitch.isChecked = funcState == "ON"
        }

    }

    fun onClickCustomButton(v: View){
        /*var customButton: Button = findViewById(R.id.custom_button)
        customButton.isEnabled = v.isEnabled*/

        /*val sharedPreference =
            getSharedPreferences("userSetting", MODE_PRIVATE) //기능 사용 on off 를 위한 변수
        val editor = sharedPreference.edit() // SharedPreference 수정을 위한 변수

        var customButton: Button = findViewById(R.id.custom_button)
        customButton.isEnabled = v.isEnabled
        val s = if(v.isEnabled) "ON" else "OFF"
        editor.putString("func",s)
        editor.commit()*/
    }

    fun onClickSettingButton(v: View){
        val intent = Intent(applicationContext,OptionActivity::class.java)
        //intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}