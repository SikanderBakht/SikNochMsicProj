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
import com.google.gson.reflect.TypeToken;
import com.hellodemo.BuildConfig;
import com.hellodemo.R;
import com.hellodemo.adapter.GroupAdapter;
import com.hellodemo.models.Group;
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

public class GroupsFragment extends Fragment {
    // manages loading icons...
    LoadingManager mainLoadingManger;
    private String TAG = "HelloDemoGroupsFragment";

    //this is the id of the song we are sending to the groups
    private long uploadID = -1;
    private RecyclerView groups_recycler_view;
    private UserModel mUserModel;
    private List<Group> mGroupList = new ArrayList<>();

    @SuppressLint("ValidFragment")
    private GroupsFragment() {
        super();
    }

    /**
     * this instantiates the fragment and fills it with groups..<br><br>
     *
     * @param uploadID id of the song to be sent.
     */
    public static GroupsFragment newInstance(long uploadID) {
        GroupsFragment f = new GroupsFragment();

        Bundle args = new Bundle();
        args.putLong("uploadID", uploadID);
        f.setArguments(args);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group_fragment, container, false);

        groups_recycler_view = view.findViewById(R.id.groups_recycler_view);
        groups_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
        groups_recycler_view.setAdapter(new GroupAdapter((AppCompatActivity) getActivity()));


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

        if (mGroupList.size() > 0) return;
        String url = BuildConfig.BASE_URL + "/get_groups";
        VolleyRequest volleyRequest = new VolleyRequest(getActivity(), url, "main", true);


        HashMap<String, String> params = new HashMap<>();
        params.put("music_id", String.valueOf(uploadID));
        params.put("user_id", String.valueOf(mUserModel.getId()));


        Log.v(TAG, "60769 Calling Get Groups API.\nURL:" + url);
        Log.v(TAG, "60769 Request Params : " + params.toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mainLoadingManger.hideLoadingIcon();
                Log.v(TAG, "60769 Response:\n" + response.toString());
                if (!response.optBoolean("error", true)) {

                    JSONArray groupsJsonArray = response.optJSONArray("message");
                    for (int i = 0; i < groupsJsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject = groupsJsonArray.getJSONObject(i);
                            Group group = new Group();
                            group.setId(jsonObject.optInt("id"));
                            group.setCount(jsonObject.optInt("count"));
                            group.setName(jsonObject.optString("name"));
                            group.setAlreadySent(jsonObject.optBoolean("already_sent"));
                            mGroupList.add(group);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    ((GroupAdapter) groups_recycler_view.getAdapter()).setDataNotify(mGroupList);

                    Log.v(TAG, "Groups: " + mGroupList.size());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                mainLoadingManger.hideLoadingIcon();
                Log.v(TAG, "Error");
            }
        });
    }

    public void performSearch(String newText) {
        ((GroupAdapter) groups_recycler_view.getAdapter()).performSearch(newText);
    }
}
