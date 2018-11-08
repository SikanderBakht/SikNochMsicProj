package com.hellodemo.adapter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.hellodemo.BuildConfig;
import com.hellodemo.MessagesBySongsActivity;
import com.hellodemo.R;
import com.hellodemo.ViewGroupMembersActivity;
import com.hellodemo.models.MessagesUserGroupListItem;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;


import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;


public class ViewGroupMembersListAdapter extends RecyclerSwipeAdapter<ViewGroupMembersListAdapter.GroupMemberViewHolder> {

    private String TAG = "HelloDemoViewGroupMembersListAdapter";
    private List<UserModel> mSelectedMembers = new ArrayList<>();
    private List<UserModel> mOriginalMembers = new ArrayList<>();
    private ViewGroupMembersActivity mActivity;
    private UserModel mUserModel;


    public ViewGroupMembersListAdapter(ViewGroupMembersActivity activity) {
        mActivity = activity;
    }

    @Override
    public GroupMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(mActivity).inflate(R.layout.contact_to_add_in_group, parent, false);
//        return new GroupMemberViewHolder(view);

        SwipeLayout swipeLayout = (SwipeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_of_group_list_element, parent, false);

        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, swipeLayout.findViewById(R.id.left_wrapper));
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, swipeLayout.findViewById(R.id.right_wrapper));

        return new GroupMemberViewHolder(swipeLayout);
    }

    @Override
    public void onBindViewHolder(GroupMemberViewHolder holder, int position) {
        holder.bindValues(position);
        /*mItemManger.bindView(holder.itemView, position);
        ((SwipeLayout) holder.itemView).addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                mItemManger.closeAllExcept(layout);
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return mSelectedMembers.size();
    }

    public void setDataNotify(List<UserModel> contactsList) {
        mSelectedMembers.clear();
        mOriginalMembers.clear();
        mSelectedMembers.addAll(contactsList);
        mOriginalMembers.addAll(contactsList);
        notifyDataSetChanged();
    }

    public void performSearch(String newText) {
        newText = newText.toLowerCase(Locale.getDefault());
        mSelectedMembers.clear();
        if (newText.length() == 0) {
            mSelectedMembers.addAll(mOriginalMembers);
        } else {
            for (UserModel contactToAdd : mOriginalMembers) {
                if (contactToAdd.getFullName().toLowerCase(Locale.getDefault()).contains(newText)) {
                    mSelectedMembers.add(contactToAdd);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.member_recycler_view_swipe_layout;
    }

    class GroupMemberViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvContactName, adminString;

        View message, delete, surface_view;

        GroupMemberViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.avatar_image);
            tvContactName = itemView.findViewById(R.id.contact_name);
            adminString = itemView.findViewById(R.id.admin_string);
            itemView.findViewById(R.id.contact_checkbox).setVisibility(View.INVISIBLE);
//            tvContactName.setTextSize(TypedValue.);

            surface_view = itemView.findViewById(R.id.surface_view);
            message = itemView.findViewById(R.id.message);
            delete = itemView.findViewById(R.id.delete);
        }

        void bindValues(final int position) {
            if (position >= mSelectedMembers.size()) {
                return;
            }

            final UserModel member = mSelectedMembers.get(position);

            try {
                Picasso.with(mActivity).load(member.getAvatar()).into(ivAvatar);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(member.getType().equals("Admin")) {
                adminString.setVisibility(View.VISIBLE);
            }

            tvContactName.setText(member.getFullName());

            // Hide delete option...
            delete.setVisibility(View.GONE);

            // get the logged in user...
            mUserModel = Utils.parseJson(UserSharedPreferences.getString(mActivity.getApplicationContext(), UserSharedPreferences.USER_MODEL), UserModel.class);

            // If group admin is logged in, show delete option for all the users...
            if (mUserModel.getId() == mActivity.groupAdminID) {

                // set visibility to visible for all the list items...
                delete.setVisibility(View.VISIBLE);
                message.setVisibility(View.VISIBLE);
            }

            // hide message and delete option on user's own name....
            if (mUserModel.getId() == member.getId()) {

                // set visibility to visible for all the list items...
                delete.setVisibility(View.GONE);
                message.setVisibility(View.GONE);
            }


            // implementing message button listener...
            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MessagesUserGroupListItem targetUser = new MessagesUserGroupListItem();
                    targetUser.setId(member.getId());
                    targetUser.setFullName(member.getFullName());
                    targetUser.setAvatar(member.getAvatar());

                    Intent intent = new Intent(mActivity, MessagesBySongsActivity.class);
                    intent.putExtra(MessagesBySongsActivity.CHAT_SCREEN_TARGET_USER, targetUser);
                    mActivity.startActivity(intent);
                }
            });


            // implements delete button listener...
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callRemoveUserFromGroupAPI(member.getId(), mActivity.groupID, position);
                }
            });

        }

    }


    // calls api to remove a user from the group...
    private void callRemoveUserFromGroupAPI(final long userID, long groupID, final int memberPosition) {

        String url = BuildConfig.BASE_URL + "/groups/delete_user_from_group";
        VolleyRequest volleyRequest = new VolleyRequest(mActivity, url, "main", true);

        HashMap<String, String> params = new HashMap<>();
        params.put("group_id", String.valueOf(groupID));
        params.put("user_id", String.valueOf(userID));

        final ProgressDialog pDialog = new ProgressDialog(mActivity);
        pDialog.setMessage("Deleting User..");
        pDialog.show();


        Log.v(TAG, "Calling Delete Member From Group API.\nURL:" + url);
        Log.v(TAG, "Delete Member From Group API Request Params : " + params.toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pDialog.dismiss();
                Log.v(TAG, "Delete Member From Group API Response:\n" + response.toString());
                if (!response.optBoolean("error", true)) {

                    // we also need to hide the data in the list...
                    // first remove from visible array list...
                    ViewGroupMembersListAdapter.this.mSelectedMembers.remove(memberPosition);
                    // then remove from original array list...
                    for (UserModel contactToDel : mOriginalMembers) {
                        if (contactToDel.getId() == userID) {
                            mOriginalMembers.remove(contactToDel);
                            break;
                        }
                    }
                    // then update the gui...
                    ViewGroupMembersListAdapter.this.notifyItemRemoved(memberPosition);
                    try {
                        String message = response.getString("message");
                        CustomToast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                Log.v(TAG, "Delete Member From Group API Error Response:\n" + error.getMessage());
                Log.v(TAG, "Delete Member From Group API Error Response:\n" + error.toString());
            }
        });

    }
}
