package org.dongq.android.rssreader.adapter;

import java.util.HashMap;

import org.dongq.android.rssreader.R;
import org.dongq.android.rssreader.dao.RssFeedDao;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class RssItemCursorAdapter extends SimpleCursorAdapter {

	private static final String tag = "RR_RssItemCursorAdapter";
	
	private Cursor cursor;
	
	public RssItemCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		Log.d(tag, "RssItemCursorAdapter");
	}

	@Override
	public void setViewText(TextView v, String text) {
		Log.d(tag, text.length()+": "+text);
		if(text.length() > 20) {
			text = text.substring(0, 18) + "...";
		}
		super.setViewText(v, text);
	}
	
	@Override
	public void setViewImage(ImageView v, String value) {
		if(Integer.valueOf(value).equals(RssFeedDao.RSS_READED)) {
			value = String.valueOf(R.drawable.content_read);
		}
		super.setViewImage(v, value);
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
