package com.wuerth.osua;

/**
 * Created by inf1783 on 20.12.15.
 */
public class projectItem {
    String projectID, projectName;
    Boolean projectEnabled;

    projectItem(String projectID, String projectName, Boolean projectEnabled){
        this.projectID = projectID;
        this.projectName = projectName;
        this.projectEnabled = projectEnabled;
    }
}