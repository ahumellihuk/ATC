package com.twitterapp;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Query;
import com.twitterapime.search.QueryComposer;
import com.twitterapime.search.SearchDevice;
import com.twitterapime.search.Tweet;
/**
 * Activity displays search screen
 * @author Dmitri Samoilov *
 */
public class SearchScreenActivity extends Activity{
	
	protected static final int SEARCH_ACTIVITY = 1;

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
					Tweet[] tweets = s.searchTweets(q);
					Intent i = new Intent(SearchScreenActivity.this, DisplayListActivity.class);
					i.putExtra("tweet", tweets);
					i.putExtra("request", SEARCH_ACTIVITY);
					startActivityForResult(i, SEARCH_ACTIVITY);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
			case RESULT_CANCELED: {
				searchScreen();
				break;
			}
			case RESULT_OK: {
				Intent mIntent = new Intent();
		    	setResult(RESULT_OK, mIntent);
		    	finish();
			}
		}		
	}
}
