package com.twitterapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.twitterapime.model.MetadataSet;
import com.twitterapime.search.Tweet;
/**
 * Activity displays list of tweets
 * @author Dmitri Samoilov *
 */
public class DisplayListActivity extends ListActivity {
	/**Currently displayed tweets*/
	private Tweet[] tweets;
	/**Currently displayed tweets content*/
	private String[] tweetsContent;
	/**Request code to determine button text*/
	private int requestCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		ListView list = (ListView)findViewById(android.R.id.list);

		
		Bundle extras = getIntent().getExtras();
		requestCode = extras.getInt("request");
		if (extras != null) {
			Object[] passedTweets = (Object[])extras.get("tweet");
			int n = passedTweets.length;
			tweetsContent = new String[n];			
			tweets = new Tweet[n];
			for (int i=0; i<n; i++) {
				if (passedTweets[i] != null)
					if (passedTweets[i] instanceof Tweet) {
						tweets[i] = (Tweet)passedTweets[i];
					}
					tweetsContent[i] = (String)tweets[i].getObject(MetadataSet.TWEET_CONTENT);
			}
		}
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, tweetsContent));
		
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(DisplayListActivity.this, DetailsScreen.class);
				i.putExtra("tweet", tweets[(int) arg3]);
				startActivityForResult(i, 0);
			}
			
		});
		
		//Back Button
		Button back = (Button)findViewById(R.id.backFromList);
		back.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent mIntent = new Intent();
		    	setResult(RESULT_OK, mIntent);
		    	finish();
			}
			
		});
		
		// Refresh/NewSearch Button
		Button refresh = (Button)findViewById(R.id.refresh);
		if (requestCode == 0) refresh.setText("Refresh");
		else if (requestCode == 1) refresh.setText("New Search");
		refresh.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent mIntent = new Intent();
		    	setResult(RESULT_CANCELED, mIntent);
		    	finish();
			}
			
		});
	}

}
