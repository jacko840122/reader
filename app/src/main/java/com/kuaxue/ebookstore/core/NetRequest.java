package com.kuaxue.ebookstore.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.kuaxue.account.Account;
import com.kuaxue.app.bookstore.Comment;

import android.content.Context;
import android.os.RemoteException;
import android.telephony.TelephonyManager;

import com.common.http.Md5;
import com.common.http.NetHttp;
import com.common.http.listener.NetListener;
import com.kuaxue.ebookstore.constant.MyConstants;


public class NetRequest {	
	private static Account mAccount;
	private static String token = "";
	private static final String version = "android";	

	public static void setToken(String token) {
		NetRequest.token = token;
	}	
	
	public static void setAccount(Account account) {
		NetRequest.mAccount = account;
	}	
	
	public static String getToken() {
		return token;
	}
	
	public static void addComment(Comment mComment,NetListener l,Context context){	
		JSONObject mJsonObject = mComment.toObject();
				
		postWithToken(MyConstants.ADDCOMMENT,mJsonObject, l,context);
	}
	
	public static void getComments(String target,int skip,int limit,NetListener l,Context context){	
		JSONObject mJsonObject = new JSONObject();
		try {
			mJsonObject.put("skip", skip);
			mJsonObject.put("limit", limit);
			mJsonObject.put("target", target);
		} catch (JSONException e) {
			e.printStackTrace();
		}
				
		postWithToken(MyConstants.GETCOMMENTS,mJsonObject, l,context);
	}
	
	/////////////////////////////////////////////////////////
	public static void postNoToken(String url,JSONObject jsonObject,NetListener l){	
		HashMap<String, String> mHashMap = new HashMap<String, String>();
		if (jsonObject != null) {
			mHashMap.put("param", jsonObject.toString());
		}		
		
		NetHttp.net.post(url,mHashMap,l);		
	}
	
	public static void postWithToken(String url,JSONObject jsonObject,NetListener l,Context context){						
		HashMap<String, String> mHashMap = getTokenParam(context);
		
		if (jsonObject != null) {
			mHashMap.put("param", jsonObject.toString());
		}		
		
		NetHttp.net.post(url,mHashMap, l);
	}	
	
	public static HashMap<String, String> getTokenParam(Context context){
		long unique = System.currentTimeMillis();
		String uni = "" + unique + "_1234567890123" + "_" + getDeviceId(context);
		String signature="";
		if(mAccount!=null){
			try {
				signature = mAccount.getSignature(uni);
				token=mAccount.getToken();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		HashMap<String, String> mHashMap = new HashMap<String, String>();
		mHashMap.put("token", token);		
		mHashMap.put("nonc", uni);		
				
//		String arr[] = {token,uni,version};
//		Arrays.sort(arr);
//		StringBuilder content = new StringBuilder();
//		for(String s:arr){
//			content.append(s);
//		}
		mHashMap.put("signature", signature);
		
		return mHashMap;		
	}	
	
	public static String buildGetMethod(String url, Map<String, String> params) {
		String p = "";
		int flag = 0;
		if (params != null) {
			for (Map.Entry<String, String> m : params.entrySet()) {
				try {
					String va = URLEncoder.encode(m.getValue(), "UTF-8");
					if (flag == 0) {
						p += (m.getKey() + "=" + va);
					} else {
						p += ("&" + m.getKey() + "=" + va);
					}
				} catch (UnsupportedEncodingException e) {					
					e.printStackTrace();
				}
				flag++;
			}
		}
		return url + "?" + p;
	}
	
	private static String sDeviceId = null;
	public static String getDeviceId(Context context) {
		if (sDeviceId == null) {
			TelephonyManager tm = (TelephonyManager)context
					.getSystemService(Context.TELEPHONY_SERVICE);
			String tmDevice, tmSerial, androidId;
			tmDevice = "" + tm.getDeviceId();
			tmSerial = "" + tm.getSimSerialNumber();
			androidId = ""
					+ android.provider.Settings.Secure.getString(
							context.getContentResolver(),
							android.provider.Settings.Secure.ANDROID_ID);
			UUID deviceUuid = new UUID(androidId.hashCode(),
					((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
			sDeviceId = deviceUuid.toString();
		}
		return sDeviceId;
	}	
}
