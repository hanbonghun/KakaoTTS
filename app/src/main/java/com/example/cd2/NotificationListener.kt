package com.example.cd2

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class NotificationListener : NotificationListenerService() {

    private var tts: TextToSpeech? =null


    override fun onListenerConnected() {
        super.onListenerConnected()
        initTextToSpeech()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.i("NotificationListener", " onNotificationPosted() - $sbn")
        Log.i("NotificationListener", " PackageName:" + sbn.packageName)
        Log.i("NotificationListener", " PostTime:" + sbn.postTime)
        val notification: Notification = sbn.notification
        val extras: Bundle = notification.extras
        val title= extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
        Log.i("NotificationListener", " Title:$title")
        Log.i("NotificationListener", " Text:$text")
        Log.i("NotificationListener", "Sub Text:$subText")
        Log.i("NotificationListener", "Time:" + sbn.postTime)
        Log.i("NotificationListener", "Time:" + sbn.user)

        //https://m.blog.naver.com/yuyyulee/221531478175
        //TODO: 발신자, 발신내용, 발신시간 switch에 따라 onOff
        //TODO: 목소리 종류? https://stackoverflow.com/questions/9815245/android-text-to-speech-male-voice

        //모든 notification을 잡기 때문에 필요한 것만 구분해야함
        if(sbn.packageName != "com.kakao.talk" && sbn.packageName.indexOf("messaging") == -1) return

        //TODO: application의 speedRate와 연결하기
        tts?.setSpeechRate(1.0F)//1.0기본 float


        if(title != null){
            // title: 카카오톡방이름
            //이모티콘(유니코드)무시 필요?
            //발신자: title로 대체?
            if(title.toString() == "나"){
                return
            }
            speakTTS(title.toString())
        }
        if(text != null){
            // 카카오톡 방 내용
            //TODO: 사진, 동영상, 파일 무시 필요?
            var t=text.toString()
            if(t == "사진" || t == "동영상을 보냈습니다."){
                return
            }
            //너무 긴 경우 무시
            if(t.length >= 20){
                t.substring(20)
                t += "   이상입니다."
            }
            speakTTS(t)
        }
        //if(subText != null)speakTTS(subText.toString()) // "4개의 안읽은 메시지"

        //날짜
        //https://soft.plusblog.co.kr/62
        //val sdf = SimpleDateFormat("HH시 MM분")
        //val postTime = sdf.format(sbn.postTime)
        //if(postTime != null)speakTTS(postTime.toString())

    }

    private fun initTextToSpeech() {



        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.KOREA)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "language not supported", Toast.LENGTH_SHORT).show()
                    return@TextToSpeech
                }

            }
        }

        //TODO: change TTS Engine
        //https://stackoverflow.com/questions/7362534/how-to-programmatically-change-tts-default-engine
        //List<TextToSpeech.EngineInfo> engineInfoList = textToSpeech.getEngines()
        //.setEngineByPackageName deprecated
        //public TextToSpeech (Context context,
        //                TextToSpeech.OnInitListener listener,
        //                String engine)
        //TODO: application onCreated할때 list를 spinner에 넣어줘야 함
        //      선택시 set Engine
        val engineList : List<TextToSpeech.EngineInfo> = tts!!.engines
        Log.i("NotificationListener", "Engine: $engineList")
