package com.hellodemo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by new user on 2/18/2018.
 */

public class NavMenuModel implements Parcelable {

    @SerializedName("demo_num")
    @Expose
    private long demoNum;
    @SerializedName("inbox_num")
    @Expose
    private long inboxNum;
    @SerializedName("inbox_unread_num")
    @Expose
    private long inboxUnreadNum;
    @SerializedName("upload_num")
    @Expose
    private long uploadNum;
    @SerializedName("favorite_num")
    @Expose
    private long favoriteNum;
    @SerializedName("group_list")
    @Expose
    private List<NavGroupItem> groupList = null;
    @SerializedName("messages_num")
    @Expose
    private long messagesNum;
    @SerializedName("notification_unread")
    @Expose
    private long unreadNotificationsNum;
    @SerializedName("unread_count_wo_msg")
    @Expose
    private long unreadNotificationsNumWOMsgs;

    private ArrayList<NavGroupItem> openGroups;
    private ArrayList<NavGroupItem> closeGroups;
    public final static Parcelable.Creator<NavMenuModel> CREATOR = new Creator<NavMenuModel>() {


        @SuppressWarnings({
                "unchecked"
        })
        public NavMenuModel createFromParcel(Parcel in) {
            return new NavMenuModel(in);
        }

        public NavMenuModel[] newArray(int size) {
            return (new NavMenuModel[size]);
        }

    };

    protected NavMenuModel(Parcel in) {
        this.demoNum = ((long) in.readValue((long.class.getClassLoader())));
        this.inboxNum = ((long) in.readValue((long.class.getClassLoader())));
        this.inboxUnreadNum = ((long) in.readValue((long.class.getClassLoader())));
        this.uploadNum = ((long) in.readValue((long.class.getClassLoader())));
        this.favoriteNum = ((long) in.readValue((long.class.getClassLoader())));
        in.readList(this.groupList, (NavGroupItem.class.getClassLoader()));
        this.messagesNum = ((long) in.readValue((long.class.getClassLoader())));
        this.unreadNotificationsNum = ((long) in.readValue((long.class.getClassLoader())));
        this.unreadNotificationsNumWOMsgs = ((long) in.readValue((long.class.getClassLoader())));
    }

    public NavMenuModel() {
    }

    public long getDemoNum() {
        return demoNum;
    }

    public void setDemoNum(long demoNum) {
        this.demoNum = demoNum;
    }

    public long getInboxNum() {
        return inboxNum;
    }

    public long getInboxUnreadNum() {
        return inboxUnreadNum;
    }

    public void setInboxNum(long inboxNum) {
        this.inboxNum = inboxNum;
    }

    public void setInboxUnreadNum(long inboxUnreadNum) {
        this.inboxUnreadNum = inboxUnreadNum;
    }

    public long getUploadNum() {
        return uploadNum;
    }

    public void setUploadNum(long uploadNum) {
        this.uploadNum = uploadNum;
    }

    public long getFavoriteNum() {
        return favoriteNum;
    }

    public void setFavoriteNum(long favoriteNum) {
        this.favoriteNum = favoriteNum;
    }

    public List<NavGroupItem> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<NavGroupItem> groupList) {
        this.groupList = groupList;
    }

    public long getMessagesNum() {
        return messagesNum;
    }

    public void setMessagesNum(long messagesNum) {
        this.messagesNum = messagesNum;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(demoNum);
        dest.writeValue(inboxNum);
        dest.writeValue(inboxUnreadNum);
        dest.writeValue(uploadNum);
        dest.writeValue(favoriteNum);
        dest.writeList(groupList);
        dest.writeValue(messagesNum);
        dest.writeValue(unreadNotificationsNum);
        dest.writeValue(unreadNotificationsNumWOMsgs);
    }

    public int describeContents() {
        return 0;
    }

    public void separteOpenAndCloseGroups() {
        openGroups = new ArrayList<NavGroupItem>();
        closeGroups = new ArrayList<NavGroupItem>();
        for (NavGroupItem gi : groupList) {
            if (gi.getType().equals("open"))
                openGroups.add(gi);
            else
                closeGroups.add(gi);

        }
    }


    public ArrayList<NavGroupItem> getOpenGroups() {
        return openGroups;
    }

    public void setOpenGroups(ArrayList<NavGroupItem> openGroups) {
        this.openGroups = openGroups;
    }

    public ArrayList<NavGroupItem> getCloseGroups() {
        return closeGroups;
    }

    public void setCloseGroups(ArrayList<NavGroupItem> closeGroups) {
        this.closeGroups = closeGroups;
    }

    public long getUnreadNotificationsNum() {
        return unreadNotificationsNum;
    }

    public void setUnreadNotificationsNum(long unreadNotificationsNum) {
        this.unreadNotificationsNum = unreadNotificationsNum;
    }

    public long getUnreadNotificationsNumWOMsgs() {
        return unreadNotificationsNumWOMsgs;
    }

    public void setUnreadNotificationsNumWOMsgs(long unreadNotificationsNumWOMsgs) {
        this.unreadNotificationsNumWOMsgs = unreadNotificationsNumWOMsgs;
    }
}