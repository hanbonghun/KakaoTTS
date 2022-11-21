package com.example.cd2;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class GlobalApplication extends Application {

    @Override
    public void onCreate() {
        KakaoSdk.init(this, getString(R.string.kakao_app_key));
        super.onCreate();
    }
}
