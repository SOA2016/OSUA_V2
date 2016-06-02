package com.wuerth.osua;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Timestamp;

/**
 * A placeholder fragment containing a simple view.
 */
public class Fragment_Settings extends Fragment {

    TextView tokenExpiresAt, serverAddress;
    SharedPreferences myPrefs;
    MainActivity mainActivity;
    Context Context;

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

            String ret = myPrefs.getString("actualTokenExpiresAt", "");

            if (ret.contains("T") && ret.contains("Z")) {

                String z = ret.replace("T", " ");
                String t = z.replace("Z", "");

                java.util.Date date = new java.util.Date();

                Timestamp time = new Timestamp(date.getTime());
                String actualTime;
                String timeConvert = time.toString();
                actualTime = timeConvert.replaceAll("-", "");
                actualTime = actualTime.replaceAll(":", "");
                actualTime = actualTime.replaceAll(" ", "");
                actualTime = actualTime.substring(0, actualTime.length() - 4);

                String timeToken;
                timeToken = t.replace(" ", "");
                timeToken = timeToken.replace("-", "");
                timeToken = timeToken.replace(":", "");
                // b= b.substring(0, b.length()-4 );

                long timetoken = Long.valueOf(timeToken);
                long actualtime = Long.valueOf(actualTime);
                if (timetoken > actualtime) {


                    tokenExpiresAt.setText(t);

                } else {
                    tokenExpiresAtLabel.setVisibility(View.GONE);
                    tokenExpiresAt.setVisibility(View.GONE);
                    serverAddressLabel.setVisibility(View.GONE);
                    serverAddress.setVisibility(View.GONE);
                }

            }

            int serverPrefix = myPrefs.getInt("serverPrefix", 0);
            String[] prefixList = mainActivity.getResources().getStringArray(R.array.serverPrefixes);
            serverAddress.setText(prefixList[serverPrefix]+myPrefs.getString("serverAddress", ""));
        }else{
            tokenExpiresAtLabel.setVisibility(View.GONE);
            tokenExpiresAt.setVisibility(View.GONE);
            serverAddressLabel.setVisibility(View.GONE);
            serverAddress.setVisibility(View.GONE);
        }

        TextView impressum = (TextView) view.findViewById(R.id.impressum);
        impressum.setText(Html.fromHtml(Context.getString(R.string.impressum)));
        return view;
    }
}