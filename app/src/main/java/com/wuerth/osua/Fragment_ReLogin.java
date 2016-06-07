package com.wuerth.osua;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Fragment_ReLogin extends DialogFragment {
    MainActivity mainActivity;
    SharedPreferences myPrefs;
    RESTClient myRESTClient;
    EditText inputUserPassword;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.fragment_relogin, null);

        inputUserPassword = (EditText) view.findViewById(R.id.input_userPassword);
        inputUserPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputUserPassword.setText("");
            }
        });

        mainActivity = (MainActivity) getActivity();
        myRESTClient = new RESTClient_V3(mainActivity);
        myPrefs = mainActivity.getSharedPreferences("MyPrefs", MainActivity.MODE_PRIVATE);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(mainActivity.getString(R.string.fragment_relogin_loginButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        new myWorker().execute(inputUserPassword.getText().toString());
                    }
                })
                .setNegativeButton(mainActivity.getString(R.string.fragment_relogin_cancelButton), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mainActivity.changeFragment(MainActivity.TAG_LOGIN, mainActivity);
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Button pButton =  ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        Button nButton =  ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE);

        pButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        nButton.setTextColor(getResources().getColor(R.color.darkgrey));
    }

    public class myWorker extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                return myRESTClient.getAuthentificationToken(params[0]);
            }
            catch(Exception e){
                Log.e("Asynctask", e.toString());
                MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {

            if(success) {
                //Toast.makeText(mainActivity, "Expires at: " + myPrefs.getString("actualTokenExpiresAt", "No Token!"), Toast.LENGTH_LONG).show();
                //Toast.makeText(mainActivity, "Token: " + myPrefs.getString("actualToken", "No Token!"), Toast.LENGTH_LONG).show();
                mainActivity.changeFragment(MainActivity.TAG_USERLIST, mainActivity);
            }else{
                mainActivity.changeFragment(MainActivity.TAG_LOGIN, mainActivity);
            }

            super.onPostExecute(success);
        }

    }
}