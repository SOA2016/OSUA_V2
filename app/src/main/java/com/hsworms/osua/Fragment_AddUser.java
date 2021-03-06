package com.hsworms.osua;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import java.util.ArrayList;

public class Fragment_AddUser extends Fragment {

    MainActivity mainActivity;
    RESTClient myRESTClient;
    ArrayList<projectItem> projectList;
    Spinner spinner;
    LinearLayout content;
    ProgressBar progressBar;
    EditText inputUserName, inputUserMail, inputUserPassword, inputUserDescription;
    SwitchCompat inputUserEnabled;

    public static Fragment_AddUser newInstance(){
        Fragment_AddUser fragment_editUser = new Fragment_AddUser();

        return fragment_editUser;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();
        mainActivity.initToolbar();

        View view = inflater.inflate(R.layout.fragment_add_user, container, false);

        setHasOptionsMenu(true);


        myRESTClient = new RESTClient_V3(mainActivity); // Here you should distinguish API V2.0 or V3.0

        spinner = (Spinner) view.findViewById(R.id.input_userProject);
        content = (LinearLayout) view.findViewById(R.id.content);
        inputUserEnabled = (SwitchCompat) view.findViewById(R.id.userEnabled);

        inputUserEnabled.setChecked(true);

        inputUserName = (EditText) view.findViewById(R.id.input_userName);
        inputUserMail = (EditText) view.findViewById(R.id.input_userMail);
        inputUserPassword = (EditText) view.findViewById(R.id.input_userPassword);
        inputUserDescription = (EditText) view.findViewById(R.id.input_userDescription);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        progressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN);

        mainActivity.hideToolbar();
        new loadProjectsAsyncTask().execute();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id== R.id.action_confirm) {
            if(inputUserName.getText().toString().isEmpty()){
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_enterUserName));
            }else if(inputUserPassword.getText().toString().isEmpty()){
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_enterPassword));
            }else {
                String userName, userMail, userPassword, userDescription;
                Boolean userEnabled;

                userName = inputUserName.getText().toString();
                userMail = inputUserMail.getText().toString();
                userDescription = inputUserDescription.getText().toString();
                userPassword = inputUserPassword.getText().toString();
                userEnabled = inputUserEnabled.isChecked();
                //Toast.makeText(mainActivity, ""+userEnabled, Toast.LENGTH_LONG).show();




                /* created by Stephan Strissel, Marco Spiess, Damir Gricic
                * wait until updateUserAsynctask is finished
                 */
                ((MainActivity)getActivity()).hideToolbar();
                progressBar.setVisibility(View.VISIBLE);
                content.setVisibility(View.GONE);
                 new addUserAsynctask(projectList.get(spinner.getSelectedItemPosition()).projectID, userName, userMail, userPassword, userDescription, userEnabled).execute();

            }
        }

        return super.onOptionsItemSelected(item);
    }

    public class loadProjectsAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                //myRESTClient.getProjects();

                return true;
            }
            catch(Exception e){
                Log.e("Asynctask", e.toString());
                mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mainActivity.showToolbar();
            if(success) {
                projectList = mainActivity.databaseAdapter.getAllProjects(mainActivity);
                ArrayList<String> projectNames = new ArrayList<>();
                for(int index = 0; index < projectList.size(); index++){
                    projectNames.add(projectList.get(index).projectName);
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_dropdown_item, projectNames);
                spinner.setAdapter(spinnerAdapter);

                progressBar.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);

            }else{
                //loginButton.setVisibility(View.VISIBLE);
                //progressBar.setVisibility(View.INVISIBLE);
            }

            super.onPostExecute(success);
        }

    }

    public class addUserAsynctask extends AsyncTask<String, Void, Boolean> {
        String userName, userMail, userPassword, projectID, userDescription;
        Boolean userEnabled;

        public addUserAsynctask(String projectID, String userName, String userMail, String userPassword, String userDescription, Boolean userEnabled){
            this.userName = userName;
            this.userMail = userMail;
            this.userPassword = userPassword;
            this.userEnabled = userEnabled;
            this.projectID = projectID;
            this.userDescription = userDescription;
            //Toast.makeText(mainActivity, "Created"+userName+ userMail+ userPassword+ userEnabled, Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                //myRESTClient.postUser();
                //Toast.makeText(mainActivity, "Called", Toast.LENGTH_LONG).show();
                return myRESTClient.postUser(projectID, userName, userMail, userPassword, userDescription, userEnabled);
            }
            catch(Exception e){
                Log.e("Asynctask", e.toString());
                mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            MainActivity mainActivity = (MainActivity) getActivity();
            progressBar.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
            mainActivity.showToolbar();
            if(success) {
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_creationSuccess));
                mainActivity.changeFragment(mainActivity.TAG_USERLIST, mainActivity);
            }else{
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_creationFail));
            }

            super.onPostExecute(success);
        }

    }
}