package com.wuerth.osua;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;


public class DatabaseAdapter {
    databaseAdapter helper;
    MainActivity mainActivity;

    public DatabaseAdapter(Context context){
        helper = new databaseAdapter(context);
        this.mainActivity = (MainActivity) context;
    }

    static class databaseAdapter extends SQLiteOpenHelper {
        private Context context;

        private static final String DATABASE_NAME="OSUA";
        private static final int DATABASE_VERSION = 3;

        /** USERLIST STUFF **/

        private static final String USERLIST_TABLE_NAME = "userlist";
        private static final String USERLIST_ID = "userID";
        private static final String USERLIST_USER_NAME = "userName";
        private static final String USERLIST_USER_MAIL = "userMail";
        private static final String USERLIST_USER_PROJECT = "userProject";
        private static final String USERLIST_USER_ENABLED = "userEnabled";

        private static final String CREATE_TABLE_USERLIST = "CREATE TABLE "+ USERLIST_TABLE_NAME +" (" +
                USERLIST_ID +" VARCHAR(255) PRIMARY KEY," +   // AUTO_INCREMENT is added automatically
                USERLIST_USER_NAME +" VARCHAR(255)," +
                USERLIST_USER_MAIL +" VARCHAR(255)," +
                USERLIST_USER_PROJECT +" VARCHAR(255)," +
                USERLIST_USER_ENABLED +" BOOLEAN" +
                ")";

        private static final String DROP_TABLE_USERLIST = "DROP TABLE IF EXISTS "+ USERLIST_TABLE_NAME;

        /** PROJECT STUFF **/

        private static final String PROJECT_TABLE_NAME = "projects";
        private static final String PROJECT_ID = "projectID";
        private static final String PROJECT_NAME = "projectName";

        private static final String CREATE_TABLE_PROJECT = "CREATE TABLE "+ PROJECT_TABLE_NAME +" (" +
                PROJECT_ID +" VARCHAR(255) PRIMARY KEY," +   // AUTO_INCREMENT is added automatically
                PROJECT_NAME +" VARCHAR(255)" +
                ")";

        private static final String DROP_TABLE_PROJECT = "DROP TABLE IF EXISTS "+ PROJECT_TABLE_NAME;

        /* CONSTRUCTOR */

