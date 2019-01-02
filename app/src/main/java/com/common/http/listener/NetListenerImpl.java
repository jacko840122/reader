package com.common.http.listener;

import android.os.Handler;
import android.os.Message;

import com.common.http.BaseResult;

public class NetListenerImpl implements NetListener {	
	private int msg = 0;
	private Handler mHandler = null;
	
	public NetListenerImpl(int msg,Handler mHandler){
		this.msg = msg;
		this.mHandler = mHandler;
	}
	
	@Override
	public void onResult(BaseResult result) {
		// TODO Auto-generated method stub
		Message message = new Message();
		message.what = msg;
		message.obj = result;
		mHandler.sendMessage(message);		
	}
}
