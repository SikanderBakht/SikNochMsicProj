package com.hellodemo.fragment.searchActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hellodemo.R;
import com.hellodemo.adapter.SearchedUserAdapter;
import com.hellodemo.models.SearchedUser;
import com.hellodemo.models.UserModel;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.LoadingManager;
import com.hellodemo.utils.Utils;

import java.util.ArrayList;

public class LabelsFragment extends Fragment {
    // manages loading icons...
    Context c = getContext();
    LoadingManager mainLoadingManger;
    private String TAG = "HelloDemoLabelsFragment";

    private RecyclerView labels_recycler_view;
    private UserModel mUserModel;
    View view;
    ArrayList<SearchedUser> labels = new ArrayList<>();

    @SuppressLint("ValidFragment")
    private LabelsFragment() {
        super();
    }


    /**
     * this instatntiates the fragment and fills it with labels..<br><br>
     */
    public static LabelsFragment newInstance() {
        LabelsFragment f = new LabelsFragment();

        return f;
    }

    public void updateList(ArrayList<SearchedUser> labels) {
        this.labels = labels;
        ((SearchedUserAdapter) labels_recycler_view.getAdapter()).updateList(labels);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_labels, container, false);

        // will contain all the content of the fragment other than loader_animator...
        View fragmentContent = view.findViewById(R.id.fragment_content);

        //loading icon...
//        GifImageView mGigImageView = getView().findViewById(R.id.loading_gif);
        ImageView loadingIcon = (ImageView) view.findViewById(R.id.loading_icon);

        // This container contains the loading icon
        View loadingContainer = view.findViewById(R.id.loading_container);

        // setup loading icon...
        mainLoadingManger = new LoadingManager(getActivity(), fragmentContent, loadingIcon, loadingContainer);


//        followlabel.setVisibility(View.INVISIBLE);
        labels_recycler_view = view.findViewById(R.id.labels_recycler_view);

        labels_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));

        labels_recycler_view.setAdapter(new SearchedUserAdapter((AppCompatActivity) getActivity()));



        mUserModel = Utils.parseJson(UserSharedPreferences.getString(getContext(), UserSharedPreferences.USER_MODEL), UserModel.class);

        return view;
    }
}