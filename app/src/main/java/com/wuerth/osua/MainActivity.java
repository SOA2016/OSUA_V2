package com.wuerth.osua;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.app.Activity;

import java.util.ArrayList;
import android.view.MenuInflater;
import android.app.SearchManager;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener,Toolbar.OnMenuItemClickListener, MenuItemCompat.OnActionExpandListener {
    final static String
            TAG_LOGIN = "Login",
            TAG_USERLIST = "Userlist",
            TAG_EDIT_USER = "Edit User",
            TAG_ADD_USER = "Add User",
            TAG_SETTINGS = "Settings",
            TAG_RELOGIN = "Relogin";

    /* FragmentManager must not be static */
    private FragmentManager manager;
    ArrayList<Integer> mSelectedItems;
    DatabaseAdapter databaseAdapter;
    static FloatingActionButton fab;

    Toolbar toolbar;
    Boolean search = false;
    SearchView searchView;
    MenuItem searchMenuItem;
    Menu Mainmenu;


    LinearLayout fragment;
    static RESTClient RESTClient;
    MainActivity mainActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = (LinearLayout) findViewById(R.id.fragment);

        RESTClient = new RESTClient_V3(this);

        manager = getSupportFragmentManager();
        manager.addOnBackStackChangedListener(this);

        databaseAdapter = new DatabaseAdapter(this);


        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFragment(TAG_ADD_USER, mainActivity);
            }
        });

        changeFragment(TAG_LOGIN, mainActivity);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // store menu
        Mainmenu = menu;

        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu); // load menu



        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        // Assumes current activity is the searchable activity
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setSubmitButtonEnabled(true);
            searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            private void callSearch(String query) {
                Fragment_UserList newFragment = Fragment_UserList.newInstance(query);
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment, newFragment);
                transaction.addToBackStack(TAG_USERLIST);
                transaction.commit();

                fragment.clearFocus();
                fragment.requestFocus();
            }

        });
    }

        switch (getCurrentFragment()) { // Manage Menu-Items
            case TAG_LOGIN: {
                menu.findItem(R.id.action_logout).setVisible(false);
                menu.findItem(R.id.action_filter).setVisible(false);
                menu.findItem(R.id.action_confirm).setVisible(false);
                menu.findItem(R.id.action_search).setVisible(false);

                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
                getSupportActionBar().setTitle(TAG_LOGIN);

                fab.hide();
                break;
            }
            case TAG_EDIT_USER: {
                menu.findItem(R.id.action_logout).setVisible(false);
                menu.findItem(R.id.action_settings).setVisible(false);
                menu.findItem(R.id.action_search).setVisible(false);
                menu.findItem(R.id.action_filter).setVisible(false);

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
                getSupportActionBar().setTitle(TAG_EDIT_USER);

                fab.hide();
                break;
            }
            case TAG_ADD_USER: {
                menu.findItem(R.id.action_logout).setVisible(false);
                menu.findItem(R.id.action_settings).setVisible(false);
                menu.findItem(R.id.action_search).setVisible(false);
                menu.findItem(R.id.action_filter).setVisible(false);
                menu.findItem(R.id.action_confirm).setVisible(true);

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
                getSupportActionBar().setTitle(TAG_ADD_USER);

                fab.hide();
                break;
            }
            case TAG_SETTINGS: {
                menu.findItem(R.id.action_logout).setVisible(false);
                menu.findItem(R.id.action_settings).setVisible(false);
                menu.findItem(R.id.action_search).setVisible(false);
                menu.findItem(R.id.action_filter).setVisible(false);
                menu.findItem(R.id.action_confirm).setVisible(false);

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
                getSupportActionBar().setTitle(TAG_SETTINGS);

                fab.hide();
                break;
            }
            case TAG_USERLIST: {
                menu.findItem(R.id.action_logout).setVisible(true);
                menu.findItem(R.id.action_settings).setVisible(true);
                menu.findItem(R.id.action_search).setVisible(true);
                menu.findItem(R.id.action_filter).setVisible(true);
                menu.findItem(R.id.action_confirm).setVisible(false);

                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
                getSupportActionBar().setTitle(TAG_USERLIST);
                fab.show();
                break;
            }
            default: {
                //enableDrawerToggle(true);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            changeFragment(TAG_SETTINGS, mainActivity);

            return true;
        }

        if(id == R.id.action_logout) {
            returnParam params = new returnParam(false, mainActivity);
            new deleteTokenAsynctask().execute(params);

            return true;
        }

        if(id== R.id.action_filter) {
            Fragment_Filter fragment_filter = new Fragment_Filter();
            fragment_filter.show(manager, "Testss");
        }

        if(id == android.R.id.home) { // Zurück Event aufrufen
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackStackChanged() {

        /*if(search) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            search = false;
        }*/

        supportInvalidateOptionsMenu();

        switch (getCurrentFragment()) {
            case TAG_LOGIN: {
                //toolbarSearch.setVisibility(View.INVISIBLE);
                break;
            }
            case TAG_EDIT_USER: {
                //toolbarSearch.setVisibility(View.INVISIBLE);
                break;
            }
            case TAG_ADD_USER: {
                //toolbarSearch.setVisibility(View.INVISIBLE);
                break;
            }
            case TAG_SETTINGS: {
                //toolbarSearch.setVisibility(View.INVISIBLE);
                break;
            }
            case TAG_USERLIST: {
                /*if(search) {
                    toolbarSearch.setVisibility(View.VISIBLE);
                }else{
                    toolbarSearch.setVisibility(View.INVISIBLE);
                }*/

                break;
            }
            default: {
                //enableDrawerToggle(true);
            }
        }
    }

    @Override
    public void onBackPressed() {

        supportInvalidateOptionsMenu();

        if(Mainmenu.findItem(R.id.action_search).isVisible()){
            if(getCurrentFragment().equals(TAG_USERLIST))
                changeFragment(TAG_USERLIST, mainActivity);
        }else {
            switch (getCurrentFragment()) {
                case TAG_LOGIN: {
                    finish();
                    break;
                }
                case TAG_USERLIST: {
                    new FireMissilesDialogFragment().show(manager, "Dialog_Fragment_Logout");
                    break;
                }
                default: {
                    super.onBackPressed();
                }
            }
        }
    }


    /*
    Method is fired when User presses hardware backbutton on phone to exit app
     */
    public static class FireMissilesDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(this.getString(R.string.mainActivity_logoutDialog_title))
                    .setMessage(Html.fromHtml(this.getString(R.string.mainActivity_logoutDialog_text)))
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            returnParam param = new returnParam(false, (MainActivity)getActivity());
                            new deleteTokenAsynctask().execute(param);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
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
    }

    public String getCurrentFragment(){
        int count = manager.getBackStackEntryCount();
        FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(count - 1); // Get the top entry (current fragment)
        return entry.getName();
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        search = true;
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        changeFragment(TAG_USERLIST, mainActivity);
        search = false;

        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        onOptionsItemSelected(item);
        return false;
    }

    public void changeFragment(String TAG, Activity ac) {

        if (ac.getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) ac.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(ac.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        switch (TAG) {
            case TAG_USERLIST: {
                Fragment_UserList newFragment = Fragment_UserList.newInstance();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment, newFragment);
                transaction.addToBackStack(TAG_USERLIST);
                transaction.commit();
                break;
            }
            case TAG_EDIT_USER: {
                /*Fragment_EditUser newFragment = Fragment_EditUser.newInstance();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment, newFragment);
                transaction.addToBackStack(TAG_EDIT_USER);
                transaction.commit();*/
                break;
            }
            case TAG_ADD_USER: {
                Fragment_AddUser newFragment = Fragment_AddUser.newInstance();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment, newFragment);
                transaction.addToBackStack(TAG_ADD_USER);
                transaction.commit();
                break;
            }
            case TAG_SETTINGS: {
                Fragment_Settings newFragment = Fragment_Settings.newInstance();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment, newFragment);
                transaction.addToBackStack(TAG_SETTINGS);
                transaction.commit();
                break;
            }
            case TAG_LOGIN: {
                Fragment_Login newFragment = Fragment_Login.newInstance();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment, newFragment);
                transaction.addToBackStack(TAG_LOGIN);
                transaction.commit();
                break;
            }
            case TAG_RELOGIN: {
                    DialogFragment newFragment =Fragment_ReLogin.newInstance();
                    newFragment.show(manager, TAG_RELOGIN);
                break;
            }
            default: {
                Toast.makeText(ac, ac.getString(R.string.error_1), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        returnParam params = new returnParam(false, mainActivity);
        new deleteTokenAsynctask().execute(params);

    }

    public static void showSnackbar (String message){
        Snackbar.make(fab, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public static class deleteTokenAsynctask extends AsyncTask<returnParam, Void, returnParam> {
        @Override
        protected returnParam doInBackground(returnParam... params) {

            try{
                //myRESTClient.postUser();
                //Toast.makeText(mainActivity, "Called", Toast.LENGTH_LONG).show();
                params[0].success= RESTClient.deleteToken();
                return params[0];
            }
            catch(Exception e){
                Log.e("Asynctask", e.toString());
                showSnackbar(params[0].mainActivity.getString(R.string.error_0));
                params[0].success=false;
                return params[0];
            }
        }
        @Override
        protected void onPostExecute(returnParam param) {
            //Toast.makeText(mainActivity, "Called"+success, Toast.LENGTH_LONG).show();
            try {
                param.mainActivity.changeFragment(TAG_LOGIN, param.mainActivity);
            } catch (Exception e){
                // No Activity -> App destoryed
            }

            if(param.success) {
                Log.d("deleteToken", "success");
            }else{
                Log.d("deleteToken", "failed");
            }
        }

    }
}