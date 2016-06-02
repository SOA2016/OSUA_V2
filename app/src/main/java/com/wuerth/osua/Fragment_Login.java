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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Context;

/**
 * A placeholder fragment containing a simple view.
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
    RESTClient myRESTClient;
    Context Context;

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
        myPrefs = mainActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        spEditor = myPrefs.edit();
        myRESTClient = new RESTClient_V3(mainActivity); // Here you should distinguish API V2.0 or V3.0

        // Load saved Key-Value-Pairs to Views

        final EditText loginServer = (EditText) view.findViewById(R.id.input_loginServer);
        loginServer.setText(myPrefs.getString("serverAddress", ""));

        final EditText loginName = (EditText) view.findViewById(R.id.input_loginName);
        loginName.setText(myPrefs.getString("loginName", ""));

        final EditText loginProject = (EditText) view.findViewById(R.id.input_loginProject);
        loginProject.setText(myPrefs.getString("loginProject", ""));

        final EditText loginDomain = (EditText) view.findViewById(R.id.input_loginDomain);
        loginProject.setText(myPrefs.getString("loginProject", ""));

        final EditText loginPassword = (EditText) view.findViewById(R.id.input_loginPassword);

        final Spinner prefixSpinner = (Spinner) view.findViewById(R.id.input_loginServerSpinner);
        prefixSpinner.setSelection(myPrefs.getInt("serverPrefix", 0));


        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        loginButton = (Button) view.findViewById(R.id.loginButton);
        loginButton.requestFocus();
        loginButton.requestFocusFromTouch();

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(loginServer.getText().toString().isEmpty()){
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterServerAddress));
                }else if(loginName.getText().toString().isEmpty()){
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterUserName));
                }else if(loginProject.getText().toString().isEmpty()){
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterUserProject));
                }else if(loginPassword.getText().toString().isEmpty()){
                    mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_enterPassword));
                }else {
                    // Save Input to as Key-Value-Pair

                    spEditor.putString("serverAddress", loginServer.getText().toString());
                    spEditor.putString("loginName", loginName.getText().toString());
                    spEditor.putString("loginProject", loginProject.getText().toString());
                    spEditor.putInt("serverPrefix", prefixSpinner.getSelectedItemPosition());
                    spEditor.putString("loginDomain", loginDomain.getText().toString());
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