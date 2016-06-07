package com.wuerth.osua;

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



/**
 * changed by Stephan Strissel
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

                /* decide whether or not login is possible (scoped, unscoped, etc.)
                * unscoped login is not possible, because you have to provide the userID instead of the username
                * If you specify the user name, you must also specify the domain, by ID or name.
                * */
                boolean try_to_login = true;

                if(loginServer.getText().toString().isEmpty()) {
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterServerAddress));
                    try_to_login = false; /* abort login */
                }
                if(loginName.getText().toString().isEmpty()) {
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterUserName));
                    try_to_login = false; /* abort login */
                }
                if(loginPassword.getText().toString().isEmpty()) {
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterPassword));
                    try_to_login = false; /* abort login */
                }
                /* when a project is specified, a project-domain has to be specified, too */
                if(!loginProject.getText().toString().isEmpty() && loginProjectDomain.getText().toString().isEmpty()) {
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterProjectDomain));
                    try_to_login = false; /* abort login */
                }
                if(loginUserDomain.getText().toString().isEmpty()) {
                    //mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterUserDomain));
                    loginUserDomain.setText("default");
                    /*continue with default domain*/
                }


                /* if preconditions are true, try to login*/
                if (try_to_login)
                {
                    /* serverAddress Prefix Autocorrect */
                    loginServer.getText().toString();
                    boolean serverAddressCorrect = false;
                    for(String prefix : getResources().getStringArray((R.array.serverPrefixes))) {
                        if (loginServer.getText().toString().substring(0,prefix.length()).equals(prefix)){
                            serverAddressCorrect = true;
                            break;
                        }
                    }
                    if (!serverAddressCorrect){
                        loginServer.setText("https://" + loginServer.getText().toString());
                    }

                    // Save Input to as Key-Value-Pair
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

                    new myWorker().execute(loginPassword.getText().toString());
                }
            }
        });

        return view;
    }

    public class myWorker extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                return myRESTClient.getAuthentificationToken(params[0]);
            }
            catch(Exception e){
                Log.e("Asynctask", e.toString());
                mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if(success) {
                //Toast.makeText(mainActivity, "Expires at: " + myPrefs.getString("actualTokenExpiresAt", "No Token!"), Toast.LENGTH_LONG).show();
                //Toast.makeText(mainActivity, "Token: " + myPrefs.getString("actualToken", "No Token!"), Toast.LENGTH_LONG).show();
                mainActivity.changeFragment(mainActivity.TAG_USERLIST, mainActivity);
            }else{
                loginButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }

            super.onPostExecute(success);
        }

    }
}