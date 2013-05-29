package org.dongq.android.rssreader.adapter;

import java.util.HashMap;

import org.dongq.android.rssreader.dao.RssFeedDao;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

public class RssFeedCursorAdapter extends SimpleCursorAdapter {

	private Cursor cursor;
	
	public RssFeedCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.cursor = c;
	}

	@Override
	public Cursor swapCursor(Cursor c) {
		this.cursor = c;
		return super.swapCursor(c);
	}
	
	@Override
	public Object getItem(int position) {
		if(this.cursor != null) {
			boolean bln = cursor.moveToPosition(position);
			if(bln) {
				String uri   = cursor.getString(cursor.getColumnIndex(RssFeedDao.RSS_FEED_URI));
				String title = cursor.getString(cursor.getColumnIndex(RssFeedDao.RSS_FEED_TITLE));
				HashMap<String, String> feed = new HashMap<String, String>();
				feed.put("uri", uri);
				feed.put("title", title);
				
				return feed;
			}
		}
		return null;
	}
	
}
