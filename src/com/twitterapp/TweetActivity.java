package com.twitterapp;


import com.twitterapime.search.Tweet;
import com.twitterapp.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Activity displays screen for posting a tweet
 * @author Dmitri Samoilov *
 */
public class TweetActivity extends Activity {
	
	private DataHandler dataHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		dataHandler = (DataHandler)this.getApplication();
		
		setContentView(R.layout.tweet);
		
		//Submit button
		Button submit = (Button)findViewById(R.id.submitTweet);
		submit.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				EditText textField = (EditText)findViewById(R.id.tweetField);
				String tweet = textField.getText().toString();
				boolean response = dataHandler.post(new Tweet(tweet));
				if (response) {
					showMessage("Tweet posted successfully!");
					textField.setText(R.string.message);
				}
				else showMessage("Failed to post tweet");
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
