package com.hellodemo;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.hellodemo.adapter.ArtistAdapter;
import com.hellodemo.adapter.MessagesListRecyclerAdapter;
import com.hellodemo.adapter.SendPopupPagerAdapter;
import com.hellodemo.fragment.ArtistsFragment;
import com.hellodemo.fragment.GroupsFragment;
import com.hellodemo.fragment.LabelsFragment;
import com.hellodemo.interfaces.SendPopupAdaptersInterface;
import com.hellodemo.interfaces.UIConstants;
import com.hellodemo.models.MusicListItem;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SendSongPopupActivity extends AppCompatActivity implements SendPopupAdaptersInterface {

    private String TAG = "HelloDemoSendSongPopupActivity";
    ArrayList<Long> selectedGroupsIds = new ArrayList<>();
    ArrayList<Long> selectedArtistsLabelsIds = new ArrayList<>();
    private TextView send_text_view;
    private Toolbar toolbar;
    private LinearLayout buttons_layout;
    SendPopupPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_song_popup);

        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        send_text_view = findViewById(R.id.send_text_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.tabs);

        buttons_layout = (LinearLayout) findViewById(R.id.buttons_layout);

        ViewPager mViewPager = findViewById(R.id.container);

        tabLayout.setupWithViewPager(mViewPager);

        MusicListItem musicListItem = getIntent().getParcelableExtra(UIConstants.IntentExtras.SELECTED_MUSIC);

        if (musicListItem == null) {
            CustomToast.makeText(this, "Invalid Song!", Toast.LENGTH_SHORT).show();
            finish();
        }
        Log.v(TAG, "Music ID Received: " + musicListItem.getId());
        mPagerAdapter = new SendPopupPagerAdapter(getSupportFragmentManager(), musicListItem.getId(), -1);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        reduceMarginsInTabs(tabLayout, 25);


        findViewById(R.id.back_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendSongPopupActivity.super.finish();
            }
        });

        setSendButtonEnableDisable();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public static void reduceMarginsInTabs(final TabLayout tabLayout, int marginOffset) {

        View tabStrip = tabLayout.getChildAt(0);
        if (tabStrip instanceof ViewGroup) {
            ViewGroup tabStripGroup = (ViewGroup) tabStrip;
            for (int i = 0; i < ((ViewGroup) tabStrip).getChildCount(); i++) {
                View tabView = tabStripGroup.getChildAt(i);
                if (tabView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).leftMargin = marginOffset;
                    ((ViewGroup.MarginLayoutParams) tabView.getLayoutParams()).rightMargin = marginOffset;
                }
            }

            tabLayout.requestLayout();

        }
    }

    private void setSendButtonEnableDisable() {
        if (selectedGroupsIds.isEmpty() && selectedArtistsLabelsIds.isEmpty()) {
            send_text_view.setEnabled(false);
            send_text_view.setVisibility(View.GONE);
            buttons_layout.setVisibility(View.GONE);
        } else {
            send_text_view.setVisibility(View.VISIBLE);
            send_text_view.setEnabled(true);
            buttons_layout.setVisibility(View.GONE);
        }
    }

    @Override
    public void selectLabelArtist(long id) {
        selectedArtistsLabelsIds.add(id);
        setSendButtonEnableDisable();
    }

    @Override
    public void selectGroup(long id) {
        selectedGroupsIds.add(id);
        setSendButtonEnableDisable();
    }

    @Override
    public void deselectGroup(long id) {
        selectedGroupsIds.remove(id);
        setSendButtonEnableDisable();
    }

    @Override
    public void deselectLabelArtist(long id) {

        selectedArtistsLabelsIds.remove(id);
        setSendButtonEnableDisable();
    }

    public void onSendMusicButtonClicked(View view) {
        MusicListItem musicListItem = getIntent().getParcelableExtra(UIConstants.IntentExtras.SELECTED_MUSIC);
        String selectedArtistsIdsStr = new Gson().toJson(selectedArtistsLabelsIds);
        String selectedGroupsIdsStr = new Gson().toJson(selectedGroupsIds);
        Intent intent = new Intent(this, LeaveMessageActivity.class);
        intent.putExtra(UIConstants.IntentExtras.SELECTED_GROUPS_IDS_JSON_STR, selectedGroupsIdsStr);
        intent.putExtra(UIConstants.IntentExtras.SELECTED_ARTISTS_IDS_JSON_STR, selectedArtistsIdsStr);
        intent.putExtra(UIConstants.IntentExtras.SELECTED_MUSIC, musicListItem);
        startActivity(intent);
        finish();
    }

    public void onCancelButtonClicked(View view) {

        finish();
    }

    public void onAddFriendsButtonClicked(View view) {
        UserModel mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);
        String url = BuildConfig.BASE_URL + "/groups/add_members";/*?upload_id=" + musicListItem.getId() +
                "&user_ids=" + selectedArtistsIds +
                "&group_ids=" + selectedGroupsIds +
                "&message_text="+et_leave_message.getText().toString();*/
        VolleyRequest volleyRequest = new VolleyRequest(this, url, "send_contact", true);
        HashMap<String, String> params = new HashMap<>();
        params.put("member_ids", new Gson().toJson(selectedArtistsLabelsIds));
        params.put("group_id", String.valueOf(getIntent().getLongExtra(UIConstants.IntentExtras.CURRENT_GROUP_ID, 1L)));
        params.put("user_id", String.valueOf(mUserModel.getId()));
        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (!response.optBoolean("error", true)) {
                    CustomToast.makeText(SendSongPopupActivity.this, response.optString("message"), Toast.LENGTH_LONG).show();
                    finish();
                }
                Log.d("", "");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("", "");
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

        GroupsFragment groupFragment = (GroupsFragment) getSupportFragmentManager().getFragments().get(0);
        ArtistsFragment artistFragment = (ArtistsFragment) getSupportFragmentManager().getFragments().get(1);
        LabelsFragment labelsFragment = (LabelsFragment) getSupportFragmentManager().getFragments().get(2);
        groupFragment.performSearch(newText);
        artistFragment.performSearch(newText);
        labelsFragment.performSearch(newText);
    }
}
