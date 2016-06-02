package com.wuerth.osua;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.apache.http.Header;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpPatch;

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

public class RESTClient_V3 extends RESTClient {

	MainActivity mainActivity;
	SharedPreferences myPrefs;
	SharedPreferences.Editor spEditor;
    Context Context;

	RESTClient_V3(MainActivity mainActivity){
		this.mainActivity = mainActivity;
		myPrefs = mainActivity.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
		spEditor = myPrefs.edit();
	}

	/***
	 * Holt sich die Liste der aktivierten Extensions.
	 * Diese Methode kann als Funktionstest genutzt werden.
	 * @return Returns Extensions as String
	 * @throws Exception
	 */

	public String getKeystoneExtensions() throws Exception{
		try{
			HttpClient client = new DefaultHttpClient();
			URI website = new URI("http://143.93.246.220:35357/v2.0/extensions");
			HttpGet request = new HttpGet();
			request.setURI(website);
			HttpResponse response = client.execute(request);

			int status = response.getStatusLine().getStatusCode();

			HttpEntity e = response.getEntity();
			if (status == 200){
				return EntityUtils.toString(e);
			}
			else{
				return response.getStatusLine().toString()+" "+EntityUtils.toString(e);
			}

		}
		catch(Exception e){
			return e.toString();
		}
	}

	public String getKeystone() throws Exception{
		try{
			HttpClient client = new DefaultHttpClient();
			URI website = new URI("http://143.93.246.220:35357/v2.0/extensions");
			HttpGet request = new HttpGet();
			request.setURI(website);
			HttpResponse response = client.execute(request);

			int status = response.getStatusLine().getStatusCode();

			HttpEntity e = response.getEntity();
			if (status == 200){
				return EntityUtils.toString(e);
			}
			else{
				return response.getStatusLine().toString()+" "+EntityUtils.toString(e);
			}
		}
		catch(Exception e){
			return e.toString();
		}
	}
	/***
	 * Holt sich ein Authentifizierungstoken vom Server und gibt dies zurueck.
	 * @return Returns the Authentification-Token from Keystone
	 * @throws URISyntaxException
	 */
	public boolean getAuthentificationToken(String loginPassword){

		String loginName = myPrefs.getString("loginName", "");
		String loginProject = myPrefs.getString("loginProject", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		int serverPrefix = myPrefs.getInt("serverPrefix", 0);
		String[] prefixList = mainActivity.getResources().getStringArray(R.array.serverPrefixes);

		if(loginName.equals("") || loginProject.equals("") || serverAddress.equals("")){
			mainActivity.showSnackbar(Context.getString(R.string.error_0));
			return false;
		}

		JSONObject jsonRequest = new JSONObject();
		JSONObject identity = new JSONObject();
		JSONObject auth = new JSONObject();
		JSONObject password = new JSONObject();
		JSONObject user = new JSONObject();
		JSONObject domain = new JSONObject();

		JSONArray methods = new JSONArray();
		methods.put("password");

		try {
			domain.put("id", "default");

			user.put("name", loginName);
			user.put("password", loginPassword);
			user.put("domain", domain);

			password.put("user", user);

			identity.put("methods", methods);
			identity.put("password", password);
			
			auth.put("identity", identity);
			jsonRequest.put("auth", auth);
		} catch (JSONException e1) {
			mainActivity.showSnackbar(Context.getString(R.string.error_0));
			return false; //new Response_Item(404, e1.toString());
		}

		//return request.toString();
		
		try {

			HttpParams httpParameters = new BasicHttpParams();

			int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI(prefixList[serverPrefix]+serverAddress + "/v3/auth/tokens");
			HttpPost request = new HttpPost();

			request.setURI(website);
			request.setEntity(new StringEntity(jsonRequest.toString()));
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");

			HttpResponse response = client.execute(request);

			//return jsonRequest.toString();

			int status = response.getStatusLine().getStatusCode();
			
			HttpEntity entity = response.getEntity();

			String responseString = EntityUtils.toString(entity);
			Log.d("Response (auth)", responseString);

			JSONObject myJSONObject;
			JSONObject access;
			JSONObject token;

			switch(status){
				case 201: {

					Header[] headers = response.getAllHeaders();
					for (Header header : headers)
					{

						String headername = new String(header.getName());

						if ( headername.equals("X-Subject-Token"))
						{
							try
							{
								myJSONObject = new JSONObject(responseString);
								token = myJSONObject.getJSONObject("token");

								spEditor.putString("actualToken", header.getValue());
								spEditor.putString("actualTokenExpiresAt", token.getString("expires_at"));
								spEditor.apply();
								return true;
							}
						 catch (JSONException e)
						 	{
								Log.e("RESTClient", ""+status);
								mainActivity.showSnackbar(Context.getString(R.string.error_0));
								return false;
							}

						}


					}
					return false;
				}
				case 400: {
					mainActivity.showSnackbar("Please enter username and password");
					return false;
				}
				case 401: {
					mainActivity.showSnackbar("Incorrect username or password");
					return false;
				}
                default: {
                    Log.e("RESTClient", ""+status);
                    mainActivity.showSnackbar(Context.getString(R.string.error_0));
                    return false;
                }
			}
		}
		 catch (IOException e) {
			 mainActivity.showSnackbar("Connection to server failed");
			 return false;
		} catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
			mainActivity.showSnackbar(Context.getString(R.string.error_0));
			return false;
		}
	}

