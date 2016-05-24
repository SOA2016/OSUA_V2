package com.wuerth.osua;

/**
 * Created by inf1783 on 20.12.15.
 */
public class userItem {
    String userID, userName, userMail, userProject;
    Boolean userEnabled = false;

    userItem(String userID, String userName, String userMail, String userProject, Boolean userEnabled){
        this.userID = userID;
        this.userName = userName;
        this.userMail = userMail;
        this.userProject = userProject;
        this.userEnabled = userEnabled;
    }
}
