package com.hellodemo.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Song implements Parcelable
{

    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("last_message")
    @Expose
    private String lastMessage;
    @SerializedName("unread_messages")
    @Expose
    private long unreadMessages;
    @SerializedName("type")
    @Expose
    private String type;

    public static final String TYPE_PACKAGE = "package";
    public static final String TYPE_TRACK = "track";

    public final static Parcelable.Creator<Song> CREATOR = new Creator<Song>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return (new Song[size]);
        }

    }
            ;

    protected Song(Parcel in) {
        this.id = ((long) in.readValue((long.class.getClassLoader())));
        this.title = ((String) in.readValue((String.class.getClassLoader())));
        this.lastMessage = ((String) in.readValue((String.class.getClassLoader())));
        this.unreadMessages = ((long) in.readValue((long.class.getClassLoader())));
        this.type = ((String) in.readValue((String.class.getClassLoader())));
    }

    public Song() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(long unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(title);
        dest.writeValue(lastMessage);
        dest.writeValue(unreadMessages);
    }

    public int describeContents() {
        return 0;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}