package com.appnexus.opensdk;
import com.appnexus.opensdk.AdView.BrowserStyle;
import com.appnexus.opensdk.utils.Clog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;


public class BrowserActivity extends Activity {
	private WebView webview;
	private ImageButton back;
	private ImageButton forward;
	private ImageButton refresh;

	@SuppressWarnings("deprecation")
	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	@Override
	public void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
		setContentView(R.layout.activity_in_app_browser);
		
		webview = (WebView) findViewById(R.id.web_view);
		back = (ImageButton) findViewById(R.id.browser_back);
		forward = (ImageButton) findViewById(R.id.browser_forward);
		refresh = (ImageButton) findViewById(R.id.browser_refresh);
		
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webview.getSettings().setDomStorageEnabled(true);
		webview.getSettings().setPluginState(PluginState.ON_DEMAND);
		
		
		back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(webview.canGoBack()){
					webview.goBack();
				}else{
					finish();
				}
			}
			
		});
		
		forward.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				webview.goForward();
			}
		});
		
		refresh.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				webview.reload();
			}
		});
		
		
		webview.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				if(url.startsWith("http")){
					Clog.d(Clog.baseLogTag, Clog.getString(R.string.opening_url, url));
					webview.loadUrl(url);
					return true;
				}
				return false;
			}
		});
		
		String url = (String) getIntent().getExtras().get("url");
		
		String id = (String) getIntent().getExtras().get("bridgeid");
		if(id!=null){
			BrowserStyle style=null;
			for(Pair<String, BrowserStyle> p : AdView.BrowserStyle.bridge){
				if(p.first.equals(id)){
					style=p.second;
					AdView.BrowserStyle.bridge.remove(p);
				}
			}
			if(style!=null){
				int sdk = android.os.Build.VERSION.SDK_INT;
				if(sdk>=android.os.Build.VERSION_CODES.JELLY_BEAN){
					back.setBackground(style.backButton);
					forward.setBackground(style.forwardButton);
					refresh.setBackground(style.refreshButton);
				}else{
					back.setBackgroundDrawable(style.backButton);
					forward.setBackgroundDrawable(style.forwardButton);
					refresh.setBackgroundDrawable(style.refreshButton);
				}
			}
		}
		
		webview.loadUrl(url);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuItem open = menu.add("Open With Browser");
		open.setOnMenuItemClickListener(new OnMenuItemClickListener(){

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Clog.d(Clog.baseLogTag, Clog.getString(R.string.opening_native_current));
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(webview.getUrl()));
				startActivity(i);
				finish();
				return true;
			}
			
		});
		return true;
	}
}