package com.hellodemo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
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
import com.google.gson.reflect.TypeToken;
import com.hellodemo.BuildConfig;
import com.hellodemo.ChatActivity;
import com.hellodemo.MainActivity;
import com.hellodemo.R;
import com.hellodemo.models.MessagesUserGroupListItem;
import com.hellodemo.models.Notification;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.ui.MemphisTextView;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;
import com.squareup.picasso.Picasso;

/**
 * Created by new user on 3/3/2018.
 */


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.MyViewHolder> {

    public static final String ACTIVITY_NAME = "notification_adapter";

    private String TAG = "HelloDemoNotificationsAdapter";
    private List<Notification> notificationsList = new ArrayList<>();
    private Activity mContext = null;

    public NotificationsAdapter(Activity context) {
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.onBindView(position);
    }

    // Clean all elements of the recycler
    public void clear() {
        notificationsList.clear();
        notifyDataSetChanged();
    }

    public void setDataNotify(List<Notification> list) {
        notificationsList.clear();
        notificationsList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (notificationsList == null)
            return 0;
        return notificationsList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView avatarCharacter, notificationText, notificationSubtext, acceptButton, declineButton;
        ImageView avatar;
        View myViewHolderView;

        MyViewHolder(View view) {
            super(view);
            myViewHolderView = view;
            avatar = view.findViewById(R.id.notification_avatar);
            avatarCharacter = view.findViewById(R.id.notification_group_name_first_letter);
            notificationText = view.findViewById(R.id.notification_text);
            notificationSubtext = view.findViewById(R.id.notification_subtext);
            acceptButton = view.findViewById(R.id.accept);
            declineButton = view.findViewById(R.id.decline);

        }

        void onBindView(final int position) {
            if (notificationsList == null) {
                return;
            }

            myViewHolderView.setVisibility((View.VISIBLE));
            acceptButton.setVisibility(View.INVISIBLE);
            declineButton.setVisibility(View.INVISIBLE);

            final Notification notification = notificationsList.get(position);

            String text = "";
            // getting relative time..
            Log.v("TESTTT", notification.getRelativeTimeStamp() + "");
            Log.v("TESTTT", System.currentTimeMillis() + "");
//            String subtext = (String) DateUtils.getRelativeTimeSpanString(notification.getTimeStamp() * 1000, System.currentTimeMillis(), 0);
            String subtext = notification.getRelativeTimeStamp();
            switch (notification.getType()) {
                case Notification.NOTIFICATION_TYPE_GROUP_INVITE:

                    // make accept and decline buttons visible...
                    acceptButton.setVisibility(View.VISIBLE);
                    declineButton.setVisibility(View.VISIBLE);

                    // set these buttons onclick listeners...
                    acceptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callAcceptGroupInvitationAPI(acceptButton, notificationText, avatar, avatarCharacter, notification.getGroupID(), position);
                            acceptButton.setVisibility(View.GONE);
                            declineButton.setVisibility(View.GONE);
                        }
                    });

                    declineButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callDeclineGroupInvitationAPI(declineButton, notificationText, avatar, avatarCharacter, notification.getGroupID(), position);
                            acceptButton.setVisibility(View.GONE);
                            declineButton.setVisibility(View.GONE);
                        }
                    });

                    text = notification.getSenderName() + " invited you to join " + notification.getGroupName();
                    avatarCharacter.setVisibility(View.INVISIBLE);
                    avatar.setVisibility(View.VISIBLE);
                    Picasso.with(mContext).load(notification.getAvatar()).into(avatar);
                    break;
                case Notification.NOTIFICATION_TYPE_GROUP_INVITE_ACCEPT:
                    text = "You joined " + notification.getGroupName();
                    avatarCharacter.setVisibility(View.VISIBLE);
                    avatarCharacter.setText(notification.getAvatar());
                    avatar.setVisibility(View.INVISIBLE);
                    break;
                case Notification.NOTIFICATION_TYPE_GROUP_INVITE_REJECT:
                    text = "You declined to join " + notification.getGroupName();
                    avatarCharacter.setVisibility(View.VISIBLE);
                    avatarCharacter.setText(notification.getAvatar());
                    avatar.setVisibility(View.INVISIBLE);
                    break;
                case Notification.NOTIFICATION_TYPE_GENERAL_MESSAGE:

                    text = notification.getMessage();
                    Picasso.with(mContext).load(notification.getAvatar()).into(avatar);
                    break;

                case Notification.NOTIFICATION_TYPE_SOLO_MESSAGE:
                case Notification.NOTIFICATION_TYPE_GROUP_MESSAGE:

                    text = notification.getSenderName() + " sent you a message";
                    Picasso.with(mContext).load(notification.getAvatar()).into(avatar);
                    break;

                case Notification.NOTIFICATION_TYPE_SOLO_TRACK:

                    text = notification.getSenderName() + " sent you a track";
                    Picasso.with(mContext).load(notification.getAvatar()).into(avatar);

                    // setting up the click listener on the notification..
                    myViewHolderView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("activity_name", NotificationsAdapter.ACTIVITY_NAME);
                            intent.putExtra("song_title", notification.getTitle());
                            mContext.startActivity(intent);
                            mContext.finish();
                        }
                    });
                    break;

                case Notification.NOTIFICATION_TYPE_SOLO_PACKAGE:

                    text = notification.getSenderName() + " sent you a package";
                    Picasso.with(mContext).load(notification.getAvatar()).into(avatar);

                    // setting up the click listener on the notification..
                    myViewHolderView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("activity_name", NotificationsAdapter.ACTIVITY_NAME);
                            intent.putExtra("song_title", notification.getTitle());
                            mContext.finish();
                            mContext.startActivity(intent);
                        }
                    });
                    break;

                case Notification.NOTIFICATION_TYPE_GROUP_TRACK:

                    text = notification.getSenderName() + " sent a track in " + notification.getGroupName();
                    Picasso.with(mContext).load(notification.getAvatar()).into(avatar);

                    // setting up the click listener on the notification..
                    myViewHolderView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setAction(MainActivity.ACTION_OPEN_GROUP_SCREEN);
                            intent.putExtra(MainActivity.KEY_GROUP_ID_EXTRA, notification.getGroupID());
                            intent.putExtra("activity_name", NotificationsAdapter.ACTIVITY_NAME);
                            intent.putExtra("song_title", notification.getTitle());
                            mContext.finish();
                            mContext.startActivity(intent);
                        }
                    });

                    break;

                case Notification.NOTIFICATION_TYPE_GROUP_PACKAGE:

                    text = notification.getSenderName() + " sent a package in " + notification.getGroupName();
                    Picasso.with(mContext).load(notification.getAvatar()).into(avatar);
                    myViewHolderView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setAction(MainActivity.ACTION_OPEN_GROUP_SCREEN);
                            intent.putExtra(MainActivity.KEY_GROUP_ID_EXTRA, notification.getGroupID());
                            intent.putExtra("activity_name", NotificationsAdapter.ACTIVITY_NAME);
                            intent.putExtra("song_title", notification.getTitle());
                            mContext.finish();
                            mContext.startActivity(intent);
                        }
                    });

                    break;

                case Notification.NOTIFICATION_TYPE_FRIEND:

                    text = "You are now friends with " + notification.getName();
                    Picasso.with(mContext).load(notification.getAvatar()).into(avatar);
                    break;

                case Notification.NOTIFICATION_TYPE_GROUP_REJECT:

                    text = notification.getSenderName() + " rejected to join your group " + notification.getGroupName();
                    Picasso.with(mContext).load(notification.getAvatar()).into(avatar);
                    break;

                case Notification.NOTIFICATION_TYPE_SNOOZE:

                    if (notification.getStatus()) {
                        text = notification.getSnoozer() + " snoozed you.";
                    } else {
                        text = notification.getSnoozer() + " unsnoozed you.";
                    }
                    Picasso.with(mContext).load(notification.getAvatar()).into(avatar);
                    break;

                case Notification.NOTIFICATION_TYPE_MUTE:

                    if (notification.getStatus()) {
                        text = notification.getMuter() + " muted you.";
                    } else {
                        text = notification.getMuter() + " unmuted you.";
                    }
                    Picasso.with(mContext).load(notification.getAvatar()).into(avatar);
                    break;

                default:
                    text = "UNKNOWN NOTIFICATION...";
                    avatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.circle_grey));
                    Log.v(TAG, "UNKNOWN NOTIFICATION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + notification.getType());
                    myViewHolderView.setVisibility(View.GONE);
            }

            notificationText.setText(text);
            notificationSubtext.setText(subtext);


        }
    }

    private void callAcceptGroupInvitationAPI(final View acceptButton, final TextView notificationText, final ImageView avatar, final TextView avatarCharacter, long groupID, final int position) {
        acceptButton.setAlpha((float) 0.5);

        String url = BuildConfig.BASE_URL + "/groups/accept_invitation";
        VolleyRequest volleyRequest = new VolleyRequest(mContext, url, "main", true);

        HashMap<String, String> params = new HashMap<>();
        params.put("group_id", String.valueOf(groupID));

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                acceptButton.setAlpha(1);

                Log.d(TAG, response.toString());

                if (!response.optBoolean("error", true)) {
                    CustomToast.makeText(mContext, response.optString("message"), Toast.LENGTH_SHORT).show();
                }
                notificationText.setText("You joined " + notificationsList.get(position).getGroupName());
                avatarCharacter.setVisibility(View.VISIBLE);
                avatarCharacter.setText(notificationsList.get(position).getAvatar());
                avatar.setVisibility(View.INVISIBLE);
//                notificationsList.remove(position);
//                NotificationsAdapter.this.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                acceptButton.setAlpha(1);
            }
        });
    }

    private void callDeclineGroupInvitationAPI(final View declineButton, final TextView notificationText, final ImageView avatar, final TextView avatarCharacter, long groupID, final int position) {
        declineButton.setAlpha((float) 0.5);

        String url = BuildConfig.BASE_URL + "/groups/reject_invitation";
        VolleyRequest volleyRequest = new VolleyRequest(mContext, url, "main", true);

        HashMap<String, String> params = new HashMap<>();
        params.put("group_id", String.valueOf(groupID));

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                declineButton.setAlpha(1);

                Log.d(TAG, response.toString());

                if (!response.optBoolean("error", true)) {
                    CustomToast.makeText(mContext, response.optString("message"), Toast.LENGTH_SHORT).show();
                }
                notificationText.setText("You declined to join " + notificationsList.get(position).getGroupName());
                avatarCharacter.setVisibility(View.VISIBLE);
                avatarCharacter.setText(notificationsList.get(position).getAvatar());
                avatar.setVisibility(View.INVISIBLE);
//                notificationsList.remove(position);
//                NotificationsAdapter.this.notifyItemRemoved(position);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                declineButton.setAlpha(1);
            }
        });
    }
}