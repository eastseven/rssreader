/**
 * 
 */
package org.dongq.android.rssreader;

import java.util.Date;
import java.util.List;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author eastseven
 *
 */
public class RssFeedDao extends SQLiteOpenHelper {

	private static final String tag = RssFeedDao.class.getSimpleName();
	
	private static final String DATABASE_NAME    = "d7.rss.reader";
	private static final int    DATABASE_VERSION = 6;
	
	public static final int RSS_UNREAD = 0;
	public static final int RSS_READED = 1;
	
    public  static final String RSS_FEED                 = "d7_rss_feed";
    public  static final String RSS_FEED_URI             = "rss_feed_uri";
    public  static final String RSS_FEED_TITLE           = "rss_feed_title";
    private static final String RSS_FEED_LINK            = "rss_feed_link";
    private static final String RSS_FEED_DESC            = "rss_feed_desc";
    public  static final String RSS_FEED_LAST_BUILD_DATE = "rss_feed_last_build_date";
    
    public static final String RSS_ITEM          = "d7_rss_item";
    public static final String RSS_ITEM_PARENT   = "rss_item_feed_uri";
    public static final String RSS_ITEM_TITLE    = "rss_item_title";
    public static final String RSS_ITEM_LINK     = "rss_item_link";
    public static final String RSS_ITEM_DESC     = "rss_item_desc";
    public static final String RSS_ITEM_AUTHOR   = "rss_item_author";
    public static final String RSS_ITEM_PUB_DATE = "rss_item_pub_date";
    public static final String RSS_ITEM_GUID     = "rss_item_guid";
    public static final String RSS_ITEM_READED   = "rss_item_readed";
    
    private static final String RSS_FEED_SCRIPT = "create table if not exists " + RSS_FEED + " (_id integer primary key autoincrement, rss_feed_uri text, rss_feed_title text, rss_feed_link text, rss_feed_desc text, rss_feed_last_build_date text, create_time TIMESTAMP default (datetime('now', 'localtime')) ) ";
    private static final String RSS_ITEM_SCRIPT = "create table if not exists " 
    											+ RSS_ITEM 
    											+ " (_id integer primary key autoincrement," 
    											+ RSS_ITEM_PARENT + " text,"
    											+ RSS_ITEM_TITLE + " text,"
    											+ RSS_ITEM_LINK + " text,"
    											+ RSS_ITEM_DESC + " text,"
    											+ RSS_ITEM_AUTHOR + " text,"
    											+ RSS_ITEM_PUB_DATE + " text,"
    											+ RSS_ITEM_GUID + " text,"
    											+ RSS_ITEM_READED + " integer default 0,"
    											+ " create_time TIMESTAMP default (datetime('now', 'localtime')) )";
    
    private String[] sqls = {RSS_FEED_SCRIPT , RSS_ITEM_SCRIPT };
	
	public RssFeedDao(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(tag, "onCreate");
		for (String sql : sqls) {
			db.execSQL(sql);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(tag, "onUpgrade: oldVersion="+oldVersion+", newVersion="+newVersion);
		String[] deletes = {RSS_FEED, RSS_ITEM};
		for (String sql : deletes) {
			db.execSQL("drop table "+sql);
		}
		
		for (String sql : sqls) {
			db.execSQL(sql);
		}
	}

	public boolean saveRssFeed(RSSFeed feed, String uri) {
		boolean isSave = false;
		try {
			
			ContentValues values = new ContentValues();
			values.put(RSS_FEED_URI, uri);
			values.put(RSS_FEED_TITLE, feed.getTitle());
			values.put(RSS_FEED_LINK, feed.getLink().toString());
			values.put(RSS_FEED_DESC, feed.getDescription());
			if(feed.getLastBuildDate() != null) {
				values.put(RSS_FEED_LAST_BUILD_DATE, feed.getLastBuildDate().toString());
			}
			
			SQLiteDatabase db = this.getWritableDatabase();
			long rowid = db.insert(RSS_FEED, null, values);
			isSave = rowid != -1;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			this.close();
		}
		return isSave;
	}
	
	public boolean updateRSSFeed(String uri, Date lastBuildDate) {
		boolean isUpdate = false;
		
		try {
			
			isUpdate = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return isUpdate;
	}
	
	public boolean saveRssItem(RSSItem item, String uri) {
		boolean isSave = false;
		try {
			ContentValues v = new ContentValues();
			v.put(RSS_ITEM_PARENT, uri);
			v.put(RSS_ITEM_DESC, item.getDescription());
			v.put(RSS_ITEM_LINK, item.getLink().toString());
			v.put(RSS_ITEM_PUB_DATE, item.getPubDate().toString());
			v.put(RSS_ITEM_TITLE, item.getTitle());
			
			long rowid = this.getWritableDatabase().insert(RSS_ITEM, null, v);
			isSave = rowid != -1;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		return isSave;
	}
	
	public boolean saveRssItems(List<RSSItem> items, String uri) {
		boolean isSave = false;
		try {
			ContentValues v = new ContentValues();
			SQLiteDatabase db = this.getWritableDatabase();
			for (RSSItem item : items) {
				v.clear();
				
				v.put(RSS_ITEM_PARENT, uri);
				v.put(RSS_ITEM_DESC, item.getDescription());
				v.put(RSS_ITEM_LINK, item.getLink().toString());
				v.put(RSS_ITEM_PUB_DATE, item.getPubDate().toString());
				v.put(RSS_ITEM_TITLE, item.getTitle());
				
				db.insert(RSS_ITEM, null, v);
			}
			isSave = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		return isSave;
	}
	
	public boolean contain(RSSItem item) {
		boolean isContain = true;
		try {
			Cursor cursor = this.getReadableDatabase().query(RSS_ITEM, null, RSS_ITEM_LINK+"=?", new String[] {item.getLink().toString()}, null, null, null);
			int count = cursor.getCount();
			isContain = count >= 1;
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		return isContain;
	}
	
	public boolean isDuplicate(String uri) {
		boolean bln = false;
		try {
			Cursor c = this.getReadableDatabase().query(RSS_FEED, new String[] {RSS_FEED_URI}, RSS_FEED_URI + "=?", new String[] {uri}, null, null, null);
			if(c.getCount() > 0) {
				bln = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bln;
	}
}
