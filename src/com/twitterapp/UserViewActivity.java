package com.twitterapp;

import java.io.IOException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.rest.UserAccount;
import com.twitterapime.rest.UserAccountManager;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Tweet;

public class UserViewActivity extends ListActivity {
	/**Currently displayed tweets*/
	private Tweet[] tweets;
	/**Currently displayed tweets content*/
	private String[] tweetsContent;
	
	private DataHandler dataHandler;
	
	private String user;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userview);
		ListView list = (ListView)findViewById(android.R.id.list);

		dataHandler = (DataHandler)this.getApplication();
		
		user = "";
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			user = extras.getString("author");
		}
		
		tweets = dataHandler.getUserTweets();
		tweetsContent = new String[tweets.length];
		for (int i=0; i<tweets.length; i++) {
			tweetsContent[i] = (String)tweets[i].getObject(MetadataSet.TWEET_CONTENT);
		}
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, tweetsContent));
		
		TextView username = (TextView)findViewById(R.id.user);		
		username.setText(user);
		
		final UserAccount account = new UserAccount(user);
		final UserAccountManager uam = dataHandler.getUAM();
		boolean following = false;
		try {
			following = uam.isFollowing(account);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (LimitExceededException e1) {
			e1.printStackTrace();
		}
		
		ToggleButton followToggle = (ToggleButton)findViewById(R.id.followToggle);
		if (following)
			followToggle.setChecked(true);
		followToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if (isChecked) {
					try {
						uam.follow(account);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (LimitExceededException e) {
						e.printStackTrace();
					}
				}
				else if (!isChecked) {
					try {
						uam.unfollow(account);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (LimitExceededException e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(UserViewActivity.this, DetailsScreen.class);
				i.putExtra("tweet", (int)arg3);
				i.putExtra("noAuthor", true);
				startActivityForResult(i, 0);
			}
			
		});
		
		//Back Button
		Button back = (Button)findViewById(R.id.backFromUser);
		back.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent mIntent = new Intent();
		    	setResult(RESULT_OK, mIntent);
		    	finish();
			}
			
		});
		
		
	}
}
