package com.github.otbproject.otbproject.users;

/**
 * Created by justin on 28/02/2015.
 */
public class User {
    private String nick;
    private UserLevel userLevel;


    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public UserLevel getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(UserLevel userLevel) {
        this.userLevel = userLevel;
    }
}
