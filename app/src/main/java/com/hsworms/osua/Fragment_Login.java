package com.hsworms.osua;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.support.v7.widget.CardView;
import android.widget.Spinner;

import android.widget.ArrayAdapter;


/**
 /* changed by Stephan Strissel, Marco Spiess, Damir Gricic
 * Manages Login and is first screen when App is opened
 * 1) auto prefix for Server-Address
 * 2) unscoped login
 * 3) scoped login
 * 4) default settings
 * 5) login precondition-check
 */
public class Fragment_Login extends Fragment {
    private ListView listView;
    MainActivity mainActivity;
    SharedPreferences myPrefs;
    SharedPreferences.Editor spEditor;
    FragmentManager manager;
    String[] benutzer;
    ProgressBar progressBar;
    Button loginButton;
    CheckBox advancedCheckbox;
    RESTClient myRESTClient;

    public Fragment_Login() {
    }


    public static Fragment_Login newInstance(){
        return new Fragment_Login();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mainActivity = (MainActivity) getActivity();
        mainActivity.initToolbar();

        manager = getActivity().getSupportFragmentManager();
        myPrefs = mainActivity.getSharedPreferences("MyPrefs", MainActivity.MODE_PRIVATE);
        spEditor = myPrefs.edit();
        myRESTClient = new RESTClient_V3(mainActivity); // Here you should distinguish API V2.0 or V3.0

        // Load saved Key-Value-Pairs to Views
        final EditText loginServer = (EditText) view.findViewById(R.id.input_loginServer);
        loginServer.setText(myPrefs.getString("serverAddress", ""));

        final EditText loginName = (EditText) view.findViewById(R.id.input_loginName);
        loginName.setText(myPrefs.getString("loginName", ""));

        final EditText loginProject = (EditText) view.findViewById(R.id.input_loginProject);
        loginProject.setText(myPrefs.getString("loginProject", ""));

        final EditText loginProjectDomain = (EditText) view.findViewById(R.id.input_loginProjectDomain);
        loginProjectDomain.setText(myPrefs.getString("loginProjectDomain", ""));

        final EditText loginUserDomain = (EditText) view.findViewById(R.id.input_loginUserDomain);
        loginUserDomain.setText(myPrefs.getString("loginUserDomain", ""));

        final EditText loginPassword = (EditText) view.findViewById(R.id.input_loginPassword);

        final Spinner loginserverPrefix= (Spinner) view.findViewById(R.id.input_loginServerPrefix);
        loginserverPrefix.setSelection(getIndex(loginserverPrefix, myPrefs.getString("serverPrefix", "")));

        /* Hide and Show Advanced Settings */
        advancedCheckbox = (CheckBox) view.findViewById(R.id.checkboxAdvancedSettings);
        final CardView advancedSettings = (CardView) view.findViewById(R.id.advanced_settings);
        advancedCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (advancedSettings.getVisibility() == View.GONE){
                    advancedSettings.setVisibility(View.VISIBLE);
                } else {
                    advancedSettings.setVisibility(View.GONE);
                }
            }
        });

        /* control Login Button */
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        loginButton = (Button) view.findViewById(R.id.loginButton);
        loginButton.requestFocus();
        loginButton.requestFocusFromTouch();
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mainActivity.hideToolbar();

                /* decide whether or not login is possible (scoped, unscoped, etc.)
                * unscoped login is not possible, because you have to provide the userID instead of the username
                * If you specify the user name, you must also specify the domain, by ID or name.
                * */
                boolean try_to_login = true;

                if (loginServer.getText().toString().isEmpty()) {
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterServerAddress));
                    try_to_login = false; /* abort login */
                }
                if (loginName.getText().toString().isEmpty()) {
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterUserName));
                    try_to_login = false; /* abort login */
                }
                if (loginPassword.getText().toString().isEmpty()) {
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterPassword));
                    try_to_login = false; /* abort login */
                }
                /* when a project is specified, a project-domain has to be specified, too */
                if (!loginProject.getText().toString().isEmpty() && loginProjectDomain.getText().toString().isEmpty()) {
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterProjectDomain));
                    try_to_login = false; /* abort login */
                }
                if (loginUserDomain.getText().toString().isEmpty()) {
                    //mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterUserDomain));
                    loginUserDomain.setText("default");
                    /*continue with default domain*/
                }


                /* if preconditions are true, try to login*/
                if (try_to_login) {
                    // Save Input to as Key-Value-Pair
                    spEditor.putString("serverPrefix", loginserverPrefix.getSelectedItem().toString());
                    spEditor.putString("serverAddress", loginServer.getText().toString());
                    spEditor.putString("loginName", loginName.getText().toString());
                    spEditor.putString("loginProject", loginProject.getText().toString());
                    spEditor.putString("loginProjectDomain", loginProjectDomain.getText().toString());
                    spEditor.putString("loginUserDomain", loginUserDomain.getText().toString());
                    spEditor.apply();

                    loginButton.setVisibility(View.INVISIBLE);
                    progressBar.getIndeterminateDrawable().setColorFilter(
                            getResources().getColor(R.color.colorPrimary),
                            android.graphics.PorterDuff.Mode.SRC_IN);
                    progressBar.setVisibility(View.VISIBLE);


                    mainActivity.hideToolbar();
                    new loginTask().execute(loginPassword.getText().toString());
                } else {
                    mainActivity.showToolbar();
                }
            }
        });

        /*
         * Created by Stephan Strissel on 09.06.2016.
         * try to Login in Background if token is still available
         */
        if (!myPrefs.getString("actualToken", "").equals("") && !myPrefs.getString("serverAddress", "").equals("")) {
            // lock Login-Screen to execute tokenValidation in Background
            mainActivity.hideToolbar();
            loginButton.setVisibility((View.GONE));
            progressBar.setVisibility(View.VISIBLE);

            // Background-Task
            returnParam2 param = new returnParam2(false, loginButton, progressBar);
            new tokenValidationTask().execute(param);
            // Notice: Login-Screen will be unlocked when Background-Task is finished
        } else {
            mainActivity.showToolbar();
        }

        return view;
    }

    //private method of your class
    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }

    public class loginTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                return myRESTClient.getAuthentificationToken(params[0]);
            }
            catch(Exception e){
                Log.e("loginTask", e.toString());
                mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if(success) {
                mainActivity.changeFragment(mainActivity.TAG_USERLIST, mainActivity);
            }else{
                mainActivity.showToolbar();
                loginButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }

            super.onPostExecute(success);
        }

    }

    /*
    * Created by Stephan Strissel, Marco Spiess, Damir Gricic on 09.06.2016.
    * trys to login in Background-Task if token is still available
     */
    public class tokenValidationTask extends AsyncTask<returnParam2, Void,  returnParam2> {

        @Override
        protected returnParam2 doInBackground(returnParam2... params) {

            try{
                params[0] = new returnParam2(myRESTClient.validateToken(), loginButton, progressBar);
            }
            catch(Exception e){
                Log.e("tokenValidationTask", e.toString());
                mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                params[0] = new returnParam2(false, loginButton, progressBar);;
            }
            return params[0];
        }

        @Override
        protected void onPostExecute(returnParam2 param) {

            if(param.success) {
                mainActivity.changeFragment(mainActivity.TAG_USERLIST, mainActivity); // token is still valid
            }else{
                param.loginButton.setVisibility(View.VISIBLE);
                param.progessBar.setVisibility(View.GONE);
            }
        }

    }
}