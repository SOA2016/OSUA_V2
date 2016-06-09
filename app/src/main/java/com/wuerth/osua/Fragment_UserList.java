package com.wuerth.osua;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.graphics.drawable.Drawable;




import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class Fragment_UserList extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{

    private ListView listView;
    static RESTClient myRESTClient;
    MainActivity mainActivity;
    ProgressBar progressBar;
    ArrayList<userItem> userList;
    String searchQuery;
    int firstItem;
    SwipeRefreshLayout userListRefresh;

    public Fragment_UserList() {
    }


    public static Fragment_UserList newInstance(String searchQuery){
        Fragment_UserList fragment_userList = new Fragment_UserList();

        Bundle args = new Bundle();
        args.putString("searchQuery", searchQuery);
        fragment_userList.setArguments(args);

        return fragment_userList;
    }

    public static Fragment_UserList newInstance(){
        return new Fragment_UserList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try{
            searchQuery = getArguments().getString("searchQuery");
        }catch (Exception e){
            searchQuery = "";
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_userlist, container, false);

        setHasOptionsMenu(true);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        mainActivity = (MainActivity) getActivity();

        myRESTClient = new RESTClient_V3(mainActivity); // Here you should distinguish API V2.0 or V3.0

        progressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.colorPrimary),
                android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.setVisibility(View.VISIBLE);

        new myWorker().execute();

        listView = (ListView) view.findViewById(R.id.userList);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((firstVisibleItem - firstItem) > 1) {
                    mainActivity.fab.hide();
                    firstItem = firstVisibleItem;
                } else if ((firstItem - firstVisibleItem) > 1) {
                    mainActivity.fab.show();
                    firstItem = firstVisibleItem;
                }
            }
        });

        userListRefresh = (SwipeRefreshLayout) view.findViewById(R.id.userListRefresh);
        userListRefresh.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorPrimary,
                R.color.colorPrimary);
        userListRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listView.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return 0;
                    }

                    @Override
                    public Object getItem(int position) {
                        return null;
                    }

                    @Override
                    public long getItemId(int position) {
                        return 0;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        return null;
                    }
                });
                new myWorker().execute();
            }
        });

        return view;
    }

    public class myWorker extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try{
                //if(myRESTClient.validateToken())    // First validate token to prevent login from non-admin)
                    if(myRESTClient.getUsers()) {

                        myRESTClient.getProjects();

                        return true;
                    } else {
                        return false;
                    }
            }
            catch(Exception e){
                Log.e("Asynctask", e.toString());
                mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));

            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {



            if(success) {
                DatabaseAdapter databaseAdapter = new DatabaseAdapter(mainActivity);
                userList = databaseAdapter.getAllUsers(searchQuery);
                ListAdapter adapter = new myListAdapter();
                listView.setAdapter(adapter);
                progressBar.setVisibility(View.INVISIBLE);

           }else{

                //mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_userList_loadFail));
                //mainActivity.changeFragment(MainActivity.TAG_LOGIN, mainActivity);
            }

            userListRefresh.setRefreshing(false);
            super.onPostExecute(success);
        }

    }

    class myListAdapter extends BaseAdapter {

        private LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public Object getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View item = convertView;
            if (item == null) { // If null create new View
                item = inflater.inflate(R.layout.userlist_item_divider, parent, false);
            }   // If not null use old View

            userItem user = userList.get(position);

            TextView name = (TextView) item.findViewById(R.id.userName);
            name.setText(user.userName);

            TextView letter = (TextView) item.findViewById(R.id.userLetter);
            letter.setText(user.userName.substring(0,1));

            ImageView userCircle = (ImageView) item.findViewById(R.id.userCircle);

            int color;
            switch(user.userName.substring(0,1).toLowerCase()){
                case "a":
                   color = getResources().getColor(R.color.a);
                    break;
                case "b":
                    color = getResources().getColor(R.color.b);
                    break;
                case "c":
                    color = getResources().getColor(R.color.c);
                    break;
                case "d":
                    color = getResources().getColor(R.color.d);
                    break;
                case "e":
                    color = getResources().getColor(R.color.e);
                    break;
                case "f":
                    color = getResources().getColor(R.color.f);
                    break;
                case "g":
                    color = getResources().getColor(R.color.g);
                    break;
                case "h":
                    color = getResources().getColor(R.color.h);
                    break;
                case "i":
                    color = getResources().getColor(R.color.i);
                    break;
                case "j":
                    color = getResources().getColor(R.color.j);
                    break;
                case "k":
                    color = getResources().getColor(R.color.k);
                    break;
                case "l":
                    color = getResources().getColor(R.color.l);
                    break;
                case "m":
                    color = getResources().getColor(R.color.m);
                    break;
                case "n":
                    color = getResources().getColor(R.color.n);
                    break;
                case "o":
                    color = getResources().getColor(R.color.o);
                    break;
                case "p":
                    color = getResources().getColor(R.color.p);
                    break;
                case "q":
                    color = getResources().getColor(R.color.q);
                    break;
                case "r":
                    color = getResources().getColor(R.color.r);
                    break;
                case "s":
                    color = getResources().getColor(R.color.s);
                    break;
                case "t":
                    color = getResources().getColor(R.color.t);
                    break;
                case "u":
                    color = getResources().getColor(R.color.u);
                    break;
                case "v":
                    color = getResources().getColor(R.color.v);
                    break;
                case "w":
                    color = getResources().getColor(R.color.w);
                    break;
                case "x":
                    color = getResources().getColor(R.color.x);
                    break;
                case "y":
                    color = getResources().getColor(R.color.y);
                    break;
                case "z":
                    color = getResources().getColor(R.color.z);
                    break;
                default:
                    color = getResources().getColor(R.color.colorAccent);
            }

            Drawable newCircle = getResources().getDrawable(R.drawable.circle_white).mutate();
            newCircle.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            userCircle.setBackground(newCircle);

            TextView mail = (TextView) item.findViewById(R.id.userMail);
            mail.setText(user.userMail);
            TextView project = (TextView) item.findViewById(R.id.userProject);
            project.setText(user.userProject);


            if(user.userEnabled){
                ImageView circle = (ImageView) item.findViewById(R.id.userEnabled);
                circle.setImageDrawable(getResources().getDrawable(R.drawable.circle_green));
            }else{
                ImageView circle = (ImageView) item.findViewById(R.id.userEnabled);
                circle.setImageDrawable(getResources().getDrawable(R.drawable.circle_red));
            }

            return item;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MainActivity mainActivity = (MainActivity) getActivity();
        FragmentManager manager = mainActivity.getSupportFragmentManager();

        Fragment_EditUser newFragment = Fragment_EditUser.newInstance(userList.get(position).userID);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment, newFragment);
        transaction.addToBackStack(mainActivity.TAG_EDIT_USER);
        transaction.commit();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        userItem user = userList.get(position);

        FireMissilesDialogFragment fireMissilesDialogFragment = new FireMissilesDialogFragment(user);
        fireMissilesDialogFragment.show(getActivity().getSupportFragmentManager(), "Testss");
        return true;
    }

    public static class FireMissilesDialogFragment extends DialogFragment {
        userItem user;


        public FireMissilesDialogFragment (userItem user){
            this.user = user;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getActivity().getString(R.string.fragment_userList_deleteDialog_title))
                    .setMessage(Html.fromHtml(getActivity().getString(R.string.fragment_userList_deleteDialog_text) + " <b>"+user.userName+"</b>?"))
                    .setPositiveButton(getActivity().getString(R.string.fragment_userList_deleteDialog_deleteButton), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new deleteUserAsynctask(user.userID).execute();
                        }
                    })
                    .setNegativeButton(getActivity().getString(R.string.fragment_userList_deleteDialog_cancelButton), new DialogInterface.OnClickListener() {
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

    public static class deleteUserAsynctask extends AsyncTask<returnParam, Void, returnParam> {
        String userID;

        public deleteUserAsynctask(String userID){
            this.userID = userID;
        }

        @Override
        protected returnParam doInBackground(returnParam... params) {

            try{
                //myRESTClient.postUser();
                //Toast.makeText(mainActivity, "Called", Toast.LENGTH_LONG).show();
                params[0].success = myRESTClient.deleteUser(userID);
                return params[0];
             } catch (Exception e){
                Log.e("Asynctask", e.toString());
                params[0].mainActivity.showSnackbar (params[0].mainActivity.getString(R.string.error_0));
                params[0].success = false;
                return params[0];
            }
        }

        @Override
        protected void onPostExecute(returnParam params) {
            //Toast.makeText(mainActivity, "Called"+success, Toast.LENGTH_LONG).show();
            if(params.success) {
                params.mainActivity.showSnackbar(params.mainActivity.getString(R.string.fragment_userList_deleteSuccess));
                params.mainActivity.changeFragment(params.mainActivity.TAG_LOGIN, params.mainActivity);
            }else {
                params.mainActivity.showSnackbar(params.mainActivity.getString(R.string.fragment_userList_deleteFail));
            }

        }

    }
}