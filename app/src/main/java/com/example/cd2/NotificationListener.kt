package com.example.cd2

import android.app.Notification
import android.content.ContentValues.TAG
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
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.template.model.*
import com.kakao.sdk.user.UserApiClient
import java.text.SimpleDateFormat
import java.util.*


class NotificationListener : NotificationListenerService() {

    private var tts: TextToSpeech? =null
    lateinit var speechRecognizer:SpeechRecognizer
    var sharedPreference: SharedPreferences? = null
    var STTResult :String =""
    var receiver :String=""

    override fun onCreate() {
        super.onCreate()
        sharedPreference = getSharedPreferences("userSetting", MODE_PRIVATE);

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(recognitionListener())
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

        if (title != null) {
            receiver = title
        }
        if(funcState == "OFF") {
            Log.i("기능 사용 여부", "기능 사용 꺼져 있음, 음성 출력 x")
            return
        }


        if (funcState != null) {
            Log.i("기능: " ,funcState)
        }
        if (fromState != null) {
            Log.i("발신자: ",fromState)
        }
        if (timeState != null) {
            Log.i("시간: ",timeState)
        }
        if (contentState != null) {
            Log.i("내용: ",contentState)
        }

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
        if(sbn.packageName != "com.kakao.talk" || sbn.packageName.indexOf("messaging") == -1) return

        if (speedRate != null) {
            tts?.setSpeechRate(speedRate)
        }

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

    private  fun startSTT() {
        //https://stackoverflow.com/questions/52400852/how-to-start-speech-recognition-as-soon-the-text-to-speech-stops
        val mainHandler = Handler(Looper.getMainLooper())
        val myRunnable =
            Runnable {
                val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                }
                speechRecognizer.startListening(speechRecognizerIntent)
            } // This is your code
        mainHandler.post(myRunnable)
    }

    private fun recognitionListener() = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) = Toast.makeText(this@NotificationListener, "음성인식 시작", Toast.LENGTH_SHORT).show()
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onEndOfSpeech() {}
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
                        Log.i("친구목록: ", friends.elements.toString())
                        for(friend in friends.elements!!){
                            if(receiver == friend.profileNickname){
                                var receiverUuid = friend.uuid
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
                            }
                        }

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
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String) {
                        startSTT()
                    }
                    override fun onError(utteranceId: String) {}
                    override fun onStart(utteranceId: String) {
                        val mainHandler = Handler(Looper.getMainLooper())
                        val myRunnable =
                            Runnable {
                                speechRecognizer.stopListening()
                            } // This is your code
                        mainHandler.post(myRunnable)

                    }
                })
            }
        }

    }

    private fun speakTTS(string:String){
        if(string == null || string == ""){
            Log.i("NotificationListener", "speakTTS:null")
            return
        }
        tts?.speak(string,TextToSpeech.QUEUE_ADD,null,"test")
    }


    //https://www.tutorialkart.com/kotlin-android/android-text-to-speech-kotlin-example/
    override fun onDestroy() {
        if(tts != null){
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

}