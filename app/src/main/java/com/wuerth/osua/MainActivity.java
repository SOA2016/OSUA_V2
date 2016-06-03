package com.wuerth.osua;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener,Toolbar.OnMenuItemClickListener, MenuItemCompat.OnActionExpandListener {

    final static String TAG_LOGIN = "Login",
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
    static Toolbar toolbarSearch;
    Boolean search = false;
    static MenuItem menuItemSearch;
    SearchView searchView;
    LinearLayout fragment;
    static RESTClient RESTClient;
    MainActivity mainActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbarSearch = (Toolbar) findViewById(R.id.toolbarSearch);
        setSupportActionBar(toolbar);

        fragment = (LinearLayout) findViewById(R.id.fragment);

        RESTClient = new RESTClient_V3(this);

        toolbarSearch = (Toolbar) findViewById(R.id.toolbarSearch);
        //toolbarSearch.setVisibility(View.VISIBLE);
        getMenuInflater().inflate(R.menu.menu_search, toolbarSearch.getMenu());
        menuItemSearch = toolbarSearch.getMenu().findItem(R.id.action_search);
        searchView = (SearchView) menuItemSearch.getActionView();
        //menuItemSearch.expandActionView();
        MenuItemCompat.setOnActionExpandListener(menuItemSearch, this);
        toolbarSearch.setOnMenuItemClickListener(this);
        EditText txtSearch = ((EditText)menuItemSearch.getActionView().findViewById(android.support.v7.appcompat.R.id.search_src_text));
        txtSearch.setHint("Search...");
        txtSearch.setHintTextColor(Color.LTGRAY);
        txtSearch.setText("");
        txtSearch.setTextColor(getResources().getColor(R.color.darkgrey));

        manager = getSupportFragmentManager();
        manager.addOnBackStackChangedListener(this);

        databaseAdapter = new DatabaseAdapter(this);

        changeFragment(TAG_LOGIN, mainActivity);

        /*Fragment_Login newFragment = Fragment_Login.newInstance();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment, newFragment);
        transaction.addToBackStack(TAG_LOGIN);
        transaction.commit();*/

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

                changeFragment(TAG_ADD_USER, mainActivity);
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        /*MenuItemImpl menuItem = (MenuItemImpl) toolbarSearch.getMenu().findItem(R.id.action_search);
        menuItem.expandActionView();
        menuItem.getActionView();*/
        //searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Fragment_UserList newFragment = Fragment_UserList.newInstance(searchView.getQuery().toString());
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment, newFragment);
                transaction.addToBackStack(TAG_USERLIST);
                transaction.commit();

                searchView.clearFocus();
                fragment.requestFocus();

                toolbarSearch.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        //toolbarSearch.setVisibility(View.INVISIBLE);

        getMenuInflater().inflate(R.menu.menu_main, menu);

        /*MenuItem menuItem = toolbar.getMenu().findItem(R.id.action_search);
        MenuItemCompat.setOnActionExpandListener(menuItem, this);

        SearchView searchView = (SearchView) menuItem.getActionView();
        EditText txtSearch = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        txtSearch.setHint("Search...");
        txtSearch.setHintTextColor(Color.LTGRAY);
        txtSearch.setText("");
        txtSearch.setTextColor(getResources().getColor(R.color.darkgrey));*/

        switch (getCurrentFragment()) { // Manage Menu-Items
            case TAG_LOGIN: {
                menu.findItem(R.id.action_logout).setVisible(false);
                menu.findItem(R.id.action_search).setVisible(false);
                menu.findItem(R.id.action_filter).setVisible(false);
                menu.findItem(R.id.action_confirm).setVisible(false);

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

        if(id== R.id.action_search) {
            search = true;
            toolbarSearch.setVisibility(View.VISIBLE);
            menuItemSearch.expandActionView();
            searchView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);


            //toolbar.setBackgroundColor(getResources().getColor(R.color.grey));
        }

        if(id == android.R.id.home) { // Zurück Event aufrufen
            onBackPressed();
            return true;
        }

        /*if(id == R.id.action_confirm) {

            /*changeFragment(TAG_USERLIST);

            FloatingActionButton.OnVisibilityChangedListener fabListener;
            fabListener = new FloatingActionButton.OnVisibilityChangedListener(){
                @Override
                public void onShown(FloatingActionButton fab) {

                    Snackbar.make(fab, "User saved", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    super.onShown(fab);
                }
            };

            fab.show(fabListener);

            return true;
        }*/

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

        if(toolbarSearch.getVisibility() == View.VISIBLE){
            toolbarSearch.setVisibility(View.INVISIBLE);
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
        //Toast.makeText(this, "Opened", Toast.LENGTH_SHORT).show();
        //toolbarSearch.setVisibility(View.VISIBLE);
        search = true;
        //supportInvalidateOptionsMenu();

        //toolbar.setBackgroundColor(getResources().getColor(R.color.grey));
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);

        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        //Toast.makeText(this, "Closed", Toast.LENGTH_SHORT).show();
        //showSnackbar("Test");
        //toolbar.setVisibility(View.INVISIBLE);
        //Toolbar toolbarSearch = (Toolbar) findViewById(R.id.toolbarSearch);
        //toolbarSearch.setVisibility(View.INVISIBLE);
        //searchView.expandActionView();
        toolbarSearch.setVisibility(View.INVISIBLE);
        changeFragment(TAG_USERLIST, mainActivity);
        //Toast.makeText(this, "hola", Toast.LENGTH_LONG).show();
        search = false;
        //supportInvalidateOptionsMenu();

        //toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        //getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //getSupportActionBar().setDisplayShowHomeEnabled(false);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);

        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        onOptionsItemSelected(item);
        return false;
    }

    public void changeFragment(String TAG, Activity ac) {

        //searchView.setQuery("", true);

        if (ac.getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) ac.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(ac.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        toolbarSearch.setVisibility(View.INVISIBLE);
        EditText txtSearch = ((EditText)menuItemSearch.getActionView().findViewById(android.support.v7.appcompat.R.id.search_src_text));
        txtSearch.setText("");

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
                showSnackbar(mainActivity.getString(R.string.error_0));
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