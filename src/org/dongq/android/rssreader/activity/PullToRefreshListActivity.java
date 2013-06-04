package org.dongq.android.rssreader.activity;

import org.dongq.android.rssreader.R;
import org.dongq.android.rssreader.adapter.RssFeedCursorAdapter;
import org.dongq.android.rssreader.dao.RssFeedDao;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class PullToRefreshListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemLongClickListener {

	private static final String tag = "RR_PullToRefreshListActivity";
	
	private PullToRefreshListView pullToRefreshListView;
	private SimpleCursorAdapter adapter;
	private ListView listview;
	private Cursor cursor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_refresh);
		
		this.pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		this.pullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				Log.d(tag, "onRefresh");
			}
		});
		this.pullToRefreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				Log.d(tag, "onLastItemVisible");
			}
		});
		this.listview = this.pullToRefreshListView.getRefreshableView();
		
		this.initLoad();
	}

	private void initLoad() {
		String[] from = {RssFeedDao.RSS_FEED_TITLE, RssFeedDao.RSS_FEED_LAST_BUILD_DATE};
		int[] to = {R.id.widget_feed_item_title, R.id.widget_feed_item_pub_date};
		this.adapter = new RssFeedCursorAdapter(this, R.layout.widget_feed_item, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		//setListAdapter(adapter);
		this.listview.setAdapter(adapter);
		getLoaderManager().initLoader(0, null, this);
		//getListView().setOnItemLongClickListener(this);
		this.listview.setOnItemLongClickListener(this);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(tag, "onItemLongClick");
		return false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Uri.parse(getString(R.string.content_uri_rss_feed));
		String[] projection = null;
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		Loader<Cursor> loader = new CursorLoader(this, uri, projection, selection, selectionArgs, sortOrder);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		this.adapter.swapCursor(data);
		this.cursor = data;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		this.adapter.swapCursor(null);
		this.cursor = null;
	}
}
