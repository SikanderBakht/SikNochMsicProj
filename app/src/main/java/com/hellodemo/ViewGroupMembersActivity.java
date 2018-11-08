package com.hellodemo;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hellodemo.adapter.ViewGroupMembersListAdapter;
import com.hellodemo.models.NavGroupItem;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ViewGroupMembersActivity extends AppCompatActivity {
//    String s;
    ArrayList<String> memberslist = new ArrayList<String>();
    private String TAG = "HelloDemoViewGroupMembersActivity";
    // DATA KEYS
    public static final String KEY_GROUP_ID = "group_id";
    public static final String KEY_GROUP_ADMIN_ID = "group_admin_id";
    String name_admin;
    public long groupID = -1;
    public long groupAdminID = -1;
    private ListView ls;
    private Toolbar toolbar;
    private RecyclerView rvMembersList;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group_members);

        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }


        groupID = getIntent().getLongExtra(ViewGroupMembersActivity.KEY_GROUP_ID, -1);
        groupAdminID = getIntent().getLongExtra(ViewGroupMembersActivity.KEY_GROUP_ADMIN_ID, -1);

        // Checking data validity...
        if (groupID == -1 || groupAdminID == -1) {
            Log.v(TAG, "Input Data:\n" +
                    groupID + "\n" +
                    groupAdminID);
            CustomToast.makeText(this, "Can not show Members", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvMembersList = findViewById(R.id.members_recycler_view);
        rvMembersList.setLayoutManager(new LinearLayoutManager(this));
        rvMembersList.setAdapter(new ViewGroupMembersListAdapter(this));

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.back_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroupMembersActivity.super.finish();
            }
        });


        onLoadService();
    }

    // calls api to get all members of this group...
    private void onLoadService() {

        String url = BuildConfig.BASE_URL + "/groups/get_members";
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);

        HashMap<String, String> params = new HashMap<>();
        params.put("group_id", String.valueOf(groupID));


        Log.v(TAG, "Calling Get Members Of Group API.\nURL:" + url);
        Log.v(TAG, "Members Of Group API Request Params : " + params.toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "Members Of Group API Response:\n" + response.toString());
                try {
                    response = response.getJSONObject("message");
                    Log.v(TAG, "Message" + response.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String adminAvatar = null, adminName = null;
                int adminId = -2;

                try {
                    JSONObject creator = response.getJSONObject("creator");
//                    s = response.getJSONObject("creator").toString();
//                    Log.v(TAG, "creator" + s);

                    adminName = creator.getString("full_name");
                    adminAvatar = creator.getString("avatar");
                    adminId = creator.getInt("id");
//                    Log.v(TAG, "username" + adminName);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                try {
//                    JSONArray member = response.getJSONArray("members");
//                    String members = member.toString();
//                    Log.v(TAG, "members" + members);
//
//
//                    for (int i = 0; i < member.length(); i++) {
//                        JSONObject memberobj = member.getJSONObject(i);
//                        String name = memberobj.getString("full_name");
//                        Log.v(TAG, "fullname_member" + name);
//                        memberslist.add(name);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

//                NavGroupItem navgroup = new NavGroupItem();
//                navgroup.setAdmin(s);
                ArrayList<UserModel> membersList = null;
                membersList = Utils.parseJsonArray(response.optJSONArray("members").toString(),
                        new TypeToken<List<UserModel>>() {
                        }.getType());


                UserModel admin = new UserModel();

                admin.setId(adminId);
                admin.setFullName(adminName);
                admin.setAvatar(adminAvatar);
                admin.setType("Admin");

                membersList.add(0, admin);
//                for (int i = 0; i < membersList.size(); i++) {
//                    if (membersList.get(i).getFullName().equals(admin.getFullName())) {
//                        membersList.get(i).setFullName(admin.getFullName() + " (Admin)");
//                        Collections.swap(membersList, membersList.indexOf(membersList.get(i)), 0);
//                    }
//                }

                ((ViewGroupMembersListAdapter) rvMembersList.getAdapter()).setDataNotify(membersList);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "Members Of Group API Error Response:\n" + error.getMessage());
                Log.v(TAG, "Members Of Group API Error Response:\n" + error.toString());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                performSearch(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void performSearch(String newText) {

        ((ViewGroupMembersListAdapter) rvMembersList.getAdapter()).performSearch(newText);
    }

}
