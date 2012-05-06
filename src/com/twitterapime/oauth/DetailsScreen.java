package com.twitterapime.oauth;

import java.io.IOException;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.rest.Credential;
import com.twitterapime.rest.TweetER;
import com.twitterapime.rest.UserAccountManager;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Tweet;
import com.twitterapime.xauth.Token;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DetailsScreen extends Activity {
	
	private final String CONSUMER_KEY = "YP6fMhYF1QkPi0slhXiJA";
	private final String CONSUMER_SECRET = "FWi27hEYJSTzpEq6ZxddMODNKOH9Qs4SyTL2DPbHss";
	
	private TweetER tweeter;
	private Token accessToken;
	
	private Tweet tweet;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.detail);
		
		SharedPreferences prefs = getSharedPreferences("ATC", MODE_PRIVATE);
    	String token = prefs.getString("AccessToken", null);
    	String secret = prefs.getString("AccessSecret", null);
    	accessToken = null;
    	if (token != null && secret != null) {
    		accessToken = new Token(token, secret);
    	}
    	Credential c = new Credential(CONSUMER_KEY, CONSUMER_SECRET, accessToken);
		UserAccountManager uam = UserAccountManager.getInstance(c);
		try {
			if (uam.verifyCredential()) {
				tweeter = TweetER.getInstance(uam);
			}
		} catch (Exception e) {
			showMessage("Error authorising.");
		} 
		
		tweet = new Tweet();
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Object passedTweet = extras.get("tweet");
			if (passedTweet instanceof Tweet)
				tweet = (Tweet) passedTweet;
		}
		
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
				Intent mIntent = new Intent();
		    	setResult(RESULT_OK, mIntent);
		    	finish();
			}
			
		});
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