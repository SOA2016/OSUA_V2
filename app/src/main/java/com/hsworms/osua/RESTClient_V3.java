package com.hsworms.osua;


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

	RESTClient_V3(MainActivity mainActivity){
		this.mainActivity = mainActivity;
		myPrefs = mainActivity.getSharedPreferences("MyPrefs", MainActivity.MODE_PRIVATE);
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
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return response.getStatusLine().toString()+" "+EntityUtils.toString(e);
			} else{
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
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return response.getStatusLine().toString()+" "+EntityUtils.toString(e);
			} else{
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
		String serverPrefix = myPrefs.getString("serverPrefix", "");
		String loginUserDomain = myPrefs.getString("loginUserDomain", "");
        String loginProjectDomain = myPrefs.getString("loginProjectDomain", "");

        /* If you specify the user name, you must also specify the domain, by ID or name. */
        /* UserDomain is mandatory and will be set by default */
        if (loginUserDomain.equals("")) {
           loginUserDomain = "default";
        }

        /* If you specify the project, you must also specify the project-domain, by ID or name. */
        if (!loginProject.equals("") && loginProjectDomain.equals("")) {
            MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
            return false;
        }

        /* loginName, serverAdress and Password are mandatory */
		if(loginName.equals("") || serverAddress.equals("") || loginPassword.equals("")){
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}

		JSONObject jsonRequest = new JSONObject();

		JSONObject identity = new JSONObject();

		JSONObject auth = new JSONObject();
		JSONObject password = new JSONObject();
		JSONObject user = new JSONObject();

		JSONObject userdomain = new JSONObject();
        JSONObject domain = new JSONObject();

		JSONObject project = new JSONObject();
        JSONObject scope = new JSONObject();

		JSONArray methods = new JSONArray();

		try {

            userdomain.put("name", loginUserDomain);
            domain.put("name", loginProjectDomain);

            /* You cannot simultaneously scope a token to a project and domain.*/
            if (!loginProject.equals("")) {
                /* scope to project */
                /* If you specify the project by name, you must also specify the project domain to uniquely identify the project. */
                project.put("name", loginProject);
                project.put("domain", domain);
                scope.put("project", project);
                auth.put("scope", scope);
                MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_loginScopedProject));
            } else if (!loginProjectDomain.equals("")) {
                /* scope to domain */
                MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_loginScopedDomain));
                scope.put("domain", domain);
                auth.put("scope", scope);
            } else {
                /* unscoped request */
				auth.put("unscoped", "");
                MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_login_loginUnscoped));
            }



            /* Username+Password */
			user.put("name", loginName);
			user.put("password", loginPassword);
			password.put("user", user);
            user.put("domain", userdomain);

            /* Methods */
            methods.put("password");

            /* Add Username+Password and Methods to identity */
			identity.put("methods", methods);
			identity.put("password", password);

            /* Add Identity to auth*/
			auth.put("identity", identity);

            /* Add auth to jsonRequest */
			jsonRequest.put("auth", auth);
		} catch (JSONException e1) {
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false; //new Response_Item(404, e1.toString());
		}

		Log.d("JSON OBJEKT", jsonRequest.toString());

		//return request.toString();
		
		try {

			HttpParams httpParameters = new BasicHttpParams();

			int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI(serverPrefix + serverAddress + "/v3/auth/tokens");
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
			JSONObject token;
			JSONObject error;

			switch(status){
				case 201: {

					Header[] headers = response.getAllHeaders();
					for (Header header : headers)
					{

						String headername = header.getName();

						if ( headername.equals("X-Subject-Token"))
						{
							try
							{
								myJSONObject = new JSONObject(responseString);
								token = myJSONObject.getJSONObject("token");

								spEditor.putString("actualToken", header.getValue());
								spEditor.putString("actualTokenExpiresAt", token.getString("expires_at"));
								spEditor.apply();
								/* changed by Stephan Strissel, Marco Spiess, Damir Gricic */
								if (!validateToken()) { // reCheck if validateToken() is functional. Otherwise App won't behave properly
									return false;
								}
								return true;
							}
						 catch (JSONException e)
						 	{
								Log.e("RESTClient", ""+status);
								MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
								return false;
							}

						}


					}
					return false;
				}
				case 400: {
					MainActivity.showSnackbar(mainActivity.getString(R.string.error_400));
					return false;
				}
				case 401: {
					/*changed by Stephan Strissel, Marco Spiess, Damir Gricic */
					// get Error-Message from Server
					try {
						myJSONObject = new JSONObject(responseString);
						error = myJSONObject.getJSONObject("error");
						MainActivity.showSnackbar(error.getString("message"));
					} catch (Exception e) {
						Log.e("RESTClient", ""+status);
						MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
						return false;
					}
					return false;
				}
				case 403: {
					MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
					return false;
				}
                default: {
                    Log.e("RESTClient", ""+status);
                    MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                    return false;
                }
			}
		}
		 catch (IOException e) {
			 MainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
			 return false;
		} catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
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
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return response.getStatusLine().toString()+" "+EntityUtils.toString(e);
			} else{
				return response.getStatusLine().toString()+" "+EntityUtils.toString(e);
			}

		} catch (UnsupportedEncodingException e) {
			return e.toString();
		} catch (ClientProtocolException e) {
			return e.toString();
		} catch (IOException e) {
			return mainActivity.getString(R.string.error_2);
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

			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return response.getStatusLine().toString()+" "+EntityUtils.toString(e);
			} else {
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
    * Created by Stephan Strissel, Marco Spiess, Damir Gricic on 09.06.2016.
    * receives Projects from Server and writes them into databaseAdapter
     */
	public boolean getProjects()
	{
		if(!validateToken()) {
			mainActivity.changeFragment(MainActivity.TAG_RELOGIN, mainActivity);
			return false;
		}

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		String serverPrefix = myPrefs.getString("serverPrefix", "");

		if(actualToken.equals("") || serverAddress.equals("")){
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}
        try {
            HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            HttpClient client = new DefaultHttpClient(httpParameters);
            URI website = new URI(serverPrefix + serverAddress + "/v3/projects");
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
                Log.d("Response (projectlist)", responseString);

                JSONObject myJSONObject;
                JSONArray projectList;

                try {
                    myJSONObject = new JSONObject(responseString);
                    projectList = myJSONObject.getJSONArray("projects");

                    mainActivity.databaseAdapter.deleteProjectList();

                    for(int index = 0; index < projectList.length(); index++){
                        JSONObject project = projectList.getJSONObject(index);
                        String projectID, projectName;

                        projectID = project.getString("id");
                        projectName = project.getString("name");


                        mainActivity.databaseAdapter.insertProject(projectID, projectName);
                    }

                    return true;

                } catch (JSONException e) {
                    Log.e("RESTClient", ""+status+e);
                    MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                    return false;
                }
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return false;
            } else{
                Log.e("RESTClient", "" + status);
                MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                return false;
            }
        }
        catch (IOException e) {
            MainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
            return false;
        } catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
            MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
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
		String serverPrefix = myPrefs.getString("serverPrefix", "");

		if(actualToken.equals("") || serverAddress.equals("")){
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}

		try {
			HttpParams httpParameters = new BasicHttpParams();

			int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI(serverPrefix + serverAddress + "/v3/users");
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
						String userID, userName, userMail, userProject, userDescription;
						Boolean userEnabled;

						userID = user.getString("id");
						userName = user.getString("name");
						userEnabled = user.getBoolean("enabled");

                        try {
                            userDescription = user.getString("description");
                        }catch (Exception e){
                            // No Description for this user
                            userDescription = "";
                        }

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

						mainActivity.databaseAdapter.insertUser(userID, userName, userMail, userProject, userEnabled, userDescription);
						//Log.d("User"+index, user.getString("name"));
					}

					return true;

				} catch (JSONException e) {
					Log.e("RESTClient", ""+status+e);
					MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
					return false;
				}
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return false;
			} else{
				Log.e("RESTClient", "" + status);
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
				return false;
			}
		}
		catch (IOException e) {
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}
	}

	/*
    * Created by Stephan Strissel, Marco Spiess, Damir Gricic on 17.07.2016.
    * receives Domains from Serve
     */
	public JSONArray getDomains()
	{

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		String serverPrefix = myPrefs.getString("serverPrefix", "");

		if(actualToken.equals("") || serverAddress.equals("")){
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return new JSONArray();
		}
		try {
			HttpParams httpParameters = new BasicHttpParams();

			int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI(serverPrefix + serverAddress + "/v3/domains");
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
				Log.d("Response (domainlist)", responseString);

				JSONObject myJSONObject;
				JSONArray domainList;

				try {
					myJSONObject = new JSONObject(responseString);
					domainList = myJSONObject.getJSONArray("domains");

					return domainList;

				} catch (JSONException e) {
					Log.e("RESTClient", ""+status+e);
					MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
					return new JSONArray();
				}
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return new JSONArray();
			} else{
				Log.e("RESTClient", "" + status);
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
				return new JSONArray();
			}
		}
		catch (IOException e) {
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
			return new JSONArray();
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return new JSONArray();
		}
	}

	public boolean getUser(String ID){

		if(!validateToken()) {
			mainActivity.changeFragment(MainActivity.TAG_RELOGIN, mainActivity);
			return false;
		}

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		String serverPrefix = myPrefs.getString("serverPrefix", "");

		if(actualToken.equals("") || serverAddress.equals("")){
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}

		try {
			HttpParams httpParameters = new BasicHttpParams();

			int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI(serverPrefix + serverAddress + "/v3/users/"+ID);
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
					String userID, userName, userMail, userProject, userDescription;
					Boolean userEnabled;

					//mainActivity.databaseAdapter.insertUser(1,"test", "testmail", "testprojekt");

					userID = user.getString("id");
					userName = user.getString("name");
					userEnabled = user.getBoolean("enabled");

                    try {
                        userDescription = user.getString("description");
                    }catch (Exception e)
                    {
                        userDescription = "";
                    }

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
					mainActivity.databaseAdapter.insertUser(userID, userName, userMail, userProject, userEnabled, userDescription);
					//Log.d("User"+index, user.getString("name"));
					//}

					return true;

				} catch (JSONException e) {
					Log.e("RESTClient", ""+status+e);
					MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
					return false;
				}
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return false;
			} else{

				Log.e("RESTClient", "" + status);
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
				return false;
			}
		}
		catch (IOException e) {
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
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
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return response.getStatusLine().toString();
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
		String serverPrefix = myPrefs.getString("serverPrefix", "");

		if(actualToken.equals("") || serverAddress.equals("")){
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}

		try {
			HttpParams httpParameters = new BasicHttpParams();

			int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI(serverPrefix + serverAddress + "/v2.0/tokens/"+actualToken);
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
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return false;
			}else{
				Log.e("RESTClient", "" + status);
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_3));
				return false;
			}
		}
		catch (IOException e) {
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_4));
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}
	}

	public boolean updateUser(String userID, String projectID, String userName, String userMail, String userPassword, String userDescription, Boolean userEnabled){

		if(!validateToken()) {
			mainActivity.changeFragment(MainActivity.TAG_RELOGIN, mainActivity);
			return false;
		}

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		String serverPrefix = myPrefs.getString("serverPrefix", "");

		if(actualToken.equals("") || serverAddress.equals("")){
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}

		try {
			HttpParams httpParameters = new BasicHttpParams();

			int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI(serverPrefix + serverAddress + "/v3/users/"+userID);
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
            userinfo.put("description", userDescription);

			user.put("user", userinfo);

			request.setEntity(new StringEntity(user.toString()));

			HttpResponse httpResponse = client.execute(request);

			int status = httpResponse.getStatusLine().getStatusCode();

			if(status == 200){
				MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_editUser_updateSuccess));
				return true;
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return false;
			} else{
				Log.e("RESTClient", "" + status);
				MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_editUser_updateFail));
				return false;
			}
		}
		catch (IOException e) {
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		} catch (JSONException e) {
			Log.e("RESTClient", e.toString());
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}
	}

	public boolean postUser(String projectID, String userName, String userMail, String userPassword, String userDescription, Boolean userEnabled){

		if(!validateToken()) {
			mainActivity.changeFragment(MainActivity.TAG_RELOGIN, mainActivity);
			return false;
		}

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		String serverPrefix = myPrefs.getString("serverPrefix", "");

		if(actualToken.equals("") || serverAddress.equals("")){
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}

		try {
			HttpParams httpParameters = new BasicHttpParams();

			int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI(serverPrefix + serverAddress + "/v3/users/");
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
            userinfo.put("description", userDescription);

			user.put("user", userinfo);

			request.setEntity(new StringEntity(user.toString()));

			HttpResponse httpResponse = client.execute(request);

			int status = httpResponse.getStatusLine().getStatusCode();

			if(status == 201){
				MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_creationSuccess));
				return true;
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return false;
			} else{
				Log.e("RESTClient", "" + status);
				MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_creationFail));
				return false;
			}
		}
		catch (IOException e) {
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		} catch (JSONException e) {
			Log.e("RESTClient", e.toString());
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
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
		String serverPrefix = myPrefs.getString("serverPrefix", "");

		if(actualToken.equals("") || serverAddress.equals("")){
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}

		try {
			HttpParams httpParameters = new BasicHttpParams();

			int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI(serverPrefix + serverAddress + "/v3/users/"+userID);
			HttpDelete request = new HttpDelete();
			request.setURI(website);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			request.setHeader("X-Auth-Token", actualToken);

			HttpResponse httpResponse = client.execute(request);

			int status = httpResponse.getStatusLine().getStatusCode();

			if(status == 204){
				MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_creationSuccess));
				return true;
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return false;
			}else{
				Log.e("RESTClient", "" + status);
				MainActivity.showSnackbar(mainActivity.getString(R.string.fragment_addUser_creationFail));
				return false;
			}
		}
		catch (IOException e) {
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_2));
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}
	}

	public boolean validateToken(){

		String actualToken = myPrefs.getString("actualToken", "");
		String serverAddress = myPrefs.getString("serverAddress", "");
		String serverPrefix = myPrefs.getString("serverPrefix", "");

		if(actualToken.equals("") || serverAddress.equals("")){
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}

		try {
			HttpParams httpParameters = new BasicHttpParams();

			int timeoutConnection = 10000;	// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;	// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI( serverPrefix + serverAddress + "/v3/auth/tokens");
			HttpGet request = new HttpGet();
			request.setURI(website);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			request.setHeader("X-Auth-Token", actualToken);
			request.setHeader("X-Subject-Token", actualToken);

			HttpResponse httpResponse = client.execute(request);

			int status = httpResponse.getStatusLine().getStatusCode();

			if(status == 200){
                HttpEntity entity = httpResponse.getEntity();
                String responseString = EntityUtils.toString(entity);
                Log.d("Response (editUser)", responseString);

                JSONObject response;
                JSONObject token;
                String expires_at;
                try {
                    response = new JSONObject(responseString);
                    token  = response.getJSONObject("token");
                    expires_at = token.getString("expires_at");
                    spEditor.putString("actualTokenExpiresAt", expires_at );
                } catch (JSONException e) {
                    Log.e("RESTClient", ""+status+e);
                    MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
                    return false;
                }

				return true;
			}else if(status == 403) {
				MainActivity.showSnackbar(mainActivity.getString(R.string.error_403));
				return false; // The identity was successfully authenticated but it is not authorized to perform the requested action, so token is not valid
			}else{
				Log.e("RESTClient", "" + status);
				spEditor.putString("actualToken", null);
				spEditor.putString("actualTokenExpiresAt", null);
				spEditor.apply();
				return false;
			}
		}
		catch (IOException e) {
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		} catch (URISyntaxException e) {
			Log.e("RESTClient", e.toString());
			MainActivity.showSnackbar(mainActivity.getString(R.string.error_0));
			return false;
		}
	}
}

