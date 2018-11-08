package com.hellodemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hellodemo.adapter.NotificationsSettingsAdapter;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Mahnoor on 11/04/2018.
 */

public class NotificationSettingsActivity extends AppCompatActivity {


    private String TAG = "HelloDemoNotificationsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);


        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }


        findViewById(R.id.back_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    onBackPressed();
            }
        });
        callGetNotificationSettingsAPI();

    }


    // this calls the api to get the required setting
    private void callGetNotificationSettingsAPI() {
            String url = BuildConfig.BASE_URL + "/setting/get";


        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);

        HashMap<String, String> params = new HashMap<>();
        Log.v(TAG, "Calling Get Notifications Settings API URL : " + url);
        Log.v(TAG, "Get Notifications Settings API Request Params : " + params.toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    Log.v(TAG, "Get Notifications Settings API Response:" + response.toString());
                    if (!response.optBoolean("error", true)) {


                        ListView li = findViewById(R.id.notification_listview);
                        String[] Account_Options = {"Recieved Track", "New Message", "New Invite", "Friendship", "Follows"};
                        String[] actionTags = {"tracks", "messages", "invites", "friends", "follows"};
                        boolean[] settingsValue = {false, false, false, false, false};
                        int[] drawableIds = {R.drawable.notifications, R.drawable.notifications, R.drawable.notifications, R.drawable.notifications, R.drawable.notifications};


//                    CustomToast.makeText(mContext, "Saved!", Toast.LENGTH_SHORT).show();
                        boolean messages = response.getJSONObject("message").getJSONObject("settings").getInt("messages") == 1;
                        boolean tracks = response.getJSONObject("message").getJSONObject("settings").getInt("tracks") == 1;
                        boolean invites = response.getJSONObject("message").getJSONObject("settings").getInt("invites") == 1;
                        boolean friends = response.getJSONObject("message").getJSONObject("settings").getInt("friends") == 1;
                        boolean follows = response.getJSONObject("message").getJSONObject("settings").getInt("follows") == 1;

                        for (int i = 0; i < settingsValue.length; i++) {
                            if (actionTags[i].equalsIgnoreCase("messages")) {
                                settingsValue[i] = messages;
                            }

                            if (actionTags[i].equalsIgnoreCase("tracks")) {
                                settingsValue[i] = tracks;
                            }

                            if (actionTags[i].equalsIgnoreCase("invites")) {
                                settingsValue[i] = invites;
                            }
                            if (actionTags[i].equalsIgnoreCase("friends")) {
                                settingsValue[i] = friends;
                            }
                            if (actionTags[i].equalsIgnoreCase("follows")) {
                                settingsValue[i] = follows;
                            }
                        }


                        NotificationsSettingsAdapter adapter = new NotificationsSettingsAdapter(NotificationSettingsActivity.this,
                                Account_Options,
                                drawableIds,
                                actionTags,
                                settingsValue);
                        li.setAdapter(adapter);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("", "");
            }
        });
    }
}
