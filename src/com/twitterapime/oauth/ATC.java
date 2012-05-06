package com.twitterapime.oauth;

import impl.android.com.twitterapime.xauth.ui.WebViewOAuthDialogWrapper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.twitterapime.rest.Credential;
import com.twitterapime.rest.Timeline;
import com.twitterapime.rest.UserAccountManager;
import com.twitterapime.search.Query;
import com.twitterapime.search.QueryComposer;
import com.twitterapime.search.SearchDeviceListener;
import com.twitterapime.search.Tweet;
import com.twitterapime.xauth.Token;
import com.twitterapime.xauth.ui.OAuthDialogListener;

public class ATC extends Activity implements OAuthDialogListener {
	
	private final int ACTIVITY_END = -1;	
	private final int ACTIVITY_REFRESH = 0;
	private final int TIMELINE_ACTIVITY = 0;
	private final int SEARCH_ACTIVITY = 1;
	private final int TWEET_ACTIVITY = 2;

	private final String CONSUMER_KEY = "YP6fMhYF1QkPi0slhXiJA";

	private final String CONSUMER_SECRET = "FWi27hEYJSTzpEq6ZxddMODNKOH9Qs4SyTL2DPbHss";

	private final String CALLBACK_URL = "http://ahumellihuk.com";

	/** Authorised Timeline instance object*/
	private Timeline timeline;

	/**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Token accessToken = getToken();
        if (accessToken == null) {
	        setContentView(R.layout.login);
	        
	        Button login = (Button)findViewById(R.id.button1);
	        login.setOnClickListener(new OnClickListener() {
	        	public void onClick(View v) {  
	        		launchWeb();
	        	}  
	        });
        }
        else {        	
        	Credential c = new Credential(CONSUMER_KEY, CONSUMER_SECRET, accessToken);
    		UserAccountManager uam = UserAccountManager.getInstance(c);
    		try {
    			if (uam.verifyCredential()) {
    				timeline = Timeline.getInstance(uam);
    				mainScreen();
    			}
    		} catch (Exception e) {
    			showMessage("Error authorising.");
    		} 
        }
    }
    /**
     * Retrieves access token from SharedPreferences
     */
    private Token getToken() {
    	SharedPreferences prefs = getSharedPreferences("ATC", MODE_PRIVATE);
    	String token = prefs.getString("AccessToken", null);
    	String secret = prefs.getString("AccessSecret", null);
    	if (token != null && secret != null) {
    		Token accessToken = new Token(token, secret);
    		return accessToken;
    	}
    	else return null;
    }
    
    /**
     * Stores the existing access token in SharedPreferences
     * @param accessToken User Access Token
     */
    private void storeToken(Token accessToken) {
    	SharedPreferences prefs = getSharedPreferences("ATC", MODE_PRIVATE);
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.putString("AccessToken", accessToken.getToken());
    	editor.putString("AccessSecret", accessToken.getSecret());
    	editor.commit();
    }
    
    /**
     * Launches webView to authorise user account
     */
    public void launchWeb() {
    	WebView webView = new WebView(this);
        setContentView(webView);
        
        WebViewOAuthDialogWrapper pageWrapper =
        	new WebViewOAuthDialogWrapper(webView);
        
		pageWrapper.setConsumerKey(CONSUMER_KEY);
		pageWrapper.setConsumerSecret(CONSUMER_SECRET);
		pageWrapper.setCallbackUrl(CALLBACK_URL);
		pageWrapper.setOAuthListener(this);
		//
		pageWrapper.login();  
    }

	/**
	 * @see com.twitterapime.xauth.ui.OAuthDialogListener#onAuthorize(com.twitterapime.xauth.Token)
	 */
	public void onAuthorize(Token accessToken) {
		storeToken(accessToken);
		Credential c = new Credential(CONSUMER_KEY, CONSUMER_SECRET, accessToken);
		UserAccountManager uam = UserAccountManager.getInstance(c);
		//
		try {
			if (uam.verifyCredential()) {
				timeline = Timeline.getInstance(uam);
				mainScreen();
			}
		} catch (Exception e) {
			showMessage("Error authorising.");
		}
	}

	/**
	 * @see com.twitterapime.xauth.ui.OAuthDialogListener#onAccessDenied(java.lang.String)
	 */
	public void onAccessDenied(String message) {
		showMessage("Access denied!");
	}

	/**
	 * @see com.twitterapime.xauth.ui.OAuthDialogListener#onFail(java.lang.String, java.lang.String)
	 */
	public void onFail(String error, String message) {
		showMessage("Error by authenticating user!");
	}	

	/**
	 * Displays the main screen
	 */
	public void mainScreen() {
		setContentView(R.layout.main);
		
		//Tweet button
		Button first = (Button)findViewById(R.id.goToTweet);
		first.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ATC.this, TweetActivity.class);
				startActivityForResult(i, TWEET_ACTIVITY);				
			}			
		});
		//Timeline button
		Button second = (Button)findViewById(R.id.goToTimeline);
		second.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				loadTimeline();				
			}			
		});
		//Search button
		Button third = (Button)findViewById(R.id.goToSearch);
		third.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ATC.this, SearchScreenActivity.class);
				startActivityForResult(i, SEARCH_ACTIVITY);			
			}			
		});
	}

	/**
	 * Loads five latest tweets from home timeline
	 */
	public void loadTimeline() {
		setContentView(R.layout.load);
		ProgressBar loading = (ProgressBar)findViewById(R.id.loading);
		loading.animate();
		Query query = QueryComposer.count(5);
		timeline.startGetHomeTweets(query, new SearchDeviceListener() {
			Tweet[] tweetArray = new Tweet[5];
			int i = 0;
			/**
			 * Executed at the end of search
			 */
			public void searchCompleted() {
				Intent i = new Intent(ATC.this, TimelineActivity.class);
				i.putExtra("tweet", tweetArray);
				startActivityForResult(i, TIMELINE_ACTIVITY);
			}
			
			/**
			 * Executed if search is failed
			 */
			public void searchFailed(Throwable arg0) {
				showMessage("Search failed!");		
			}
			
			/**
			 * Executed when a tweet is found
			 * @param tweet Found tweet
			 */ 
			public void tweetFound(Tweet tweet) {
				tweetArray[i] = tweet;
				i++;
			}
		});
	}	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case TIMELINE_ACTIVITY: {
				if (resultCode == ACTIVITY_REFRESH)
					loadTimeline();
				else if (resultCode == ACTIVITY_END)
					mainScreen();
				break;
			}
			case SEARCH_ACTIVITY: {
				if (resultCode == ACTIVITY_REFRESH) {
					Intent i = new Intent(ATC.this, SearchScreenActivity.class);
					startActivityForResult(i, SEARCH_ACTIVITY);	
				}
				else if (resultCode == ACTIVITY_END)
					mainScreen();
				break;
			}
			case TWEET_ACTIVITY: {
				mainScreen();
				break;
			}
		}
	}
		
	/**
	 * Shows a dialog window
	 * @param msg Text passed to window
	 */
	private void showMessage(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		//
		builder.create().show();
	}
}