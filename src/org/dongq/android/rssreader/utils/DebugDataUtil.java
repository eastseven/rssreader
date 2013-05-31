package org.dongq.android.rssreader.utils;

import java.util.Random;

/**
 * 调试用数据工具类
 * @author eastseven
 *
 */
public final class DebugDataUtil {

	final static String[] randomUris = {
			"http://www.importnew.com/feed",
			"http://programmer.csdn.net/rss_programmer.html",
			"http://coolshell.cn/feed",
			"http://www.raychase.net/feed",
			"http://blog.sina.com.cn/rss/1581720921.xml",
			"http://blog.csdn.net/rss.html",
			"http://www.infoq.com/cn/rss/rss.action?token=b7jzTwJLcjcXt421su1fH4XSWSDSeoBF"
	};
	
	public static String getUriByRandom() {
		Random r = new Random();
		String uri = randomUris[r.nextInt(randomUris.length)];
		return uri;
	}
}
