package com.hellodemo.models;

public class SearchedUser {

    private long id;
    private String fullName;
    private String type;
    private String avatar;
    private boolean isFollowing = false;    // means the searched user is following the app user...
    private boolean isFollowed = false;     // means the app user is following the searched user...
    private boolean isFriend = false;

    public SearchedUser(long id,
                        String fullName,
                        String type,
                        String avatar,
                        boolean isFollowing,
                        boolean isFollowed,
                        boolean isFriend) {
        this.id = id;
        this.fullName = fullName;
        this.type = type;
        this.avatar = avatar;
        this.isFollowing = isFollowing;
        this.isFollowed = isFollowed;
        this.isFriend = isFriend;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public boolean isFollowed() {
        return isFollowed;
    }

    public void setFollowed(boolean followed) {
        isFollowed = followed;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }
}
