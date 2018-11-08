package com.hellodemo;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.hellodemo.utils.TypefaceUtil;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.intercom.android.sdk.Intercom;


/**
 * Created by new user on 2/16/2018.
 */

public class MyApp extends Application {

    private String TAG = "HelloDemoMyApp";

    @Override
    public void onCreate() {
        super.onCreate();
        if(BuildConfig.BASE_URL.equals("https://beta.hello-demo.com/api") || BuildConfig.BASE_URL.equals("https://web.hello-demo.com/api")) {
            Intercom.initialize(this, "android_sdk-6f910a2cd2ef34e14cec0ff45c84787c69f6e7bc", "wy6u5pym");
        }
        Fabric.with(this, new Crashlytics());
        //TypefaceUtil.overrideFont(getApplicationContext(), "normal", "fonts/MemphisLTStd-Medium.otf"); // font from assets: "assets/fonts/Roboto-Regular.ttf

    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
