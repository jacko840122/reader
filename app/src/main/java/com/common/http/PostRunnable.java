package com.common.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;

import com.common.http.listener.NetListener;
import com.kuaxue.ebookstore.constant.NetErrorCode;

public class PostRunnable implements Runnable {
	private NetListener mListener;
	private HashMap<String, String> mParams;
	private String mUrl;

	private static final int RETRY_COUNT = 3;
	
	public PostRunnable(String url, HashMap<String, String> params,
			NetListener l) {
		mListener = l;
		mParams = params;
		mUrl = url;
	}

	private void withData() throws UnsupportedEncodingException {
		for (int i = 0; i < RETRY_COUNT; i++) {
			HttpPost httpPost = new HttpPost(mUrl);		
			if (mParams != null) {
				List<NameValuePair> vParams = NetHttp
						.buildPostMethod(mUrl, mParams);
				httpPost.setEntity(new UrlEncodedFormEntity(vParams, HTTP.UTF_8));
			}								
			try {
				HttpResponse response = HttpManager.execute(httpPost);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity(); 
					final InputStream in = entity.getContent();

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] b = new byte[1024];
					int n = 0;					
					while ((n = in.read(b)) != -1) {											
						out.write(b, 0, n);
					}
					in.close();
					String s = new String(out.toByteArray(), "UTF-8");
					
					if (mListener != null) {						
						mListener.onResult(NetHttp.getNetBaseResult(s));
					}
					entity.consumeContent();
					break;//成功，跳出循环
				} else {
					if (isLoopEnd(i) && (mListener != null)) {
						mListener.onResult(NetHttp.getNetBaseResult(response.getStatusLine().getStatusCode()));
					}
				}
			}catch(SocketTimeoutException e){			
				if (isLoopEnd(i) && (mListener != null)) {
					mListener.onResult(NetHttp
									.getNetBaseResult(NetErrorCode.NET_REQUEST_TIMEOUT));					
				}
				
				if(e != null){
					e.printStackTrace();
				}
			}catch (Exception e) {
				// TODO Auto-generated catch block
				if (isLoopEnd(i) && (mListener != null)) {
					mListener.onResult(NetHttp
							.getNetBaseResult(NetErrorCode.NET_REQUEST_EXCEPTION));					
				}
				if(e != null){
					e.printStackTrace();
				}
			} finally {
				if (httpPost != null)
					httpPost.abort();
			}
		}		
	}

	private boolean isLoopEnd(int count){
		return (count >= (RETRY_COUNT - 1));
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			withData();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