	/***
	 * Ruft die angelegten Tenants ab und gibt diese zurueck.
	 * @param
	 * @return
	 */
	public String listTenants(String X_Auth_Token){
		try {

			HttpClient client = new DefaultHttpClient();
			URI website = new URI("http://143.93.246.220:35357/v2.0/tenants");
			HttpGet request = new HttpGet();
			request.setURI(website);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			request.setHeader("X-Auth-Token", X_Auth_Token);

			HttpResponse response = client.execute(request);
			Integer status = response.getStatusLine().getStatusCode();
			Log.d("Status-Code: ", status.toString());

			HttpEntity e = response.getEntity();
			if (status == 200){
				return EntityUtils.toString(e);
			}
			else{
				return response.getStatusLine().toString()+" "+EntityUtils.toString(e);
			}

		} catch (UnsupportedEncodingException e) {
			return e.toString();
		} catch (ClientProtocolException e) {
			return e.toString();
		} catch (IOException e) {
			return "Es konnte keine Verbindung hergestellt werden";
		} catch (URISyntaxException e1) {
			return e1.toString();
		}
	}

	/***
	 * Ruft die angelegten Benutzer auf dem Server ab und gibt die Liste zurueck.
	 * @param
	 * @return Returns list of Users
	 */
	public String listUsers(String X_Auth_Token){
		try {
			
			HttpClient client = new DefaultHttpClient();
			URI website = new URI("http://143.93.246.220:35357/v2.0/users");
			HttpGet request = new HttpGet();
			request.setURI(website);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			request.setHeader("X-Auth-Token", X_Auth_Token);
			
			HttpResponse response = client.execute(request);
			int status = response.getStatusLine().getStatusCode();
			
			HttpEntity e = response.getEntity();
			if (status == 200){
				return EntityUtils.toString(e);
			}
			else{
				return response.getStatusLine().toString()+" "+EntityUtils.toString(e);
			}
			
		} catch (UnsupportedEncodingException e) {
			return e.toString();
		} catch (ClientProtocolException e) {
			return e.toString();
		} catch (IOException e) {
			return e.toString();
		} catch (URISyntaxException e1) {
			return e1.toString();
		}
	}

	/*
	not yet implemented
	 */
	public boolean getProjects()
	{
		return false;
	}