//        EngineInfo{name=com.samsung.SMT}
//        EngineInfo{name=com.google.android.tts}


    }

    private fun speakTTS(string:String){
        if(string == null || string == ""){
            Log.i("NotificationListener", "speakTTS:null")
            return
        }
        tts?.speak(string,TextToSpeech.QUEUE_ADD,null,null)
    }

    //https://www.tutorialkart.com/kotlin-android/android-text-to-speech-kotlin-example/
    override fun onDestroy() {
        if(tts != null){
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
//    2022-10-09 09:45:50.819 2790-2790/com.example.cd2 I/NotificationListener: Sub Text:null
//    2022-10-09 09:45:53.098 2877-10286/? D/androidtc: Initializing SystemTextClassifier, type = System
//    2022-10-09 09:45:53.299 3126-3361/? I/Launcher.NotificationListener: notificationIsValidForUI : com.kakao.talk missingTitleAndText : true isGroupHeader : true
//    2022-10-09 09:45:53.307 2790-2790/com.example.cd2 I/NotificationListener:  Text:null
//    2022-10-09 09:45:53.307 2790-2790/com.example.cd2 I/NotificationListener: Sub Text:1개의 안 읽은 메시지
//    2022-10-09 09:45:53.326 3126-3361/? I/Launcher.NotificationListener: notificationIsValidForUI : com.kakao.talk missingTitleAndText : false isGroupHeader : false
//    2022-10-09 09:45:53.336 2790-2790/com.example.cd2 I/NotificationListener:  Text:,ㅇㅇ
//    2022-10-09 09:45:53.336 2790-2790/com.example.cd2 I/NotificationListener: Sub Text:null

//    onNotificationPosted : com.kakao.talk number : 0
//    2022-10-09 19:14:27.147  3126-3361  Launcher.N...onListener pid-3126                             I  notificationIsValidForUI : com.kakao.talk missingTitleAndText : true isGroupHeader : true
//    2022-10-09 19:14:27.159 24798-24798 NotificationListener    com.example.cd2                      I   onNotificationPosted() - StatusBarNotification(pkg=com.kakao.talk user=UserHandle{0} id=1 tag=null key=0|com.kakao.talk|1|null|10317: Notification(channel=quiet_new_message shortcut=null contentView=null vibrate=null sound=null defaults=0x0 flags=0x200 color=0xff4d3e36 category=msg groupKey=chat_message vis=PRIVATE semFlags=0x0 semPriority=0 semMissedCount=0))
//    2022-10-09 19:14:27.159 24798-24798 NotificationListener    com.example.cd2                      I   PackageName:com.kakao.talk
//    2022-10-09 19:14:27.159 24798-24798 NotificationListener    com.example.cd2                      I   PostTime:1665310466929
//    2022-10-09 19:14:27.160 24798-24798 NotificationListener    com.example.cd2                      I   Title:null
//    2022-10-09 19:14:27.160 24798-24798 NotificationListener    com.example.cd2                      I   Text:null
//    2022-10-09 19:14:27.161 24798-24798 NotificationListener    com.example.cd2                      I  Sub Text:3개의 안 읽은 메시지
//    2022-10-09 19:14:27.161 24798-24798 NotificationListener    com.example.cd2                      I  Time:1665310466929
//    2022-10-09 19:14:27.161 24798-24798 NotificationListener    com.example.cd2                      I  Time:UserHandle{0}
//    2022-10-09 19:14:27.168  3126-3126  Launcher.N...onListener pid-3126                             I  onNotificationPosted : com.kakao.talk number : 3
//    2022-10-09 19:14:27.169  3126-3361  Launcher.N...onListener pid-3126                             I  notificationIsValidForUI : com.kakao.talk missingTitleAndText : false isGroupHeader : false
//    2022-10-09 19:14:27.195 24798-24798 NotificationListener    com.example.cd2                      I   onNotificationPosted() - StatusBarNotification(pkg=com.kakao.talk user=UserHandle{0} id=2 tag=100025487382719 key=0|com.kakao.talk|2|100025487382719|10317: Notification(channel=quiet_new_message shortcut=4729f6957d99d466eb0b05f552319a0531a64a8728eab27a73178a0624c85fde contentView=null vibrate=null sound=null tick defaults=0x0 flags=0x10 color=0xff4d3e36 category=msg groupKey=chat_message sortKey=9223370371544309401 actions=2 vis=PRIVATE semFlags=0x0 semPriority=0 semMissedCount=0))
//    2022-10-09 19:14:27.196 24798-24798 NotificationListener    com.example.cd2                      I   PackageName:com.kakao.talk
//    2022-10-09 19:14:27.196 24798-24798 NotificationListener    com.example.cd2                      I   PostTime:1665310466942
//    2022-10-09 19:14:27.196 24798-24798 NotificationListener    com.example.cd2                      I   Title:나
//    2022-10-09 19:14:27.196 24798-24798 NotificationListener    com.example.cd2                      I   Text:Good Morning
//    2022-10-09 19:14:27.196 24798-24798 NotificationListener    com.example.cd2                      I  Sub Text:null
//    2022-10-09 19:14:27.196 24798-24798 NotificationListener    com.example.cd2                      I  Time:1665310466942
//    2022-10-09 19:14:27.196 24798-24798 NotificationListener    com.example.cd2                      I  Time:UserHandle{0}

//    onNotificationPosted() - StatusBarNotification(pkg=com.samsung.android.messaging user=UserHandle{0} id=123 tag=com.samsung.android.messaging:MESSAGE_RECEIVED:176 key=0|com.samsung.android.messaging|123|com.samsung.android.messaging:MESSAGE_RECEIVED:176|10125: Notification(channel=CHANNEL_ID_SMS_MMS shortcut=7f8701ab82378d31 contentView=null vibrate=null sound=null tick defaults=0x0 flags=0x10 color=0xff3e91ff category=msg groupKey=MESSAGE_RECEIVED sortKey=9223370371543499140 actions=3 vis=PRIVATE semFlags=0x2008 semPriority=0 semMissedCount=1))
//    2022-10-09 19:27:57.807 25415-25415 NotificationListener    com.example.cd2                      I   PackageName:com.samsung.android.messaging
//    2022-10-09 19:27:57.807 25415-25415 NotificationListener    com.example.cd2                      I   PostTime:1665311277505
//    2022-10-09 19:27:57.807 25415-25415 NotificationListener    com.example.cd2                      I   Title:⁨나⁩
//    2022-10-09 19:27:57.807 25415-25415 NotificationListener    com.example.cd2                      I   Text:집
//    2022-10-09 19:27:57.808 25415-25415 NotificationListener    com.example.cd2                      I  Sub Text:null
//    2022-10-09 19:27:57.808 25415-25415 NotificationListener    com.example.cd2                      I  Time:1665311277505
//    2022-10-09 19:27:57.808 25415-25415 NotificationListener    com.example.cd2                      I  Time:UserHandle{0}
}