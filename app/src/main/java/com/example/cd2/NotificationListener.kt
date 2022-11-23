package com.example.cd2

import android.app.Notification
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.template.model.*
import com.kakao.sdk.user.UserApiClient
import java.text.SimpleDateFormat
import java.util.*


class NotificationListener : NotificationListenerService() {

    private var tts: TextToSpeech? =null
    var sharedPreference: SharedPreferences? = null
    var STTResult :String =""
    override fun onCreate() {
        super.onCreate()
        sharedPreference = getSharedPreferences("userSetting", MODE_PRIVATE);

    }
    override fun onListenerConnected() {
        super.onListenerConnected()
        initTextToSpeech()
    }

    //TODO: 각 기능 on off state에 따라 음성 출력값 다르게
    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val notification: Notification = sbn.notification
        val extras: Bundle = notification.extras
        val title= extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)

        //각 기능 on off 값을 변수에 저장함
        // "ON" => 사용  , "OFF" => 미사용
        val funcState = sharedPreference?.getString("func","none")   // none은 func에 해당하는 값이 없을 때 default
        val fromState = sharedPreference?.getString("from","none")
        val timeState = sharedPreference?.getString("time","none")
        val contentState = sharedPreference?.getString("content","none")
        val speedRate = sharedPreference?.getFloat("speed",1.0F)

        if(funcState == "OFF") {
            System.out.println("기능 사용 꺼져있음, 음성 출력 x ")
            return
        }

        System.out.println("기능" +funcState)
        System.out.println("발신자"+fromState)
        System.out.println("타임"+timeState)
        System.out.println("컨텐츠"+contentState)

        Log.i("NotificationListener", " onNotificationPosted() - $sbn")
        Log.i("NotificationListener", " PackageName:" + sbn.packageName)
        Log.i("NotificationListener", " PostTime:" + sbn.postTime)
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
        if (speedRate != null) {
            tts?.setSpeechRate(speedRate)
        }//1.0기본 float

        var t =""
        if(title != null){
            // title: 카카오톡방이름
            //이모티콘(유니코드)무시 필요?
            //발신자: title로 대체?
            if(title.toString() == "나"){
                return
            }
           if(fromState.equals("ON"))  t+=title.toString()
            t+="....."

            if(text != null && contentState.equals("ON")){
                // 카카오톡 방 내용
                //TODO: 사진, 동영상, 파일 무시 필요?
                t+=text.toString()
                if(t == "사진" || t == "동영상을 보냈습니다."){
                    return
                }
                //너무 긴 경우 무시
                if(t.length >= 20){
                    t.substring(20)
                    t += "   이상입니다."
                }



            }

            if(timeState.equals("ON")) {
                val sdf = SimpleDateFormat("HH시 MM분")
                val postTime = sdf.format(sbn.postTime)
                t+= "....."+postTime.toString()

            }
            t+="....답장을 원하시면 내용을 말씀하세요"
        }



        speakTTS(t)


        //if(subText != null)speakTTS(subText.toString()) // "4개의 안읽은 메시지"

        //날짜
        //https://soft.plusblog.co.kr/62
        //val sdf = SimpleDateFormat("HH시 MM분")
        //val postTime = sdf.format(sbn.postTime)
        //if(postTime != null)speakTTS(postTime.toString())

    }
    val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    }

    var speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
    private  fun startSTT() {
        while(tts?.isSpeaking()==true){}
     speechRecognizer.setRecognitionListener(recognitionListener())
        speechRecognizer.startListening(speechRecognizerIntent)


    }

    private fun recognitionListener() = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) = Toast.makeText(this@NotificationListener, "음성인식 시작", Toast.LENGTH_SHORT).show()

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onPartialResults(partialResults: Bundle?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onBeginningOfSpeech() {

        }

        override fun onEndOfSpeech() {

        }

        override fun onError(error: Int) {
            when(error) {
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> Toast.makeText(this@NotificationListener, "퍼미션 없음", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResults(results: Bundle) {


            Toast.makeText(this@NotificationListener, results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!![0], Toast.LENGTH_SHORT).show()
            STTResult =results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!![0]


            val text = TextTemplate(
                text = STTResult.trimIndent(),
                link = Link(
                    webUrl = "https://developers.kakao.com",
                    mobileWebUrl = "https://developers.kakao.com"
                )
            )

//            if (ShareClient.instance.isKakaoTalkSharingAvailable(this@NotificationListener)) {
//                // 카카오톡으로 카카오톡 공유 가능
//                ShareClient.instance.shareDefault(this@NotificationListener, text) { sharingResult, error ->
//                    if (error != null) {
//                        Log.e(TAG, "카카오톡 공유 실패", error)
//                    }
//                    else if (sharingResult != null) {
//
//                        Log.d(TAG, "카카오톡 공유 성공 ${sharingResult.intent}")
//                        startActivity(sharingResult.intent)
//
//                        // 카카오톡 공유에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
//                        Log.w(TAG, "Warning Msg: ${sharingResult.warningMsg}")
//                        Log.w(TAG, "Argument Msg: ${sharingResult.argumentMsg}")
//                    }
//                }
//            }
            TalkApiClient.instance.friends { friends, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡 친구 목록 가져오기 실패", error)
                }
                else {
                    Log.d(TAG, "카카오톡 친구 목록 가져오기 성공 \n${friends!!.elements?.joinToString("\n")}")

                    if (friends.elements?.isEmpty() == true) {
                        Log.e(TAG, "메시지를 보낼 수 있는 친구가 없습니다")
                    }
                    else {
                        System.out.println(friends.elements);
                        var receiverUuid = friends.elements?.get(0)?.uuid
                        var receiverUuids: List<String> = listOf(receiverUuid) as List<String>
                        var template =text

                        TalkApiClient.instance.sendDefaultMessage(receiverUuids, template) { result, error ->
                                if (error != null) {
                                    Log.e(TAG, "메시지 보내기 실패", error)
                                }
                                else if (result != null) {
                                    Log.i(TAG, "메시지 보내기 성공 ${result.successfulReceiverUuids}")

                                    if (result.failureInfos != null) {
                                        Log.d(TAG, "메시지 보내기에 일부 성공했으나, 일부 대상에게는 실패 \n${result.failureInfos}")
                                    }
                                }
                            }

                        // 서비스에 상황에 맞게 메시지 보낼 친구의 UUID를 가져오세요.
                        // 이 예제에서는 친구 목록을 화면에 보여주고 체크박스로 선택된 친구들의 UUID 를 수집하도록 구현했습니다.
//                        FriendsActivity.startForResult(
//                            context,
//                            friends.elements.map { PickerItem(it.uuid, it.profileNickname, it.profileThumbnailImage) }
//                        ) { selectedItems ->
//                            if (selectedItems.isEmpty()) return@startForResult
//                            Log.d(TAG, "선택된 친구:\n${selectedItems.joinToString("\n")}")
//
//
//                            // 메시지 보낼 친구의 UUID 목록
//                            val receiverUuids = selectedItems
//
//                            // Feed 메시지
//                            val template = defaultFeed
//
//                            // 메시지 보내기
//                            TalkApiClient.instance.sendDefaultMessage(receiverUuids, template) { result, error ->
//                                if (error != null) {
//                                    Log.e(TAG, "메시지 보내기 실패", error)
//                                }
//                                else if (result != null) {
//                                    Log.i(TAG, "메시지 보내기 성공 ${result.successfulReceiverUuids}")
//
//                                    if (result.failureInfos != null) {
//                                        Log.d(TAG, "메시지 보내기에 일부 성공했으나, 일부 대상에게는 실패 \n${result.failureInfos}")
//                                    }
//                                }
//                            }
//                        }
                    }
                }
            }

        }
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
    }

    private fun speakTTS(string:String){
        speechRecognizer.stopListening()
        if(string == null || string == ""){
            Log.i("NotificationListener", "speakTTS:null")
            return
        }
        tts?.speak(string,TextToSpeech.QUEUE_ADD,null,null)
        startSTT()

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