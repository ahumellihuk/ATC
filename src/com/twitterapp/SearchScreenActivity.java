package com.twitterapp;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.twitterapime.model.MetadataSet;
import com.twitterapp.R;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Query;
import com.twitterapime.search.QueryComposer;
import com.twitterapime.search.SearchDevice;
import com.twitterapime.search.Tweet;
/**
 * Activity displays search screen and the five tweets found
 * @author Dmitri Samoilov *
 */
public class SearchScreenActivity extends Activity implements OnTouchListener {
	/**Currently displayed tweets*/
	private Tweet[] tweets;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		searchScreen();
	}
	/**
	 * Displays search screen
	 */
	protected void searchScreen() {
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
				Intent mIntent = new Intent();
		    	setResult(RESULT_OK, mIntent);
		    	finish();
			}
			
		});
	}
	
	/**
	 * Displays five tweets found
	 */
	public void timelineScreen() {
		setContentView(R.layout.timeline); //App gets to this line and does not go any further	

		TextView [] textFields = {(TextView)findViewById(R.id.tweet1),(TextView)findViewById(R.id.tweet2),(TextView)findViewById(R.id.tweet3),
				(TextView)findViewById(R.id.tweet4),(TextView)findViewById(R.id.tweet5)};
		
		int n = tweets.length;
		if (n>5) n=5;
		
		//Hide unused fields and set listeners to others
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
				Intent mIntent = new Intent();
		    	setResult(RESULT_OK, mIntent);
		    	finish();
			}
			
		});
		//New Search button
		Button newSearch = (Button)findViewById(R.id.newSearch);
		newSearch.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				searchScreen();
			}
		});
	}
	/**
	 * When a tweet is touched, goes to Details Screen
	 */
	public boolean onTouch(View v, MotionEvent arg1) {
		Intent i = new Intent(SearchScreenActivity.this, DetailsScreen.class);		
		if (v.equals(findViewById(R.id.tweet1)))
			i.putExtra("tweet", tweets[0]);
		else if (v.equals(findViewById(R.id.tweet2)))
			i.putExtra("tweet", tweets[1]);
		else if (v.equals(findViewById(R.id.tweet3)))
			i.putExtra("tweet", tweets[2]);
		else if (v.equals(findViewById(R.id.tweet4)))
			i.putExtra("tweet", tweets[3]);
		else if (v.equals(findViewById(R.id.tweet5)))
			i.putExtra("tweet", tweets[4]);
		startActivityForResult(i, 0);
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		timelineScreen();
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
