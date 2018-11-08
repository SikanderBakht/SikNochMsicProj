package com.hellodemo;

/**
 * Created by Mahnoor on 20/04/2018.
 */


import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private String TAG = "HelloDemoMyFirebaseInstanceIDService";

    @Override
    public void onTokenRefresh() {

        //For registration of token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //To displaying token on logcat
        Log.v(TAG, "App FCM Token: " + refreshedToken);

    }

}