package com.twitterapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import com.twitterapime.model.MetadataSet;
import com.twitterapp.R;
import com.twitterapime.search.Tweet;
/**
 * Activity displays five latest tweets from Home timeline
 * @author Dmitri Samoilov *
 */
public class TimelineActivity extends Activity implements OnTouchListener {
	/**Currently displayed tweets*/
	private Tweet[] tweets;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline_activity);
		
		TextView [] textFields = {(TextView)findViewById(R.id.tweet1),(TextView)findViewById(R.id.tweet2),(TextView)findViewById(R.id.tweet3),
				(TextView)findViewById(R.id.tweet4),(TextView)findViewById(R.id.tweet5)};
		
		for (int i=0; i<5; i++)
			textFields[i].setOnTouchListener(this);
		
		tweets = new Tweet[5];
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Object[] passedTweets = (Object[])extras.get("tweet");
			for (int i=0; i<5; i++) {
				if (passedTweets[i] != null)
					if (passedTweets[i] instanceof Tweet) {
						tweets[i] = (Tweet)passedTweets[i];
					}
					textFields[i].setText((String)tweets[i].getObject(MetadataSet.TWEET_CONTENT));
			}
		}
		//Back Button
		Button back = (Button)findViewById(R.id.backFromTimeline);
		back.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent mIntent = new Intent();
		    	setResult(RESULT_OK, mIntent);
		    	finish();
			}
			
		});
		
		//Refresh Button
		Button refresh = (Button)findViewById(R.id.refresh);
		refresh.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent mIntent = new Intent();
		    	setResult(RESULT_CANCELED, mIntent);
		    	finish();
			}
			
		});
	}
	/**
	 * When a tweet is touched, goes to Details Screen
	 */
	public boolean onTouch(View v, MotionEvent arg1) {
		Intent i = new Intent(TimelineActivity.this, DetailsScreen.class);		
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

}
