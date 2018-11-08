package com.hellodemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hellodemo.adapter.FriendAdapter;
import com.hellodemo.models.Friend;
import com.hellodemo.network.VolleyRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DirectMessageActivity extends AppCompatActivity {
    private JSONArray friends;

    private RecyclerView friends_list_recycler_view;
    private ImageView back_icon;
    private TextView title;

    private List<Friend> mFriendsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_message);

        title = findViewById(R.id.title);
        back_icon = findViewById(R.id.back_icon);
        friends_list_recycler_view = findViewById(R.id.friend_list_recycler_view);

        friends_list_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        friends_list_recycler_view.setAdapter(new FriendAdapter(this));

        onLoadService();

        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DirectMessageActivity.super.onBackPressed();
            }
        });
    }

    private void onLoadService() {
        String url = BuildConfig.BASE_URL + "/get_friends";
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "get friends", true);
        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (!response.optBoolean("error", true)) {
                    friends = response.optJSONArray("message");
                    for (int i = 0; friends != null && i < friends.length(); i++) {
                        try {
                            JSONObject jsonObject = friends.getJSONObject(i);
                            Friend friend = new Friend();
                            friend.setId(jsonObject.optLong("id"));
                            friend.setAvatar(jsonObject.optString("avatar"));
                            friend.setFullName(jsonObject.optString("full_name"));
                            mFriendsList.add(friend);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    ((FriendAdapter) friends_list_recycler_view.getAdapter()).setDataNotify(mFriendsList);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");
            }
        });
    }
}
