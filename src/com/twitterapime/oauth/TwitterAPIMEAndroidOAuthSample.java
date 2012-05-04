package com.twitterapime.oauth;

import java.io.IOException;

import impl.android.com.twitterapime.xauth.ui.WebViewOAuthDialogWrapper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.rest.Credential;
import com.twitterapime.rest.Timeline;
import com.twitterapime.rest.TweetER;
import com.twitterapime.rest.UserAccountManager;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Query;
import com.twitterapime.search.QueryComposer;
import com.twitterapime.search.SearchDevice;
import com.twitterapime.search.SearchDeviceListener;
import com.twitterapime.search.Tweet;
import com.twitterapime.xauth.Token;
import com.twitterapime.xauth.ui.OAuthDialogListener;

public class TwitterAPIMEAndroidOAuthSample extends Activity implements OAuthDialogListener, SearchDeviceListener {

	private final String CONSUMER_KEY = "YP6fMhYF1QkPi0slhXiJA";

	private final String CONSUMER_SECRET = "FWi27hEYJSTzpEq6ZxddMODNKOH9Qs4SyTL2DPbHss";

	private final String CALLBACK_URL = "http://ahumellihuk.com";
	
	/** Authorised TweetER instance object*/
	private TweetER tweeter;
	/** Authorised Timeline instance object*/
	private Timeline timeline;
	/** Array to keep latest tweets*/
	private Tweet[] tweets;
	/** Counter to go through TextViews*/
	private int i;

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
    		//
    		try {
    			if (uam.verifyCredential()) {
    				tweeter = TweetER.getInstance(uam);
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
				tweeter = TweetER.getInstance(uam);
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
				tweetScreen();				
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
				searchScreen();				
			}			
		});
	}
	/**
	 * Displays tweet screen
	 */
	public void tweetScreen() {
		setContentView(R.layout.tweet);
		
		//Submit button
		Button submit = (Button)findViewById(R.id.submitTweet);
		submit.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				EditText textField = (EditText)findViewById(R.id.tweetField);
				String tweet = textField.getText().toString();
				try {
					tweeter.post(new Tweet(tweet));
					showMessage("Tweet posted successfully!");
					textField.setText(R.string.message);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (LimitExceededException e) {
					showMessage("Please enter no more than 140 symbols!");
					e.printStackTrace();
				}
			}
			
		});
		//Remove text from the field on touch
		final EditText editText = (EditText)findViewById(R.id.tweetField);
		editText.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {
				editText.setText("");
				return false;
			}
			
		});
		//Back button
		Button back = (Button)findViewById(R.id.backFromTweet);
		back.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				mainScreen();
			}
			
		});
	}
	
	public void searchScreen() {
		setContentView(R.layout.search);
		
		Button submit = (Button)findViewById(R.id.searchButton);
		submit.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				EditText field = (EditText)findViewById(R.id.searchField);
				CheckBox usernameCheck = (CheckBox)findViewById(R.id.usernameCheck);
				if (usernameCheck.isChecked()) {
					EditText usernameField = (EditText)findViewById(R.id.username);
					String username = usernameField.getText().toString();
					SearchDevice s = SearchDevice.getInstance();
					Query q = QueryComposer.append(QueryComposer.containAll("Java"),QueryComposer.from(username));
					
					try {
						tweets = s.searchTweets(q);
						timelineScreen();
					} catch (IOException e) {
						Log.w("IOException", "IOException");
						e.printStackTrace();
					} catch (LimitExceededException e) {
						Log.w("LimitExceededException", "LimitExceededException");
						e.printStackTrace();
					}	
				}
				else {
					SearchDevice s = SearchDevice.getInstance();
					Query q = QueryComposer.containAll(field.getText().toString());
					try {
						tweets = s.searchTweets(q);
						timelineScreen();
					} catch (IOException e) {
						Log.w("IOException", "IOException");
						e.printStackTrace();
					} catch (LimitExceededException e) {
						Log.w("LimitExceededException", "LimitExceededException");
						e.printStackTrace();
					}	
				}
			}
			
		});
		
		//Remove text from the field on touch
		final EditText editText = (EditText)findViewById(R.id.searchField);
		editText.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {
				editText.setText("");
				return false;
			}
					
		});
		
		//Back button
		Button back = (Button)findViewById(R.id.backFromSearch);
		back.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				mainScreen();
			}
			
		});
		
	}
	
	/**
	 * Loads five latest tweets from home timeline
	 */
	public void loadTimeline() {
		Query query = QueryComposer.count(5);
		i = 0;
		tweets = new Tweet[5];
		timeline.startGetHomeTweets(query, this);
	}
	
	/**
	 * Displays five latest tweets from home timeline
	 */
	public void timelineScreen() {
		setContentView(R.layout.timeline); //App gets to this line and does not go any further	

		TextView text1 = (TextView)findViewById(R.id.tweet1);
		text1.setText((String)tweets[0].getObject(MetadataSet.TWEET_CONTENT));
		TextView text2 = (TextView)findViewById(R.id.tweet2);
		text2.setText((String)tweets[1].getObject(MetadataSet.TWEET_CONTENT));
		TextView text3 = (TextView)findViewById(R.id.tweet3);
		text3.setText((String)tweets[2].getObject(MetadataSet.TWEET_CONTENT));
		TextView text4 = (TextView)findViewById(R.id.tweet4);
		text4.setText((String)tweets[3].getObject(MetadataSet.TWEET_CONTENT));
		TextView text5 = (TextView)findViewById(R.id.tweet5);
		text5.setText((String)tweets[4].getObject(MetadataSet.TWEET_CONTENT));
		
		//Back button
		Button back = (Button)findViewById(R.id.backFromTimeline);
		back.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				mainScreen();
			}
			
		});
		//Refresh button
		Button refresh = (Button)findViewById(R.id.newSearch);
		refresh.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				searchScreen();
			}
		});
	}
	
	/**
	 * Executed at the end of search
	 */
	public void searchCompleted() {
		timelineScreen();
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
		String content = (String)tweet.getObject(MetadataSet.TWEET_CONTENT);
		tweets[i] = tweet;
		i++;
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