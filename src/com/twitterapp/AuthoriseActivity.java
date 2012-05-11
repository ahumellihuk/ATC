package com.twitterapp;

import impl.android.com.twitterapime.xauth.ui.WebViewOAuthDialogWrapper;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebView;

import com.twitterapime.xauth.Token;
import com.twitterapime.xauth.ui.OAuthDialogListener;

public class AuthoriseActivity extends Activity implements OAuthDialogListener {

	private final String CONSUMER_KEY = "YP6fMhYF1QkPi0slhXiJA";
	private final String CONSUMER_SECRET = "FWi27hEYJSTzpEq6ZxddMODNKOH9Qs4SyTL2DPbHss";
	private final String CALLBACK_URL = "http://ahumellihuk.com";
	private DataHandler dataHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataHandler = (DataHandler)this.getApplication();
		
        setContentView(R.layout.webview);
        
        WebView webView = (WebView)findViewById(R.id.webView);
        WebViewOAuthDialogWrapper pageWrapper =
        	new WebViewOAuthDialogWrapper(webView);
        
		pageWrapper.setConsumerKey(CONSUMER_KEY);
		pageWrapper.setConsumerSecret(CONSUMER_SECRET);
		pageWrapper.setCallbackUrl(CALLBACK_URL);
		pageWrapper.setOAuthListener(this);
		//
		pageWrapper.login();  
    }

	/**
	 * @see com.twitterapime.xauth.ui.OAuthDialogListener#onAuthorize(com.twitterapime.xauth.Token)
	 */
	public void onAuthorize(Token accessToken) {
		dataHandler.storeToken(accessToken);
		Intent mIntent = new Intent();
    	setResult(RESULT_OK, mIntent);
    	finish();		
	}

	/**
	 * @see com.twitterapime.xauth.ui.OAuthDialogListener#onAccessDenied(java.lang.String)
	 */
	public void onAccessDenied(String message) {
		Intent mIntent = new Intent();
    	setResult(RESULT_CANCELED, mIntent);
    	finish();
	}

	/**
	 * @see com.twitterapime.xauth.ui.OAuthDialogListener#onFail(java.lang.String, java.lang.String)
	 */
	public void onFail(String error, String message) {
		Intent mIntent = new Intent();
    	setResult(RESULT_CANCELED, mIntent);
    	finish();
	}	

}
