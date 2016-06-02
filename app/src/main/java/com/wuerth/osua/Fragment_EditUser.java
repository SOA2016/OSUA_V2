package com.wuerth.osua;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Fragment_EditUser extends Fragment {

    MainActivity mainActivity;
    RESTClient myRESTClient;
    //EditText userName, userMail;
    SwitchCompat inputUserEnabled;
    String userID;
    ArrayList<projectItem> projectList;
    ArrayList<userItem> userList;
    Spinner spinner;
    LinearLayout content;
    ProgressBar progressBar;
    EditText inputUserName, inputUserMail, inputUserPassword;
    Context Context;

    public static Fragment_EditUser newInstance(String userID){
        Fragment_EditUser fragment_editUser = new Fragment_EditUser();

        Bundle args = new Bundle();
        args.putString("userID", userID);
        fragment_editUser.setArguments(args);

        return fragment_editUser;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        userID = getArguments().getString("userID");

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_user, container, false);

        setHasOptionsMenu(true);

        mainActivity = (MainActivity) getActivity();
        myRESTClient = new RESTClient_V3(mainActivity); // Here you should distinguish API V2.0 or V3.0

        inputUserName = (EditText) view.findViewById(R.id.input_userName);
        inputUserMail = (EditText) view.findViewById(R.id.input_userMail);
        inputUserEnabled = (SwitchCompat) view.findViewById(R.id.userEnabled);
        inputUserPassword = (EditText) view.findViewById(R.id.input_userPassword);
        spinner = (Spinner) view.findViewById(R.id.input_userProject);
        content = (LinearLayout) view.findViewById(R.id.content);

        final AppCompatButton changePasswordButton = (AppCompatButton) view.findViewById(R.id.changePasswortButton);
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputUserPassword.setText("");
                inputUserPassword.setEnabled(true);
                changePasswordButton.setVisibility(View.INVISIBLE);
                inputUserPassword.setVisibility(View.VISIBLE);
                inputUserPassword.requestFocus();
                inputUserPassword.requestFocusFromTouch();
                InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });

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
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_editUser_enterUserName));
            }else if(inputUserPassword.getText().toString().isEmpty() && inputUserPassword.isEnabled()){
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_editUser_enterPassword));
            }else{
                String userName, userMail, userPassword;
                Boolean userEnabled;

                userName = inputUserName.getText().toString();
                userMail = inputUserMail.getText().toString();
                userEnabled = inputUserEnabled.isChecked();

                if(inputUserPassword.isEnabled()){
                    userPassword = inputUserPassword.getText().toString();
                }else{
                    userPassword = null;
                }

                new updateUserAsynctask(userList.get(0).userID, projectList.get(spinner.getSelectedItemPosition()).projectID, userName, userMail, userPassword, userEnabled).execute();
            }


        }

        return super.onOptionsItemSelected(item);
    }

    public class myWorker extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                myRESTClient.getUser(userID);
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

            if(success) {
                userList = mainActivity.databaseAdapter.getAllUsers("");
                userItem user = userList.get(0);

                inputUserMail.setText(user.userMail);
                inputUserName.setText(user.userName);
                inputUserEnabled.setChecked(user.userEnabled);

                projectList = mainActivity.databaseAdapter.getAllProjects(mainActivity);
                int selectedProject = 0;
                ArrayList<String> projectNames = new ArrayList<>();
                for(int index = 0; index < projectList.size(); index++){
                    projectNames.add(projectList.get(index).projectName);
                    if(user.userProject.equals(projectList.get(index).projectName))
                        selectedProject = index;
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_spinner_dropdown_item, projectNames);
                spinner.setAdapter(spinnerAdapter);
                spinner.setSelection(selectedProject);

                progressBar.setVisibility(View.INVISIBLE);
                content.setVisibility(View.VISIBLE);

            }else{
                //loginButton.setVisibility(View.VISIBLE);
                //progressBar.setVisibility(View.INVISIBLE);
            }

            super.onPostExecute(success);
        }

    }

    public class updateUserAsynctask extends AsyncTask<String, Void, Boolean> {
        String userName, userMail, userPassword, projectID, userID;
        Boolean userEnabled;

        public updateUserAsynctask(String userID, String projectID, String userName, String userMail, String userPassword, Boolean userEnabled){
            this.userName = userName;
            this.userMail = userMail;
            this.userPassword = userPassword;
            this.userEnabled = userEnabled;
            this.projectID = projectID;
            this.userID = userID;
            //Toast.makeText(mainActivity, "Created"+userName+ userMail+ userPassword+ userEnabled, Toast.LENGTH_LONG).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                //myRESTClient.postUser();
                //Toast.makeText(mainActivity, "Called", Toast.LENGTH_LONG).show();
                return myRESTClient.updateUser(userID, projectID, userName, userMail, userPassword, userEnabled);

            }
            catch(Exception e){
                Log.e("Asynctask", e.toString());
                mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            //Toast.makeText(mainActivity, "Called"+success, Toast.LENGTH_LONG).show();
            if(success) {
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_editUser_updateSuccess));
                mainActivity.changeFragment(mainActivity.TAG_USERLIST);
            }else{
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_editUser_updateFail));
            }

            super.onPostExecute(success);
        }

    }
}