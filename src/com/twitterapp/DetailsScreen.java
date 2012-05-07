package com.twitterapp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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

import com.twitterapime.model.MetadataSet;
import com.twitterapime.rest.Credential;
import com.twitterapime.rest.TweetER;
import com.twitterapime.rest.UserAccountManager;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Tweet;
import com.twitterapime.xauth.Token;
/**
 * Activity displays tweet details
 * @author Dmitri Samoilov *
 */
public class DetailsScreen extends Activity {
	
	private final String CONSUMER_KEY = "YP6fMhYF1QkPi0slhXiJA";
	private final String CONSUMER_SECRET = "FWi27hEYJSTzpEq6ZxddMODNKOH9Qs4SyTL2DPbHss";
	/**Authorised instance of TweetER*/
	private TweetER tweeter;
	/**Current tweet*/
	private Tweet tweet;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.detail);
		
		//Retrieve access token and generate TweetER instance
		SharedPreferences prefs = getSharedPreferences("ATC", MODE_PRIVATE);
    	String token = prefs.getString("AccessToken", null);
    	String secret = prefs.getString("AccessSecret", null);
    	Token accessToken = null;
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
		
		//Retrieve Tweet from bundle extras
		tweet = new Tweet();		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Object passedTweet = extras.get("tweet");
			if (passedTweet instanceof Tweet)
				tweet = (Tweet) passedTweet;
		}
		
		//Display Tweet
		String details = (String)tweet.getObject(MetadataSet.TWEET_CONTENT);
		String author = (String)tweet.getObject(MetadataSet.TWEET_AUTHOR_USERNAME);
		String stringDate = (String)tweet.getObject(MetadataSet.TWEET_PUBLISH_DATE);
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'at' hh:mm a");
		Long longDate = Long.parseLong(stringDate);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(longDate);

		TextView tweetDetails = (TextView)findViewById(R.id.tweetDetails);
		TextView authorView = (TextView)findViewById(R.id.author);
		TextView hashtagView = (TextView)findViewById(R.id.hashtag);
		tweetDetails.setText(details);
		if (author != null)
			authorView.setText("@"+author);
		else authorView.setText("No author");
		hashtagView.setText(formatter.format(calendar.getTime()));
		
		//Retweet Button
		Button retweet = (Button)findViewById(R.id.retweet);
		retweet.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				try {
					tweeter.repost(tweet);
					showMessage("Tweet successfully reposted!");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (LimitExceededException e) {
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
