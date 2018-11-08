package com.hellodemo.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hellodemo.ChatActivity;
import com.hellodemo.MainActivity;
import com.hellodemo.MessagesBySongsActivity;
import com.hellodemo.R;
import com.hellodemo.interfaces.UIConstants;
import com.hellodemo.models.MessagesUserGroupListItem;
import com.hellodemo.preferences.UserSharedPreferences;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by new user on 2/24/2018.
 */

public class MessagesListRecyclerAdapter extends RecyclerView.Adapter<MessagesListRecyclerAdapter.MessagesViewHolder> {

    private String TAG = "HelloDemoMessagesListRecyclerAdapter";
    private final Activity mActivity;
    private List<MessagesUserGroupListItem> mSearchedUsersLists = new ArrayList<>();
    private List<MessagesUserGroupListItem> mOriginalUsersLists = new ArrayList<>();

    public MessagesListRecyclerAdapter(Activity appCompatActivity) {
        mActivity = appCompatActivity;
    }

    @Override
    public MessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_messages_list, parent, false);
        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessagesViewHolder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        return mSearchedUsersLists.size();
    }

    public void setDataNotify(List<MessagesUserGroupListItem> users_list) {
        mSearchedUsersLists.clear();
        mSearchedUsersLists.addAll(users_list);
        mOriginalUsersLists.clear();
        mOriginalUsersLists.addAll(users_list);
        notifyDataSetChanged();
    }

    public void performSearch(String newText) {
        newText = newText.toLowerCase(Locale.getDefault());
        mSearchedUsersLists.clear();

        if (newText.length() == 0) {
            mSearchedUsersLists.addAll(mOriginalUsersLists);
        } else {
            for (MessagesUserGroupListItem messagesUserGroupListItem : mOriginalUsersLists) {
                //if user...
                if ((messagesUserGroupListItem.getType().equals("user") || messagesUserGroupListItem.getType().equals("direct")) &&
                        messagesUserGroupListItem.getFullName().toLowerCase(Locale.getDefault()).contains(newText)) {
                    mSearchedUsersLists.add(messagesUserGroupListItem);
                }

                // if its a group...
                if (messagesUserGroupListItem.getType().equals("group") &&
                        messagesUserGroupListItem.getGroupName().toLowerCase(Locale.getDefault()).contains(newText)) {
                    mSearchedUsersLists.add(messagesUserGroupListItem);
                }
            }
        }
        notifyDataSetChanged();
    }


    class MessagesViewHolder extends RecyclerView.ViewHolder {
        private TextView recycler_view_list_item_title, unread_messages_count, group_name_first_letter;
        private ImageView recycler_view_list_item_image;

        MessagesViewHolder(View itemView) {
            super(itemView);
            recycler_view_list_item_image = itemView.findViewById(R.id.recycler_view_list_item_image);
            recycler_view_list_item_title = itemView.findViewById(R.id.recycler_view_list_item_title);
            unread_messages_count = itemView.findViewById(R.id.unread_messages_count);
            group_name_first_letter = itemView.findViewById(R.id.messages_list_item_group_name_first_letter);
        }

        void bindView(int position) {
            MessagesUserGroupListItem userGroup = mSearchedUsersLists.get(position);


            // in case of user, we show their image and name
            if (userGroup.getType().equals("user") || userGroup.getType().equals("direct")) {

                // hide group initial and show image...
                group_name_first_letter.setVisibility(View.GONE);
                recycler_view_list_item_image.setVisibility(View.VISIBLE);

                // setting image and name...
                Picasso.with(mActivity).load(userGroup.getAvatar()).into(recycler_view_list_item_image);
                recycler_view_list_item_title.setText(userGroup.getFullName());
            }
            // else if group, we show group initial and group name
            else if (userGroup.getType().equals("group")) {

                // show group initial and hide image...
                group_name_first_letter.setVisibility(View.VISIBLE);
                recycler_view_list_item_image.setVisibility(View.GONE);

                // setting initial and name...
                if (userGroup.getGroupName() != null && !userGroup.getGroupName().isEmpty()) {
                    group_name_first_letter.setText(userGroup.getGroupName().toUpperCase().charAt(0)+"");
                }
                recycler_view_list_item_title.setText(userGroup.getGroupName());

            }

            // setting up number of unread messages...
            if (userGroup.getUnreadCount() > 0) {
                unread_messages_count.setText(String.valueOf(userGroup.getUnreadCount()));
                unread_messages_count.setVisibility(View.VISIBLE);
            } else {
                unread_messages_count.setVisibility(View.GONE);
            }

            // settling up click listener
            itemView.setTag(userGroup);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MessagesUserGroupListItem userGroup = (MessagesUserGroupListItem) view.getTag();

                    // in case of users we will show songs these two have shared...
                    if (userGroup.getType().equals("user")) {
                        Intent intent = new Intent(mActivity, MessagesBySongsActivity.class);
                        intent.putExtra(MessagesBySongsActivity.CHAT_SCREEN_TARGET_USER, userGroup);
                        mActivity.startActivity(intent);
                    } else if(userGroup.getType().equals("direct")) {
                        Intent intent = new Intent(mActivity, ChatActivity.class);
                        intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_FRIEND);
                        intent.putExtra(ChatActivity.KEY_TARGET_USER_ID, userGroup.getId());
                        intent.putExtra(ChatActivity.KEY_TARGET_USER_FULLNAME, userGroup.getFullName());
                        mActivity.startActivity(intent);
                    }
                    // else if a group list item is clicked, we will take user to the chat screen...
                    else if (userGroup.getType().equals("group")) {

                        Intent intent = new Intent(mActivity, ChatActivity.class);

                        intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_GROUP);

                        intent.putExtra(ChatActivity.KEY_GROUP_ID, userGroup.getId());
                        intent.putExtra(ChatActivity.KEY_GROUP_NAME, userGroup.getGroupName());
                        intent.putExtra(ChatActivity.KEY_ADMIN_ID, userGroup.getCreater());

                        mActivity.startActivity(intent);
                    }
                }
            });
        }
    }
}
