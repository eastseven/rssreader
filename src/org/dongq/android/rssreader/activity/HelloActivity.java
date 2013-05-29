package org.dongq.android.rssreader.activity;

import org.dongq.android.rssreader.R;
import org.dongq.android.rssreader.utils.Constant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class HelloActivity extends Activity implements OnClickListener {

	private static final String tag = "RR_HelloActivity";
	
	private EditText email;
	private Button   login;
	
	private SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(tag, "onCreate");
		setContentView(R.layout.activity_hello);
		
		this.email = (EditText) findViewById(R.id.hello_email);
		this.login = (Button)   findViewById(R.id.hello_login);
		this.login.setOnClickListener(this);
		
		this.sp = getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
	}

	@Override
	public void onClick(View view) {
		String _email = email.getText().toString();
		if(TextUtils.isEmpty(_email)) {
			email.setError("请填写一个常用邮箱作为登录账号");
			return;
		}
		
		Editor editor = sp.edit();
		editor.putBoolean(Constant.IS_FIRST_LOGIN, false);
		editor.commit();
		
		//TODO 请求服务器，下载RssFeed列表信息
		
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}
}
