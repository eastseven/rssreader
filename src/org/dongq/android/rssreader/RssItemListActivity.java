package org.dongq.android.rssreader;

import java.util.HashMap;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class RssItemListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String tag = "RR_RssItemListActivity";

	private SimpleCursorAdapter adapter;
	
	private String _uri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(tag, "onCreate...");

		_uri = getIntent().getStringExtra("uri");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		String[] from = {RssFeedDao.RSS_ITEM_TITLE};
		int[] to = {android.R.id.text1};
		this.adapter = new RssItemCursorAdapter(this, android.R.layout.simple_list_item_1, null, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		
		setListAdapter(adapter);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(tag, "onOptionsItemSelected: " + item);
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			Log.d(tag, "back to Main");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void onListItemClick(ListView l, View v, int position, long id) {
		HashMap<String, Object> item = (HashMap<String, Object>) l.getAdapter().getItem(position);
		String link = (String) item.get(RssFeedDao.RSS_ITEM_LINK);
		String desc = (String) item.get(RssFeedDao.RSS_ITEM_DESC);
		
		for(String key : item.keySet()) {
			Log.d(tag, key + ": " + item.get(key));
		}
		
		Intent intent = new Intent(this, RssContentActivity.class);
		intent.putExtra("link", link);
		intent.putExtra("content", desc);
		intent.putExtra("uri", _uri);
		startActivity(intent);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(tag, "onCreateLoader");
		Uri uri = RssFeedContentProvider.CONTENT_URI_ITEM;
		return new CursorLoader(this, uri, null, RssFeedDao.RSS_ITEM_PARENT+"=?", new String[] {_uri}, RssFeedDao.RSS_ITEM_PUB_DATE);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(tag, "onLoadFinished");
		this.adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(tag, "onLoaderReset");
		this.adapter.swapCursor(null);
	}
}
