package com.kuaxue.app.bookstore;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Environment;
import android.util.Log;

public class BookCacheData {

	private static final String AUTHORITY = "com.kuaxue.app.bookstore.db";	
	
	
	private static final String  TABLE_DOWNLOAD_BOOKS_INFO= "download";
	
	public static final Uri URI_DOWNLOAD_BOOKS_INFO=Uri.parse("content://"+AUTHORITY+"/"+TABLE_DOWNLOAD_BOOKS_INFO);
	
	
	public static final String CACHEDATA_DIR=new File(Environment.getExternalStorageDirectory(),".BooksCacheData").toString();

	public static int 	DOWNLOAD_STATUS_NONE=0;
	public static int 	DOWNLOAD_STATUS_DOWNLOADING=1;
	public static int 	DOWNLOAD_STATUS_FINISH=2;
	public static int 	DOWNLOAD_STATUS_ABORT=3;
	public static int   DOWNLOAD_STATUS_PAUSE=4;
	public static int 	DOWNLOAD_STATUS_ERROR=-1;
	
	

	public static final String TABLE_DOWNLOAD_BOOKS_INFO_ITEM_BOOK_ID="book_id";
	public static final String TABLE_DOWNLOAD_BOOKS_INFO_ITEM_DOWNLOAD_STATUS= "download_status";
	public static final String TABLE_DOWNLOAD_BOOKS_INFO_ITEM_DOWNLOAD_SIZE= "download_size";
	public static final String TABLE_DOWNLOAD_BOOKS_INFO_ITEM_FILE_LENGTH= "file_length";
	public static final String TABLE_DOWNLOAD_BOOKS_INFO_ITEM_URL= "Url"  ;   
	public static final String TABLE_DOWNLOAD_BOOKS_INFO_ITEM_PATH= "path";
	
	private static final String TABLE_DOWNLOAD_BOOKS_ITEMS_NAME[]={
		"id",
		"book_id",	
		"download_status",
		"download_size",
		"file_length",
		"Url",
		"path"
	};
	
	
	//TABLE_DOWNLOAD_BOOKS_ITEMS_NAME对应
	static public class DownloadBookInfo implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 954559044035028502L;
	
