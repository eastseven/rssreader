package org.dongq.android.rssreader.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dongq.android.rssreader.R;
import org.dongq.android.rssreader.adapter.RssFeedCursorAdapter;
import org.dongq.android.rssreader.dao.RssFeedDao;
import org.dongq.android.rssreader.utils.Constant;
import org.dongq.android.rssreader.utils.DebugDataUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemLongClickListener {
	
	private static final String tag = "RR_MainActivity";
	
	private SimpleCursorAdapter adapter;
	private ProgressDialog loading, syncProgress;
	private EditText rssUri;
	
	private RssFeedDao dao;
	private Cursor cursor;

	private SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(tag, "ListActivityMain onCreate...");

		/*
		 * TODO 在启动页面做首次登录的判断，不是最佳方式，兹待改进，暂时将就用，以后改
		 * */
		this.sp = getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
		boolean isFirst = this.sp.getBoolean(Constant.IS_FIRST_LOGIN, true);
		if(isFirst) {
			startActivity(new Intent(this, HelloActivity.class));
		} else {
			setContentView(R.layout.activity_main);
			initLoad();
		}
		
	}

	private void initLoad() {
		this.dao = new RssFeedDao(this);
		String[] from = {RssFeedDao.RSS_FEED_TITLE, RssFeedDao.RSS_FEED_LAST_BUILD_DATE};
		int[] to = {R.id.widget_feed_item_title, R.id.widget_feed_item_pub_date};
		this.adapter = new RssFeedCursorAdapter(this, R.layout.widget_feed_item, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		setListAdapter(adapter);
		getLoaderManager().initLoader(0, null, this);
		getListView().setOnItemLongClickListener(this);
	}
	
	private void refresh() {
		CursorLoader loader = new CursorLoader(this, Uri.parse(getString(R.string.content_uri_rss_feed)), null, null, null, null);
		cursor = loader.loadInBackground();
		adapter.changeCursor(cursor);
		adapter.notifyDataSetChanged();
		
		if(cursor == null) return;
		
		if(!cursor.isFirst()) {
			cursor.moveToFirst();
		}
		
		int count = cursor.getCount();
		String[] uris = new String[count];
		for(int index = 0; index < count; index++) {
			String uri = cursor.getString(cursor.getColumnIndex(RssFeedDao.RSS_FEED_URI));
			uris[index] = uri;
			cursor.moveToNext();
		}
		updateRssItems(uris);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(tag, "onOptionsItemSelected menu item: " + item);
		
		switch (item.getItemId()) {
		case R.id.main_navigation_refresh:
			refresh();
			return true;
			
		case R.id.main_content_new:
			addRssFeedUri();
			return true;
			
		case R.id.main_content_sync:
			syncRssFeedFromRemote();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		
	}

	private void syncRssFeedFromRemote() {
		syncProgress = new ProgressDialog(this);
		syncProgress.setTitle("Sync RssFeedUri From Remote");
		syncProgress.setMessage("努力同步中。。。");
		syncProgress.setProgress(100);
		syncProgress.setCancelable(true);
		syncProgress.setIndeterminate(false);
		syncProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
		final AsyncTask<String, Integer, Object> task = new AsyncTask<String, Integer, Object>() {

			@Override
			protected Object doInBackground(String... params) {
				Log.d(tag, "1.doInBackground: ");
				String url = params[0];
				HttpGet request = new HttpGet(url);
				DefaultHttpClient client = new DefaultHttpClient();
				RSSReader reader = new RSSReader();
				RSSFeed feed = null;
				try {
					HttpResponse response = client.execute(request);
					String json = EntityUtils.toString(response.getEntity());
					JSONArray array = new JSONArray(json);
					int size = array.length();
					for(int index = 0; index < size; index++) {
						JSONObject o = (JSONObject) array.get(index);
						String uri = o.getString("uri");
						if(dao.isDuplicate(uri)) continue;
						
						feed = reader.load(uri);
						dao.saveRssFeed(feed, uri);
						
						publishProgress((int) ((index / (float) size) * 100));
						if (isCancelled()) break;
					}
					
					return true;
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (RSSReaderException e) {
					e.printStackTrace();
				} finally {
					reader.close();
				}
				
				return false;
			}
			
			@Override
			protected void onProgressUpdate(Integer... values) {
				int value = values[0];
				syncProgress.setProgress(value);
				Log.d(tag, "onProgressUpdate="+value);
			}
			
			@Override
			protected void onPostExecute(Object result) {
				Log.d(tag, "3.onPostExecute");
				syncProgress.dismiss();
				if(result.equals(true)) {
					refresh();
				}
			}
			
		};
		
		task.execute(getString(R.string.config_param_sync_uri));
		
		syncProgress.setButton(ProgressDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				task.cancel(true);
				refresh();
			}
		});
		syncProgress.show();
	}

	private void addRssFeedUri() {
		rssUri = new EditText(this);
		rssUri.setText(DebugDataUtil.getUriByRandom());
		AlertDialog.Builder form = new AlertDialog.Builder(this);
		form.setView(rssUri);
		form.setTitle("Add RSS URI");
		form.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String uri = rssUri.getText().toString();
				Log.d(tag, "uri: " + uri);
				if(TextUtils.isEmpty(uri)) {
					Toast.makeText(MainActivity.this, "URI is not null", Toast.LENGTH_LONG).show();
				} else {
					boolean bln = dao.isDuplicate(uri);
					if(bln) {
						Toast.makeText(MainActivity.this, "重复的URI", Toast.LENGTH_LONG).show();
					} else {
						loadFeeds(uri);
					}
				}
			}
		});
		form.setNegativeButton("取消", null);
		form.show();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(l.getAdapter().getItem(position) instanceof HashMap<?, ?>) {
			HashMap<String, String> feed = (HashMap<String, String>) l.getAdapter().getItem(position);
			String uri = feed.get("uri");
			String title = feed.get("title");
			Intent intent = new Intent(this, RssItemListActivity.class);
			intent.putExtra("uri", uri);
			intent.putExtra("title", title);
			startActivity(intent);
		}
	}
	
	private void loadFeeds(final String uri) {
		loading = ProgressDialog.show(this, "Loading", "努力加载中。。。");
		new AsyncTask<String, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(String... params) {
				boolean isDuplicate = false;
				final String uri = params[0];
				RSSReader reader = new RSSReader();
				try {
					final RSSFeed _feeds = reader.load(uri);
					
					boolean bln = dao.saveRssFeed(_feeds, uri);
					Log.d(tag, "Rss Feed save: " + bln);
					isDuplicate = false;
					
					new Thread(new Runnable() {
						@Override
						public void run() {
							List<RSSItem> items = _feeds.getItems();
							if(items != null && !items.isEmpty()) {
								boolean bln = dao.saveRssItems(items, uri);
								Log.d(tag, "Rss Item save: " + bln);
							}
						}
					}).start();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					reader.close();
				}
				return isDuplicate;
			}
			
			@Override
			protected void onProgressUpdate(Void... values) {
				super.onProgressUpdate(values);
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				loading.dismiss();
				if(!result) {
					refresh();
				} else {
					Toast.makeText(MainActivity.this, "重复的URI", Toast.LENGTH_LONG).show();
				}
			}
		}.execute(uri);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		Log.d(tag, "onCreateLoader: id="+id+", bundle="+bundle);
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
		Log.d(tag, "onLoadFinished");
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		this.adapter.swapCursor(null);
		this.cursor = null;
		Log.d(tag, "onLoaderReset");
	}
	
	private void updateRssItems(final String[] uris) {
		
		new AsyncTask<String, Void, Map<String, List<RSSItem>>>() {
			@Override
			protected Map<String, List<RSSItem>> doInBackground(String... params) {
				RSSReader reader = new RSSReader();
				RSSFeed feed = null;
				try {
					Map<String, List<RSSItem>> items = new HashMap<String, List<RSSItem>>();
					for (String uri : params) {
						feed = reader.load(uri);
						items.put(uri, feed.getItems());
						dao.updateRSSFeed(uri, feed.getLastBuildDate());
					}
					
					return items;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					reader.close();
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(Map<String, List<RSSItem>> result) {
				if(result == null) {
					Toast.makeText(MainActivity.this, "无更新内容", Toast.LENGTH_LONG).show();
					return;
				}
				
				if(result.isEmpty()) {
					Toast.makeText(MainActivity.this, "无更新内容", Toast.LENGTH_LONG).show();
					return;
				}
				
				List<RSSItem> newItems = new ArrayList<RSSItem>();
				for(String uri : result.keySet()) {
					for(RSSItem item : result.get(uri)) {
						boolean isContain = dao.contain(item);
						if(isContain) continue;
						newItems.add(item);
					}
					
					if(!newItems.isEmpty()) {
						boolean bln = dao.saveRssItems(newItems, uri);
						Log.d(tag, uri + " update: " + bln + "["+newItems.size()+"]");
					}
					newItems.clear();
				}
				Toast.makeText(MainActivity.this, "更新完毕", Toast.LENGTH_LONG).show();
			}
			
		}.execute(uris);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(tag, "onItemLongClick: parent="+parent+", view="+view+", position="+position+", id="+id);
		Object o = parent.getItemAtPosition(position);
		HashMap<String, String> item = (HashMap<String, String>) o;
		final String originTitle = item.get("title");
		final String _id = item.get("_id");
		final EditText title = new EditText(this);
		title.setText(originTitle);
		AlertDialog.Builder form = new AlertDialog.Builder(this);
		form.setTitle("修改");
		form.setView(title);
		form.setNegativeButton(R.string.common_positive_cancel, null);
		form.setPositiveButton(R.string.common_negative_confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newTitle = title.getText().toString();
				if(TextUtils.isEmpty(newTitle)) return;
				if(newTitle.equals(originTitle)) return;
				
				new AsyncTask<String, Void, Object>() {
					@Override
					protected Object doInBackground(String... params) {
						String title = params[0];
						SQLiteDatabase db = dao.getWritableDatabase();
						ContentValues values = new ContentValues();
						values.put(RssFeedDao.RSS_FEED_TITLE, title);
						Integer rowid = db.update(RssFeedDao.RSS_FEED, values, "_id=?", new String[]{_id});
						db.close();
						return rowid;
					}
					
					protected void onPostExecute(Object result) {
						if(!result.equals(-1)) {
							refresh();
						}
					};
				}.execute(newTitle);
			}
		});
		form.show();
		return true;
	}
}
