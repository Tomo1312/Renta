package com.example.partyrental.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    String name, surname, mobile, email, userId, username, ownHouseId;
    boolean banned;
    public User() {
    }

    public User(String name, String surname, String mobile, String email, String userId, String username, boolean banned) {
        this.name = name;
        this.surname = surname;
        this.mobile = mobile;
        this.email = email;
        this.userId = userId;
        this.username = username;
        this.banned = banned;
    }

    public User(String name, String surname, String username, String mobile, String email) {
        this.name = name;
        this.surname = surname;
        this.mobile = mobile;
        this.email = email;
        this.username = username;
        this.banned = false;
        this.ownHouseId="";

    }

    protected User(Parcel in) {
        name = in.readString();
        surname = in.readString();
        mobile = in.readString();
        email = in.readString();
        userId = in.readString();
        username = in.readString();
        ownHouseId = in.readString();
        banned = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getOwnHouseId() {
        return ownHouseId;
    }

    public void setOwnHouseId(String ownHouseId) {
        this.ownHouseId = ownHouseId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(mobile);
        dest.writeString(email);
        dest.writeString(userId);
        dest.writeString(username);
        dest.writeString(ownHouseId);
        dest.writeByte((byte) (banned ? 1 : 0));
    }
}
