package com.hellodemo;

/**
 * Created by Mahnoor on 20/04/2018.
 */

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hellodemo.MainActivity;
import com.hellodemo.R;
import com.hellodemo.adapter.NotificationsAdapter;
import com.hellodemo.models.UserModel;
import com.hellodemo.network.VolleyRequest;
import com.hellodemo.preferences.UserSharedPreferences;
import com.hellodemo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String ACTIVITY_NAME = "firebase_messaging_service";
    public static final String HELLODEMO_CHANNEL_ID = "com.hellodemo.HELLODEMOCHANNEL";
    public static final String HELLODEMO_CHANNEL_NAME = "HELLODEMO CHANNEL";

    private NotificationManager mManager;
    private String TAG = "HelloDemoMyFirebaseMessagingService";
    private static final int NOTIFICATION_CODE_FRIEND_ADDED = 1001;
    private static final int NOTIFICATION_CODE_FOLLOWED_YOU = 1002;
    private static final int NOTIFICATION_CODE_NEW_MESSAGE = 1003;
    private static final int NOTIFICATION_CODE_NEW_GROUP_MESSAGE = 1004;
    private static final int NOTIFICATION_CODE_NEW_INBOX = 1005;
    private static final int NOTIFICATION_CODE_NEW_GROUP_TRACK = 1006;
    private static final int NOTIFICATION_CODE_NEW_GROUP_INVITE = 1007;

    public static List<Long> msgNotificationIDs = new ArrayList<Long>();
    public static List<Long> msgSenderIDs = new ArrayList<Long>();

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Log.v(TAG, "Message Received");

        // From now on, we will get all required data in json format.
        Log.v(TAG, "Message Data: " + message.getData().toString());
        try {
            String messagex = message.getData().toString().replace("response=", "\"response\":");
            JSONObject jObj = new JSONObject(messagex);
            showNotification(jObj);
        } catch (JSONException e) {
            Log.v(TAG, "Couldn't parse notification");
            e.printStackTrace();
        }
    }

    public void createChannels() {
        // create android channel
        NotificationChannel helloDemoChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            helloDemoChannel = new NotificationChannel(HELLODEMO_CHANNEL_ID,
                    HELLODEMO_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            // Sets whether notifications posted to this channel should display notification lights
            helloDemoChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            helloDemoChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            helloDemoChannel.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            helloDemoChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mManager.createNotificationChannel(helloDemoChannel);
        }
    }

    private void showNotification(JSONObject message) throws JSONException {
        JSONObject response = message.getJSONObject("response");
        String title = response.getString("title");
        String body = response.getString("body");
        int code = response.getInt("code");
        JSONObject payload = response.getJSONObject("payload");

        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createChannels();
        // We have to see if the notification is being sent to the correct user..
        if (!isNotificationForCurrentUser(payload)) {
            return;
        }
        if (code == NOTIFICATION_CODE_FRIEND_ADDED) {
            showFriendAddedNotification(message);
        } else if (code == NOTIFICATION_CODE_NEW_INBOX) {
            // if main activity is open, we will let it know about new inbox notification as well
//            if (MainActivity.isActive()) {
//                Intent intent = new Intent(this, MainActivity.class);
//                intent.putExtra("message", message.toString());
//                intent.setAction(MainActivity.ACTION_NEW_INBOX_TRACK_NOTIFICATION);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                intent.putExtra("activity_name", NotificationsAdapter.ACTIVITY_NAME);
//                intent.putExtra("song_title", title);
//                startActivity(intent);
//            }
            showNewInboxNotification(message);
        } else if (code == NOTIFICATION_CODE_NEW_MESSAGE) {
            // if chat activity is open, we will let it handle the messages
            if (ChatActivity.isActive()) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("message", message.toString());
                intent.setAction(ChatActivity.ACTION_NEW_MESSAGE_NOTIFICATION);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                showNewMessageNotification(message, this);
            }
        } else if (code == NOTIFICATION_CODE_NEW_GROUP_MESSAGE) {
            // if chat activity is open, we will let it handle the messages
            if (ChatActivity.isActive()) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("message", message.toString());
                intent.setAction(ChatActivity.ACTION_NEW_GROUP_MESSAGE_NOTIFICATION);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            } else {
                showNewGroupMessageNotification(message, this);
            }
        } else if (code == NOTIFICATION_CODE_NEW_GROUP_TRACK) {
            showNewGroupTrackNotification(message);
        } else if (code == NOTIFICATION_CODE_NEW_GROUP_INVITE) {
            showGroupInviteNotification(message);
        } else {
            //On click of notification it redirect to this Activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, HELLODEMO_CHANNEL_ID)
                    .setSmallIcon(R.drawable.icon_hello)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setContentIntent(pendingIntent);

//            NotificationManager notificationManager =
//                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mManager.notify((new Random()).nextInt(), notificationBuilder.build());
            MainActivity a = (MainActivity) MainActivity.getAppContext();
            a.setUnreadAnimation();
        }
    }

    // checks if notification is for a single user...
    // if yes, checks if user is logged in...
    // if yes, verifies user id..
    // if correct returns true...
    private boolean isNotificationForCurrentUser(JSONObject payload) throws JSONException {

        // check if notification is for a single user...
        if (payload.has("push_notification_for")) {

            long id = payload.getInt("push_notification_for");

            // if yes, checks if user is logged in...
            if (UserSharedPreferences.getBoolean(this, UserSharedPreferences.USER_LOGGED_IN)) {
                UserModel mUserModel = Utils.parseJson(UserSharedPreferences.getString(this, UserSharedPreferences.USER_MODEL), UserModel.class);

                // if yes, verifies user id..
                return mUserModel.getId() == id;
            }

            return false;
        }
        // if it isn't for a single user we can return yes...
        else {
            return true;
        }

    }

    private void showFriendAddedNotification(JSONObject message) throws JSONException {
        JSONObject response = message.getJSONObject("response");
        String title = response.getString("title");
        String body = response.getString("body");
        int code = response.getInt("code");
        JSONObject payload = response.getJSONObject("payload");

        //On click of notification it redirect to this Activity
        Intent intent = new Intent(this, NotificationsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(MainActivity.ACTION_OPEN_INBOX_SCREEN);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, HELLODEMO_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_hello)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mManager.notify((new Random()).nextInt(), notificationBuilder.build());
        MainActivity a = (MainActivity) MainActivity.getAppContext();
        a.setUnreadAnimation();
    }

    private void showNewInboxNotification(JSONObject message) throws JSONException {
        JSONObject response = message.getJSONObject("response");
        String title = response.getString("title");
        String body = response.getString("body");
        int code = response.getInt("code");
        JSONObject payload = response.getJSONObject("payload");


        //On click of notification it redirect to this Activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("activity_name", MyFirebaseMessagingService.ACTIVITY_NAME);
        intent.putExtra("song_title", title);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(MainActivity.ACTION_OPEN_INBOX_SCREEN);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, HELLODEMO_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_hello)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mManager.notify(NOTIFICATION_CODE_NEW_INBOX, notificationBuilder.build());
        MainActivity a = (MainActivity) MainActivity.getAppContext();
        a.setUnreadAnimation();
    }

    public static void showNewMessageNotification(JSONObject message, Context context) throws JSONException {
        JSONObject response = message.getJSONObject("response");
        String title = response.getString("title");
        String body = response.getString("body");
        int code = response.getInt("code");
        JSONObject payload = response.getJSONObject("payload");

//        Log.v(TAG, "TEST1:\n"
//                + payload.getInt("sender_id")
//                + payload.getInt("target_user_id")
//                + payload.getString("music_id"));
        long senderID = payload.getInt("sender_id");
        long targetUserID = payload.getInt("target_user_id");
        String targetUserName = payload.getString("target_user_name");

        long packageID = 0;
        if (payload.optString("package_id") != null && !payload.optString("package_id").equals("null"))
            packageID = Long.parseLong(payload.optString("package_id"));

        long musicID = 0;
        if (payload.optString("music_id") != null && !payload.optString("music_id").equals("null"))
            musicID = Long.parseLong(payload.optString("music_id"));

        String musicTitle = payload.optString("music_title");
        if(musicTitle == null || musicTitle.equals("null")){
            musicTitle = payload.optString("package_title");
        }
        if(musicTitle == null || musicTitle.equals("null")){
            musicTitle = "";
        }

        long notificationID;

        if(msgSenderIDs.isEmpty() || !msgSenderIDs.contains(senderID)) {
            notificationID = senderID * 100;
            msgSenderIDs.add(senderID);
            msgNotificationIDs.add(notificationID);
        } else {
            notificationID = msgNotificationIDs.get(msgSenderIDs.indexOf(senderID));
            notificationID++;
            msgNotificationIDs.set(msgSenderIDs.indexOf(senderID), notificationID);
        }


        //On click of notification it redirect to this Activity
        Intent intent = new Intent(context, ChatActivity.class);

        // passing the values in parameter...
        if (packageID == 0) {   // if the message is on on song...
            intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_SONG);
            intent.putExtra(ChatActivity.KEY_MUSIC_UPLOAD_ID, musicID);
        } else if (musicID == 0) { // if the message is on on package...
            intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_PACKAGE);
            intent.putExtra(ChatActivity.KEY_MUSIC_UPLOAD_ID, packageID);
        } else {
            return;
        }

        intent.putExtra(ChatActivity.KEY_TARGET_USER_ID, senderID);
        intent.putExtra(ChatActivity.KEY_TARGET_USER_FULLNAME, targetUserName);
        intent.putExtra(ChatActivity.KEY_MUSIC_TITLE, musicTitle);


