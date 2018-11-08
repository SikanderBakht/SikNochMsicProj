package com.hellodemo;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.hellodemo.adapter.ChatAdapter;
import com.hellodemo.fragment.MessagesFragment;
import com.hellodemo.fragment.MusicPlayerFragment;
import com.hellodemo.interfaces.XmlClickableInterface;
import com.hellodemo.models.ChatScreen;
import com.hellodemo.models.Message;
import com.hellodemo.models.MusicListItem;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.ui.MemphisTextView;
import com.hellodemo.utils.CustomToast;
import com.hellodemo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String TAG = "HelloDemoChatActivity";

    // DATA KEYS
    public static final String KEY_TARGET_USER_ID = "target_user_id";
    public static final String KEY_TARGET_USER_FULLNAME = "target_user_fullname";
    public static final String KEY_MUSIC_TITLE = "music_title";
    public static final String KEY_MUSIC_UPLOAD_ID = "music_upload_id";
    public static final String KEY_GROUP_ID = "group_id";
    public static final String KEY_GROUP_NAME = "group_name";
    public static final String KEY_ADMIN_ID = "admin_id";
    public static final long KEY_ADMIN_NAME = 0;

    // ACTION KEYS
    public static final String ACTION_OPEN_CHAT_BY_PACKAGE = "chat_by_package";
    public static final String ACTION_OPEN_CHAT_BY_SONG = "chat_by_song";
    public static final String ACTION_OPEN_CHAT_BY_GROUP = "chat_by_group";
    public static final String ACTION_OPEN_CHAT_BY_FRIEND = "chat_by_friend";

    // notification actions keys
    public static final String ACTION_NEW_MESSAGE_NOTIFICATION = "message_notification";
    public static final String ACTION_NEW_GROUP_MESSAGE_NOTIFICATION = "group_message_notification";

    MusicListItem mMusicListItem;
    FrameLayout flContent;
    private UserModel mUserModel;
    private String mAction = "";
    private ChatScreen mChatScreenModel;
    private RecyclerView chat_recycler_view;
    private EditText messageInput;
    private ImageView sendButton;
    private ChatAdapter mChatAdapter;
    private long targetUserID, musicUploadIDOrPackageID, groupID;
    private int code;
    long adminId;

    // this variable will be true if activity is visible on screen
    private static boolean isActive = false;

    MusicPlayerFragment musicFragment = null;
    private Socket mSocket;
    // flag variable..
    private boolean onResumeCalledFromNewIntentMethod = false;
    private boolean muted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "onCreate started");

        setContentView(R.layout.activity_chat);

        if (!UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);

        TextView tvSubtitle = findViewById(R.id.chat_subtitle);
        TextView tvTitle = findViewById(R.id.chat_title_song_name);
        chat_recycler_view = findViewById(R.id.chat_recycler_view);
        sendButton = findViewById(R.id.send_button);
        messageInput = findViewById(R.id.message_input);
        ImageView back_icon = findViewById(R.id.back_icon);
        ImageView settings_icon = findViewById(R.id.settings_icon);
        ImageView notes_icon = findViewById(R.id.notes);
        flContent = (FrameLayout) findViewById(R.id.flContent);

        if (getIntent() == null || getIntent().getAction() == null) {
            CustomToast.makeText(this, "Can not open chat!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mAction = getIntent().getAction();
        Log.v(TAG, "Chat screen action: " + mAction);

        // getting passed data based on the calling action...
        if (mAction.equals(ACTION_OPEN_CHAT_BY_SONG) || mAction.equals(ACTION_OPEN_CHAT_BY_PACKAGE)) {
            targetUserID = getIntent().getLongExtra(ChatActivity.KEY_TARGET_USER_ID, -1);
            String targetUserFullName = getIntent().getStringExtra(ChatActivity.KEY_TARGET_USER_FULLNAME);
            musicUploadIDOrPackageID = getIntent().getLongExtra(ChatActivity.KEY_MUSIC_UPLOAD_ID, -1);
            String musicTitle = getIntent().getStringExtra(ChatActivity.KEY_MUSIC_TITLE);
            settings_icon.setVisibility(View.GONE);
            notes_icon.setVisibility(View.VISIBLE);

            // Checking data validity...
            Log.v(TAG, "Input Data:\n" +
                    targetUserID + "\n" +
                    musicUploadIDOrPackageID + "\n" +
                    targetUserFullName + "\n" +
                    musicTitle);
            if (targetUserID == -1 || musicUploadIDOrPackageID == -1 || targetUserFullName == null || musicTitle == null) {
                CustomToast.makeText(this, "Chat Screen: Invalid input", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            tvTitle.setText(musicTitle);
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setText(targetUserFullName);

        } else if (mAction.equals(ACTION_OPEN_CHAT_BY_GROUP)) {

            groupID = getIntent().getLongExtra(ChatActivity.KEY_GROUP_ID, -1);
            String groupName = getIntent().getStringExtra(ChatActivity.KEY_GROUP_NAME);
            adminId = getIntent().getLongExtra(ChatActivity.KEY_ADMIN_ID, 0);

            // Checking data validity...
            Log.v(TAG, "Input Data:\n" +
                    groupID + "\n" +
                    groupName);
            if (groupID == -1 || groupName == null) {
                CustomToast.makeText(this, "Chat Screen: Invalid input", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            tvTitle.setText(groupName);


        } else if(mAction.equals(ACTION_OPEN_CHAT_BY_FRIEND)) {
            targetUserID = getIntent().getLongExtra(ChatActivity.KEY_TARGET_USER_ID, -1);
            String targetUserFullName = getIntent().getStringExtra(ChatActivity.KEY_TARGET_USER_FULLNAME);
            settings_icon.setVisibility(View.GONE);
            notes_icon.setVisibility(View.GONE);

            if (targetUserID == -1 || targetUserFullName == null) {
                CustomToast.makeText(this, "Chat Screen: Invalid input", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            tvTitle.setText(targetUserFullName);
            tvSubtitle.setVisibility(View.VISIBLE);
            tvSubtitle.setText(targetUserFullName);
        }

        chat_recycler_view.setLayoutManager(new LinearLayoutManager(this));
        mChatAdapter = new ChatAdapter(this, mUserModel.getId());
        chat_recycler_view.setAdapter(mChatAdapter);


        notes_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, NotesActivity.class);

                switch (mAction) {
                    case ACTION_OPEN_CHAT_BY_SONG:

                        intent.setAction(NotesActivity.ACTION_OPEN_NOTES_BY_SONG);
                        intent.putExtra(NotesActivity.KEY_MUSIC_UPLOAD_ID, musicUploadIDOrPackageID);

                        break;
                    case ACTION_OPEN_CHAT_BY_GROUP:

                        intent.setAction(NotesActivity.ACTION_OPEN_NOTES_BY_GROUP);
                        intent.putExtra(NotesActivity.KEY_GROUP_ID, groupID);

                        break;
                    case ACTION_OPEN_CHAT_BY_PACKAGE:
                        intent.setAction(NotesActivity.ACTION_OPEN_NOTES_BY_PACKAGE);
                        intent.putExtra(NotesActivity.KEY_PACKAGE_ID, musicUploadIDOrPackageID);
                }
                startActivity(intent);
            }
        });
        settings_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater li = LayoutInflater.from(getApplicationContext());
                final View promptsView = li.inflate(R.layout.settings_opengroupchat, null);

                final BottomSheetDialog dialog = new BottomSheetDialog(ChatActivity.this, R.style.CustomBottomSheetDialogTheme);
                dialog.setContentView(promptsView);
                dialog.show();
                View vLeaveGroup = promptsView.findViewById(R.id.leave_group_button);
                long userid = Utils.parseJson(UserSharedPreferences.getString(getBaseContext(),
                        UserSharedPreferences.USER_MODEL), UserModel.class).getId();

                if (userid == adminId) {
                    vLeaveGroup.setAlpha(.5f);
                    vLeaveGroup.setEnabled(false);
                }
                final MemphisTextView silentBtn = promptsView.findViewById(R.id.silent_button);
                if(muted) {
                    silentBtn.setText("Un-Silent Group");
                } else {
                    silentBtn.setText("Silent Group");
                }
                silentBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //api/groups/delete_user_from_group
                        String url = BuildConfig.BASE_URL + "/groups/" + groupID + "/silence";
                        VolleyRequest volleyRequest = new VolleyRequest(getBaseContext(), url, "silent_group", true);
                        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // mainLoadingManger.hideLoadingIcon();
                                if (!response.optBoolean("error", true)) {
                                    CustomToast.makeText(ChatActivity.this, response.optString("message"), Toast.LENGTH_LONG).show();
                                    if(response.optString("message").equals("Group has been un-silenced")) {
                                        silentBtn.setText("Silent Group");
                                    } else {
                                        silentBtn.setText("Un-Silent Group");
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //     mainLoadingManger.hideLoadingIcon();
                                Log.d("", "");
                            }
                        });
                    }
                });
                MemphisTextView leaveGroupBtn = promptsView.findViewById(R.id.leave_group_button);
                leaveGroupBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //api/groups/delete_user_from_group

                        String url = BuildConfig.BASE_URL + "/groups/delete_user_from_group";
                        VolleyRequest volleyRequest = new VolleyRequest(getBaseContext(), url, "delete", true);

                        HashMap<String, String> params = new HashMap<>();
                        params.put("user_id", (String.valueOf(mUserModel.getId())));
                        params.put("group_id", String.valueOf(groupID));

                        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                                //mainLoadingManger.hideLoadingIcon();
                                if (!response.optBoolean("error", true)) {
                                    CustomToast.makeText(ChatActivity.this, response.optString("message"), Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //     mainLoadingManger.hideLoadingIcon();
                                Log.d("", "");
                            }
                        });
                    }
                });

                promptsView.findViewById(R.id.notes_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        Intent intent = new Intent(ChatActivity.this, NotesActivity.class);

                        if (mAction.equals(ACTION_OPEN_CHAT_BY_SONG)) {

                            intent.setAction(NotesActivity.ACTION_OPEN_NOTES_BY_SONG);
                            intent.putExtra(NotesActivity.KEY_MUSIC_UPLOAD_ID, musicUploadIDOrPackageID);

                        } else if (mAction.equals(ACTION_OPEN_CHAT_BY_GROUP)) {

                            intent.setAction(NotesActivity.ACTION_OPEN_NOTES_BY_GROUP);
                            intent.putExtra(NotesActivity.KEY_GROUP_ID, groupID);

                        }
                        startActivity(intent);
                    }
                });

                promptsView.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

            }
        });

        back_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!messageInput.getText().toString().trim().isEmpty()) {
                    sendMessage(messageInput.getText().toString().trim());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //musicFragment.cleanPlayer(true, true);
    }

    private void markConversationRead() {

        String url = BuildConfig.BASE_URL + "/messages/mark_as_read";

        switch (mAction) {
            case ACTION_OPEN_CHAT_BY_SONG:
                url += "?sender_id=" + targetUserID
                        + "&receiver_id=" + mUserModel.getId()
                        + "&music_id=" + musicUploadIDOrPackageID
                        + "&group_id=";
                break;
            case ACTION_OPEN_CHAT_BY_PACKAGE:
                url += "?sender_id=" + targetUserID
                        + "&receiver_id=" + mUserModel.getId()
                        + "&package_id=" + musicUploadIDOrPackageID
                        + "&group_id=";
                break;
            case ACTION_OPEN_CHAT_BY_GROUP:
                url += "?user_id=" + mUserModel.getId()
                        + "&group_id=" + groupID;
                break;
            case ACTION_OPEN_CHAT_BY_FRIEND:
                url += "?sender_id=" + targetUserID
                        + "&receiver_id=" + mUserModel.getId();
                break;
        }

        VolleyRequest volleyRequest = new VolleyRequest(this, url, "mark_as_read", true);

        Log.v(TAG, "67834 Calling Mark Message as Read API\nURL: " + url);
        volleyRequest.requestServer(Request.Method.POST, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.v(TAG, "67834 Mark Message as Read API Response: " + response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.v(TAG, "67834 Mark Message as Read Error Response: " + error.getMessage());
                Log.v(TAG, "67834 Mark Message as Read Error Response: " + error.toString());

            }
        });
    }

    public void sendMessage(String messageText) {

        // append message to the chat immediately...
        final Message message = new Message();
        message.setUserId(mUserModel.getId());
        message.setUser_avatar(mUserModel.getAvatar());
        message.setMessage(messageText);
        message.setCreatedAt((DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()).toString()));
        message.setUpdatedAt((DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()).toString()));
        message.setDayDate((DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()).toString()));
        message.setDeliveryState(Message.MESSAGE_DELIVERY_PENDING);
        mChatAdapter.appendMessageToData(message);

        messageInput.setText("");
        scrollToBottom();

        String url = BuildConfig.BASE_URL + "/send_message";

        HashMap<String, String> params = new HashMap<>();

        if (mAction.equals(ACTION_OPEN_CHAT_BY_SONG)) {
            params.put("user_id", String.valueOf(mUserModel.getId()));
            params.put("target_user_id", String.valueOf(targetUserID));
            params.put("music_id", String.valueOf(musicUploadIDOrPackageID));
            params.put("message", String.valueOf(message.getMessage()));
            params.put("group_id", "");
        } else if (mAction.equals(ACTION_OPEN_CHAT_BY_PACKAGE)) {
            params.put("user_id", String.valueOf(mUserModel.getId()));
            params.put("target_user_id", String.valueOf(targetUserID));
            params.put("package_id", String.valueOf(musicUploadIDOrPackageID));
            params.put("message", String.valueOf(message.getMessage()));
            params.put("group_id", "");
        } else if (mAction.equals(ACTION_OPEN_CHAT_BY_GROUP)) {
            params.put("user_id", String.valueOf(mUserModel.getId()));
            params.put("message", String.valueOf(message.getMessage()));
            params.put("group_id", groupID + "");
        } else if (mAction.equals(ACTION_OPEN_CHAT_BY_FRIEND)) {
            params.put("target_user_id", String.valueOf(targetUserID));
            params.put("message", String.valueOf(message.getMessage()));
        }

        VolleyRequest volleyRequest = new VolleyRequest(this, url, "send_message", true);

        Log.v(TAG, "93874 Calling Send Message API\nURL: " + url);
        Log.v(TAG, "93874 Send Message API\nParams: " + params.toString());

        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.v(TAG, "93874 Send Message API Response: " + response.toString());

                if (!response.optBoolean("error", true)) {
                    message.setDeliveryState(Message.MESSAGE_DELIVERY_SUCCESS);
                    mChatAdapter.notifyDataSetChanged();
                } else {
                    message.setDeliveryState(Message.MESSAGE_DELIVERY_FAILED);
                    mChatAdapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "93874 Send Message Error Response: " + error.getMessage());
                Log.v(TAG, "93874 Send Message Error Response: " + error.toString());

                message.setDeliveryState(Message.MESSAGE_DELIVERY_FAILED);
                mChatAdapter.notifyDataSetChanged();
            }
        });

    }

    private void onLoadWebservice() {
        String url = BuildConfig.BASE_URL + "/conversation";

        if (mAction.equals(ACTION_OPEN_CHAT_BY_PACKAGE)) {
            url = BuildConfig.BASE_URL + "/messages/package";
        }

        boolean isGroup = false;

        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(mUserModel.getId()));

        switch (mAction) {
            case ACTION_OPEN_CHAT_BY_SONG:
                params.put("target_user_id", targetUserID + "");
                params.put("music_id", musicUploadIDOrPackageID + "");
                break;

            case ACTION_OPEN_CHAT_BY_PACKAGE:
                params.put("target_id", targetUserID + "");
                params.put("package_id", musicUploadIDOrPackageID + "");
                break;

            case ACTION_OPEN_CHAT_BY_GROUP:
                params.put("group_id", String.valueOf(groupID));
                url += "/groups";
                isGroup = true;
                break;
            case ACTION_OPEN_CHAT_BY_FRIEND:
                params.put("target_user_id", targetUserID + "");
                url += "/direct";
                break;
        }

        final boolean finalIsGroup = isGroup;

        Log.v(TAG, "Conversation API url : " + url);
        Log.v(TAG, "Conversation API Params : " + params.toString());

        VolleyRequest volleyRequest = new VolleyRequest(this, url, "conversation", true);
        volleyRequest.requestServer(Request.Method.POST, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // if we get new conversation, we need to mark it as read..
                markConversationRead();
                Log.v(TAG, response.toString());
                Log.v(TAG, "Conversation API Response  : " + response.toString());
                if (!response.optBoolean("error", true) && response.has("message")) {
                    try {
                        mChatScreenModel = Utils.parseJson(response.optJSONObject("message").toString(), ChatScreen.class);
                        if(response.optJSONObject("message").has("music_url")) {
                            mMusicListItem = Utils.parseJson(response.optJSONObject("message").optJSONObject("music_object").toString(), MusicListItem.class);
                            mMusicListItem.setMusicUrl(response.optJSONObject("message").optString("music_url"));
                            mMusicListItem.setArtist(response.optJSONObject("message").optJSONObject("music_object").getString("artist_name"));
                            mMusicListItem.setAvatar(response.optJSONObject("message").optString("user_avatar"));
                            if (mAction.equals(ACTION_OPEN_CHAT_BY_SONG) || mAction.equals(ACTION_OPEN_CHAT_BY_PACKAGE)) {
                                try {
                                    musicFragment = MusicPlayerFragment.newInstance(MusicPlayerFragment.KEY_SCREEN_CHAT, null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                updateFragment();
                            }
                        } else {
                            flContent.setVisibility(View.GONE);
                        }
                        if(response.optJSONObject("message").has("muted")) {
                            muted = response.optJSONObject("message").getBoolean("muted");
                        }
//                        tvSubtitle.setText(mChatScreenModel.getUploaderName());
                        mChatAdapter.setData(mChatScreenModel, finalIsGroup);
                        mChatAdapter.notifyDataSetChanged();
//                        tvTitle.setText(mChatScreenModel.getSongTitle());
                        scrollToBottom();
                    } catch (Exception e) {
                        Log.v("", "");
                    }
                    Log.v("", "");
                } else {
                    CustomToast.makeText(ChatActivity.this, "Unable to get conversation", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "Error Response  : " + error.getMessage());
                Log.v(TAG, "Error Response  : " + error.getLocalizedMessage());
            }
        });


    }

    public void onFragmentElementClicked(View v) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager != null) {
            if (fragmentManager.getFragments().size() == 1) {

                // all fragments must implement XmlClickableInterface or the code will crash...
                ((XmlClickableInterface) fragmentManager.getFragments().get(0)).onViewClicked(v);
            }
        }
    }

    private void scrollToBottom() {
        chat_recycler_view.scrollToPosition(chat_recycler_view.getAdapter().getItemCount() - 1);
    }

    public static boolean isActive() {
        return isActive;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
        NotificationManager notificationManager =
                (NotificationManager) ChatActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
        if(MyFirebaseMessagingService.msgSenderIDs.contains(targetUserID)) {
            long i = MyFirebaseMessagingService.msgNotificationIDs.get(MyFirebaseMessagingService.msgSenderIDs.indexOf(targetUserID));
            while(i >= targetUserID * 100) {
                notificationManager.cancel((int)i);
                i--;
            }
            MyFirebaseMessagingService.msgNotificationIDs.set(MyFirebaseMessagingService.msgSenderIDs.indexOf(targetUserID), targetUserID * 100);
        }
        if(MyFirebaseMessagingService.msgSenderIDs.contains(groupID)) {
            long i = MyFirebaseMessagingService.msgNotificationIDs.get(MyFirebaseMessagingService.msgSenderIDs.indexOf(groupID));
            while(i >= groupID * 100) {
                notificationManager.cancel((int)i);
                i--;
            }
            MyFirebaseMessagingService.msgNotificationIDs.set(MyFirebaseMessagingService.msgSenderIDs.indexOf(groupID), groupID * 100);
        }
        /*if(code == 1003) {
            for(int i = 0; i < MyFirebaseMessagingService.notification_new_message_id; i++) {
                notificationManager.cancel(i);
            }
            MyFirebaseMessagingService.notification_new_message_id = 0;
        }*/
        Log.v(TAG, "onResume");

        if (onResumeCalledFromNewIntentMethod) {
            onResumeCalledFromNewIntentMethod = false;
            return;
        }
        Log.v(TAG, "onResume 2");

        onLoadWebservice();
        // connecting socket...
        setupSocket();
        setupMessageSeenSocket();
    }

    private void updateFragment() {
        if (musicFragment == null)
            return;

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).
                replace(R.id.flContent, musicFragment)
                .commit();
    }
    public MusicListItem getMusicListItem() {
        return mMusicListItem;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        onResumeCalledFromNewIntentMethod = true;

        if (intent != null && intent.getAction() != null) {

            // if we get a notification for new message...
            if (intent.getAction().equals(ChatActivity.ACTION_NEW_MESSAGE_NOTIFICATION)) {

                // getting notification data...
                String messageString = intent.getStringExtra("message");

                try {
                    // parse data into json object...
                    JSONObject message = new JSONObject(messageString);

                    // if the current chat is not for a track or package, we will show notification instead...
                    if (mAction.equals(ACTION_OPEN_CHAT_BY_GROUP)) {
                        MyFirebaseMessagingService.showNewMessageNotification(message, this);
                        return;
                    }

                    // now we need to check if the current chat is for the same user and track...
                    JSONObject response = message.getJSONObject("response");
                    JSONObject payload = response.getJSONObject("payload");

                    long senderID = payload.getInt("sender_id");
                    long packageID = 0;
                    if (payload.optString("package_id") != null && !payload.optString("package_id").equals("null"))
                        packageID = Long.parseLong(payload.optString("package_id"));

                    long musicID = 0;
                    if (payload.optString("music_id") != null && !payload.optString("music_id").equals("null"))
                        musicID = Long.parseLong(payload.optString("music_id"));

                    long musicPackageID = 0;
                    if (packageID == 0) {   // if the message is on on song...
                        musicPackageID = musicID;
                    } else if (musicID == 0) { // if the message is on on package...
                        musicPackageID = packageID;
                    }


                    // if its the same user and track...
                    if (targetUserID == senderID && musicPackageID == musicUploadIDOrPackageID) {
                        // we will ignore the push notification and let the socket handle the message.
                        return;
                    }
                    // if its not the same user and track...
                    else {
                        MyFirebaseMessagingService.showNewMessageNotification(message, this);
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // if we get a notification for new message in group...
            else if (intent.getAction().equals(ChatActivity.ACTION_NEW_GROUP_MESSAGE_NOTIFICATION)) {

                // getting notification data...
                String messageString = intent.getStringExtra("message");

                try {
                    // parse data into json object...
                    JSONObject message = new JSONObject(messageString);

                    // if the current chat is not for a group, we will show notification instead...
                    if (!mAction.equals(ACTION_OPEN_CHAT_BY_GROUP)) {
                        MyFirebaseMessagingService.showNewGroupMessageNotification(message, this);
                        return;
                    }

                    // now we need to check if the current chat is for the same user and track...
                    JSONObject response = message.getJSONObject("response");
                    JSONObject payload = response.getJSONObject("payload");

                    long groupIDNotification = payload.getInt("group_id");

                    // if its the same group...
                    if (groupIDNotification == groupID) {
                        // we will ignore the push notification and let the socket handle the message.
                        return;
                    }
                    // if its not the same group...
                    else {
                        MyFirebaseMessagingService.showNewGroupMessageNotification(message, this);
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off();
    }

    private void setupSocket() {
        try {
            if (mSocket != null && mSocket.connected()) {
                mSocket.off();
                mSocket.disconnect();
            }

            mSocket = IO.socket(BuildConfig.SOCKET_URL);

            // if its a group page, we need to add group id to the socket params
            String groupIdAppend = "";
            if (mAction.equalsIgnoreCase(ACTION_OPEN_CHAT_BY_GROUP)) {
                groupIdAppend = ",\"groupId\": '[{\"id\": " + groupID + "}]'";
            }
            final JSONObject connObj = new JSONObject("{\"userId\": " + mUserModel.getId() + groupIdAppend + "}");
            mSocket.emit("myConnection", connObj);

            Emitter.Listener messageLisetener = new Emitter.Listener() {
                @Override
                public void call(final Object... args) {

                    // Stuff that updates the UI should be put on ui thread...
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (args != null) {
                                if (args.length > 0) {
                                    Log.v(TAG, "New message received on Socket:\n" + args[0]);
                                    handleNewMessageReceivedOnSocket((String) args[0]);
                                    scrollToBottom();
                                }
                            }
                        }
                    });
                }
            };


//            if (mAction.equalsIgnoreCase(ACTION_OPEN_CHAT_BY_GROUP))
//                mSocket.on("group-" + groupID, messageLisetener);
//            else
            mSocket.on("message", messageLisetener);
            mSocket.connect();

            Log.v(TAG, "Emitting message on myConnection: " + "{\"userId\": " + mUserModel.getId() + groupIdAppend + "}");
            Log.v(TAG, "Socket Connected on " + BuildConfig.SOCKET_URL + " : " + mSocket.connected());

        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.v(TAG, "Socket Error");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupMessageSeenSocket() {
        try {
            if (mSocket != null && mSocket.connected()) {
                mSocket.off();
                mSocket.disconnect();
            }

            mSocket = IO.socket(BuildConfig.SOCKET_URL);

            // if its a group page, we need to add group id to the socket params
            String groupIdAppend = "";
            if (mAction.equalsIgnoreCase(ACTION_OPEN_CHAT_BY_GROUP)) {
                groupIdAppend = ",\"groupId\": '[{\"id\": " + groupID + "}]'";
            }
            final JSONObject connObj = new JSONObject("{\"userId\": " + mUserModel.getId() + groupIdAppend + "}");
            mSocket.emit("myConnection", connObj);

            Emitter.Listener messageLisetener1 = new Emitter.Listener() {
                @Override
                public void call(final Object... args) {

                    // Stuff that updates the UI should be put on ui thread...
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (args != null) {
                                if (args.length > 0) {
                                    Log.v(TAG, "Message Seen on Socket:\n" + args[0]);
                                    List<Message> list = mChatScreenModel.getMessages();
                                    int listSize = list.size();
                                    mChatScreenModel.getMessages().get(listSize - 1).setStatus(1);
                                    mChatAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });
                }
            };
//            if (mAction.equalsIgnoreCase(ACTION_OPEN_CHAT_BY_GROUP))
//                mSocket.on("group-" + groupID, messageLisetener);
//            else
            mSocket.on("message_read", messageLisetener1);
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.v(TAG, "Socket Error");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void notifyAboutNewMessage() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();

            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            assert am != null;
            switch (am.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:
                    Log.i("MyApp", "Silent mode");
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    Log.i("MyApp", "Vibrate mode");
                    ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(500);
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    Log.i("MyApp", "Normal mode");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleNewMessageReceivedOnSocket(String jsonString) {
        try {
            JSONObject jObj = new JSONObject(jsonString);
            String messageType = jObj.getString("message_type");
            // find out if group message or direct message...
            if (messageType.equalsIgnoreCase("direct")) {
                // getting parameters...
                JSONObject jSender = jObj.getJSONObject("user");
                JSONObject jReceiver = jObj.getJSONObject("target_user");
                String messageText = jObj.getString("message");
                String messageTime = jObj.getString("dayDate");
                long packageId = jObj.optLong("packageId");
                long musicId = jObj.optLong("music");
                String senderAvatar = jSender.getString("avatar");
                long senderID = jSender.getLong("id");
                long receiverID = jReceiver.getLong("id");
                if(packageId == 0 && musicId == 0) {
                    if (!mAction.equalsIgnoreCase(ACTION_OPEN_CHAT_BY_FRIEND)) {
                        // we don't want to process this message since our chat screen isn't displaying group messages...
                        return;
                    }
                    final Message message = new Message();
                    message.setUserId(senderID);
                    message.setUser_avatar(senderAvatar);
                    message.setMessage(messageText);
                    message.setDayDate(messageTime);
                    mChatAdapter.appendMessageToData(message);
                    markConversationRead();
                    notifyAboutNewMessage();
                }
                // lets see if its on a package or on a track...
                else if (packageId == 0) {
                    // its on a track...
                    if (!mAction.equalsIgnoreCase(ACTION_OPEN_CHAT_BY_SONG)) {
                        // we don't want to process this message since our chat screen isnt displaying song messages...
                        return;
                    }
                    // now we check if this is the correct song chat screen..
                    if (musicId != musicUploadIDOrPackageID) {
                        return;
                    }
                    // lets append the message to the chat...
                    final Message message = new Message();
                    message.setUserId(senderID);
                    message.setUser_avatar(senderAvatar);
                    message.setMessage(messageText);
                    message.setDayDate(messageTime);
                    mChatAdapter.appendMessageToData(message);
                    markConversationRead();
                    notifyAboutNewMessage();
                } else if (musicId == 0) {
                    // its on a package...
                    if (!mAction.equalsIgnoreCase(ACTION_OPEN_CHAT_BY_PACKAGE)) {
                        // we don't want to process this message since our chat screen isnt displaying package messages...
                        return;
                    }

                    // now we check if this is the correct package chat screen..
                    if (packageId != musicUploadIDOrPackageID) {
                        return;
                    }

                    // lets append the message to the chat...
                    final Message message = new Message();
                    message.setUserId(senderID);
                    message.setUser_avatar(senderAvatar);
                    message.setMessage(messageText);

                    mChatAdapter.appendMessageToData(message);
                    markConversationRead();
                    notifyAboutNewMessage();
                }

            } else if (messageType.equalsIgnoreCase("group")) {
                // its a group message...

                if (!mAction.equalsIgnoreCase(ACTION_OPEN_CHAT_BY_GROUP)) {
                    // we don't want to process this message since our chat screen isn't displaying group messages...
                    return;
                }

                // getting data
                String messageText = jObj.getString("message");
//                String senderAvatar = jObj.getString("self_avatar_url");
//                long senderID = jObj.getLong("user_id");
                JSONObject jSender = jObj.getJSONObject("user");
                String senderAvatar = jSender.getString("avatar");
                long senderID = jSender.getLong("id");

                // if message wasn't sent by this user, append it to the chat...
                if (senderID != mUserModel.getId()) {

                    // lets append the message to the chat...
                    final Message message = new Message();
                    message.setUserId(senderID);
                    message.setUser_avatar(senderAvatar);
                    message.setMessage(messageText);

                    mChatAdapter.appendMessageToData(message);
                    markConversationRead();
                    notifyAboutNewMessage();
                }

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
