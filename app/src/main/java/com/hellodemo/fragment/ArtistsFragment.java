package com.hellodemo.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hellodemo.BuildConfig;
import com.hellodemo.MainActivity;
import com.hellodemo.R;
import com.hellodemo.adapter.ArtistAdapter;
import com.hellodemo.interfaces.UIConstants;
import com.hellodemo.models.ArtistLabel;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.LoadingManager;
import com.hellodemo.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by new user on 2/24/2018.
 */
public class ArtistsFragment extends Fragment {
    // manages loading icons...
    View friendslabel, followlabel;
    Context c = getContext();
    LoadingManager mainLoadingManger;
    private String TAG = "HelloDemoArtistsFragment";
    View promptsView;
    //this is the id of the song we are sending to the artists
    private long uploadID = -1;
    private RecyclerView artist_recycler_view_friends, artist_recycler_view_follow;
    private UserModel mUserModel;
    private List<ArtistLabel> mFriendsArtistsList = new ArrayList<>();
    private List<ArtistLabel> mYouFollowArtistsList = new ArrayList<>();
    View view, followlayout;

    @SuppressLint("ValidFragment")
    private ArtistsFragment() {
        super();
    }


    /**
     * this instatntiates the fragment and fills it with artists..<br><br>
     *
     * @param uploadID id of the song to be sent.
     */
    public static ArtistsFragment newInstance(long uploadID) {
        ArtistsFragment f = new ArtistsFragment();

        Bundle args = new Bundle();
        args.putLong("uploadID", uploadID);
        f.setArguments(args);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.artists_fragment, container, false);

        // will contain all the content of the fragment other than loader_animator...
        View fragmentContent = view.findViewById(R.id.fragment_content1);

        //loading icon...
//        GifImageView mGigImageView = getView().findViewById(R.id.loading_gif);
        ImageView loadingIcon = (ImageView) view.findViewById(R.id.loading_icon);

        // This container contains the loading icon
        View loadingContainer = view.findViewById(R.id.loading_container);

        // setup loading icon...
        mainLoadingManger = new LoadingManager(getActivity(), fragmentContent, loadingIcon, loadingContainer);


//        followlabel.setVisibility(View.INVISIBLE);
        artist_recycler_view_friends = view.findViewById(R.id.artist_recycler_view_friends);
        artist_recycler_view_follow = view.findViewById(R.id.artist_recycler_view_you_follow);

        artist_recycler_view_friends.setLayoutManager(new LinearLayoutManager(getContext()));
        artist_recycler_view_follow.setLayoutManager(new LinearLayoutManager(getContext()));

        artist_recycler_view_friends.setAdapter(new ArtistAdapter((AppCompatActivity) getActivity()));
        artist_recycler_view_follow.setAdapter(new ArtistAdapter((AppCompatActivity) getActivity()));


        //if(artist_recycler_view_follow.get)
        mUserModel = Utils.parseJson(UserSharedPreferences.getString(getContext(), UserSharedPreferences.USER_MODEL), UserModel.class);