		protected int id;
		protected String book_id;
		protected int download_status;
		protected int  download_size;
		protected int file_length;
		protected String  Url;
		protected String  path;
	}
	
	
	
	public static DownloadBookInfo getDownloadBookInfo(Context context,String book_id){
		if(context==null||book_id==null) return null;
	
		Cursor cursor= context.getContentResolver().query(URI_DOWNLOAD_BOOKS_INFO, null, "book_id="+"\""+book_id+"\"", null, null);
		
		if(cursor.moveToNext()){			
			DownloadBookInfo downloadInfo=new DownloadBookInfo();
			
			for(int i=0;i<TABLE_DOWNLOAD_BOOKS_ITEMS_NAME.length;i++){
				try {
					Field field=DownloadBookInfo.class.getDeclaredField(TABLE_DOWNLOAD_BOOKS_ITEMS_NAME[i]);
					field.setAccessible(true);
					if(field.getType()==String.class){
						field.set(downloadInfo, cursor.getString(cursor.getColumnIndex(TABLE_DOWNLOAD_BOOKS_ITEMS_NAME[i])));
					}else if(field.getType()==int.class){
						field.set(downloadInfo, cursor.getInt(cursor.getColumnIndex(TABLE_DOWNLOAD_BOOKS_ITEMS_NAME[i])));
					}else if(field.getType()==long.class){
						field.set(downloadInfo, cursor.getLong(cursor.getColumnIndex(TABLE_DOWNLOAD_BOOKS_ITEMS_NAME[i])));
					}
					
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			cursor.close();	
			return downloadInfo;
		}
		if(cursor!=null){
			cursor.close();	
		}
		return null;
	}
	
	
	public static boolean UpdateDownloadBookInfo(Context context,DownloadBookInfo info){		
		if(context==null||info==null) return false;
	
		Cursor cursor= context.getContentResolver().query(URI_DOWNLOAD_BOOKS_INFO, null, "book_id="+"\""+info.book_id+"\"", null, null);
		
		if(cursor.moveToNext()){
			ContentValues values=new ContentValues();
			values.put(TABLE_DOWNLOAD_BOOKS_INFO_ITEM_PATH, info.path);
			values.put(TABLE_DOWNLOAD_BOOKS_INFO_ITEM_URL, info.Url);
			if(info.download_size>=0)
				values.put(TABLE_DOWNLOAD_BOOKS_INFO_ITEM_DOWNLOAD_SIZE, info.download_size);
			values.put(TABLE_DOWNLOAD_BOOKS_INFO_ITEM_DOWNLOAD_STATUS, info.download_status);
			if(info.file_length>0)
				values.put(TABLE_DOWNLOAD_BOOKS_INFO_ITEM_FILE_LENGTH, info.file_length);
			//context.getContentResolver().update(URI_DOWNLOAD_BOOKS_INFO, values, "book_id="+"\""+info.book_id+"\"", null);
			int id=cursor.getInt(cursor.getColumnIndex("id"));
			context.getContentResolver().update(Uri.withAppendedPath(URI_DOWNLOAD_BOOKS_INFO, Integer.toString(id)), values, null, null);
		}else{
			ContentValues values=new ContentValues();
			values.put(TABLE_DOWNLOAD_BOOKS_INFO_ITEM_BOOK_ID, info.book_id);
			values.put(TABLE_DOWNLOAD_BOOKS_INFO_ITEM_PATH, info.path);
			values.put(TABLE_DOWNLOAD_BOOKS_INFO_ITEM_URL, info.Url);
			if(info.download_size>=0)
				values.put(TABLE_DOWNLOAD_BOOKS_INFO_ITEM_DOWNLOAD_SIZE, info.download_size);
			values.put(TABLE_DOWNLOAD_BOOKS_INFO_ITEM_DOWNLOAD_STATUS, info.download_status);
			if(info.file_length>0)
				values.put(TABLE_DOWNLOAD_BOOKS_INFO_ITEM_FILE_LENGTH, info.file_length);
			context.getContentResolver().insert(URI_DOWNLOAD_BOOKS_INFO, values);
		}
		cursor.close();
		return true;
		
	}
	
	public static void DeleteDownloadBookInfo(Context context,String bookId){
		if(context==null||bookId==null) return;
	
		context.getContentResolver().delete(
				URI_DOWNLOAD_BOOKS_INFO, "book_id="+"\""+bookId+"\"", null);		
	}
	
	public static List<DownloadBookInfo> getDownloadBookInfos(Context context){	
		ArrayList<DownloadBookInfo> mArrayList = new ArrayList<DownloadBookInfo>();
		Cursor cursor= context.getContentResolver().query(URI_DOWNLOAD_BOOKS_INFO, 
				null,null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				DownloadBookInfo downloadInfo=new DownloadBookInfo();
				
				for(int i=0;i<TABLE_DOWNLOAD_BOOKS_ITEMS_NAME.length;i++){
					try {
						Field field=DownloadBookInfo.class.getDeclaredField(TABLE_DOWNLOAD_BOOKS_ITEMS_NAME[i]);
						field.setAccessible(true);
						if(field.getType()==String.class){
							field.set(downloadInfo, cursor.getString(cursor.getColumnIndex(TABLE_DOWNLOAD_BOOKS_ITEMS_NAME[i])));
						}else if(field.getType()==int.class){
							field.set(downloadInfo, cursor.getInt(cursor.getColumnIndex(TABLE_DOWNLOAD_BOOKS_ITEMS_NAME[i])));
						}else if(field.getType()==long.class){
							field.set(downloadInfo, cursor.getLong(cursor.getColumnIndex(TABLE_DOWNLOAD_BOOKS_ITEMS_NAME[i])));
						}						
					} catch (Exception e) {						
						e.printStackTrace();						
					}
				}
				mArrayList.add(downloadInfo);				
			}
			cursor.close();
			cursor = null;
		}
		
		return mArrayList;
	}
	
}	
