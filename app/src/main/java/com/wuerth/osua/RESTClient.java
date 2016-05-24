package com.wuerth.osua;

import android.content.SharedPreferences;

/**
 * Created by Stephan Strissel on 24.05.2016.
 * Base functionality of RESTClient. All RESTClient-Versions have to extend from here
 */
public abstract class RESTClient {
    MainActivity mainActivity;
    SharedPreferences myPrefs;
    SharedPreferences.Editor spEditor;
    String[] prefixList;
    String[] snackbarNotifications;

    public abstract boolean getAuthentificationToken(String loginPassword);

    public abstract boolean deleteToken();

    public abstract boolean validateToken();

    public abstract boolean getUsers();

    public abstract boolean getProjects();

    public abstract boolean deleteUser(String userID);

    public abstract boolean postUser(String projectID, String userName, String userMail, String userPassword, Boolean userEnabled);

    public abstract boolean updateUser(String userID, String projectID, String userName, String userMail, String userPassword, Boolean userEnabled);

    public abstract boolean getUser(String ID);

}
