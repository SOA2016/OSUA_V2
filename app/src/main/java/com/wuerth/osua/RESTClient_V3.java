package com.wuerth.osua;

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

public class RESTClient_V3 {
	
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
	public boolean getAuthentificationToken(String loginName, String loginPassword, String serverAddress, MainActivity mainActivity){

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
			mainActivity.showSnackbar("Unexpected Error");
			return false; //new Response_Item(404, e1.toString());
		}

		//return request.toString();
		
		try {

			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			// The default value is zero, that means the timeout is not used.
			int timeoutConnection = 10000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 10000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			
			HttpClient client = new DefaultHttpClient(httpParameters);
			URI website = new URI(serverAddress + "/v3/auth/tokens");
			HttpPost request = new HttpPost();

			request.setURI(website);
			request.setEntity(new StringEntity(jsonRequest.toString()));
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			
			HttpResponse response = client.execute(request);

			//return jsonRequest.toString();

			int status = response.getStatusLine().getStatusCode();
			
			HttpEntity e = response.getEntity();

			switch(status){
				case 201: {
                    Log.d("Response", EntityUtils.toString(e));
					return true;
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
                    mainActivity.showSnackbar("Unexpected Error");
                    return false;
                }
			}
		}
		 catch (IOException e) {
			 mainActivity.showSnackbar("Connection to server failed");
			 return false;
		} catch (URISyntaxException e) {
            Log.e("RESTClient", e.toString());
			mainActivity.showSnackbar("Unexpected Error");
			return false;
		}
	}

	/***
	 * Ruft die angelegten Tenants ab und gibt diese zurueck.
	 * @param Benutzereingaben
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
	 * @param Benutzereingaben
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


	/***
	 * Fuegt einen neuen Benutzer hinzu.
	 * @param Benutzereingaben
	 * @return
	 */
	public String addUser(String X_Auth_Token, String username, String password, String tenantID){
		
		HttpClient client = new DefaultHttpClient();
		try {
			URI website = new URI("http://143.93.246.220:35357/v2.0/users");
			HttpPost request = new HttpPost();
			request.setURI(website);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			request.setHeader("X-Auth-Token", X_Auth_Token);
			
			JSONObject user = new JSONObject();
			JSONObject userinfo = new JSONObject();
			
			userinfo.put("name", username);
			userinfo.put("password", password);
			userinfo.put("enabled", true);
			userinfo.put("tenantId", tenantID);
			
			user.put("user", userinfo);
			
			request.setEntity(new StringEntity(user.toString()));
			
			HttpResponse response = client.execute(request);
			
			int status = response.getStatusLine().getStatusCode();
			
			HttpEntity e = response.getEntity();
			if (status == 200){
				return EntityUtils.toString(e);
			}
			else{
				return response.getStatusLine().toString()+" "+EntityUtils.toString(e);
			}
			
			
		} catch (URISyntaxException e) {
			return e.toString();
		} catch (JSONException e) {
			return e.toString();
		} catch (UnsupportedEncodingException e) {
			return e.toString();
		} catch (ClientProtocolException e1) {
			return e1.toString();
		} catch (IOException e1) {
			return e1.toString();
		}
	}
	
	
	/***
	 * Luescht einen bestehenden Benutzer.
	 * @param Benutzereingaben
	 * @return
	 */
	public String deleteUser(String X_Auth_Token, String UserID){
		try {
			HttpClient client = new DefaultHttpClient();
			URI website = new URI("http://143.93.246.220:35357/v2.0/users/"+UserID);
			HttpDelete request = new HttpDelete();
			request.setURI(website);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Accept", "application/json");
			request.setHeader("X-Auth-Token", X_Auth_Token);
			
			HttpResponse response = client.execute(request);
			
			return response.getStatusLine().toString();
			
			
		} catch (URISyntaxException e) {
			return e.toString();
		} catch (ClientProtocolException e1) {
			return e1.toString();
		} catch (IOException e2) {
			return e2.toString();
		}
	}
	
	/***
	 * uendert das Passwort eines Benutzers.
	 * @param Benutzereingaben
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
}

