package com.kuaxue.app.bookstore;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Comment implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5600604583566039443L;
	
	public String userName;// 作者
	
	public String target;// 被评论者ID
	
	public String type; // 类型
	
	public String content;// 内容
	
	public int star; // 得分
	
	public long createTime;

	private final static String TYPE_BOOK = "book";
	
	
	public JSONObject toObject(){
		JSONObject jsonObject=new JSONObject();
		try {
			jsonObject.put("userName", userName);
			jsonObject.put("target", target);
			jsonObject.put("type", TYPE_BOOK);
			jsonObject.put("content", content);
			jsonObject.put("star", star);
			long time=System.currentTimeMillis();
			jsonObject.put("createTime", time);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;
		
	}
	
	public boolean  setJson(JSONObject jsonObject){
		boolean ret=false;
		if(jsonObject==null) return ret;
		try {
			userName=(String) jsonObject.get("userName");
			target=(String) jsonObject.get("target");
			type=(String) jsonObject.get("type");
			content=(String) jsonObject.get("content");
			star=((Integer)(jsonObject.get("star"))).intValue();
			createTime=(Long) jsonObject.get("createTime");
		} catch (JSONException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return ret;
		
	}
	 

}
