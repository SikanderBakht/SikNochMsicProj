package com.hellodemo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hellodemo.BuildConfig;
import com.hellodemo.R;
import com.hellodemo.adapter.ArtistAdapter;
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

/**
 * Created by new user on 2/24/2018.
 */

public class LabelsFragment extends Fragment {
    // manages loading icons...
    LoadingManager mainLoadingManger;
    private String TAG = "HelloDemoLabelsFragment";

    //this is the id of the song we are sending to the labels
    private long uploadID = -1;
    private RecyclerView labels_recycler_view;
    private UserModel mUserModel;
    private ArrayList<ArtistLabel> mLabelsList = new ArrayList<>();

    @SuppressLint("ValidFragment")
    private LabelsFragment() {
        super();
    }

    /**
     * this instantiates the fragment and fills it with labels..<br><br>
     * @param uploadID id of the song to be sent.
     */
    public static LabelsFragment newInstance(long uploadID) {
        LabelsFragment f = new LabelsFragment();

        Bundle args = new Bundle();
        args.putLong("uploadID", uploadID);
        f.setArguments(args);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.labels_fragment, container, false);

        labels_recycler_view = view.findViewById(R.id.labels_recycler_view);
        labels_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
        labels_recycler_view.setAdapter(new ArtistAdapter((AppCompatActivity)getActivity()));



        // will contain all the content of the fragment other than loader_animator...
        View fragmentContent = view.findViewById(R.id.fragment_content);

        //loading icon...
//        GifImageView mGigImageView = getView().findViewById(R.id.loading_gif);
        ImageView loadingIcon = (ImageView) view.findViewById(R.id.loading_icon);

        // This container contains the loading icon
        View loadingContainer = view.findViewById(R.id.loading_container);

        // setup loading icon...
        mainLoadingManger = new LoadingManager(getActivity(), fragmentContent, loadingIcon, loadingContainer);

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
        if (mLabelsList.size() > 0) {
            return;
        }

        String url = BuildConfig.BASE_URL + "/get_labels";
        VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "main", true);

        HashMap<String, String> params = new HashMap<>();
        params.put("music_id", String.valueOf(uploadID));
        params.put("user_id", String.valueOf(mUserModel.getId()));


        Log.v(TAG, "65869 Calling Get Labels API.\nURL:" + url);
        Log.v(TAG, "65869 Request Params : " + params.toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mainLoadingManger.hideLoadingIcon();
                Log.v(TAG, "65869 Response:\n" + response.toString());
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
                            mLabelsList.add(artistLabel);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    ((ArtistAdapter) labels_recycler_view.getAdapter()).setDataNotify(mLabelsList);
                    Log.d("","");
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
        ((ArtistAdapter) labels_recycler_view.getAdapter()).performSearch(newText);
    }
}
