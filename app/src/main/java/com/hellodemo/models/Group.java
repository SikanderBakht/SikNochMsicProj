package com.hellodemo.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Group implements Parcelable {

    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("count")
    @Expose
    private long count;
    @SerializedName("already_sent")
    @Expose
    private boolean alreadySent;
    public final static Parcelable.Creator<Group> CREATOR = new Creator<Group>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        public Group[] newArray(int size) {
            return (new Group[size]);
        }

    };

    protected Group(Parcel in) {

        this.id = ((long) in.readValue((long.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.count = ((long) in.readValue((long.class.getClassLoader())));
        this.alreadySent = ((boolean) in.readValue((boolean.class.getClassLoader())));
    }

    public Group() {
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

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public boolean isAlreadySent() {
        return alreadySent;
    }

    public void setAlreadySent(boolean alreadySent) {
        this.alreadySent = alreadySent;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(name);
        dest.writeValue(count);
        dest.writeValue(alreadySent);
    }

    public int describeContents() {
        return 0;
    }

}