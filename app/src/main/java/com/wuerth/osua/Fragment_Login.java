package com.wuerth.osua;

import android.content.Context;
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

        // Load saved Key-Value-Pairs to Views
        final EditText loginServer = (EditText) view.findViewById(R.id.input_loginServer);
        loginServer.setText(myPrefs.getString("serverAddress", ""));

        final EditText loginName = (EditText) view.findViewById(R.id.input_loginName);
        loginName.setText(myPrefs.getString("loginName", ""));

        final EditText loginProject = (EditText) view.findViewById(R.id.input_loginProject);
        loginProject.setText(myPrefs.getString("loginProject", ""));

        final EditText loginPassword = (EditText) view.findViewById(R.id.input_loginPassword);

        final Spinner prefixSpinner = (Spinner) view.findViewById(R.id.input_loginServerSpinner);
        prefixSpinner.setSelection(myPrefs.getInt("serverPrefix", 0));

        final Spinner versionSpinner = (Spinner) view.findViewById(R.id.input_restfullApiVersionSpinner);
        versionSpinner.setSelection(myPrefs.getInt("restfullApiVersion", 1));

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        loginButton = (Button) view.findViewById(R.id.loginButton);
        loginButton.requestFocus();
        loginButton.requestFocusFromTouch();

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(loginServer.getText().toString().isEmpty()){
                    mainActivity.showSnackbar("Please enter your server-address");
                }else if(loginName.getText().toString().isEmpty()){
                    mainActivity.showSnackbar("Please enter your user-name");
                }else if(loginProject.getText().toString().isEmpty()){
                    mainActivity.showSnackbar("Please enter your user-project");
                }else if(loginPassword.getText().toString().isEmpty()){
                    mainActivity.showSnackbar("Please enter your password");
                }else {

                    /**
                     * Created by Stephan Strissel on 24.05.2016.
                     * Delete Database Cache / Refresh DatabaseAdapter
                     */
                    mainActivity.databaseAdapter.deleteUserList();
                    mainActivity.databaseAdapter.deleteProjectList();
                    /**
                     * Created by Stephan Strissel on 24.05.2016.
                     * Distinguish between API V2.0 or V3.0
                     * No Switch-Case possible, because case needs 'constant expression'
                     */
                    if (versionSpinner.getSelectedItem().toString().equals(getResources().getStringArray(R.array.restfullVersion)[1].toString())) { /* API V3.0 */
                    /* API V3.0 */
                        mainActivity.myRESTClient = new RESTClient_V3(mainActivity);
                        mainActivity.showSnackbar(getResources().getStringArray(R.array.snackbarNotifications)[3]);
                    } else if (versionSpinner.getSelectedItem().toString().equals(getResources().getStringArray(R.array.restfullVersion)[0].toString())) { /* API V2.0 */
                    /* API V2.0 */
                        mainActivity.myRESTClient = new RESTClient_V2(mainActivity);
                        mainActivity.showSnackbar(getResources().getStringArray(R.array.snackbarNotifications)[2]);
                    } else {
                    /* default case API V2.0*/
                       mainActivity.myRESTClient = new RESTClient_V2(mainActivity);
                       mainActivity.showSnackbar(getResources().getStringArray(R.array.snackbarNotifications)[0]);
                    }


                    // Save Input to as Key-Value-Pair
                    spEditor.putString("serverAddress", loginServer.getText().toString());
                    spEditor.putString("loginName", loginName.getText().toString());
                    spEditor.putString("loginProject", loginProject.getText().toString());
                    spEditor.putInt("serverPrefix", prefixSpinner.getSelectedItemPosition());
                    spEditor.putInt("restfullApiVersion", versionSpinner.getSelectedItemPosition());
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
                return mainActivity.myRESTClient.getAuthentificationToken(params[0]);
            }
            catch(Exception e){
                Log.e("Asynctask", e.toString());
                mainActivity.showSnackbar("Unexpected Error");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if(success) {
                //Toast.makeText(mainActivity, "Expires at: " + myPrefs.getString("actualTokenExpiresAt", "No Token!"), Toast.LENGTH_LONG).show();
                //Toast.makeText(mainActivity, "Token: " + myPrefs.getString("actualToken", "No Token!"), Toast.LENGTH_LONG).show();
                mainActivity.changeFragment(mainActivity.TAG_USERLIST);
            }else{
                loginButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }

            super.onPostExecute(success);
        }

    }
}