//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int dummyuniqueInt = new Random().nextInt(543254);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, dummyuniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, HELLODEMO_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_hello)
                .setContentTitle(title)
                .setContentText(body)
                //.setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) notificationID, notificationBuilder.build());
    }


    public static void showNewGroupMessageNotification(JSONObject message, Context context) throws JSONException {
        JSONObject response = message.getJSONObject("response");
        String title = response.getString("title");
        String body = response.getString("body");
        int code = response.getInt("code");
        JSONObject payload = response.getJSONObject("payload");

//        Log.v(TAG, "TEST1:\n"
//                + payload.getInt("sender_id")
//                + payload.getInt("target_user_id")
//                + payload.getString("music_id"));
//        long senderID = payload.getInt("sender_id");
        long groupID = payload.getInt("group_id");
        String groupName = payload.getString("group_name");

        long notificationID;

        if(msgSenderIDs.isEmpty() || !msgSenderIDs.contains(groupID)) {
            notificationID = groupID * 100;
            msgSenderIDs.add(groupID);
            msgNotificationIDs.add(notificationID);
        } else {
            notificationID = msgNotificationIDs.get(msgSenderIDs.indexOf(groupID));
            notificationID++;
            msgNotificationIDs.set(msgSenderIDs.indexOf(groupID), notificationID);
        }


        //On click of notification it redirect to this Activity
        Intent intent = new Intent(context, ChatActivity.class);

        // passing the values in parameter...
        intent.setAction(ChatActivity.ACTION_OPEN_CHAT_BY_GROUP);
        intent.putExtra(ChatActivity.KEY_GROUP_NAME, groupName);
        intent.putExtra(ChatActivity.KEY_GROUP_ID, groupID);


//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int dummyuniqueInt = new Random().nextInt(543254);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, dummyuniqueInt, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, HELLODEMO_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_hello)
                .setContentTitle(title)
                .setContentText(body)
                //.setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) notificationID, notificationBuilder.build());
    }

    private void showNewGroupTrackNotification(JSONObject message) throws JSONException {
        JSONObject response = message.getJSONObject("response");
        String title = response.getString("title");
        String body = response.getString("body");
        int code = response.getInt("code");
        JSONObject payload = response.getJSONObject("payload");

        long senderID = payload.getInt("sender_id");
        long groupID = payload.getInt("group_id");
        long uploadID = payload.getInt("upload_id");

        //On click of notification it redirect to this Activity
        Intent intent = new Intent(this, MainActivity.class);

        // passing the values in parameter...
        intent.setAction(MainActivity.ACTION_OPEN_GROUP_SCREEN);
        intent.putExtra(MainActivity.KEY_GROUP_ID_EXTRA, groupID);
        intent.putExtra("activity_name", NotificationsAdapter.ACTIVITY_NAME);
        intent.putExtra("song_title", title);


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, HELLODEMO_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_hello)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mManager.notify((new Random()).nextInt(), notificationBuilder.build());
        MainActivity a = (MainActivity) MainActivity.getAppContext();
        a.setUnreadAnimation();



        // if main activity is open, we will let it know about new inbox notification as well
//        if (MainActivity.isActive()) {
//            Intent intent2 = new Intent(this, MainActivity.class);
//            intent2.putExtra("group_id", groupID);
//            intent2.setAction(MainActivity.ACTION_NEW_GROUP_TRACK_NOTIFICATION);
//            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            startActivity(intent2);
//        }
    }

    private void showGroupInviteNotification(JSONObject message) throws JSONException {
        JSONObject response = message.getJSONObject("response");
        String title = response.getString("title");
        String body = response.getString("body");
        int code = response.getInt("code");
        JSONObject payload = response.getJSONObject("payload");

        //On click of notification it redirect to this Activity
        Intent intent = new Intent(this, NotificationsActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, HELLODEMO_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_hello)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mManager.notify((new Random()).nextInt(), notificationBuilder.build());
        MainActivity a = (MainActivity) MainActivity.getAppContext();
        a.setUnreadAnimation();
    }
}