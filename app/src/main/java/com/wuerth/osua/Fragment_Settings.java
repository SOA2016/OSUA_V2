package com.wuerth.osua;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
 * A placeholder fragment containing a simple view.
 */
public class Fragment_Settings extends Fragment {
    TextView tokenExpiresAt, serverAddress;
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

        myPrefs = mainActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        TextView tokenExpiresAtLabel = (TextView) view.findViewById(R.id.tokenExpiresAtLabel);
        tokenExpiresAt = (TextView) view.findViewById(R.id.tokenExpiresAt);
        TextView serverAddressLabel = (TextView) view.findViewById(R.id.serverAddressLabel);
        serverAddress = (TextView) view.findViewById(R.id.serverAddress);
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
                MainActivity.showSnackbar("Failed to convert timetoken");
            }

            long difference = tokentime.getTime() - actualtime.getTime();
            tokenExpiresAt.setText(tokentime.toString() + " (+" + (difference/1000/60) + " " + mainActivity.getString(R.string.fragment_settings_minutes_remaining) + ")");
            serverAddress.setText(myPrefs.getString("serverAddress", ""));

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

        TextView impressum = (TextView) view.findViewById(R.id.impressum);
        impressum.setText(Html.fromHtml(mainActivity.getString(R.string.imprint_text)));

        flushButton = (Button) view.findViewById(R.id.loginButton);
        flushButton.requestFocus();
        flushButton.requestFocusFromTouch();

        flushButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mainActivity.databaseAdapter.deleteUserList();
                mainActivity.databaseAdapter.deleteProjectList();
                MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_settings_flushed));
                mainActivity.changeFragment(MainActivity.TAG_RELOGIN, mainActivity);
            }
        });
        return view;

    }
}