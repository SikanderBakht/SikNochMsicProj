package com.hellodemo;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hellodemo.adapter.SearchPagerAdapter;
import com.hellodemo.fragment.searchActivity.ArtistsFragment;
import com.hellodemo.fragment.searchActivity.LabelsFragment;
import com.hellodemo.models.SearchedUser;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.LoadingManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity {

    private String TAG = "HelloDemoSearchActivity";
    SearchPagerAdapter mPagerAdapter;
    LoadingManager mainLoadingManger;
    private String searchFriendsAPITag = "search_friend";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // return if not logged in...
        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // setting up toolbar...
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        setUpSearchViewInToolbar();

        TabLayout tabLayout = findViewById(R.id.tabs);
        ViewPager mViewPager = findViewById(R.id.container);


        // setting up loading manager
        View loaderContainer = findViewById(R.id.loading_container);
        ImageView loadingIcon = (ImageView) findViewById(R.id.loading_icon);
        mainLoadingManger = new LoadingManager(SearchActivity.this, mViewPager, loadingIcon, loaderContainer);

        tabLayout.setupWithViewPager(mViewPager);

        mPagerAdapter = new SearchPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        reduceMarginsInTabs(tabLayout, 25);


        findViewById(R.id.back_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchActivity.super.finish();
            }
        });

    }

    private void setUpSearchViewInToolbar() {
//        SearchView mSearchView = findViewById(R.id.search_toolbar);
////        mSearchView.setQueryHint("Search groups, artists, labels...");
////        mSearchView.setIconified(true);
//        mSearchView.onActionViewExpanded();
//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(final String query) {
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(final String newText) {
//                Log.v(TAG, "Search Text: " + newText);
//                callSearchAPI(newText);
//                return true;
//            }
//        });

        final EditText etSearch = findViewById(R.id.search_edit_text_toolbar);
        final View cancel = findViewById(R.id.cross_toolbar);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
                cancel.setVisibility(View.INVISIBLE);
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    cancel.setVisibility(View.VISIBLE);
                } else {
                    cancel.setVisibility(View.INVISIBLE);
                }

                callSearchAPI(s.toString());
                Log.v(TAG, "Search Text: " + s.toString());

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        Log.v(TAG, "onCreateOptionsMenu started...");
//        getMenuInflater().inflate(R.menu.search_menu, menu);
//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        mSearchView.setQueryHint("Search groups, artists, labels...");
//        mSearchView.setIconified(true);
//        mSearchView.onActionViewExpanded();
//        LinearLayout searchEditFrame = (LinearLayout) mSearchView.findViewById(R.id.search_edit_frame); // Get the Linear Layout
//
//        // Get the associated LayoutParams and set leftMargin
//        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) searchEditFrame.getLayoutParams();
//
//        ((LinearLayout.LayoutParams) searchEditFrame.getLayoutParams()).rightMargin = 0;
////        ((LinearLayout.LayoutParams) searchEditFrame.getLayoutParams()).leftMargin = -26;
//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(final String query) {
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(final String newText) {
//                Log.v(TAG, "Search Text: " + newText);
//                callSearchAPI(newText);
//                return true;
//            }
//        });
//        return super.onCreateOptionsMenu(menu);
//    }

    private void callSearchAPI(String searchQuery) {
        if(searchQuery == null || searchQuery.isEmpty()){
            updateLists(new ArrayList<SearchedUser>(), new ArrayList<SearchedUser>());
            return;
        }
//        mPagerAdapter.performSearch(newText);

        mainLoadingManger.showLoadingIcon();

        // cancel all previous requests...
        VolleyRequest.getRequestQueue(SearchActivity.this).cancelAll(searchFriendsAPITag);

        String url = BuildConfig.BASE_URL + "/search_friends";
        VolleyRequest volleyRequest = new VolleyRequest(SearchActivity.this, url, searchFriendsAPITag, true);

        HashMap<String, String> params = new HashMap<>();
        params.put("query", searchQuery);


        Log.v(TAG, "Calling Search Friends API.\nURL:" + url);
        Log.v(TAG, "Search Friends API Request Params : " + params.toString());
        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "Search Friends API Response:\n" + response.toString());

                mainLoadingManger.hideLoadingIcon();

                ArrayList<SearchedUser> artists = new ArrayList<>();
                ArrayList<SearchedUser> labels = new ArrayList<>();

                if (!response.optBoolean("error", true)) {
                    try {
                        JSONArray peopleJsonArray = response.optJSONObject("message").optJSONArray("people");
                        JSONArray contactsJsonArray = response.optJSONObject("message").optJSONArray("contacts");
                        JSONArray artistsJsonArray = response.optJSONObject("message").optJSONArray("artists");
                        JSONArray labelsJsonArray = response.optJSONObject("message").optJSONArray("labels");

                        // making sure we get all the required data...
                        if (peopleJsonArray == null) peopleJsonArray = new JSONArray();
                        if (contactsJsonArray == null) contactsJsonArray = new JSONArray();
                        if (artistsJsonArray == null) artistsJsonArray = new JSONArray();
                        if (labelsJsonArray == null) labelsJsonArray = new JSONArray();


                        for (int i = 0; i < peopleJsonArray.length(); i++) {

                            JSONObject peopleJson = (JSONObject) peopleJsonArray.get(i);

                            SearchedUser user = new SearchedUser(
                                    peopleJson.getLong("id"),
                                    peopleJson.getString("full_name"),
                                    peopleJson.getString("type"),
                                    peopleJson.getString("avatar_path"),
                                    false,
                                    false,
                                    false);

                            if (peopleJson.getBoolean("following")) {

                                user.setFriend(true);
                                user.setFollowed(false);
                                user.setFollowing(false);
                            } else {

                                user.setFriend(false);
                                user.setFollowed(false);
                                user.setFollowing(true);
                            }

                            // add to the list..
                            if (user.getType().equalsIgnoreCase("artist")) {
                                artists.add(user);
                            } else if (user.getType().equalsIgnoreCase("label")) {
                                labels.add(user);
                            }
                        }

                        for (int i = 0; i < contactsJsonArray.length(); i++) {

                            JSONObject contactJson = (JSONObject) contactsJsonArray.get(i);

                            SearchedUser user = new SearchedUser(
                                    contactJson.getLong("id"),
                                    contactJson.getString("full_name"),
                                    contactJson.getString("type"),
                                    contactJson.getString("avatar_path"),
                                    false,
                                    false,
                                    false);

                            if (contactJson.getBoolean("following")) {

                                user.setFriend(true);
                                user.setFollowed(false);
                                user.setFollowing(false);
                            } else {

                                user.setFriend(false);
                                user.setFollowed(false);
                                user.setFollowing(true);
                            }

                            // add to the list..
                            if (user.getType().equalsIgnoreCase("artist")) {
                                artists.add(user);
                            } else if (user.getType().equalsIgnoreCase("label")) {
                                labels.add(user);
                            }
                        }

                        for (int i = 0; i < artistsJsonArray.length(); i++) {

                            JSONObject artistJson = (JSONObject) artistsJsonArray.get(i);

                            SearchedUser user = new SearchedUser(
                                    artistJson.getLong("id"),
                                    artistJson.getString("full_name"),
                                    artistJson.getString("type"),
                                    artistJson.getString("avatar_path"),
                                    false,
                                    false,
                                    false);

                            if (artistJson.getBoolean("following")) {
                                user.setFriend(false);
                                user.setFollowed(true);
                                user.setFollowing(false);
                            } else {
                                user.setFriend(false);
                                user.setFollowed(false);
                                user.setFollowing(false);
                            }

                            // add to the list..
                            if (user.getType().equalsIgnoreCase("artist")) {
                                artists.add(user);
                            } else if (user.getType().equalsIgnoreCase("label")) {
                                labels.add(user);
                            }

                        }

                        for (int i = 0; i < labelsJsonArray.length(); i++) {

                            JSONObject labelJson = (JSONObject) labelsJsonArray.get(i);

                            SearchedUser user = new SearchedUser(
                                    labelJson.getLong("id"),
                                    labelJson.getString("full_name"),
                                    labelJson.getString("type"),
                                    labelJson.getString("avatar_path"),
                                    false,
                                    false,
                                    false);

                            if (labelJson.getBoolean("following")) {
                                user.setFriend(false);
                                user.setFollowed(true);
                                user.setFollowing(false);
                            } else {
                                user.setFriend(false);
                                user.setFollowed(false);
                                user.setFollowing(false);
                            }

                            // add to the list..
                            labels.add(user);

                        }
                    } catch (JSONException e) {

                        e.printStackTrace();
                        CustomToast.makeText(SearchActivity.this, "Could not get Search Results.", Toast.LENGTH_SHORT).show();
                    }

                }

                updateLists(artists, labels);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mainLoadingManger.hideLoadingIcon();
                Log.d("", "");
            }
        });
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

    private void updateLists(ArrayList<SearchedUser> artists, ArrayList<SearchedUser> labels) {

        ArtistsFragment artistFragment = (ArtistsFragment) getSupportFragmentManager().getFragments().get(0);
        LabelsFragment labelsFragment = (LabelsFragment) getSupportFragmentManager().getFragments().get(1);
        artistFragment.updateList(artists);
        labelsFragment.updateList(labels);
    }
}
