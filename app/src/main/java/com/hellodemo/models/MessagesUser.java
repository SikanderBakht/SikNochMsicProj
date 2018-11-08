package com.hellodemo.models;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MessagesUser implements Parcelable {

    @SerializedName("sender_name")
    @Expose
    private String senderName;
    @SerializedName("sender_image")
    @Expose
    private String senderImage;
    @SerializedName("unread_count")
    @Expose
    private long unreadCount;
    @SerializedName("songs")
    @Expose
    private List<Song> songs = null;

    public final static Parcelable.Creator<MessagesUser> CREATOR = new Creator<MessagesUser>() {


        @SuppressWarnings({
                "unchecked"
        })
        public MessagesUser createFromParcel(Parcel in) {
            return new MessagesUser(in);
        }

        public MessagesUser[] newArray(int size) {
            return (new MessagesUser[size]);
        }

    };

    protected MessagesUser(Parcel in) {
        this.senderName = ((String) in.readValue((String.class.getClassLoader())));
        this.senderImage = ((String) in.readValue((String.class.getClassLoader())));
        this.unreadCount = ((long) in.readValue((long.class.getClassLoader())));
        in.readList(this.songs, (com.hellodemo.models.Song.class.getClassLoader()));
    }

    public MessagesUser() {
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(senderName);
        dest.writeValue(senderImage);
        dest.writeValue(unreadCount);
        dest.writeList(songs);
    }

    public int describeContents() {
        return 0;
    }

}
