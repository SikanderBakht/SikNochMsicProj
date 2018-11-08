package com.hellodemo.adapter;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.hellodemo.BuildConfig;
import com.hellodemo.R;
import com.hellodemo.SearchActivity;
import com.hellodemo.models.SearchedUser;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.utils.CustomToast;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchedUserAdapter extends RecyclerView.Adapter<SearchedUserAdapter.SearchedUserViewHolder> {

    private String TAG = "HelloDemoSearchedUserAdapter";
    private List<SearchedUser> mSearchedUsers = new ArrayList<>();
    private AppCompatActivity mActivity;

    public void updateList(List<SearchedUser> mSearchedUsers) {
        this.mSearchedUsers = mSearchedUsers;
        notifyDataSetChanged();

    }

    public SearchedUserAdapter(AppCompatActivity activity) {
        mActivity = activity;
    }

    @Override
    public SearchedUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mActivity).inflate(R.layout.searched_user_list_item, parent, false);
        return new SearchedUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchedUserViewHolder holder, int position) {
        holder.bindValues(position);
    }


    public int getItemCount() {
        return mSearchedUsers.size();
    }

    class SearchedUserViewHolder extends RecyclerView.ViewHolder {
        private ImageView recycler_view_list_item_image;
        private TextView artist_label_name, tvActionButton;

        SearchedUserViewHolder(View itemView) {
            super(itemView);
            artist_label_name = itemView.findViewById(R.id.artist_label_name);
            recycler_view_list_item_image = itemView.findViewById(R.id.recycler_view_list_item_image);
            tvActionButton = itemView.findViewById(R.id.action_button);
        }


        public void bindValues(int position) {
            if (position >= mSearchedUsers.size())
                return;

            final SearchedUser user = mSearchedUsers.get(position);
            Picasso.with(mActivity).load(user.getAvatar()).into(recycler_view_list_item_image);
            artist_label_name.setText(user.getFullName());
            itemView.setTag(user);

            // setting up action button...
            tvActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    followFriendButtonClicked(user, tvActionButton);
                }
            });

            setupActionButton(user, tvActionButton);
        }

    }

    private void setupActionButton(SearchedUser user, TextView tvActionButton) {
        if (user.isFriend()) {
            tvActionButton.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.rounded_rectangle_secondary, null));
            tvActionButton.setText("Unfriend");
        } else if (user.isFollowed()) {
            tvActionButton.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.rounded_rectangle_secondary, null));
            tvActionButton.setText("Unfollow");
        } else {
            tvActionButton.setBackground(ResourcesCompat.getDrawable(mActivity.getResources(), R.drawable.rounded_rectangle_primary, null));
            tvActionButton.setText("Follow");
        }
    }

    private void followFriendButtonClicked(final SearchedUser user, final TextView tvActionButton) {

        // set button transparency to 50%
        tvActionButton.setAlpha((float) 0.5);

        String url = BuildConfig.BASE_URL + "/followButtonAction/" + user.getId();
        VolleyRequest volleyRequest = new VolleyRequest(mActivity, url, "main", true);

        HashMap<String, String> params = new HashMap<>();

        Log.v(TAG, "Calling followButtonAction API.\nURL:" + url);
        Log.v(TAG, "followButtonAction API Request Params : " + params.toString());
        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                tvActionButton.setAlpha(1);

                Log.v(TAG, "followButtonAction API Response:\n" + response.toString());

                ArrayList<SearchedUser> artists = new ArrayList<>();
                ArrayList<SearchedUser> labels = new ArrayList<>();

                if (!response.optBoolean("error", true)) {
                    try {
                        // getting the new state of follow/friend....
                        boolean isFriend = response.getJSONObject("message").getBoolean("is_friend");
                        // this is_following means user is followed by app user
                        boolean isFollowed = response.getJSONObject("message").getBoolean("is_following");

                        // updating the model list...
                        user.setFriend(isFriend);
                        user.setFollowed(isFollowed);

                        // updating button gui....
                        setupActionButton(user, tvActionButton);

                    } catch (JSONException e) {

                        e.printStackTrace();
//                        CustomToast.makeText(mActivity, "Could not get Search Results.", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tvActionButton.setAlpha(1);
                Log.d("", "");
            }
        });
    }

}
