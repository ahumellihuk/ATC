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

public class ATC extends Activity implements OAuthDialogListener, SearchDeviceListener, OnTouchListener {

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
		 Button search = (Button)findViewById(R.id.searchButton);
		 search.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				EditText field = (EditText)findViewById(R.id.searchField);
				CheckBox usernameCheck = (CheckBox)findViewById(R.id.usernameCheck);
				EditText usernameField = (EditText)findViewById(R.id.username);
				CheckBox hashCheck = (CheckBox)findViewById(R.id.hashCheck);
				EditText hashtagField = (EditText)findViewById(R.id.hashField);
				String username = usernameField.getText().toString();
				String keywords = field.getText().toString();
				String hashtag = hashtagField.getText().toString();
				SearchDevice s = SearchDevice.getInstance();
				Query q;
				if (usernameCheck.isChecked() && !hashCheck.isChecked()) 
					q = QueryComposer.append(QueryComposer.containAll(keywords),QueryComposer.from(username));
				else if (hashCheck.isChecked() && !usernameCheck.isChecked())
					q = QueryComposer.append(QueryComposer.containAll(keywords), QueryComposer.containHashtag(hashtag));
				else if (usernameCheck.isChecked() && !hashCheck.isChecked() &&(keywords == "" || keywords == null))
					q = QueryComposer.from(username);
				else if (usernameCheck.isChecked() && hashCheck.isChecked()) {
					q = QueryComposer.append(QueryComposer.from(username), QueryComposer.containAll(keywords));
					q = QueryComposer.append(q, QueryComposer.containHashtag(hashtag));
				}					
				else
					q = QueryComposer.containAll(keywords);
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

		TextView [] textFields = {(TextView)findViewById(R.id.tweet1),(TextView)findViewById(R.id.tweet2),(TextView)findViewById(R.id.tweet3),
				(TextView)findViewById(R.id.tweet4),(TextView)findViewById(R.id.tweet5)};
		
		int n = tweets.length;
		if (n>5) n=5;
		
		switch (n) {
			case 0: {
				textFields[0].setVisibility(4);
				textFields[1].setVisibility(4);
				textFields[2].setVisibility(4);
				textFields[3].setVisibility(4);
				textFields[4].setVisibility(4);
				showMessage("Unfortunately, nothing found");
				break;
			}
			case 1: {
				textFields[0].setOnTouchListener(this);
				textFields[1].setVisibility(4);
				textFields[2].setVisibility(4);
				textFields[3].setVisibility(4);
				textFields[4].setVisibility(4);
				break;
			}
			case 2: {
				textFields[0].setOnTouchListener(this);
				textFields[1].setOnTouchListener(this);
				textFields[2].setVisibility(4);
				textFields[3].setVisibility(4);
				textFields[4].setVisibility(4);
				break;
			}
			case 3: {
				textFields[0].setOnTouchListener(this);
				textFields[1].setOnTouchListener(this);
				textFields[2].setOnTouchListener(this);
				textFields[3].setVisibility(4);
				textFields[4].setVisibility(4);
				break;
			}
			case 4: {
				textFields[0].setOnTouchListener(this);
				textFields[1].setOnTouchListener(this);
				textFields[2].setOnTouchListener(this);
				textFields[3].setOnTouchListener(this);
				textFields[4].setVisibility(4);
				break;
			}
			default: {
				textFields[0].setOnTouchListener(this);
				textFields[1].setOnTouchListener(this);
				textFields[2].setOnTouchListener(this);
				textFields[3].setOnTouchListener(this);
				textFields[4].setOnTouchListener(this);
				break;
			}
		}
		
		for (int i=0; i<n; i++) {
			textFields[i].setText((String)tweets[i].getObject(MetadataSet.TWEET_CONTENT));
		}
		
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
	
	public void detailScreen(final Tweet tweet) {
		setContentView(R.layout.detail);
		
		String details = (String)tweet.getObject(MetadataSet.TWEET_CONTENT);
		String author = (String)tweet.getObject(MetadataSet.TWEET_AUTHOR_USERNAME);
		String hashtag = (String)tweet.getObject(MetadataSet.TWEETENTITY_HASHTAG);
		
		TextView tweetDetails = (TextView)findViewById(R.id.tweetDetails);
		TextView authorView = (TextView)findViewById(R.id.author);
		TextView hashtagView = (TextView)findViewById(R.id.hashtag);
		tweetDetails.setText(details);
		authorView.setText("@"+author);
		hashtagView.setText("#"+hashtag);
		
		Button retweet = (Button)findViewById(R.id.retweet);
		retweet.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				try {
					tweeter.repost(tweet);
					showMessage("Tweet successfully reposted!");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LimitExceededException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			
		});
		
		//Back button
		Button back = (Button)findViewById(R.id.backFromDetail);
		back.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				timelineScreen();
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
	public boolean onTouch(View v, MotionEvent arg1) {
		if (v.equals(findViewById(R.id.tweet1)))
			detailScreen(tweets[0]);
		else if (v.equals(findViewById(R.id.tweet2)))
			detailScreen(tweets[1]);
		else if (v.equals(findViewById(R.id.tweet3)))
			detailScreen(tweets[2]);
		else if (v.equals(findViewById(R.id.tweet4)))
			detailScreen(tweets[3]);
		else if (v.equals(findViewById(R.id.tweet5)))
			detailScreen(tweets[4]);
		return false;
	}

}