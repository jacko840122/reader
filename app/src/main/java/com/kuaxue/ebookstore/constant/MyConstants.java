package com.kuaxue.ebookstore.constant;

import android.os.Environment;

public class MyConstants {
	public static final boolean DEBUG = true;
	
	public final static String SCAN_PACKAGE = "com.ozing.scan";
	public final static String TEXTDOWN_PACKAGE = "com.kuaxue.tangfei.ui";
	
	public static final String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();
	public final static String PICTURE_DIR = ROOT_DIR + "/synclesson/picture/";
	
	public static final String EXTENSION_NAME = "tb";	
	public final static String SAVE_EXTENSION_NAME = ".save";		
	
	public static final String ACTION_TOKEN_ERROR = "com.synclesson.activity.action_token_error";
	public static final String ACTION_REFRESH_COMMENT = "com.synclesson.activity.action_refresh_comment";
	
	public static String SAVE_DIR = null; 	
	public static String getSAVE_DIR() {
		return SAVE_DIR;
	}
	
	public static void setSAVE_DIR(String save_dir) {
		SAVE_DIR = save_dir;
	}	
	
	private static float density = 2.0f;	
	
	public static float getDensity() {
		return density;
	}

	public static void setDensity(float density) {
		MyConstants.density = density;
	}

	private static String bookName;
	public static String getBookName() {
		return bookName;
	}

	public static void setBookName(String bookName) {
		MyConstants.bookName = bookName;
	}	
	
	private static String subject = null;	
	public static String getSubject() {
		return subject;
	}

	public static void setSubject(String subject) {
		MyConstants.subject = subject;
	}

	public static final String BASE_IP = "http://shenzhen.kuaxue.com/";
	
	public static final String COMMENT = BASE_IP + "comment/app/";	
	public static final String ADDCOMMENT = COMMENT + "addComment";
	public static final String GETCOMMENTS = COMMENT + "getComments";
	
	
}
