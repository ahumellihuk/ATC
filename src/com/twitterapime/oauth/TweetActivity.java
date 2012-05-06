package com.twitterapime.oauth;

import java.io.IOException;

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
import android.widget.EditText;

public class TweetActivity extends Activity {

	private final String CONSUMER_KEY = "YP6fMhYF1QkPi0slhXiJA";
	private final String CONSUMER_SECRET = "FWi27hEYJSTzpEq6ZxddMODNKOH9Qs4SyTL2DPbHss";
	
	private TweetER tweeter;
	private Token accessToken;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tweet);
		
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
		//Back button
		Button back = (Button)findViewById(R.id.backFromTweet);
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
