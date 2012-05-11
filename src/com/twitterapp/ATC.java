package com.twitterapp;

import impl.android.com.twitterapime.xauth.ui.WebViewOAuthDialogWrapper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

import com.twitterapp.R;
/**
 * Main Activity
 * @author AHuMELLIHuK
 *
 */
public class ATC extends Activity {
	
	private final int ACTIVITY_END = -1;	
	private final int ACTIVITY_REFRESH = 0;
	private final int TIMELINE_ACTIVITY = 0;
	private final int SEARCH_ACTIVITY = 1;
	private final int TWEET_ACTIVITY = 2;
	private final int AUTHORISE_ACTIVITY = 3;

	private DataHandler dataHandler;

	/**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataHandler = (DataHandler)this.getApplication();
        if (dataHandler.isOnline()) {	        
	        if (!dataHandler.checkToken()) {
		        setContentView(R.layout.login);
		        
		        Button login = (Button)findViewById(R.id.loginButton);
		        login.setOnClickListener(new OnClickListener() {
		        	public void onClick(View v) {  
		        		launchWeb();
		        	}  
		        });
	        }
	        else mainScreen();
        }
        else {
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("No Internet Connection!").setCancelable(false)
    				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						finish();
    					}
    				});
    		builder.create().show();
        	
        }
    }    

	/**
     * Launches webView to authorise user account
     */
    public void launchWeb() {
    	Intent i = new Intent(ATC.this, AuthoriseActivity.class);
		startActivityForResult(i, AUTHORISE_ACTIVITY);	
	}	

	/**
	 * Displays the main screen
	 */
	public void mainScreen() {
		setContentView(R.layout.main);
		
		//Tweet button
		Button first = (Button)findViewById(R.id.goToTweet);
		first.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(ATC.this, TweetActivity.class);
				startActivityForResult(i, TWEET_ACTIVITY);				
			}			
		});
		//Timeline button
		Button second = (Button)findViewById(R.id.goToTimeline);
		second.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (dataHandler.isOnline()) {
					dataHandler.loadTimeline();
					setContentView(R.layout.load);
					ProgressBar loading = (ProgressBar)findViewById(R.id.loading);
					loading.setIndeterminate(true);		
					Intent i = new Intent(ATC.this, DisplayListActivity.class);
					i.putExtra("request", TIMELINE_ACTIVITY);
					startActivityForResult(i, TIMELINE_ACTIVITY);
				}
				else showMessage("No Internet Connection!");
								
			}			
		});
		//Search button
		Button third = (Button)findViewById(R.id.goToSearch);
		third.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (dataHandler.isOnline()) {
					Intent i = new Intent(ATC.this, SearchScreenActivity.class);
					startActivityForResult(i, SEARCH_ACTIVITY);	
				}
				else showMessage("No Internet Connection!");
						
			}			
		});
	}	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
			case TIMELINE_ACTIVITY: {
				if (resultCode == ACTIVITY_REFRESH) {
					Intent i = new Intent(ATC.this, DisplayListActivity.class);
					i.putExtra("request", TIMELINE_ACTIVITY);
					startActivityForResult(i, TIMELINE_ACTIVITY);
				}
				else if (resultCode == ACTIVITY_END)
					mainScreen();
				break;
			}
			case SEARCH_ACTIVITY: {
				mainScreen();
				break;
			}
			case TWEET_ACTIVITY: {
				mainScreen();
				break;
			}
			case AUTHORISE_ACTIVITY: {
				mainScreen();
				break;
			}
		}
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
	
	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to log in with another account?").setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						launchWeb();
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.create().show();
	}
	
	
	
	@Override
	protected void onDestroy() {
	   	System.runFinalizersOnExit(true);
		super.onDestroy();
	}
}