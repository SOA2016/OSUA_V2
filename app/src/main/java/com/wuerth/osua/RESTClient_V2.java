package com.wuerth.osua;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

public class RESTClient_V2 extends RESTClient {

    MainActivity mainActivity;
    SharedPreferences myPrefs;
    SharedPreferences.Editor spEditor;
    Context Context;

    RESTClient_V2(MainActivity mainActivity){
        this.mainActivity = mainActivity;
        myPrefs = mainActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        spEditor = myPrefs.edit();
    }

	public boolean getAuthentificationToken(String loginPassword){

        String loginName = myPrefs.getString("loginName", "");
        String loginProject = myPrefs.getString("loginProject", "");
        String serverAddress = myPrefs.getString("serverAddress", "");
        String[] prefixList = mainActivity.getResources().getStringArray(R.array.serverPrefixes);

        if(loginName.equals("") || loginProject.equals("") || serverAddress.equals("")){
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }

		JSONObject jsonRequest = new JSONObject();
		JSONObject auth = new JSONObject();
		JSONObject passwordCredentials = new JSONObject();

		try {
			passwordCredentials.put("username", loginName);
			passwordCredentials.put("password", loginPassword);

            auth.put("tenantName", loginProject);
			auth.put("passwordCredentials", passwordCredentials);

			jsonRequest.put("auth", auth);
		} catch (JSONException e1) {
			mainActivity.showSnackbar(Context.getString(R.string.error_0));
			return false;
		}
		
		try {

			HttpParams httpParameters = new BasicHttpParams();

			int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI(serverAddress + "/v2.0/tokens");
			HttpPost request = new HttpPost();

			request.setURI(website);
			request.setEntity(new StringEntity(jsonRequest.toString()));
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			
			HttpResponse httpResponse = client.execute(request);

			int status = httpResponse.getStatusLine().getStatusCode();

			switch(status){
				case 200: {
					HttpEntity entity = httpResponse.getEntity();
                    String responseString = EntityUtils.toString(entity);
                    Log.d("Response (auth)", responseString);

                    JSONObject myJSONObject;
                    JSONObject access;
                    JSONObject token;

                    try {
                        myJSONObject = new JSONObject(responseString);
                        access = myJSONObject.getJSONObject("access");
                        token = access.getJSONObject("token");

                        spEditor.putString("actualToken", token.getString("id"));
                        spEditor.putString("actualTokenExpiresAt", token.getString("expires"));
                        spEditor.apply();

                        return true;

                    } catch (JSONException e) {
                        Log.e("RESTClient", ""+status);
                        mainActivity.showSnackbar(Context.getString(R.string.error_0));
                        return false;
                    }
				}
				case 400: {
					mainActivity.showSnackbar(mainActivity.getString(R.string.error_400));
					return false;
				}
				case 401: {
					mainActivity.showSnackbar(mainActivity.getString(R.string.error_401));
					return false;
				}
                default: {
                    Log.e("RESTClient", ""+status);
                    mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                    return false;
                }
			}
		}
		 catch (IOException e) {
			 mainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
			 return false;
		} catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
			mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}
	}

    public boolean deleteToken(){

        String actualToken = myPrefs.getString("actualToken", "");
        String serverAddress = myPrefs.getString("serverAddress", "");
        int serverPrefix = myPrefs.getInt("serverPrefix", 0);
        String[] prefixList = mainActivity.getResources().getStringArray(R.array.serverPrefixes);

        if(actualToken.equals("") || serverAddress.equals("")){
            mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
            return false;
        }

        try {
            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpClient client = new DefaultHttpClient(httpParameters);
            URI website = new URI(prefixList[serverPrefix]+serverAddress + "/v2.0/tokens/"+actualToken);
            HttpDelete request = new HttpDelete();
            request.setURI(website);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader("X-Auth-Token", actualToken);

            HttpResponse httpResponse = client.execute(request);

            int status = httpResponse.getStatusLine().getStatusCode();

            if(status == 204){
                spEditor.putString("actualToken", null);
                spEditor.putString("actualTokenExpiresAt", null);
                spEditor.apply();
                return true;
            }else{
                Log.e("RESTClient", "" + status);
                mainActivity.showSnackbar(mainActivity.getString(R.string.error_3));
                return false;
            }
        }
        catch (IOException e) {
            mainActivity.showSnackbar(mainActivity.getString(R.string.error_4));
            return false;
        } catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());

            mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
            return false;
        }
    }

    public boolean validateToken(){

        String actualToken = myPrefs.getString("actualToken", "");
        String serverAddress = myPrefs.getString("serverAddress", "");
        String[] prefixList = mainActivity.getResources().getStringArray(R.array.serverPrefixes);

        if(actualToken.equals("") || serverAddress.equals("")){
            mainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
            return false;
        }

        try {
            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpClient client = new DefaultHttpClient(httpParameters);
            URI website = new URI(serverAddress + "/v2.0/tokens/"+actualToken);
            HttpGet request = new HttpGet();
            request.setURI(website);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader("X-Auth-Token", actualToken);

            HttpResponse httpResponse = client.execute(request);

            int status = httpResponse.getStatusLine().getStatusCode();

            if(status == 200){
                return true;
            }else if(status == 403) {
                mainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
                mainActivity.changeFragment(mainActivity.TAG_LOGIN, mainActivity);
                return false;
            }else{
                Log.e("RESTClient", "" + status);
                spEditor.putString("actualToken", null);
                spEditor.putString("actualTokenExpiresAt", null);
                spEditor.apply();
                return false;
            }
        }
        catch (IOException e) {
            mainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
            return false;
        } catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }
    }

    public boolean getUsers(){

        if(!validateToken()) {
            mainActivity.changeFragment(MainActivity.TAG_RELOGIN, mainActivity);
            return false;
        }

        String actualToken = myPrefs.getString("actualToken", "");
        String serverAddress = myPrefs.getString("serverAddress", "");
        String[] prefixList = mainActivity.getResources().getStringArray(R.array.serverPrefixes);

        if(actualToken.equals("") || serverAddress.equals("")){
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }

        try {
            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpClient client = new DefaultHttpClient(httpParameters);
            URI website = new URI(serverAddress + "/v2.0/users");
            HttpGet request = new HttpGet();
            request.setURI(website);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader("X-Auth-Token", actualToken);

            HttpResponse httpResponse = client.execute(request);

            int status = httpResponse.getStatusLine().getStatusCode();

            if(status == 200){
                HttpEntity entity = httpResponse.getEntity();
                String responseString = EntityUtils.toString(entity);
                Log.d("Response (userlist)", responseString);

                JSONObject myJSONObject;
                JSONArray userList;

                try {
                    myJSONObject = new JSONObject(responseString);
                    userList = myJSONObject.getJSONArray("users");

                    mainActivity.databaseAdapter.deleteUserList();

                    for(int index = 0; index < userList.length(); index++){
                        JSONObject user = userList.getJSONObject(index);
                        String userID, userName, userMail, userProject;
                        Boolean userEnabled;

                        //mainActivity.databaseAdapter.insertUser(1,"test", "testmail", "testprojekt");

                        userID = user.getString("id");
                        userName = user.getString("name");
                        userEnabled = user.getBoolean("enabled");

                        try{
                            userMail = user.getString("email");
                        }catch (Exception e){
                            // No email for this user
                            userMail = "";
                        }

                        try{
                            userProject = user.getString("tenantId");
                        }catch (Exception e){
                            // No project selected for this user
                            userProject = "";
                        }

                        mainActivity.databaseAdapter.insertUser(userID, userName, userMail, userProject, userEnabled);
                        //Log.d("User"+index, user.getString("name"));
                    }

                    return true;

                } catch (JSONException e) {
                    Log.e("RESTClient", ""+status+e);
                    mainActivity.showSnackbar(Context.getString(R.string.error_0));
                    return false;
                }
            } else{
                Log.e("RESTClient", "" + status);
                mainActivity.showSnackbar(Context.getString(R.string.error_0));
                return false;
            }
        }
        catch (IOException e) {
            mainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
            return false;
        } catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }
    }

    public boolean getUser(String ID){

        if(!validateToken()) {
            mainActivity.changeFragment(MainActivity.TAG_RELOGIN, mainActivity);
            return false;
        }

        String actualToken = myPrefs.getString("actualToken", "");
        String serverAddress = myPrefs.getString("serverAddress", "");
        String[] prefixList = mainActivity.getResources().getStringArray(R.array.serverPrefixes);

        if(actualToken.equals("") || serverAddress.equals("")){
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }

        try {
            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpClient client = new DefaultHttpClient(httpParameters);
            URI website = new URI(serverAddress + "/v2.0/users/"+ID);
            HttpGet request = new HttpGet();
            request.setURI(website);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader("X-Auth-Token", actualToken);
            request.setHeader("userId", ID);

            HttpResponse httpResponse = client.execute(request);

            int status = httpResponse.getStatusLine().getStatusCode();

            if(status == 200){
                HttpEntity entity = httpResponse.getEntity();
                String responseString = EntityUtils.toString(entity);
                Log.d("Response (editUser)", responseString);

                JSONObject myJSONObject;

                try {
                    myJSONObject = new JSONObject(responseString);

                    //mainActivity.databaseAdapter.deleteUserList();

                    //for(int index = 0; index < userList.length(); index++){
                        JSONObject user = myJSONObject.getJSONObject("user");
                        String userID, userName, userMail, userProject;
                        Boolean userEnabled;

                        //mainActivity.databaseAdapter.insertUser(1,"test", "testmail", "testprojekt");

                        userID = user.getString("id");
                        userName = user.getString("name");
                        userEnabled = user.getBoolean("enabled");

                        try{
                            userMail = user.getString("email");
                        }catch (Exception e){
                            // No email for this user
                            userMail = "";
                        }

                        try{
                            userProject = user.getString("tenantId");
                        }catch (Exception e){
                            // No project selected for this user
                            userProject = "";
                        }

                        mainActivity.databaseAdapter.deleteUserList();
                        mainActivity.databaseAdapter.insertUser(userID, userName, userMail, userProject, userEnabled);
                        //Log.d("User"+index, user.getString("name"));
                    //}

                    return true;

                } catch (JSONException e) {
                    Log.e("RESTClient", ""+status+e);
                    mainActivity.showSnackbar(Context.getString(R.string.error_0));
                    return false;
                }
            } else{
                Log.e("RESTClient", "" + status);
                mainActivity.showSnackbar(Context.getString(R.string.error_0));
                return false;
            }
        }
        catch (IOException e) {
            mainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
            return false;
        } catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }
    }

    public boolean getProjects(){

        if(!validateToken()) {
            mainActivity.changeFragment(MainActivity.TAG_RELOGIN, mainActivity);
            return false;
        }

        String actualToken = myPrefs.getString("actualToken", "");
        String serverAddress = myPrefs.getString("serverAddress", "");
        String[] prefixList = mainActivity.getResources().getStringArray(R.array.serverPrefixes);

        if(actualToken.equals("") || serverAddress.equals("")){
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }

        try {
            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpClient client = new DefaultHttpClient(httpParameters);
            URI website = new URI(serverAddress + "/v2.0/tenants");
            HttpGet request = new HttpGet();
            request.setURI(website);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader("X-Auth-Token", actualToken);

            HttpResponse httpResponse = client.execute(request);

            int status = httpResponse.getStatusLine().getStatusCode();

            if(status == 200){
                HttpEntity entity = httpResponse.getEntity();
                String responseString = EntityUtils.toString(entity);
                Log.d("Response (projects)", responseString);

                JSONObject myJSONObject;
                JSONArray tenantList;

                try {
                    myJSONObject = new JSONObject(responseString);
                    tenantList = myJSONObject.getJSONArray("tenants");

                    mainActivity.databaseAdapter.deleteProjectList();

                    for(int index = 0; index < tenantList.length(); index++){
                        JSONObject tenant = tenantList.getJSONObject(index);
                        String tenantID, tenantName;

                        //mainActivity.databaseAdapter.insertUser(1,"test", "testmail", "testprojekt");

                        tenantID = tenant.getString("id");
                        tenantName = tenant.getString("name");

                        mainActivity.databaseAdapter.insertProject(tenantID, tenantName);
                        //Log.d("User"+index, user.getString("name"));
                    }

                    return true;

                } catch (JSONException e) {
                    Log.e("RESTClient", ""+status+e);
                    mainActivity.showSnackbar(Context.getString(R.string.error_0));
                    return false;
                }
            } else{
                Log.e("RESTClient", "" + status);
                mainActivity.showSnackbar(Context.getString(R.string.error_0));
                return false;
            }
        }
        catch (IOException e) {
            mainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
            return false;
        } catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }
    }

    public boolean postUser(String projectID, String userName, String userMail, String userPassword, Boolean userEnabled){

        if(!validateToken()) {
            mainActivity.changeFragment(MainActivity.TAG_RELOGIN, mainActivity);
            return false;
        }

        String actualToken = myPrefs.getString("actualToken", "");
        String serverAddress = myPrefs.getString("serverAddress", "");
        String[] prefixList = mainActivity.getResources().getStringArray(R.array.serverPrefixes);

        if(actualToken.equals("") || serverAddress.equals("")){
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }

        try {
            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpClient client = new DefaultHttpClient(httpParameters);
            URI website = new URI(serverAddress + "/v2.0/users/");
            HttpPost request = new HttpPost();
            request.setURI(website);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader("X-Auth-Token", actualToken);

            JSONObject user = new JSONObject();
            JSONObject userinfo = new JSONObject();

            userinfo.put("name", userName);
            userinfo.put("email", userMail);
            userinfo.put("password", userPassword);
            userinfo.put("enabled", userEnabled);
            userinfo.put("tenantId", projectID);

            user.put("user", userinfo);

            request.setEntity(new StringEntity(user.toString()));

            HttpResponse httpResponse = client.execute(request);

            int status = httpResponse.getStatusLine().getStatusCode();

            if(status == 200){
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_creationSuccess));
                return true;
            } else{
                Log.e("RESTClient", "" + status);
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_creationFail));
                return false;
            }
        }
        catch (IOException e) {
            mainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
            return false;
        } catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        } catch (JSONException e) {
            Log.e("RESTClient", e.toString());
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }
    }

    public boolean updateUser(String userID, String projectID, String userName, String userMail, String userPassword, Boolean userEnabled){

        if(!validateToken()) {
            mainActivity.changeFragment(MainActivity.TAG_RELOGIN, mainActivity);
            return false;
        }

        String actualToken = myPrefs.getString("actualToken", "");
        String serverAddress = myPrefs.getString("serverAddress", "");
        String[] prefixList = mainActivity.getResources().getStringArray(R.array.serverPrefixes);

        if(actualToken.equals("") || serverAddress.equals("")){
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }

        try {
            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpClient client = new DefaultHttpClient(httpParameters);
            URI website = new URI(serverAddress + "/v2.0/users/"+userID);
            HttpPut request = new HttpPut();
            request.setURI(website);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader("X-Auth-Token", actualToken);

            JSONObject user = new JSONObject();
            JSONObject userinfo = new JSONObject();

            userinfo.put("name", userName);
            userinfo.put("email", userMail);
            if(userPassword != null)
                userinfo.put("password", userPassword);
            userinfo.put("enabled", userEnabled);
            userinfo.put("tenantId", projectID);

            user.put("user", userinfo);

            request.setEntity(new StringEntity(user.toString()));

            HttpResponse httpResponse = client.execute(request);

            int status = httpResponse.getStatusLine().getStatusCode();

            if(status == 200){
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_editUser_updateSuccess));
                return true;
            } else{
                Log.e("RESTClient", "" + status);
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_editUser_updateFail));
                return false;
            }
        }
        catch (IOException e) {
            mainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
            return false;
        } catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        } catch (JSONException e) {
            Log.e("RESTClient", e.toString());
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }
    }

    public boolean deleteUser(String userID){

        if(!validateToken()) {
            mainActivity.changeFragment(MainActivity.TAG_RELOGIN, mainActivity);
            return false;
        }

        String actualToken = myPrefs.getString("actualToken", "");
        String serverAddress = myPrefs.getString("serverAddress", "");
        String[] prefixList = mainActivity.getResources().getStringArray(R.array.serverPrefixes);

        if(actualToken.equals("") || serverAddress.equals("")){
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }

        try {
            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpClient client = new DefaultHttpClient(httpParameters);
            URI website = new URI(serverAddress + "/v2.0/users/"+userID);
            HttpDelete request = new HttpDelete();
            request.setURI(website);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");
            request.setHeader("X-Auth-Token", actualToken);

            HttpResponse httpResponse = client.execute(request);

            int status = httpResponse.getStatusLine().getStatusCode();

            if(status == 204){
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_creationSuccess));
                return true;
            }else{
                Log.e("RESTClient", "" + status);
                mainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_creationFail));
                return false;
            }
        }
        catch (IOException e) {
            mainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
            return false;
        } catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
            mainActivity.showSnackbar(Context.getString(R.string.error_0));
            return false;
        }
    }
}

