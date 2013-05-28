package org.dongq.android.rssreader;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class RssFeedContentProvider extends ContentProvider {

	private static final String AUTHORITH        = "org.dongq.android.rssreader.provider";
	public  static final Uri CONTENT_URI_FEED = Uri.parse("content://"+AUTHORITH+"/rss/feed");
	public  static final Uri CONTENT_URI_ITEM = Uri.parse("content://"+AUTHORITH+"/rss/item");
	
	private static final int RSS_FEED = 1;
	private static final int RSS_ITEM = 2;
	
	private RssFeedDao dao;
	private SQLiteDatabase db;
	
	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static {
		uriMatcher.addURI(AUTHORITH, "rss/feed", RSS_FEED);
		uriMatcher.addURI(AUTHORITH, "rss/item", RSS_ITEM);
	}
	
	@Override
	public boolean onCreate() {
		dao = new RssFeedDao(getContext());
		return dao != null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		try {
			String table = null;
			db = dao.getReadableDatabase();
			int code = uriMatcher.match(uri);
			switch (code) {
			case RSS_FEED:
				table = RssFeedDao.RSS_FEED;
				break;
			case RSS_ITEM:
				table = RssFeedDao.RSS_ITEM;
				break;
			default:
				table = "";
				break;
			}
			
			return db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

}
