package com.wuerth.osua;

/**
 * Created by Stephan Strissel on 02.06.2016.
 * returnParam-Structure for mainActivity Background Task "deleteAsyncToken"
 */
public class returnParam {
    Boolean success;
    MainActivity mainActivity;

    returnParam(Boolean success, MainActivity ac) {
        this.success = success;
        this.mainActivity = ac;
    }
}
