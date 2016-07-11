package com.hsworms.osua;

/**
 * Created by inf1783 on 20.12.15.
 */
public class userItem {
    String userID, userName, userMail, userProject, userDescription;
    Boolean userEnabled = false;

    userItem(String userID, String userName, String userMail, String userProject, Boolean userEnabled, String userDescription){
        this.userID = userID;
        this.userName = userName;
        this.userMail = userMail;
        this.userProject = userProject;
        this.userDescription = userDescription;
        this.userEnabled = userEnabled;
    }
}
