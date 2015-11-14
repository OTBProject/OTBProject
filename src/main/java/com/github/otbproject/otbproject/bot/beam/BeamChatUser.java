package com.github.otbproject.otbproject.bot.beam;

import pro.beam.api.resource.BeamUser;

import java.util.List;

class BeamChatUser {

    private int userId;
    private String userName;
    private List<BeamUser.Role> userRoles;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<BeamUser.Role> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<BeamUser.Role> userRoles) {
        this.userRoles = userRoles;
    }
}
