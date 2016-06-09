package com.wuerth.osua;

import android.widget.Button;
import android.widget.ProgressBar;

/**
 * Created by Stephan Strissel on 09.06.2016.
 * returnParam-Structure for mainActivity Background Task "tokenValidationTask"
 * passes boolean success/fail, loginButton and progressBar to Async Task
 */
public class returnParam2 {
    Boolean success;
    Button loginButton;
    ProgressBar progessBar;

    returnParam2(Boolean success, Button loginButton, ProgressBar progressBar) {
        this.success = success;
        this.loginButton = loginButton;
        this.progessBar = progressBar;
    }
}
