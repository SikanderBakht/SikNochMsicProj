package com.hellodemo.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by new user on 2/18/2018.
 */

public class MusicListItem implements Parcelable {
    private String TAG = "HelloDemoMusicListItem";

    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("uploads_id")
    @Expose
    private long uploadsId;
    @SerializedName("receiver")
    @Expose
    private long receiver;
    @SerializedName("favorite")
    @Expose
    private long favorite;
    @SerializedName("filter")
    @Expose
    private long filter;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("ondelete")
    @Expose
    private long ondelete;
    @SerializedName("check")
    @Expose
    private long check = -1;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("file_name")
    @Expose
    private String fileName;
    @SerializedName("uploader_id")
    @Expose
    private long uploaderId;
    @SerializedName("border_color")
    @Expose
    private String borderColor;
    @SerializedName("music_url")
    @Expose
    private String musicUrl;
    @SerializedName("artist")
    @Expose
    private String artist;

    // used by favorites page only. This value is returned by get-favorite api only as well.
    // possible values are 'group' and 'inbox'
    @SerializedName("type")
    @Expose
    private String type;

    // used by favorites page only. This value is returned by get-favorite api only as well.
    @SerializedName("group_id")
    @Expose
    private long groupID;
    public final static Parcelable.Creator<MusicListItem> CREATOR = new Creator<MusicListItem>() {


        @SuppressWarnings({"unchecked"})
        public MusicListItem createFromParcel(Parcel in) {
            return new MusicListItem(in);
        }

        public MusicListItem[] newArray(int size) {
            return (new MusicListItem[size]);
        }

    };

    protected MusicListItem(Parcel in) {
        this.id = ((long) in.readValue((long.class.getClassLoader())));
        this.uploadsId = ((long) in.readValue((long.class.getClassLoader())));
        this.receiver = ((long) in.readValue((long.class.getClassLoader())));
        this.favorite = ((long) in.readValue((long.class.getClassLoader())));
        this.filter = ((long) in.readValue((long.class.getClassLoader())));
        this.createdAt = ((String) in.readValue((String.class.getClassLoader())));
        this.updatedAt = ((String) in.readValue((String.class.getClassLoader())));
        this.ondelete = ((long) in.readValue((long.class.getClassLoader())));
        this.check = ((long) in.readValue((long.class.getClassLoader())));
        this.fullName = ((String) in.readValue((String.class.getClassLoader())));
        this.avatar = ((String) in.readValue((String.class.getClassLoader())));
        this.title = ((String) in.readValue((String.class.getClassLoader())));
        this.fileName = ((String) in.readValue((String.class.getClassLoader())));
        this.uploaderId = ((long) in.readValue((long.class.getClassLoader())));
        this.borderColor = ((String) in.readValue((String.class.getClassLoader())));
        this.musicUrl = ((String) in.readValue((String.class.getClassLoader())));
        this.artist = ((String) in.readValue((String.class.getClassLoader())));
        this.type = ((String) in.readValue((String.class.getClassLoader())));
        this.groupID = ((long) in.readValue((long.class.getClassLoader())));
    }

    public MusicListItem() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUploadsId() {
        return uploadsId;
    }

    public void setUploadsId(long uploadsId) {
        this.uploadsId = uploadsId;
    }

    public long getReceiver() {
        return receiver;
    }

    public void setReceiver(long receiver) {
        this.receiver = receiver;
    }

    public long getFavorite() {
        return favorite;
    }

    public void setFavorite(long favorite) {
        this.favorite = favorite;
    }

    public long getFilter() {
        return filter;
    }

    public void setFilter(long filter) {
        this.filter = filter;
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

    public long getOndelete() {
        return ondelete;
    }

    public void setOndelete(long ondelete) {
        this.ondelete = ondelete;
    }

    public long getCheck() {
        return check;
    }

    public void setCheck(long check) {
        this.check = check;
    }

    public String getFullName() {
        return fullName;
    }

    public void setfullName(String firstName) {
        this.fullName = firstName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeValue(id);
        dest.writeValue(uploadsId);
        dest.writeValue(receiver);
        dest.writeValue(favorite);
        dest.writeValue(filter);
        dest.writeValue(createdAt);
        dest.writeValue(updatedAt);
        dest.writeValue(ondelete);
        dest.writeValue(check);
        dest.writeValue(fullName);
        dest.writeValue(avatar);
        dest.writeValue(title);
        dest.writeValue(fileName);
        dest.writeValue(uploaderId);
        dest.writeValue(borderColor);
        dest.writeValue(musicUrl);
        dest.writeValue(artist);
        dest.writeValue(type);
        dest.writeValue(groupID);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }
}
