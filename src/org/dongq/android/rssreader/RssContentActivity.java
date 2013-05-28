package org.dongq.android.rssreader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class RssContentActivity extends Activity {

	private static final String tag = "RR_RssContentActivity";
	
	private WebView webView;
	
	private String feedUri, feedTitle;
	private String originalUri, content;
	private final String mimeType = "text/html";
	private final String encoding = "UTF-8";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content);
		webView = (WebView) findViewById(R.id.rss_item_content);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		feedUri     = getIntent().getStringExtra("uri");
		feedTitle   = getIntent().getStringExtra("title");
		content     = getIntent().getStringExtra("content");
		originalUri = getIntent().getStringExtra("link");
		Log.d(tag, originalUri);
		webView.loadDataWithBaseURL(null, content, mimeType, encoding, null);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.content, menu);
		//TextView view = new TextView(this);
		Button view = new Button(this);
		view.setText("查看原文");
		view.setTextSize(getResources().getDimension(R.dimen.word_size_08));
		view.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(originalUri));
				startActivity(intent);
			}
		});
		menu.findItem(R.id.content_navigation_original).setActionView(view);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case android.R.id.home:
			intent = new Intent(this, RssItemListActivity.class);
			//intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("uri", this.feedUri);
			intent.putExtra("title", this.feedTitle);
			startActivity(intent);
			Log.d(tag, "back to RssItemList");
			return true;
		case R.id.content_navigation_original:
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(originalUri));
			startActivity(intent);
			Log.d(tag, "加载原文："+originalUri);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