        public databaseAdapter(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
            //Toast.makeText(context, "Constructor was called", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE_USERLIST);
                db.execSQL(CREATE_TABLE_PROJECT);
            } catch (SQLException e){
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL(DROP_TABLE_USERLIST);
                db.execSQL(DROP_TABLE_PROJECT);
                onCreate(db);
                Toast.makeText(context, "onUpgrade was called", Toast.LENGTH_SHORT).show();
            }catch (SQLException e){
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*** METHODS ***/

    /** CATEGORY STUFF **/

    public long insertUser(String userID, String userName, String userMail, String userProject, Boolean userEnabled){
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(databaseAdapter.USERLIST_ID, userID);
        contentValues.put(databaseAdapter.USERLIST_USER_NAME, userName);
        if(!userMail.equals(""))
            contentValues.put(databaseAdapter.USERLIST_USER_MAIL, userMail);
        if(!userProject.equals(""))
            contentValues.put(databaseAdapter.USERLIST_USER_PROJECT, userProject);
        contentValues.put(databaseAdapter.USERLIST_USER_ENABLED, userEnabled);

        long rowID = db.insert(databaseAdapter.USERLIST_TABLE_NAME, null, contentValues);
        db.close();

        return rowID;
    }

    public ArrayList<userItem> getAllUsers(String searchQuery){
        SQLiteDatabase db = helper.getWritableDatabase();

        SharedPreferences myPrefs;
        myPrefs = mainActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String query =   databaseAdapter.USERLIST_TABLE_NAME+" a " +
                            "INNER JOIN "+databaseAdapter.PROJECT_TABLE_NAME+" b " +
                            "ON a."+ databaseAdapter.USERLIST_USER_PROJECT +" = b."+databaseAdapter.PROJECT_ID;

        String disabledProjects = myPrefs.getString("filterDisabledProjects", "");
        String[] disabledProjectsArray = disabledProjects.split(",");
        for(int index = 0; index < disabledProjectsArray.length; index++){
            disabledProjectsArray[index] = "'"+ disabledProjectsArray[index] +"'";
        }

        Cursor cursor = db.query(query, null, databaseAdapter.USERLIST_USER_PROJECT + " NOT IN("+TextUtils.join(",", disabledProjectsArray)+") AND "+databaseAdapter.USERLIST_USER_NAME+" LIKE '%"+searchQuery+"%'", null, null, null, databaseAdapter.USERLIST_USER_NAME+" COLLATE NOCASE");

        ArrayList<userItem> userList = new ArrayList<userItem>();

        while(cursor.moveToNext()){
            String userID = cursor.getString(cursor.getColumnIndex(databaseAdapter.USERLIST_ID));
            String userName = cursor.getString(cursor.getColumnIndex(databaseAdapter.USERLIST_USER_NAME));
            String userMail = cursor.getString(cursor.getColumnIndex(databaseAdapter.USERLIST_USER_MAIL));
            String userProject = cursor.getString(cursor.getColumnIndex(databaseAdapter.PROJECT_NAME));
            Boolean userEnabled = cursor.getInt(cursor.getColumnIndex(databaseAdapter.USERLIST_USER_ENABLED)) != 0;

            userList.add(new userItem(userID, userName, userMail, userProject, userEnabled));
        }

        cursor.close();
        db.close();

        return userList;
    }

    public Integer getProjectUserCount (String projectID){
        SQLiteDatabase db = helper.getWritableDatabase();

        String[] columns = {databaseAdapter.USERLIST_ID};
        String[] selectionArgs = {projectID};
        Cursor cursor = db.query(databaseAdapter.USERLIST_TABLE_NAME, columns, databaseAdapter.USERLIST_USER_PROJECT + "=?", selectionArgs, null, null, null);
        cursor.moveToNext();

        Integer size = cursor.getCount();

        cursor.close();
        db.close();

        return size;
    }
/*
    public JSONArray getAllCategoryMeta(){
        SQLiteDatabase db = helper.getWritableDatabase();

        String[] columns = {databaseAdapter.USERLIST_ID,
                            databaseAdapter.CATEGORY_LAST_UPDATE};
        Cursor cursor = db.query(databaseAdapter.USERLIST_TABLE_NAME, columns, null, null, null, null, null);

        JSONArray data = new JSONArray();
        while(cursor.moveToNext()){
                JSONObject item = new JSONObject();
                try {
                    item.put("id", cursor.getInt(cursor.getColumnIndex(databaseAdapter.USERLIST_ID)));
                    item.put("timestamp", cursor.getString(cursor.getColumnIndex(databaseAdapter.CATEGORY_LAST_UPDATE)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                data.put(item);
        }

        cursor.close();
        db.close();

        return data;
    }*/

    public void deleteUserList(){
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete(databaseAdapter.USERLIST_TABLE_NAME, null, null); // Clean Table
    }

    /** CATEGORY STUFF **/

    public long insertProject(String projectID, String projectName){
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(databaseAdapter.PROJECT_ID, projectID);
        contentValues.put(databaseAdapter.PROJECT_NAME, projectName);
        long rowID = db.insert(databaseAdapter.PROJECT_TABLE_NAME, null, contentValues);

        db.close();

        return rowID;
    }

    public ArrayList<projectItem> getAllProjects(MainActivity mainActivity){
        SQLiteDatabase db = helper.getWritableDatabase();

        SharedPreferences myPrefs;
        SharedPreferences.Editor spEditor;
        myPrefs = mainActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        spEditor = myPrefs.edit();

        String disabledProjects = myPrefs.getString("filterDisabledProjects", "");
        String[] disabledProjectsArray = disabledProjects.split(",");

        Cursor cursor = db.query(databaseAdapter.PROJECT_TABLE_NAME, null, null, null, null, null, null);

        ArrayList<projectItem> projectList = new ArrayList<projectItem>();

        while(cursor.moveToNext()){
            String projectID = cursor.getString(cursor.getColumnIndex(databaseAdapter.PROJECT_ID));
            String projectName = cursor.getString(cursor.getColumnIndex(databaseAdapter.PROJECT_NAME));
            Boolean projectEnabled = true;

            for (String disabledProject : disabledProjectsArray) {
                if (disabledProject.equals(projectID)) // Check if checked Item is under the disabled projects
                    projectEnabled = false;
            }

            projectList.add(new projectItem(projectID, projectName, projectEnabled));
        }

        cursor.close();
        db.close();

        return projectList;
    }

    public void deleteProjectList(){
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete(databaseAdapter.PROJECT_TABLE_NAME, null, null); // Clean Table
    }
}
