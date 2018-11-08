package com.hellodemo.adapter;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hellodemo.R;
import com.hellodemo.interfaces.ScreenInterfaceListener;
import com.hellodemo.models.NavMenuModel;
import com.hellodemo.models.UserModel;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.Utils;

/**
 * Created by new user on 2/17/2018.
 */

public class NavScreenAdapter extends RecyclerView.Adapter<NavScreenAdapter.NavScreenViewHolder> {

//    private String[] screens = {"Inbox", "Uploads", "Messages", "Favorites", "Settings"};
    private String[] screens = {};

    private AppCompatActivity mActivity;
    private int selectedItemPosition = -1;
    private NavMenuModel data;
    private UserModel mUserModel;

    public NavScreenAdapter(AppCompatActivity appCompatActivity) {
        mActivity = appCompatActivity;
        mUserModel = Utils.parseJson(UserSharedPreferences.getString(mActivity, UserSharedPreferences.USER_MODEL), UserModel.class);
        if(mUserModel.getType().equals("label")) {
            screens = new String[]{"Inbox", "Messages", "Favorites", "Settings"};
        } else {
            screens = new String[]{"Inbox", "Uploads", "Messages", "Favorites", "Settings"};
        }
    }

    @Override
    public NavScreenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nav_menu, parent, false);
        return new NavScreenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NavScreenViewHolder holder, int position) {
        holder.onBindView(position);
    }

    @Override
    public int getItemCount() {
        return screens.length;
    }

    public void setData(NavMenuModel data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
        notifyDataSetChanged();
    }

    public class NavScreenViewHolder extends RecyclerView.ViewHolder {
        TextView screen_title, screen_nav_count;
        View unread_messages_dot;

        public NavScreenViewHolder(View itemView) {
            super(itemView);
            screen_title = itemView.findViewById(R.id.screen_title);
            screen_nav_count = itemView.findViewById(R.id.screen_nav_count);
            unread_messages_dot = itemView.findViewById(R.id.unread_messages_dot);
        }

        public void onBindView(int position) {
            screen_title.setText(screens[position]);
            if (position == selectedItemPosition) {
                itemView.setSelected(true);
            } else {
                itemView.setSelected(false);
            }
            if(mUserModel.getType().equals("label")) {
                switch (position) {
                    case 0: { //inbox
                        if (data != null)
                            screen_nav_count.setText(String.format("(%d)", data.getInboxNum()));
                        break;
                    }
                    case 1: { //messages
                        if (data != null) {
                            screen_nav_count.setText(String.format("(%d)", data.getMessagesNum()));
                            if (data.getMessagesNum() > 0) {
                                unread_messages_dot.setVisibility(View.VISIBLE);
                            } else {
                                unread_messages_dot.setVisibility(View.GONE);
                            }
                        }
                        break;
                    }
                    case 2: { //Favorite
                        if (data != null)
                            screen_nav_count.setText(String.format("(%d)", data.getFavoriteNum()));
                        break;
                    }
                    case 3: { //Settings
                        break;
                    }
                }
            } else {
                switch (position) {
                    case 0: { //inbox
                        if (data != null)
                            screen_nav_count.setText(String.format("(%d)", data.getInboxNum()));
                        break;
                    }
                    case 1: { //uploads
                        if (data != null)
                            screen_nav_count.setText(String.format("(%d)", data.getUploadNum()));
                        break;
                    }
                    case 2: { //messages
                        if (data != null) {
                            screen_nav_count.setText(String.format("(%d)", data.getMessagesNum()));
                            if (data.getMessagesNum() > 0) {
                                unread_messages_dot.setVisibility(View.VISIBLE);
                            } else {
                                unread_messages_dot.setVisibility(View.GONE);
                            }
                        }
                        break;
                    }
                    case 3: { //Favorite
                        if (data != null)
                            screen_nav_count.setText(String.format("(%d)", data.getFavoriteNum()));
                        break;
                    }
                    case 4: { //Settings
                        break;
                    }
                }
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedItemPosition = getAdapterPosition();
                    notifyDataSetChanged();
                    if (mActivity != null && selectedItemPosition != -1) {
                        ((ScreenInterfaceListener) mActivity).selectedScreen(selectedItemPosition, screens[selectedItemPosition]);
                    }
                }
            });
        }
    }
}
