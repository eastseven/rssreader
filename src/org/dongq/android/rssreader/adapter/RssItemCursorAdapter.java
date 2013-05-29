package org.dongq.android.rssreader.adapter;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

public class RssItemCursorAdapter extends SimpleCursorAdapter {

	private Cursor cursor;
	
	public RssItemCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}

	@Override
	public Cursor swapCursor(Cursor c) {
		this.cursor = c;
		return super.swapCursor(c);
	}
	
	@Override
	public Object getItem(int position) {
		if(this.cursor != null) {
			String[] names = cursor.getColumnNames();
			HashMap<String, Object> item = new HashMap<String, Object>();
			for(String name : names) {
				Object value = cursor.getString(cursor.getColumnIndex(name));
				item.put(name, value);
			}
			return item;
		}
		return null;
	}
}
