package com.hellodemo.models;

import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChatScreen implements Parcelable
{

    @SerializedName("song_title")
    @Expose
    private String songTitle;
    @SerializedName("music_id")
    @Expose
    private String musicId;
    @SerializedName("uploader_name")
    @Expose
    private String uploaderName;
    @SerializedName("self_avatar")
    @Expose
    private String selfAvatar;
    @SerializedName("user_avatar")
    @Expose
    private String userAvatar;
    @SerializedName("messages")
    @Expose
    private List<Message> messages = null;
    public final static Parcelable.Creator<ChatScreen> CREATOR = new Creator<ChatScreen>() {


        @SuppressWarnings({
                "unchecked"
        })
        public ChatScreen createFromParcel(Parcel in) {
            return new ChatScreen(in);
        }

        public ChatScreen[] newArray(int size) {
            return (new ChatScreen[size]);
        }

    }
            ;

    protected ChatScreen(Parcel in) {
        this.songTitle = ((String) in.readValue((String.class.getClassLoader())));
        this.musicId = ((String) in.readValue((String.class.getClassLoader())));
        this.uploaderName = ((String) in.readValue((String.class.getClassLoader())));
        this.selfAvatar = ((String) in.readValue((String.class.getClassLoader())));
        this.userAvatar = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.messages, (com.hellodemo.models.Message.class.getClassLoader()));
    }

    public ChatScreen() {
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getSelfAvatar() {
        return selfAvatar;
    }

    public void setSelfAvatar(String selfAvatar) {
        this.selfAvatar = selfAvatar;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(songTitle);
        dest.writeValue(musicId);
        dest.writeValue(uploaderName);
        dest.writeValue(selfAvatar);
        dest.writeValue(userAvatar);
        dest.writeList(messages);
    }

    public int describeContents() {
        return 0;
    }

}