	public boolean getUsers(){

		if(!validateToken()) {
			Fragment_ReLogin fragment_reLogin = new Fragment_ReLogin();
			fragment_reLogin.show(mainActivity.getSupportFragmentManager(), "Relogin");
			return false;
		}

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		int serverPrefix = myPrefs.getInt("serverPrefix", 0);
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
			URI website = new URI(prefixList[serverPrefix]+serverAddress + "/v3/users");
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
							userProject = user.getString("default_project_id");
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
			mainActivity.showSnackbar("Connection to server failed");
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			mainActivity.showSnackbar(Context.getString(R.string.error_0));
			return false;
		}
	}

	public boolean getUser(String ID){

		if(!validateToken()) {
			Fragment_ReLogin fragment_reLogin = new Fragment_ReLogin();
			fragment_reLogin.show(mainActivity.getSupportFragmentManager(), "Relogin");
			return false;
		}

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		int serverPrefix = myPrefs.getInt("serverPrefix", 0);
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
			URI website = new URI(prefixList[serverPrefix]+serverAddress + "/v3/users/"+ID);
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
						userProject = user.getString("default_project_id");
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
			mainActivity.showSnackbar("Connection to server failed");
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			mainActivity.showSnackbar(Context.getString(R.string.error_0));
			return false;
		}
	}

	/***
	 * uendert das Passwort eines Benutzers.
	 * @param
	 * @return
	 */
	public String changePassword(String X_Auth_Token, String UserID, String Password){
		HttpClient client = new DefaultHttpClient();
		try {
			URI website = new URI("http://143.93.246.220:35357/v2.0/users/"+UserID);
			HttpPut request = new HttpPut();
			request.setURI(website);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			request.setHeader("X-Auth-Token", X_Auth_Token);
			
			JSONObject password = new JSONObject();
			JSONObject user = new JSONObject();
			password.put("password", Password);
			user.put("user", password);
			
			request.setEntity(new StringEntity(user.toString()));
			
			HttpResponse response = client.execute(request);
			
			int status = response.getStatusLine().getStatusCode();
			
			if (status == 200){
				HttpEntity e = response.getEntity();
				return EntityUtils.toString(e);
			}
			else{
				return response.getStatusLine().toString();
			}
			
		} catch (URISyntaxException e) {
			return e.toString();
		} catch (ClientProtocolException e) {
			return e.toString();
		} catch (IOException e) {
			return e.toString();
		} catch (JSONException e) {
			return e.toString();
		}
	}

	public boolean deleteToken(){

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		int serverPrefix = myPrefs.getInt("serverPrefix", 0);
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
				mainActivity.showSnackbar("Error. Token couldn't be deleted");
				return false;
			}
		}
		catch (IOException e) {
			mainActivity.showSnackbar("Server timeout. Token couldn't be deleted");
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			mainActivity.showSnackbar(Context.getString(R.string.error_0));
			return false;
		}
	}

	public boolean updateUser(String userID, String projectID, String userName, String userMail, String userPassword, Boolean userEnabled){

		if(!validateToken()) {
			Fragment_ReLogin fragment_reLogin = new Fragment_ReLogin();
			fragment_reLogin.show(mainActivity.getSupportFragmentManager(), "Relogin");
			return false;
		}

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		int serverPrefix = myPrefs.getInt("serverPrefix", 0);
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
			URI website = new URI(prefixList[serverPrefix]+serverAddress + "/v3/users/"+userID);
			HttpPatch request = new HttpPatch();
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
			userinfo.put("default_project_id", projectID);

			user.put("user", userinfo);

			request.setEntity(new StringEntity(user.toString()));

			HttpResponse httpResponse = client.execute(request);

			int status = httpResponse.getStatusLine().getStatusCode();

			if(status == 200){
				mainActivity.showSnackbar("User updated successfully");
				return true;
			} else{
				Log.e("RESTClient", "" + status);
				mainActivity.showSnackbar("Error. User couldn't be updated");
				return false;
			}
		}
		catch (IOException e) {
			mainActivity.showSnackbar("Connection to server failed");
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

	public boolean postUser(String projectID, String userName, String userMail, String userPassword, Boolean userEnabled){

		if(!validateToken()) {
			Fragment_ReLogin fragment_reLogin = new Fragment_ReLogin();
			fragment_reLogin.show(mainActivity.getSupportFragmentManager(), "Relogin");
			return false;
		}

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		int serverPrefix = myPrefs.getInt("serverPrefix", 0);
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
			URI website = new URI(prefixList[serverPrefix]+serverAddress + "/v3/users/");
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
			userinfo.put("default_project_id", projectID);

			user.put("user", userinfo);

			request.setEntity(new StringEntity(user.toString()));

			HttpResponse httpResponse = client.execute(request);

			int status = httpResponse.getStatusLine().getStatusCode();

			if(status == 201){
				mainActivity.showSnackbar("User created successfully");
				return true;
			} else{
				Log.e("RESTClient", "" + status);
				mainActivity.showSnackbar("Error. User not created");
				return false;
			}
		}
		catch (IOException e) {
			mainActivity.showSnackbar("Connection to server failed");
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
			Fragment_ReLogin fragment_reLogin = new Fragment_ReLogin();
			fragment_reLogin.show(mainActivity.getSupportFragmentManager(), "Relogin");
			return false;
		}

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		int serverPrefix = myPrefs.getInt("serverPrefix", 0);
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
			URI website = new URI(prefixList[serverPrefix]+serverAddress + "/v3/users/"+userID);
			HttpDelete request = new HttpDelete();
			request.setURI(website);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			request.setHeader("X-Auth-Token", actualToken);

			HttpResponse httpResponse = client.execute(request);

			int status = httpResponse.getStatusLine().getStatusCode();

			if(status == 204){
				return true;
			}else{
				Log.e("RESTClient", "" + status);
				mainActivity.showSnackbar("Error. User not created");
				return false;
			}
		}
		catch (IOException e) {
			mainActivity.showSnackbar("Connection to server failed");
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			mainActivity.showSnackbar(Context.getString(R.string.error_0));
			return false;
		}
	}

	public boolean validateToken(){

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		int serverPrefix = myPrefs.getInt("serverPrefix", 0);
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
			URI website = new URI(prefixList[serverPrefix]+serverAddress + "/v3/auth/tokens");
			HttpGet request = new HttpGet();
			request.setURI(website);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			request.setHeader("X-Auth-Token", actualToken);
			request.setHeader("X-Subject-Token", actualToken);

			HttpResponse httpResponse = client.execute(request);

			int status = httpResponse.getStatusLine().getStatusCode();

			if(status == 200){
				return true;
			}else if(status == 403) {
				mainActivity.showSnackbar("Please login with an admin account");
				mainActivity.changeFragment(mainActivity.getString(R.string.fragment_login), mainActivity);
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
			mainActivity.showSnackbar("Connection to server failed");
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			mainActivity.showSnackbar(Context.getString(R.string.error_0));
			return false;
		}
	}
}

