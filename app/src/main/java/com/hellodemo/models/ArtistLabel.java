package com.hellodemo.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ArtistLabel implements Parcelable
{

    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("following")
    @Expose
    private boolean following;
    @SerializedName("friend")
    @Expose
    private boolean friend;
    @SerializedName("already_sent")
    @Expose
    private boolean alreadySent;
    public final static Parcelable.Creator<ArtistLabel> CREATOR = new Creator<ArtistLabel>() {


        @SuppressWarnings({
                "unchecked"
        })
        public ArtistLabel createFromParcel(Parcel in) {
            return new ArtistLabel(in);
        }

        public ArtistLabel[] newArray(int size) {
            return (new ArtistLabel[size]);
        }

    };

    protected ArtistLabel(Parcel in) {
        this.id = ((long) in.readValue((long.class.getClassLoader())));
        this.fullName = ((String) in.readValue((String.class.getClassLoader())));
        this.avatar = ((String) in.readValue((String.class.getClassLoader())));
        this.following = ((boolean) in.readValue((boolean.class.getClassLoader())));
        this.friend = ((boolean) in.readValue((boolean.class.getClassLoader())));
        this.alreadySent = ((boolean) in.readValue((boolean.class.getClassLoader())));
    }

    public ArtistLabel() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public boolean isAlreadySent() {
        return alreadySent;
    }

    public void setAlreadySent(boolean alreadySent) {
        this.alreadySent = alreadySent;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(fullName);
        dest.writeValue(avatar);
        dest.writeValue(following);
        dest.writeValue(friend);
        dest.writeValue(alreadySent);
    }

    public int describeContents() {
        return 0;
    }

}