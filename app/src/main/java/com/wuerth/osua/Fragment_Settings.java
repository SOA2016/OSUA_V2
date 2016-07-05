package com.wuerth.osua;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * changed by Stephan Strissel
 * 1) A fragment to show Token-Expiration-Date and Server-Address
 * 2) also implements a flush-button to delete local Userlist and Projectlist, forces Relogin
 * 3) shows the imprint
 */
public class Fragment_Settings extends Fragment {
    TextView tokenExpiresAt, serverAddress, loginName, loginUserDomain, loginUserProject, loginProjectDomain;
    SharedPreferences myPrefs;
    MainActivity mainActivity;
    Button flushButton;

    public Fragment_Settings() {
    }


    public static Fragment_Settings newInstance(){
        return new Fragment_Settings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mainActivity = (MainActivity) getActivity();
        mainActivity.initToolbar();

        myPrefs = mainActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        TextView tokenExpiresAtLabel = (TextView) view.findViewById(R.id.tokenExpiresAtLabel);
        tokenExpiresAt = (TextView) view.findViewById(R.id.tokenExpiresAt);
        TextView serverAddressLabel = (TextView) view.findViewById(R.id.serverAddressLabel);
        serverAddress = (TextView) view.findViewById(R.id.serverAddress);
        loginName = (TextView) view.findViewById(R.id.input_loginName);
        loginUserDomain = (TextView) view.findViewById(R.id.input_loginUserDomain);
        loginUserProject =  (TextView) view.findViewById(R.id.input_loginUserProject);
        loginProjectDomain =  (TextView) view.findViewById(R.id.input_loginProjectDomain);

        if(!myPrefs.getString("actualTokenExpiresAt", "").equals("")) {


            /*
             Stephan Strissel
             implemented new time comparsion
             */
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date tokentime = new Date();
            Date actualtime = new Date();
            try {
                tokentime = simpleDateFormat.parse(myPrefs.getString("actualTokenExpiresAt", ""));
            } catch (Exception e) {
                MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_settings_parseTimeTokenFailed));
            }

            long difference = tokentime.getTime() - actualtime.getTime();
            tokenExpiresAt.setText(tokentime.toString() + " (+" + (difference/1000/60) + " " + mainActivity.getString(R.string.fragment_settings_minutes_remaining) + ")");
            serverAddress.setText(myPrefs.getString("serverPrefix", "") + myPrefs.getString("serverAddress", ""));
            loginName.setText(myPrefs.getString("loginName", ""));
            loginUserDomain.setText(myPrefs.getString("loginUserDomain", ""));
            loginUserProject.setText(myPrefs.getString("loginUserProject", ""));
            loginProjectDomain.setText(myPrefs.getString("loginProjectDomain", ""));

            if (difference > 0) {
                    tokenExpiresAtLabel.setVisibility(View.VISIBLE);
                    tokenExpiresAt.setVisibility(View.VISIBLE);
                    serverAddressLabel.setVisibility(View.VISIBLE);
                    serverAddress.setVisibility(View.VISIBLE);

                } else {
                    tokenExpiresAtLabel.setVisibility(View.GONE);
                    tokenExpiresAt.setVisibility(View.GONE);
                    serverAddressLabel.setVisibility(View.GONE);
                    serverAddress.setVisibility(View.GONE);

                }

        } else {
            tokenExpiresAtLabel.setVisibility(View.GONE);
            tokenExpiresAt.setVisibility(View.GONE);
            serverAddressLabel.setVisibility(View.GONE);
            serverAddress.setVisibility(View.GONE);
        }

        /* text in this Textview has to be loaded in Code, because text is html */
        TextView impressum = (TextView) view.findViewById(R.id.impressum);
        impressum.setText(Html.fromHtml(mainActivity.getString(R.string.imprint_text)));


        /* Stephan Strissel */
        /* initialise flushButton */
        flushButton = (Button) view.findViewById(R.id.loginButton);
        flushButton.requestFocus();
        flushButton.requestFocusFromTouch();

        flushButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.databaseAdapter.deleteUserList(); // delete local Userlist
                mainActivity.databaseAdapter.deleteProjectList(); // delete local Projectlist
                MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_settings_flushed));
            }
        });

        mainActivity.showToolbar();
        return view;

    }
}