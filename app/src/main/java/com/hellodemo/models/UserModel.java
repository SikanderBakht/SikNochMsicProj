package com.hellodemo.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by new user on 2/13/2018.
 */

public class UserModel implements Parcelable
{

    @SerializedName("id")
    @Expose
    private long id;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("full_name")
    @Expose
    private String full_name;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("sms_token")
    @Expose
    private String smsToken;
    @SerializedName("email_token")
    @Expose
    private String emailToken;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("remember_token")
    @Expose
    private String rememberToken;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("qrcode")
    @Expose
    private String qrcode;
    @SerializedName("subscription")
    @Expose
    private String subscription;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("ios_token")
    @Expose
    private String iosToken;
    @SerializedName("second_email")
    @Expose
    private String secondEmail;
    @SerializedName("qr_code")
    @Expose
    private String qrCode;
    @SerializedName("access_token")
    @Expose
    private String accessToken;
    @SerializedName("location")
    @Expose
    private  String location;
    @SerializedName("full_name_null")
    @Expose
    private  int full_name_null;

    public final static Parcelable.Creator<UserModel> CREATOR = new Creator<UserModel>() {


        @SuppressWarnings({
                "unchecked"
        })
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        public UserModel[] newArray(int size) {
            return (new UserModel[size]);
        }

    }
            ;

    protected UserModel(Parcel in) {
        this.id = ((long) in.readValue((long.class.getClassLoader())));
        this.email = ((String) in.readValue((String.class.getClassLoader())));
        this.phone = ((String) in.readValue((String.class.getClassLoader())));
        this.username = ((String) in.readValue((String.class.getClassLoader())));
        this.full_name=((String) in.readValue(String.class.getClassLoader()));
//        this.firstName = ((String) in.readValue((String.class.getClassLoader())));
//        this.lastName = ((String) in.readValue((String.class.getClassLoader())));
        this.password = ((String) in.readValue((String.class.getClassLoader())));
        this.smsToken = ((String) in.readValue((String.class.getClassLoader())));
        this.emailToken = ((String) in.readValue((String.class.getClassLoader())));
        this.updatedAt = ((String) in.readValue((String.class.getClassLoader())));
        this.createdAt = ((String) in.readValue((String.class.getClassLoader())));
        this.rememberToken = ((String) in.readValue((String.class.getClassLoader())));
        this.type = ((String) in.readValue((String.class.getClassLoader())));
        this.qrcode = ((String) in.readValue((String.class.getClassLoader())));
        this.subscription = ((String) in.readValue((String.class.getClassLoader())));
        this.avatar = ((String) in.readValue((String.class.getClassLoader())));
        this.iosToken = ((String) in.readValue((String.class.getClassLoader())));
        this.secondEmail = ((String) in.readValue((String.class.getClassLoader())));
        this.qrCode = ((String) in.readValue((String.class.getClassLoader())));
        this.accessToken = ((String) in.readValue((String.class.getClassLoader())));
        this.full_name_null = ((int) in.readValue((int.class.getClassLoader())));
    }

    public UserModel() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return full_name;
    }

    public void setFullName(String full_name) {
        this.full_name = full_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSmsToken() {
        return smsToken;
    }

    public void setSmsToken(String smsToken) {
        this.smsToken = smsToken;
    }

    public String getEmailToken() {
        return emailToken;
    }

    public void setEmailToken(String emailToken) {
        this.emailToken = emailToken;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getRememberToken() {
        return rememberToken;
    }

    public void setRememberToken(String rememberToken) {
        this.rememberToken = rememberToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
    }

    public  String getLocation() {
        return location;
    }

    public void setLocation(String loc) {
        this.location = loc;
    }

    public  String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getIosToken() {
        return iosToken;
    }

    public void setIosToken(String iosToken) {
        this.iosToken = iosToken;
    }

    public String getSecondEmail() {
        return secondEmail;
    }

    public void setSecondEmail(String secondEmail) {
        this.secondEmail = secondEmail;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public int getFull_name_null() {
        return full_name_null;
    }

    public void setFull_name_null(int full_name_null) {
        this.full_name_null = full_name_null;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(email);
        dest.writeValue(phone);
        dest.writeValue(username);
        dest.writeValue(full_name);
//        dest.writeValue(firstName);
//        dest.writeValue(lastName);
        dest.writeValue(password);
        dest.writeValue(smsToken);
        dest.writeValue(emailToken);
        dest.writeValue(updatedAt);
        dest.writeValue(createdAt);
        dest.writeValue(rememberToken);
        dest.writeValue(type);
        dest.writeValue(qrcode);
        dest.writeValue(subscription);
        dest.writeValue(avatar);
        dest.writeValue(iosToken);
        dest.writeValue(secondEmail);
        dest.writeValue(qrCode);
        dest.writeValue(accessToken);
        dest.writeValue(full_name_null);
    }

    public int describeContents() {
        return 0;
    }

}
