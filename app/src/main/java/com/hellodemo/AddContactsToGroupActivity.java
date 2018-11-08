package com.hellodemo;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hellodemo.adapter.AddContactsToGroupListAdapter;
import com.hellodemo.adapter.ArtistAdapter;
import com.hellodemo.interfaces.UIConstants;
import com.hellodemo.models.ArtistLabel;
import com.hellodemo.models.ContactToAddInGroupModel;
import com.hellodemo.models.MusicListItem;
import com.hellodemo.models.NavMenuModel;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddContactsToGroupActivity extends AppCompatActivity {

    private String TAG = "HelloDemoAddContactsToGroupActivity";

    // DATA KEYS
    public static final String KEY_GROUP_ID = "group_id";

    private long groupID = -1;
    public String groupType;
    ArrayList<Long> selectedContacts = new ArrayList<>();
    private TextView tvAddToGroup, title;
    private Toolbar toolbar;
    private RecyclerView rvContactsList;

    private UserModel mUserModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts_to_group);

        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);

        groupID = getIntent().getLongExtra(AddContactsToGroupActivity.KEY_GROUP_ID, -1);
        groupType = getIntent().getStringExtra("type");

        // Checking data validity...
        if (groupID == -1) {
            Log.v(TAG, "Input Data:\n" +
                    groupID);
            Toast.makeText(this, "Add To Group Screen: Invalid input", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        rvContactsList = findViewById(R.id.contacts_recycler_view);
        rvContactsList.setLayoutManager(new LinearLayoutManager(this));
        rvContactsList.setAdapter(new AddContactsToGroupListAdapter(this));


        tvAddToGroup = findViewById(R.id.add_to_group);
        title = findViewById(R.id.title);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.v(TAG, groupType);

        if (groupType.equals("open")) {
            title.setText("Invite");
            tvAddToGroup.setText("Invite Members");
        } else if (groupType.equals("close")) {
            title.setText("Contacts");
            tvAddToGroup.setText("Add Contacts");
        }

        findViewById(R.id.back_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddContactsToGroupActivity.super.finish();
            }
        });


        setAddButtonEnableDisable();


        onLoadService();
    }

    // calls api to get friends for this group...
    private void onLoadService() {

        String url = BuildConfig.BASE_URL + "/groups/get_friends";
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);

        HashMap<String, String> params = new HashMap<>();
        params.put("group_id", String.valueOf(groupID));
        params.put("user_id", String.valueOf(mUserModel.getId()));


        Log.v(TAG, "Calling Get Friends By Group API.\nURL:" + url);
        Log.v(TAG, "Friends By Group API Request Params : " + params.toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "Friends By Group API Response:\n" + response.toString());

                if (!response.optBoolean("error", true)) {

                    ArrayList<ContactToAddInGroupModel> contactsToAddList = Utils.parseJsonArray(response.optJSONArray("message").toString(),
                            new TypeToken<List<ContactToAddInGroupModel>>() {
                            }.getType());

                   /* for(int i=0; i< contactsToAddList.size(); i++) {
                        if (contactsToAddList.get(i).isInGroup()) {
                            contactsToAddList.remove(contactsToAddList.get(i));

                        }
                    }*/
                    ((AddContactsToGroupListAdapter) rvContactsList.getAdapter()).setDataNotify(contactsToAddList);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "Friends By Group API Error Response:\n" + error.getMessage());
                Log.v(TAG, "Friends By Group API Error Response:\n" + error.toString());
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

        ((AddContactsToGroupListAdapter) rvContactsList.getAdapter()).performSearch(newText);
    }


    private void setAddButtonEnableDisable() {
        if (selectedContacts.isEmpty()) {
            tvAddToGroup.setEnabled(false);
            tvAddToGroup.setVisibility(View.GONE);
        } else {
            tvAddToGroup.setVisibility(View.VISIBLE);
            tvAddToGroup.setEnabled(true);
        }
    }

    public void onContactSelect(long id) {
        selectedContacts.add(id);
        setAddButtonEnableDisable();
    }

    public void onContactDeselect(long id) {
        selectedContacts.remove(id);
        setAddButtonEnableDisable();
    }

    public void onAddButtonClicked(View view) {

        String url = BuildConfig.BASE_URL + "/groups/add_members";
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "main", true);

        HashMap<String, String> params = new HashMap<>();
        params.put("group_id", String.valueOf(groupID));
        params.put("member_ids", String.valueOf((new Gson()).toJson(selectedContacts)));


        Log.v(TAG, "Calling Add Members to Group API.\nURL:" + url);
        Log.v(TAG, "Add Members to Group API Request Params : " + params.toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "Add Members to Group API Response:\n" + response.toString());

                if (!response.optBoolean("error", true)) {
                    if(groupType.equals("close")) {
                        CustomToast.makeText(AddContactsToGroupActivity.this, "Friend Added!", Toast.LENGTH_SHORT).show();
                    } else {
                        CustomToast.makeText(AddContactsToGroupActivity.this, "Invitations sent!", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } else {
                    CustomToast.makeText(AddContactsToGroupActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "Add Members to Group API Error Response:\n" + error.getMessage());
                Log.v(TAG, "Add Members to Group API Error Response:\n" + error.toString());
                CustomToast.makeText(AddContactsToGroupActivity.this, "Some Error occurred", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
