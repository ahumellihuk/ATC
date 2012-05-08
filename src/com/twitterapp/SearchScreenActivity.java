package com.twitterapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Activity displays search screen
 * @author Dmitri Samoilov *
 */
public class SearchScreenActivity extends Activity{
	
	protected static final int SEARCH_ACTIVITY = 1;
	
	private DataHandler dataHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataHandler = (DataHandler)this.getApplication();
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
				String author = usernameField.getText().toString();
				String keywords = field.getText().toString();
				String hashtag = hashtagField.getText().toString();
				if (usernameCheck.isChecked() && !hashCheck.isChecked()) 
					dataHandler.searchKeywordsAuthor(keywords, author);
				else if (hashCheck.isChecked() && !usernameCheck.isChecked())
					dataHandler.searchKeywordsHashtag(keywords, hashtag);
				else if (usernameCheck.isChecked() && !hashCheck.isChecked() &&(keywords == "" || keywords == null))
					dataHandler.searchAuthor(author);
				else if (!usernameCheck.isChecked() && hashCheck.isChecked() &&(keywords == "" || keywords == null))
					dataHandler.searchHashtag(hashtag);
				else if (usernameCheck.isChecked() && hashCheck.isChecked())
					dataHandler.searchAll(keywords, author, hashtag);		
				else
					dataHandler.searchKeywords(keywords);
				Intent i = new Intent(SearchScreenActivity.this, DisplayListActivity.class);
				i.putExtra("request", SEARCH_ACTIVITY);
				startActivityForResult(i, SEARCH_ACTIVITY);
					
				
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
	
	@Override
	public void onBackPressed() {
		
		Intent mIntent = new Intent();
    	setResult(RESULT_OK, mIntent);
    	finish();
    	super.onBackPressed();
	}
}
