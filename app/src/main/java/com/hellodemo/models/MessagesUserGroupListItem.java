package com.hellodemo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// Same Class may contain model data for user or model data for group...
// identifiable by type...
public class MessagesUserGroupListItem implements Parcelable
{
    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("name")
    @Expose
    private String name;                        // Used for Group Name
    @SerializedName("creater")
    @Expose
    private long creater;                       // Used for Creater of the Group ID
    @SerializedName("group_note")
    @Expose
    private String groupNote;;                  // Used for Group Notes
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("sms_token")
    @Expose
    private String smsToken;
    @SerializedName("email_token")
    @Expose
    private String emailToken;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("remember_token")
    @Expose
    private String rememberToken;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("qrcode")
    @Expose
    private String qrcode;
    @SerializedName("subscription")
    @Expose
    private String subscription;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("ios_token")
    @Expose
    private Object iosToken;
    @SerializedName("second_email")
    @Expose
    private String secondEmail;
    @SerializedName("msg_id")
    @Expose
    private long msgId;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("music_id")
    @Expose
    private long musicId;
    @SerializedName("unread_count")
    @Expose
    private long unreadCount;
    public final static Parcelable.Creator<MessagesUserGroupListItem> CREATOR = new Creator<MessagesUserGroupListItem>() {
        @SuppressWarnings({
                "unchecked"
        })
        public MessagesUserGroupListItem createFromParcel(Parcel in) {
            return new MessagesUserGroupListItem(in);
        }
        public MessagesUserGroupListItem[] newArray(int size) {
            return (new MessagesUserGroupListItem[size]);
        }
    };

    protected MessagesUserGroupListItem(Parcel in) {
        this.id = ((long) in.readValue((long.class.getClassLoader())));
        this.email = ((String) in.readValue((String.class.getClassLoader())));
        this.phone = ((String) in.readValue((String.class.getClassLoader())));
        this.username = ((String) in.readValue((String.class.getClassLoader())));
        this.fullName = ((String) in.readValue((String.class.getClassLoader())));
        this.smsToken = ((String) in.readValue((String.class.getClassLoader())));
        this.emailToken = ((String) in.readValue((String.class.getClassLoader())));
        this.updatedAt = ((String) in.readValue((String.class.getClassLoader())));
        this.createdAt = ((String) in.readValue((String.class.getClassLoader())));
        this.rememberToken = ((String) in.readValue((String.class.getClassLoader())));
        this.type = ((String) in.readValue((String.class.getClassLoader())));
        this.qrcode = ((String) in.readValue((String.class.getClassLoader())));
        this.subscription = ((String) in.readValue((String.class.getClassLoader())));
        this.avatar = ((String) in.readValue((String.class.getClassLoader())));
        this.iosToken = ((Object) in.readValue((Object.class.getClassLoader())));
        this.secondEmail = ((String) in.readValue((String.class.getClassLoader())));
        this.msgId = ((long) in.readValue((long.class.getClassLoader())));
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.musicId = ((long) in.readValue((long.class.getClassLoader())));
        this.unreadCount = ((long) in.readValue((long.class.getClassLoader())));
    }

    public MessagesUserGroupListItem() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGroupName() {
        return name;
    }

    public void setGroupName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String firstName) {
        this.fullName = firstName;
    }

    public long getCreater() {
        return creater;
    }

    public void setCreater(long creater) {
        this.creater = creater;
    }

    public String getSmsToken() {
        return smsToken;
    }

    public void setSmsToken(String smsToken) {
        this.smsToken = smsToken;
    }

    public String getEmailToken() {
        return emailToken;
    }

    public void setEmailToken(String emailToken) {
        this.emailToken = emailToken;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getRememberToken() {
        return rememberToken;
    }

    public void setRememberToken(String rememberToken) {
        this.rememberToken = rememberToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Object getIosToken() {
        return iosToken;
    }

    public void setIosToken(Object iosToken) {
        this.iosToken = iosToken;
    }

    public String getSecondEmail() {
        return secondEmail;
    }

    public void setSecondEmail(String secondEmail) {
        this.secondEmail = secondEmail;
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getMusicId() {
        return musicId;
    }

    public void setMusicId(long musicId) {
        this.musicId = musicId;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(email);
        dest.writeValue(phone);
        dest.writeValue(username);
        dest.writeValue(fullName);
        dest.writeValue(smsToken);
        dest.writeValue(emailToken);
        dest.writeValue(updatedAt);
        dest.writeValue(createdAt);
        dest.writeValue(rememberToken);
        dest.writeValue(type);
        dest.writeValue(qrcode);
        dest.writeValue(subscription);
        dest.writeValue(avatar);
        dest.writeValue(iosToken);
        dest.writeValue(secondEmail);
        dest.writeValue(msgId);
        dest.writeValue(message);
        dest.writeValue(musicId);
        dest.writeValue(unreadCount);
    }

    public int describeContents() {
        return 0;
    }

}