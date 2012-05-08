package com.twitterapp;

import java.io.IOException;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;

import com.twitterapime.rest.Credential;
import com.twitterapime.rest.Timeline;
import com.twitterapime.rest.TweetER;
import com.twitterapime.rest.UserAccountManager;
import com.twitterapime.search.LimitExceededException;
import com.twitterapime.search.Query;
import com.twitterapime.search.QueryComposer;
import com.twitterapime.search.SearchDevice;
import com.twitterapime.search.SearchDeviceListener;
import com.twitterapime.search.Tweet;
import com.twitterapime.xauth.Token;

public class DataHandler extends Application{

	private Token accessToken;
	private TweetER tweeter;
	private Timeline timeline;
	private Tweet[] tweets;
	public boolean loaded;
	
	private final String CONSUMER_KEY = "YP6fMhYF1QkPi0slhXiJA";
	private final String CONSUMER_SECRET = "FWi27hEYJSTzpEq6ZxddMODNKOH9Qs4SyTL2DPbHss";
	
	public boolean checkToken() {
		if (accessToken!=null)
			return true;
		else return false;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		accessToken = getToken();
		if (checkToken()) {
			initialize();
		}
	}
	
	public void initialize() {
		Credential c = new Credential(CONSUMER_KEY, CONSUMER_SECRET, accessToken);
		UserAccountManager uam = UserAccountManager.getInstance(c);
		try {
			if (uam.verifyCredential()) {
				timeline = Timeline.getInstance(uam);
				tweeter = TweetER.getInstance(uam);
			}
		} catch (Exception e) {
			
		}
	}
	
	public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    if (cm.getActiveNetworkInfo() != null)
	    	return cm.getActiveNetworkInfo().isConnectedOrConnecting();
	    else return false;
	}

	/**
     * Stores the existing access token in SharedPreferences
     * @param accessToken User Access Token
     */
    public void storeToken(Token accessToken) {
    	this.accessToken = accessToken;
    	SharedPreferences prefs = getSharedPreferences("ATC", MODE_PRIVATE);
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.putString("AccessToken", accessToken.getToken());
    	editor.putString("AccessSecret", accessToken.getSecret());
    	editor.commit();
    	initialize();
    }
    
    /**
     * Retrieves access token from SharedPreferences
     */
    public Token getToken() {
    	SharedPreferences prefs = getSharedPreferences("ATC", MODE_PRIVATE);
    	String token = prefs.getString("AccessToken", null);
    	String secret = prefs.getString("AccessSecret", null);
    	if (token != null && secret != null) {
    		Token accessToken = new Token(token, secret);
    		return accessToken;
    	}
    	else return null;
    }
    
    public Tweet[] getTweets() {
    	return tweets;
    }
    
    /**
	 * Loads latest tweets from home timeline
	 */
	public void loadTimeline() {
		tweets = new Tweet[20];
		loaded = false;
		Query query = QueryComposer.count(20);
		timeline.startGetHomeTweets(query, new SearchDeviceListener() {
			int i = 0;
			/**
			 * Executed at the end of search
			 */
			public void searchCompleted() {
				loaded = true;
			}
			
			/**
			 * Executed if search is failed
			 */
			public void searchFailed(Throwable arg0) {		
			}
			
			/**
			 * Executed when a tweet is found
			 * @param tweet Found tweet
			 */ 
			public void tweetFound(Tweet tweet) {
				tweets[i] = tweet;
				i++;
			}
		});
	}	
	
	public boolean retweet(Tweet tweet) {
		try {
			tweeter.repost(tweet);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (LimitExceededException e) {
			e.printStackTrace();
			return false;			
		}
		return true;
	}
	
	public boolean post(Tweet tweet) {
		try {
			tweeter.post(tweet);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (LimitExceededException e) {
			e.printStackTrace();
			return false;			
		}
		return true;
	}
	
	public void doSearch(Query q) {
		SearchDevice s = SearchDevice.getInstance();
		try {
			tweets = s.searchTweets(q);
			loaded = true;
		} catch (IOException e) {
			Log.w("IOException", "IOException");
			e.printStackTrace();
		} catch (LimitExceededException e) {
			Log.w("LimitExceededException", "LimitExceededException");
			e.printStackTrace();
		}
	}
	
	public void searchAll(String keywords, String author, String hashtag) {		
		Query q = QueryComposer.append(QueryComposer.from(author), QueryComposer.containAll(keywords));
		q = QueryComposer.append(q, QueryComposer.containHashtag(hashtag));
		doSearch(q);
	}
	
	public void searchHashtag(String hashtag) {
		Query q = QueryComposer.containHashtag(hashtag);
		doSearch(q);
	}
	
	public void searchAuthor(String author) {
		Query q = QueryComposer.from(author);
		doSearch(q);
	}
	
	public void searchKeywords(String keywords) {
		Query q = QueryComposer.containAll(keywords);
		doSearch(q);
	}
	
	public void searchKeywordsHashtag(String keywords, String hashtag) {
		Query q = QueryComposer.append(QueryComposer.containAll(keywords), QueryComposer.containHashtag(hashtag));
		doSearch(q);
	}
	
	public void searchKeywordsAuthor(String keywords, String author) {
		Query q = QueryComposer.append(QueryComposer.containAll(keywords),QueryComposer.from(author));
		doSearch(q);
	}
}
