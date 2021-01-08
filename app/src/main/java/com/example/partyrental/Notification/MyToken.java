package com.example.partyrental.Notification;

public class MyToken {
    private String token,user;
    private NotificationCommon.TOKEN_TYPE tokenType;

    public MyToken() {
    }

    public MyToken(String token, String user, NotificationCommon.TOKEN_TYPE tokenType) {
        this.token = token;
        this.user = user;
        this.tokenType = tokenType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public NotificationCommon.TOKEN_TYPE getTokenType() {
        return tokenType;
    }

    public void setTokenType(NotificationCommon.TOKEN_TYPE tokenType) {
        this.tokenType = tokenType;
    }
}
