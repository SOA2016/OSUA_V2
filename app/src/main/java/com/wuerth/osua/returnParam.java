package com.wuerth.osua;

/**
 * Created by Stephan Strissel on 02.06.2016.
 * returnParam-Structure for mainActivity Background Task "deleteAsyncToken"
 * passes boolean success/fail and mainActivity-Instance to Async Task
 */
public class returnParam {
    Boolean success;
    MainActivity mainActivity;

    returnParam(Boolean success, MainActivity ac) {
        this.success = success;
        this.mainActivity = ac;
    }
}
