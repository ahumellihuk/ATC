package com.twitterapp;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.search.Tweet;
/**
 * Activity displays tweet details
 * @author Dmitri Samoilov *
 */
public class DetailsScreen extends Activity {

	/**Current tweet*/
	private Tweet tweet;
	protected DataHandler dataHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataHandler = (DataHandler)this.getApplication();
		
		setContentView(R.layout.detail);
		
		//Retrieve Tweet from bundle extras
		tweet = new Tweet();		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			int n = extras.getInt("tweet");
			tweet = dataHandler.getTweets()[n];
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
		TextView dateView = (TextView)findViewById(R.id.date);
		tweetDetails.setText(details);
		if (author != null)
			authorView.setText("@"+author);
		else authorView.setText("No author");
		dateView.setText(formatter.format(calendar.getTime()));
		
		//Retweet Button
		Button retweet = (Button)findViewById(R.id.retweet);
		retweet.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				if (dataHandler.isOnline())
					dataHandler.retweet(tweet);
				else showMessage("No Internet Connection!");
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
	
	@Override
	public void onBackPressed() {
		
		Intent mIntent = new Intent();
    	setResult(RESULT_OK, mIntent);
    	finish();
    	super.onBackPressed();
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
