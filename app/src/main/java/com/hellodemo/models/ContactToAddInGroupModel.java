package com.hellodemo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ContactToAddInGroupModel implements Parcelable {


    @SerializedName("id")
    @Expose
    private long id;

    @SerializedName("avatar")
    @Expose
    private String avatar;

    @SerializedName("full_name")
    @Expose
    private String fullName;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("is_in_group")
    @Expose
    private boolean isInGroup;


    public ContactToAddInGroupModel(Parcel in) {
        id = in.readLong();
        avatar = in.readString();
        fullName = in.readString();
        isInGroup = in.readByte() != 0;
    }

    public static final Creator<ContactToAddInGroupModel> CREATOR = new Creator<ContactToAddInGroupModel>() {
        @Override
        public ContactToAddInGroupModel createFromParcel(Parcel in) {
            return new ContactToAddInGroupModel(in);
        }

        @Override
        public ContactToAddInGroupModel[] newArray(int size) {
            return new ContactToAddInGroupModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(avatar);
        dest.writeString(fullName);
        dest.writeString(type);
        dest.writeByte((byte) (isInGroup ? 1 : 0));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isInGroup() {
        return isInGroup;
    }

    public void setInGroup(boolean inGroup) {
        isInGroup = inGroup;
    }

    public static Creator<ContactToAddInGroupModel> getCREATOR() {
        return CREATOR;
    }
}
