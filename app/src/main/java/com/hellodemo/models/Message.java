package com.hellodemo.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Message implements Parcelable {

    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("user_id")
    @Expose
    private long userId;
    @SerializedName("receiver_id")
    @Expose
    private long receiverId;
    @SerializedName("music_id")
    @Expose
    private long musicId;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("user_avatar")
    @Expose
    private String user_avatar;
    @SerializedName("status")
    @Expose
    private long status;
    @SerializedName("is_notes")
    @Expose
    private long isNotes;
    @SerializedName("group_id")
    @Expose
    private Object groupId;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("dayDate")
    @Expose
    private String dayDate;

    // this flag is used to identify if the message sent has actually been delivered or not...
    // its used only for the messages sent... to show error in case of bad network...
    private int deliveryState = 2;
    public static final int MESSAGE_DELIVERY_SUCCESS = 0;
    public static final int MESSAGE_DELIVERY_FAILED = 1;
    public static final int MESSAGE_DELIVERY_PENDING = 2;


    public final static Parcelable.Creator<Message> CREATOR = new Creator<Message>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return (new Message[size]);
        }

    };

    protected Message(Parcel in) {
        this.id = ((long) in.readValue((long.class.getClassLoader())));
        this.userId = ((long) in.readValue((long.class.getClassLoader())));
        this.receiverId = ((long) in.readValue((long.class.getClassLoader())));
        this.musicId = ((long) in.readValue((long.class.getClassLoader())));
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.status = ((long) in.readValue((long.class.getClassLoader())));
        this.isNotes = ((long) in.readValue((long.class.getClassLoader())));
        this.groupId = ((Object) in.readValue((Object.class.getClassLoader())));
        this.createdAt = ((String) in.readValue((String.class.getClassLoader())));
        this.updatedAt = ((String) in.readValue((String.class.getClassLoader())));
        this.user_avatar = ((String) in.readValue((String.class.getClassLoader())));
        this.dayDate = ((String) in.readValue((String.class.getClassLoader())));
    }

    public Message() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(long receiverId) {
        this.receiverId = receiverId;
    }

    public long getMusicId() {
        return musicId;
    }

    public void setMusicId(long musicId) {
        this.musicId = musicId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public long getIsNotes() {
        return isNotes;
    }

    public void setIsNotes(long isNotes) {
        this.isNotes = isNotes;
    }

    public Object getGroupId() {
        return groupId;
    }

    public void setGroupId(Object groupId) {
        this.groupId = groupId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUser_avatar() {
        return user_avatar;
    }

    public void setUser_avatar(String user_avatar) {
        this.user_avatar = user_avatar;
    }

    public String getDayDate() {
        return dayDate;
    }

    public void setDayDate(String dayDate) {
        this.dayDate = dayDate;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(userId);
        dest.writeValue(receiverId);
        dest.writeValue(musicId);
        dest.writeValue(message);
        dest.writeValue(status);
        dest.writeValue(isNotes);
        dest.writeValue(groupId);
        dest.writeValue(createdAt);
        dest.writeValue(updatedAt);
        dest.writeValue(user_avatar);
        dest.writeValue(dayDate);
    }

    public int describeContents() {
        return 0;
    }

    public int getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(int deliveryState) {
        this.deliveryState = deliveryState;
    }
}