package org.dongq.android.rssreader;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
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
				String[] names = cursor.getColumnNames();
				for (String name : names) {
					int index = cursor.getColumnIndex(name);
					Log.d(this.getClass().getName(), name + ": " + cursor.getString(index));
				}
				return cursor.getString(cursor.getColumnIndex(RssFeedDao.RSS_FEED_URI));
			}
		}
		return null;
	}
	
}
