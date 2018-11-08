package com.hellodemo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by new user on 2/18/2018.
 */

public class NavGroupItem implements Parcelable
{

    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("creater")
    @Expose
    private long creater;
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private Object updatedAt;
    @SerializedName("group_note")
    @Expose
    private Object groupNote;
    @SerializedName("music_num")
    @Expose
    private long musicNum;
    @SerializedName("type")
    @Expose
    private String type;
    public final static Parcelable.Creator<NavGroupItem> CREATOR = new Creator<NavGroupItem>() {


        @SuppressWarnings({
                "unchecked"
        })
        public NavGroupItem createFromParcel(Parcel in) {
            return new NavGroupItem(in);
        }

        public NavGroupItem[] newArray(int size) {
            return (new NavGroupItem[size]);
        }

    }
            ;

    protected NavGroupItem(Parcel in) {
        this.id = ((long) in.readValue((long.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.creater = ((long) in.readValue((long.class.getClassLoader())));
        this.color = ((String) in.readValue((String.class.getClassLoader())));
        this.createdAt = ((String) in.readValue((String.class.getClassLoader())));
        this.updatedAt = ((Object) in.readValue((Object.class.getClassLoader())));
        this.groupNote = ((Object) in.readValue((Object.class.getClassLoader())));
        this.musicNum = ((long) in.readValue((long.class.getClassLoader())));
        this.type = ((String) in.readValue((String.class.getClassLoader())));
    }

    public NavGroupItem() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreater() {
        return creater;
    }

    public void setCreater(long creater) {
        this.creater = creater;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Object getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Object updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Object getGroupNote() {
        return groupNote;
    }

    public void setGroupNote(Object groupNote) {
        this.groupNote = groupNote;
    }

    public long getMusicNum() {
        return musicNum;
    }

    public void setMusicNum(long musicNum) {
        this.musicNum = musicNum;
    }


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(name);
        dest.writeValue(creater);
        dest.writeValue(color);
        dest.writeValue(createdAt);
        dest.writeValue(updatedAt);
        dest.writeValue(groupNote);
        dest.writeValue(musicNum);
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
