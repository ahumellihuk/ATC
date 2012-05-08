package com.twitterapp;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
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
	
	private DataHandler dataHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		ListView list = (ListView)findViewById(android.R.id.list);

		dataHandler = (DataHandler)this.getApplication();
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
			requestCode = extras.getInt("request");
		
		while (!dataHandler.loaded) {
		}
			
		tweets = dataHandler.getTweets();
		tweetsContent = new String[tweets.length];
		for (int i=0; i<tweets.length; i++) {
			tweetsContent[i] = (String)tweets[i].getObject(MetadataSet.TWEET_CONTENT);
		}
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, tweetsContent));
		
		list.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(DisplayListActivity.this, DetailsScreen.class);
				i.putExtra("tweet", (int)arg3);
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
				if (dataHandler.isOnline()) {
					Intent mIntent = new Intent();
					setResult(RESULT_CANCELED, mIntent);
					finish();
				}
				else showMessage("No Internet Connection!");
				
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
