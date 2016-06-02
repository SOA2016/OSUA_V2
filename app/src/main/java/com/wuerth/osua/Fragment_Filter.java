package com.wuerth.osua;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class Fragment_Filter extends DialogFragment {

    ListView projectFilterList;
    ArrayList<projectItem> projectList;
    MainActivity mainActivity;
    SharedPreferences myPrefs;
    SharedPreferences.Editor spEditor;
    View view;
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    RESTClient myRESTClient;
    ProgressBar progressBar;
    Context Context;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //this.view = view;
        this.builder = new AlertDialog.Builder(getActivity());
        mainActivity = (MainActivity) getActivity();
        myRESTClient = new RESTClient_V2(mainActivity);

        View view = inflater.inflate(R.layout.fragment_filter, null);
        this.view = view;

        mainActivity = (MainActivity) getActivity();
        myPrefs = mainActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        spEditor = myPrefs.edit();

        projectFilterList = (ListView) view.findViewById(R.id.projectFilterList);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
        // Add action buttons
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //LoginDialogFragment.this.getDialog().cancel();
                    }
                });

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mainActivity = (MainActivity) getActivity();
        myRESTClient = new RESTClient_V2(mainActivity); // Here you should distinguish API V2.0 or V3.0

        progressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN);

        new myWorker().execute();

        alertDialog = builder.create();

        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                // Add action buttons
                "Accept", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        ArrayList<String> newFilter = new ArrayList<String>();

                        for (projectItem project : projectList) {
                            if (!project.projectEnabled)
                                newFilter.add(project.projectID);
                        }

                        String newFilterString = TextUtils.join(",", newFilter);

                        Log.d("Disabled_Projects", newFilterString);

                        spEditor.putString("filterDisabledProjects", newFilterString);
                        spEditor.apply();

                        FragmentManager manager = mainActivity.getSupportFragmentManager();
                        Fragment_UserList newFragment = Fragment_UserList.newInstance(mainActivity.searchView.getQuery().toString());
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.fragment, newFragment);
                        transaction.addToBackStack(mainActivity.TAG_USERLIST);
                        transaction.commit();
                    }
                });

        return alertDialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Button pButton =  ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        Button nButton =  ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE);

        pButton.setTextColor(getResources().getColor(R.color.grey));
        pButton.setEnabled(false);
        nButton.setTextColor(getResources().getColor(R.color.darkgrey));
    }

    public class myWorker extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                if(myRESTClient.validateToken()) {   // First validate token to prevent login from non-admin)
                    return myRESTClient.getProjects();
                }else{
                    return false;
                }
            }
            catch(Exception e){
                Log.e("Asynctask", e.toString());
                mainActivity.showSnackbar(Context.getString(R.string.error_0));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if(success) {
                try{
                    progressBar.setVisibility(View.INVISIBLE);

                    projectList = mainActivity.databaseAdapter.getAllProjects(mainActivity);

                    projectFilterList.setAdapter(new myListAdapter());

                    Button pButton =  ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
                    pButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                    pButton.setEnabled(true);
                }catch (Exception e){
                    // Dialog dismissed
                }



            }else{
                alertDialog.dismiss();
                //loginButton.setVisibility(View.VISIBLE);
                //progressBar.setVisibility(View.INVISIBLE);
            }

            super.onPostExecute(success);
        }

    }

    class myListAdapter extends BaseAdapter {

        private LayoutInflater inflater = mainActivity.getLayoutInflater();

        @Override
        public int getCount() {
            return projectList.size();
        }

        @Override
        public Object getItem(int position) {
            return projectList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View item = convertView;
            if (item == null) { // If null create new View
                item = inflater.inflate(R.layout.filter_list_item, parent, false);
            }   // If not null use old View

            final projectItem project = projectList.get(position);

            TextView itemTitle = (TextView) item.findViewById(R.id.itemTitle);
            itemTitle.setText(project.projectName);

            TextView itemUserCount = (TextView) item.findViewById(R.id.itemUsers);
            Integer projectUserCount = mainActivity.databaseAdapter.getProjectUserCount(project.projectID);
            String addon = "User";
            if(projectUserCount != 1)
                addon = addon + "s";
            itemUserCount.setText(projectUserCount.toString() + " " + addon);

            String disabledProjects = myPrefs.getString("filterDisabledProjects", "");
            String[] disabledProjectsArray = disabledProjects.split(",");

            final CheckBox checkbox = (CheckBox) item.findViewById(R.id.itemCheckbox);
            checkbox.setChecked(true);

            for(String disabledProject : disabledProjectsArray){
                if (project.projectID.equals(disabledProject))
                    checkbox.setChecked(false);
            }


            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(checkbox.isChecked()){
                        project.projectEnabled = true;
                    }else{
                        project.projectEnabled = false;
                    }
                }
            });

            return item;
        }
    }
}