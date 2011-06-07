package com.rdio.example;



import java.io.BufferedReader;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.utils.URLEncodedUtils;

public class Example {
	private static final String CONSUMER_KEY = "XYZ";
	private static final String CONSUMER_SECRET = "ABC";
	
	private static final String REQUEST_TOKEN = "http://api.rdio.com/oauth/request_token";
	private static final String ACCESS_TOKEN = "http://api.rdio.com/oauth/access_token";
	private static final String AUTHORIZE = "https://www.rdio.com/oauth/authorize";
	private static final String ENDPOINT = "http://api.rdio.com/1/";

	public static void main(String[] args) throws Exception {
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        OAuthProvider provider = new CommonsHttpOAuthProvider(REQUEST_TOKEN, ACCESS_TOKEN, AUTHORIZE);

        System.out.println("Fetching request token from Rdio...");

        // we do not support callbacks, thus pass OOB
        String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);

        System.out.println("Request token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());

        System.out.println("Now visit:\n" + authUrl + "\n... and grant this app authorization");
        System.out.println("Enter the verification code and hit ENTER when you're done");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String code = br.readLine();

        System.out.println("Fetching access token from Rdio...");

        provider.retrieveAccessToken(consumer, code);

        System.out.println("Access token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());

        HttpPost request = new HttpPost(ENDPOINT);
        
        List<NameValuePair> request_args = new ArrayList<NameValuePair>();
        request_args.add(new BasicNameValuePair("method", "currentUser"));
        
        StringEntity body = new StringEntity(URLEncodedUtils.format(request_args, "UTF-8"));
        body.setContentType("application/x-www-form-urlencoded");
        request.setEntity(body);

        consumer.sign(request);

        System.out.println("sending currentUser request to Rdio");

        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);

        System.out.println("Response: " + response.getStatusLine().getStatusCode() + " "
                + response.getStatusLine().getReasonPhrase());
        
        if (response.getStatusLine().getStatusCode() == 200) {
        	InputStreamReader reader = new InputStreamReader(response.getEntity().getContent());
        	while(true) {
        		try {
                	char[] buf = new char[64*1024];
                	if (reader.read(buf) < 0) break;
        			System.out.print(new String(buf));
        		} catch(EOFException ex) {
        			break;
        		}
        	}
        }
        System.out.println("");
    }
}