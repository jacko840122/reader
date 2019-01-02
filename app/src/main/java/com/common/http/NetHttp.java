package com.common.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.common.http.listener.NetListener;
import com.kuaxue.ebookstore.core.NetRequest;

public class NetHttp {
	public static NetHttp net = new NetHttp();

	private List<Future<?>> mList = new ArrayList<Future<?>>();
	private ThreadPoolExecutor threadPool;
	
	public static final int CORE_POOL_SIZE = 8;

	public NetHttp() {
		threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		threadPool.setCorePoolSize(CORE_POOL_SIZE);
	}

	public void post(String url, HashMap<String, String> params,
			NetListener l) {		
		Future<?> f = threadPool.submit(new PostRunnable(url, params, l));
		mList.add(f);
	}

	public void cancel() {
		for (Future<?> f : mList) {
			if (f != null) {
				if (!f.isDone()) {
					f.cancel(true);
				}
			}
			mList.remove(f);
		}
	}
	
	public static String buildGetMethod(String url, Map<String, String> params) {
		String p = "";
		int flag = 0;
		if (params != null) {
			for (Map.Entry<String, String> m : params.entrySet()) {
				try {
					String va = URLEncoder.encode(m.getValue(), "UTF-8");					
					if (flag != 0) {
						p += "&";						
					}					
					if (m.getKey().equals("param")) {
						p += m.getKey() + "={" + va + "}";
					}else{
						p += m.getKey() + "=" + va;
					}					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				flag++;
			}
		}
		return url + "?" + p;
	}
	
	public static List<NameValuePair> buildPostMethod(String url,
			Map<String, String> params) {
		List<NameValuePair> retParams = new ArrayList<NameValuePair>();
		if (params != null) {
			for (Map.Entry<String, String> m : params.entrySet()) {
				retParams.add(new BasicNameValuePair(m.getKey(), m.getValue()));
			}
		}
		return retParams;
	}
	
	public static BaseResult getNetBaseResult(int code) {
		BaseResult result = new BaseResult();
		result.errorCode = code;		
		return result;
	}
	
	public static BaseResult getNetBaseResult(String s)
			throws JSONException {
		BaseResult result = new BaseResult();
		JSONObject obj = new JSONObject(s);		
		if (obj.has("errorCode")) {
			result.errorCode = obj.getInt("errorCode");
		}				
		
		if (obj.has("time")) {
			result.time = obj.getLong("time");
		}
						
		if (obj.has("ret")) {
			result.object = obj.getJSONObject("ret");		

			if (result.object != null && result.object.has("token")) {
				String token = result.object.getString("token");				
				if (!TextUtils.isEmpty(token) && !token.equals("null")) {
					NetRequest.setToken(token);
					result.token = token;					
				}				
			}
		}		

		
		return result;
	}
}
