package com.hellodemo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Created by Mahnoor on 11/04/2018.
 */

public class StorageActivity extends AppCompatActivity {

    private TextView txtProgress, txtProgressBack;
    private ProgressBar progressBar;
    private int pStatus = 0;
    private Handler handler = new Handler();
    private UserModel mUserModel;

    private String TAG = "HelloDemoStorageActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.storage);

        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);


        txtProgress = (TextView) findViewById(R.id.txtProgress);
        txtProgressBack = (TextView) findViewById(R.id.txtProgressBack);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        findViewById(R.id.back_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        String url = "";
        url = BuildConfig.BASE_URL + "/user/storage";


        VolleyRequest volleyRequest = new VolleyRequest(getApplicationContext(), url, "StorageActivity", true);
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(mUserModel.getId()));


        Log.v(TAG, "StorageActivity API Called:\n"
                + "URL: " + url + "\n"
                + "Params: " + params.toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("StorageActivity", "StorageActivity API Called:\n"
                        + response.toString());

                if (!response.optBoolean("error", true)) {
                    try {
                        response = response.getJSONObject("message");
                        Double used = response.getDouble("used") / 1024;
                        Double total = response.getDouble("total") / 1024;

                        txtProgress.setText(roundTwoDecimals(used) + "gb");
                        txtProgressBack.setText(roundTwoDecimals(used) + "gb/" + total.intValue() + "gb");

                        // to calculate percentage
                        double frac = used / total;
                        double perc = frac * 100;

                        progressBar.setProgress((int) perc);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                CustomToast.makeText(StorageActivity.this, "Error Occurred!", Toast.LENGTH_LONG).show();

            }
        });
    }

    double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }
}