        uploadID = getArguments().getLong("uploadID", -1);
        if (uploadID == -1) {
            CustomToast.makeText(getActivity(), "Invalid Song!", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        onLoadService();

        return view;
    }

    private void onLoadService() {

        mainLoadingManger.showLoadingIcon();
        if (mFriendsArtistsList.size() > 0
                || mYouFollowArtistsList.size() > 0)
            return;

        String url = BuildConfig.BASE_URL + "/get_artists";
        VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "main", true);

        HashMap<String, String> params = new HashMap<>();
        params.put("music_id", String.valueOf(uploadID));
        params.put("user_id", String.valueOf(mUserModel.getId()));


        Log.v(TAG, "34869 Calling Get Artists API.\nURL:" + url);
        Log.v(TAG, "34869 Request Params : " + params.toString());
        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(TAG, "348699 Response:\n" + response.toString());

                mainLoadingManger.hideLoadingIcon();
                if (!response.optBoolean("error", true)) {
                    JSONArray groupsJsonArray = response.optJSONArray("message");

                    for (int i = 0; i < groupsJsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject = groupsJsonArray.getJSONObject(i);
                            ArtistLabel artistLabel = new ArtistLabel();
                            artistLabel.setAvatar(jsonObject.optString("avatar"));
                            artistLabel.setFullName(jsonObject.optString("full_name"));
                            artistLabel.setFollowing(jsonObject.optBoolean("following"));
                            artistLabel.setFriend(jsonObject.optBoolean("friend"));
                            artistLabel.setId(jsonObject.optInt("id"));
                            artistLabel.setAlreadySent(jsonObject.optBoolean("already_sent"));


                            if (artistLabel.isFriend()) {
                                mFriendsArtistsList.add(artistLabel);
                                //    mFriendsArtistsList.add("");
                            }
                            if (artistLabel.isFollowing()) {
                                mYouFollowArtistsList.add(artistLabel);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                    Log.v(TAG, "Friends: " + mFriendsArtistsList.size());
                    Log.v(TAG, "Follows: " + mYouFollowArtistsList.size());

                    ((ArtistAdapter) artist_recycler_view_follow.getAdapter()).setDataNotify(mYouFollowArtistsList);
                    ((ArtistAdapter) artist_recycler_view_friends.getAdapter()).setDataNotify(mFriendsArtistsList);


                    final Button friends = view.findViewById(R.id.friends);
                    final Button followers = view.findViewById(R.id.followers);
                    //friends.setText(R.string.friends);
                    artist_recycler_view_follow.setVisibility(View.GONE);

                    // simulate friends clicked
                    friends.setBackgroundColor(Color.GRAY);
                    friends.setTextColor(Color.WHITE);
                    followers.setTextColor(Color.GRAY);
                    followers.setBackgroundColor(Color.WHITE);

                    friends.setOnClickListener(new Button.OnClickListener() {
                        public void onClick(View v) {
                            artist_recycler_view_friends.setVisibility(View.VISIBLE);
                            artist_recycler_view_follow.setVisibility(View.GONE);
                            friends.setBackgroundColor(Color.GRAY);
                            friends.setTextColor(Color.WHITE);
                            followers.setTextColor(Color.GRAY);
                            followers.setBackgroundColor(Color.WHITE);

                        }
                    });


                    followers.setOnClickListener(new Button.OnClickListener() {
                        public void onClick(View v) {
                            //hide friends and show followers
                            friends.setBackgroundColor(Color.WHITE);
                            followers.setTextColor(Color.WHITE);
                            friends.setTextColor(Color.GRAY);
                            followers.setBackgroundColor(Color.GRAY);
                            artist_recycler_view_follow.setVisibility(View.VISIBLE);
                            artist_recycler_view_friends.setVisibility(View.GONE);

                        }
                    });

//                    if (artist_recycler_view_follow.hasOnClickListeners()) {
//
//                        followers.setVisibility(View.GONE);
//                    }
//
//
//                    if (artist_recycler_view_friends.hasOnClickListeners()) {
//
//                        friends.setVisibility(View.GONE);
//                    }


//                    // check if no friends then remove friends label
//                    if (mFriendsArtistsList.size() == 0) {
//                        friendslabel.setVisibility(View.GONE);
//                    } else {
//                        //  friendslabel.setVisibility(View.VISIBLE);
//                    }
//
//
//                    // check if no follow then remove follows label
//                    if (mYouFollowArtistsList.size() == 0) {
//                        followlayout.setVisibility(View.GONE);
//                    } else {
//                        //followlayout.setVisibility(View.VISIBLE);
//                    }

                    Log.d("", "");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mainLoadingManger.hideLoadingIcon();
                Log.d("", "");
            }
        });
    }

    public void performSearch(String newText) {
        ((ArtistAdapter) artist_recycler_view_follow.getAdapter()).performSearch(newText);
        ((ArtistAdapter) artist_recycler_view_friends.getAdapter()).performSearch(newText);
    }
}
