package com.hellodemo.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hellodemo.ChatActivity;
import com.hellodemo.R;
import com.hellodemo.models.Friend;
import com.hellodemo.ui.MemphisTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    private AppCompatActivity mActivity;
    private List<Friend> friendsList = new ArrayList<>();

    public FriendAdapter(AppCompatActivity activity) {
        mActivity = activity;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.friend_list_item, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        holder.bindValues(position);
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public void setDataNotify(List<Friend> fList) {
        friendsList.clear();
        friendsList.addAll(fList);
        notifyDataSetChanged();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder{
        private ImageView recycler_view_list_item_image;
        private MemphisTextView friend_name;

        public FriendViewHolder(final View itemView) {
            super(itemView);
            friend_name = itemView.findViewById(R.id.friend_name);
            recycler_view_list_item_image = itemView.findViewById(R.id.recycler_view_list_item_image);
        }

        public void bindValues(int position) {
            if (position >= friendsList.size())
                return;

            final Friend friend = friendsList.get(position);
            Picasso.with(mActivity).load(friend.getAvatar()).into(recycler_view_list_item_image);
            friend_name.setText(friend.getFullName());
            itemView.setTag(friend);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mActivity, ChatActivity.class);
                    intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_FRIEND);
                    intent.putExtra(ChatActivity.KEY_TARGET_USER_ID, friend.getId());
                    intent.putExtra(ChatActivity.KEY_TARGET_USER_FULLNAME, friend.getFullName());
                    mActivity.startActivity(intent);
                }
            });
        }
    }
}
