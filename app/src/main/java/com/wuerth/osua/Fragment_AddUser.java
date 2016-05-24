package com.wuerth.osua;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class Fragment_AddUser extends Fragment {

    MainActivity mainActivity;
    ArrayList<projectItem> projectList;
    Spinner spinner;
    LinearLayout content;
    ProgressBar progressBar;
    EditText inputUserName, inputUserMail, inputUserPassword;
    SwitchCompat inputUserEnabled;

    public static Fragment_AddUser newInstance(){
        Fragment_AddUser fragment_editUser = new Fragment_AddUser();

        /*Bundle args = new Bundle();
        args.putString("userID", userID);
        fragment_editUser.setArguments(args);*/

        return fragment_editUser;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //userID = getArguments().getString("userID");

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_user, container, false);

        setHasOptionsMenu(true);

        mainActivity = (MainActivity) getActivity();

        spinner = (Spinner) view.findViewById(R.id.input_userProject);
        content = (LinearLayout) view.findViewById(R.id.content);
        inputUserEnabled = (SwitchCompat) view.findViewById(R.id.userEnabled);

        inputUserEnabled.setChecked(true);

        inputUserName = (EditText) view.findViewById(R.id.input_userName);
        inputUserMail = (EditText) view.findViewById(R.id.input_userMail);
        inputUserPassword = (EditText) view.findViewById(R.id.input_userPassword);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        progressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN);

        new myWorker().execute();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id== R.id.action_confirm) {
            if(inputUserName.getText().toString().isEmpty()){
                mainActivity.showSnackbar("Please enter a user-name");
            }else if(inputUserPassword.getText().toString().isEmpty()){
                mainActivity.showSnackbar("Please enter a password");
            }else {
                String userName, userMail, userPassword;
                Boolean userEnabled;

                userName = inputUserName.getText().toString();
                userMail = inputUserMail.getText().toString();
                userPassword = inputUserPassword.getText().toString();
                userEnabled = inputUserEnabled.isChecked();
                //Toast.makeText(mainActivity, ""+userEnabled, Toast.LENGTH_LONG).show();
                new addUserAsynctask(projectList.get(spinner.getSelectedItemPosition()).projectID, userName, userMail, userPassword, userEnabled).execute();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public class myWorker extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                mainActivity.myRESTClient.getProjects();

                return true;
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
                projectList = mainActivity.databaseAdapter.getAllProjects(mainActivity);
                ArrayList<String> projectNames = new ArrayList<>();
                for(int index = 0; index < projectList.size(); index++){
                    projectNames.add(projectList.get(index).projectName);
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_dropdown_item, projectNames);
                spinner.setAdapter(spinnerAdapter);

                progressBar.setVisibility(View.INVISIBLE);
                content.setVisibility(View.VISIBLE);

            }else{
                //loginButton.setVisibility(View.VISIBLE);
                //progressBar.setVisibility(View.INVISIBLE);
            }

            super.onPostExecute(success);
        }

    }

    public class addUserAsynctask extends AsyncTask<String, Void, Boolean> {
        String userName, userMail, userPassword, projectID;
        Boolean userEnabled;

        public addUserAsynctask(String projectID, String userName, String userMail, String userPassword, Boolean userEnabled){
            this.userName = userName;
            this.userMail = userMail;
            this.userPassword = userPassword;
            this.userEnabled = userEnabled;
            this.projectID = projectID;
            //Toast.makeText(mainActivity, "Created"+userName+ userMail+ userPassword+ userEnabled, Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                //myRESTClient.postUser();
                //Toast.makeText(mainActivity, "Called", Toast.LENGTH_LONG).show();
                return mainActivity.myRESTClient.postUser(projectID, userName, userMail, userPassword, userEnabled);
            }
            catch(Exception e){
                Log.e("Asynctask", e.toString());
                mainActivity.showSnackbar("Unexpected Error");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            //Toast.makeText(mainActivity, "Called"+success, Toast.LENGTH_LONG).show();
            if(success) {
                mainActivity.showSnackbar("User created successfully");
                mainActivity.changeFragment(mainActivity.TAG_USERLIST);
            }else{
                mainActivity.showSnackbar("User couldn't be created");
            }

            super.onPostExecute(success);
        }

    }
}