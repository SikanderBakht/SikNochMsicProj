package com.hellodemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.hellodemo.adapter.NotificationsAdapter;
import com.hellodemo.models.Notification;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.LoadingManager;
import com.hellodemo.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Mahnoor on 04/06/2018.
 */

public class NotificationsActivity extends AppCompatActivity {

    private String TAG = "HelloDemoNotificationsActivity";
    RecyclerView notificationsRecyclerView;
    NotificationsAdapter mAdapter;
    ImageView back_icon;
    TextView title;

    // manages loading icons...
    LoadingManager mainLoadingManger;

    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // logout if not signed in...
        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        // Setting up actionbar...
        back_icon = findViewById(R.id.back_icon);
        title = findViewById(R.id.title);
        Toolbar toolbar = findViewById(R.id.toolbarx);
        setSupportActionBar(toolbar);
        title.setText("Notifications");
        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        // Set up loading manager....
        setupLoadingManager();

        notificationsRecyclerView = findViewById(R.id.notifications_groups_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        notificationsRecyclerView.setLayoutManager(mLayoutManager);
        notificationsRecyclerView.setAdapter(new NotificationsAdapter(this));

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                onLoadService();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorPrimary);
    }

    private void setupLoadingManager() {
        // content of the fragment...
        // will contain all the content of the activity other than loader_animator...
        View activityContent = findViewById(R.id.swipeRefreshLayout);

        //loading icon...
        ImageView loadingIcon = (ImageView) findViewById(R.id.loading_icon);

        // This container contains the loading icon
        View loadingContainer = findViewById(R.id.loading_container);

        // setup loading icon...
        mainLoadingManger = new LoadingManager(this, activityContent, loadingIcon, loadingContainer);
    }


    @Override
    protected void onResume() {
        super.onResume();
        onLoadService();
    }

    private void onLoadService() {

        mainLoadingManger.showLoadingIcon();

        String url = BuildConfig.BASE_URL + "/" + "notification/get";
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);
        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                swipeContainer.setRefreshing(false);
                mainLoadingManger.hideLoadingIcon();

                if (!response.optBoolean("error", true)) {
                    List<Notification> notificationList = Utils.parseJsonArray(response.optJSONObject("message").optJSONArray("notifications").toString(),
                            new TypeToken<List<Notification>>() {
                            }.getType());
                    if (notificationsRecyclerView != null) {
                        notificationList = Notification.removeUnknownNotifications(notificationList);
                        ((NotificationsAdapter) notificationsRecyclerView.getAdapter()).setDataNotify(notificationList);
                    }
                    Log.d("", "");

                    callMarkNotificationsAsReadAPI();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                swipeContainer.setRefreshing(false);
                mainLoadingManger.hideLoadingIcon();
                Log.d("", "");
            }
        });
    }

    private void callMarkNotificationsAsReadAPI() {
        String url = BuildConfig.BASE_URL + "/notifications/open";
        HashMap<String, String> params = new HashMap<>();
        params.put("all", "true");
        params.put("notifications", "null");
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);
        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }
}

