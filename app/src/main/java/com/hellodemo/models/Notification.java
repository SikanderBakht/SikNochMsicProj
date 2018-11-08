package com.hellodemo.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Notification implements Parcelable {

    private static final String TAG = "HelloDemoNotification";

    @SerializedName("timestamp")
    @Expose
    private String relativeTimeStamp;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @SerializedName("sender")
    @Expose
    private String senderName;
    @SerializedName("group_name")
    @Expose
    private String groupName;
    @SerializedName("group_id")
    @Expose
    private long groupID;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("status")
    @Expose
    private int status;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("snoozer")
    @Expose
    private String snoozer;
    @SerializedName("muter")
    @Expose
    private String muter;
    @SerializedName("title")
    @Expose
    private String title;

    public static final String NOTIFICATION_TYPE_GROUP_INVITE = "group invite";//
    public static final String NOTIFICATION_TYPE_GROUP_INVITE_ACCEPT = "group_invite_accepted";//
    public static final String NOTIFICATION_TYPE_GROUP_INVITE_REJECT = "group_invite_rejected";//
    public static final String NOTIFICATION_TYPE_GENERAL_MESSAGE = "general_message";
    public static final String NOTIFICATION_TYPE_SOLO_MESSAGE = "solo message";
    public static final String NOTIFICATION_TYPE_GROUP_MESSAGE = "group message";
    public static final String NOTIFICATION_TYPE_SOLO_TRACK = "solo track";//
    public static final String NOTIFICATION_TYPE_SOLO_PACKAGE = "solo_package";//
    public static final String NOTIFICATION_TYPE_GROUP_TRACK = "group track";//
    public static final String NOTIFICATION_TYPE_GROUP_PACKAGE = "group_package";//
    public static final String NOTIFICATION_TYPE_FRIEND = "friend";//
    public static final String NOTIFICATION_TYPE_GROUP_REJECT = "group reject";
    public static final String NOTIFICATION_TYPE_SNOOZE = "snooze";//
    public static final String NOTIFICATION_TYPE_MUTE = "mute";

    protected Notification(Parcel in) {
        this.relativeTimeStamp = ((String) in.readValue((String.class.getClassLoader())));
        this.senderName = ((String) in.readValue((String.class.getClassLoader())));
        this.groupName = ((String) in.readValue((String.class.getClassLoader())));
        this.groupID = ((long) in.readValue((long.class.getClassLoader())));
        this.avatar = ((String) in.readValue((String.class.getClassLoader())));
        this.type = ((String) in.readValue((String.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.snoozer = ((String) in.readValue((String.class.getClassLoader())));
        this.muter = ((String) in.readValue((String.class.getClassLoader())));
        this.status = ((int) in.readValue((int.class.getClassLoader())));
        this.title = ((String) in.readValue((String.class.getClassLoader())));
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public Notification(String relativeTimeStamp, String senderName, String groupName,
                        long groupID, String avatar, String message,
                        int status, String type, String name, String snoozer, String muter, String title) {
        this.relativeTimeStamp = relativeTimeStamp;
        this.senderName = senderName;
        this.groupName = groupName;
        this.groupID = groupID;
        this.avatar = avatar;
        this.message = message;
        this.type = type;
        this.name = name;
        this.status = status;
        this.snoozer = snoozer;
        this.muter = muter;
        this.title = title;
    }

    public String getRelativeTimeStamp() {
        return relativeTimeStamp;
    }

    public void setRelativeTimeStamp(String timeStamp) {
        this.relativeTimeStamp = relativeTimeStamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getStatus() {
        return status != 0;
    }

    public void setStatus(boolean status) {
        if (status)
            this.status = 1;
        else
            this.status = 0;
    }

    public String getSnoozer() {
        return snoozer;
    }

    public void setSnoozer(String snoozer) {
        this.snoozer = snoozer;
    }

    public String getMuter() {
        return muter;
    }

    public void setMuter(String muter) {
        this.muter = muter;
    }

    public static List<Notification> removeUnknownNotifications(List<Notification> notificationList) {
        if (notificationList == null) {
            return null;
        }

        for (int i = 0; i < notificationList.size(); i++) {

            Notification notification = notificationList.get(i);

            switch (notification.getType()) {
                case Notification.NOTIFICATION_TYPE_GROUP_INVITE:
                case Notification.NOTIFICATION_TYPE_GROUP_INVITE_ACCEPT:
                case Notification.NOTIFICATION_TYPE_GROUP_INVITE_REJECT:
                case Notification.NOTIFICATION_TYPE_GENERAL_MESSAGE:
                case Notification.NOTIFICATION_TYPE_SOLO_MESSAGE:
                case Notification.NOTIFICATION_TYPE_GROUP_MESSAGE:
                case Notification.NOTIFICATION_TYPE_SOLO_TRACK:
                case Notification.NOTIFICATION_TYPE_SOLO_PACKAGE:
                case Notification.NOTIFICATION_TYPE_GROUP_TRACK:
                case Notification.NOTIFICATION_TYPE_GROUP_PACKAGE:
                case Notification.NOTIFICATION_TYPE_FRIEND:
                case Notification.NOTIFICATION_TYPE_GROUP_REJECT:
                case Notification.NOTIFICATION_TYPE_SNOOZE:
                case Notification.NOTIFICATION_TYPE_MUTE:
                    break;

                default:
                    Log.v(TAG, "UNKNOWN NOTIFICATION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    Log.v(TAG, "Type: " + notification.getType());
                    notificationList.remove(i);
                    i--;
            }
        }

        return notificationList;
    }
}