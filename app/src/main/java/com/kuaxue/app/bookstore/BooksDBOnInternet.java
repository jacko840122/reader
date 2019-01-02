package com.kuaxue.app.bookstore;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class BooksDBOnInternet {

	private static final String AUTHORITY = "com.kuaxue.syncdata.SyncDataProvider";	
	
	private static final String  TABLE_BOOK= "book";
	
	private static final Uri URI_BOOK=Uri.parse("content://"+AUTHORITY+"/"+TABLE_BOOK);
	
	
	private static final String  TABLE_SUMMARY= "summary";
	
	private static final Uri URI_SUMMARY=Uri.parse("content://"+AUTHORITY+"/"+TABLE_SUMMARY);
	
	private static final String TABLE_BOOK_ITEMS_NAME[]={
		"id",
		"type",//		private String		type;				//类型
		"name",//		private String		name;				//书名
		"publishingHouse",//		private String		publishingHouse;	//出版社
		"author",//		private String      author;				//作者
		"version",//		private String		version;			//版本
		"date",//		private String		date;				//出版日期
		"summary",//		private String		summary;			//简介
		"image",//		private String		image;				//图片
		"path",//		private String		path;				//下载路径
		"length",//		private String		length;				//文件大小
		"focus", //		private int			focus;				//热门图书
		//"syncKey"//		private long		syncKey;
	};
	
	
	private static final String TABLE_SUMMARY_ITEMS_NAME[]={
		"downloadTimes",//		private 	int			downloadTimes;	//下载次数
		"commentTimes",//		private 	int			commentTimes;	//评论次数，对视频、教材和课外书有效
		"star",//		private     String		star;			//平均星级

	};
	
	//TABLE_BOOK_ITEMS_NAME和TABLE_SUMMARY_ITEMS_NAME对应
	static public class BookInfo implements Serializable , Parcelable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -3925519331623133805L;
		protected String		id;				    //类型
		protected String		type;				//类型
		protected String		name;				//书名
		protected String		publishingHouse;	//出版社
		protected String      	author;				//作者
		protected String		version;			//版本
		protected String		date;				//出版日期
		protected String		summary;			//简介
		protected String		image;				//图片
		protected String		path;				//下载路径
		protected String		length;				//文件大小
		protected String		focus;				//热门图书
		
		protected 	String			downloadTimes;	//下载次数
		protected 	String			commentTimes;	//评论次数，对视频、教材和课外书有效
		protected     String		star;			//平均星级
		
		
		
	     public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}

		public BookInfo(){
	    	 
	     }
		
		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			// TODO Auto-generated method stub
			dest.writeString(id);				    //类型
			dest.writeString(type);				//类型
			dest.writeString(name);				//书名
			dest.writeString(publishingHouse);	//出版社
			dest.writeString(author);				//作者
			dest.writeString(version);			//版本
			dest.writeString(date);				//出版日期
			dest.writeString(summary);			//简介
			dest.writeString(image);				//图片
			dest.writeString(path);				//下载路径
			dest.writeString(length);				//文件大小
			dest.writeString(focus);			//热度
			
			dest.writeString(downloadTimes);	//下载次数
			dest.writeString(commentTimes);	//评论次数，对视频、教材和课外书有效
			dest.writeString(star);			//平均星级
			
			
		}
		
		public static final Parcelable.Creator<BookInfo> CREATOR=new Creator<BookInfo>() {

			@Override
			public BookInfo createFromParcel(Parcel source) {
				// TODO Auto-generated method stub
				return new BookInfo(source);
			}

			@Override
			public BookInfo[] newArray(int size) {
				// TODO Auto-generated method stub
				return new BookInfo[size];
			}
		};
		
	     private BookInfo(Parcel in) {
	    	 id				=in.readString();				    //类型
	    	 type			=in.readString();				//类型
	    	 name			=in.readString();				//书名
	    	 publishingHouse=in.readString();	//出版社
	    	 author			=in.readString();				//作者
	    	 version		=in.readString();			//版本
	    	 date			=in.readString();				//出版日期
	    	 summary		=in.readString();			//简介
	    	 image			=in.readString();				//图片
	    	 path			=in.readString();				//下载路径
	    	 length			=in.readString();				//文件大小
	    	 focus			=in.readString();			//热度	
				
	    	 downloadTimes	=in.readString();	//下载次数
	    	 commentTimes	=in.readString();	//评论次数，对视频、教材和课外书有效
	    	 star			=in.readString();			//平均星级
	    	
	     }




		//protected long		syncKey;
	}
	
	
	public static ArrayList<BookInfo>SortBookInfoListByFocus(ArrayList<BookInfo> Booklist){
		ArrayList<BookInfo> bookInfos = new ArrayList<BooksDBOnInternet.BookInfo>();
		
		for(BookInfo bookInfo:Booklist){
			if("1".equals(bookInfo.focus)){
				bookInfos.add(bookInfo);
			}
		}
		
		return bookInfos;
	}
	
	
	public static ArrayList<BookInfo>getBookInfoList(Context context){
		ArrayList<BookInfo> BooksList=new ArrayList<BookInfo>();
		if(context==null) return BooksList;
		Cursor cursor=null;
		try{
			cursor=context.getContentResolver().query(URI_BOOK,  TABLE_BOOK_ITEMS_NAME, null, null, null);
			while(cursor.moveToNext()){			
				BookInfo bookinfo=new BookInfo();
				
				for(int i=0;i<TABLE_BOOK_ITEMS_NAME.length;i++){
					try {
						Field field=BookInfo.class.getDeclaredField(TABLE_BOOK_ITEMS_NAME[i]);
						field.setAccessible(true);
						if(field.getType()==String.class){
							field.set(bookinfo, cursor.getString(cursor.getColumnIndex(TABLE_BOOK_ITEMS_NAME[i])));
						}else if(field.getType()==int.class){
							field.set(bookinfo, cursor.getInt(cursor.getColumnIndex(TABLE_BOOK_ITEMS_NAME[i])));
						}else if(field.getType()==long.class){
							field.set(bookinfo, cursor.getLong(cursor.getColumnIndex(TABLE_BOOK_ITEMS_NAME[i])));
						}
						
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
				Cursor cursor2=context.getContentResolver().query(URI_SUMMARY,  TABLE_SUMMARY_ITEMS_NAME, "id="+"\""+bookinfo.id+"\"", null, null);
				if(cursor2.moveToNext()){
					for(int i=0;i<TABLE_SUMMARY_ITEMS_NAME.length;i++){
						try {
							Field field=BookInfo.class.getDeclaredField(TABLE_SUMMARY_ITEMS_NAME[i]);
							field.setAccessible(true);
							if(field.getType()==String.class){
								field.set(bookinfo, cursor2.getString(cursor2.getColumnIndex(TABLE_SUMMARY_ITEMS_NAME[i])));
							}else if(field.getType()==int.class){
								field.set(bookinfo, cursor2.getInt(cursor2.getColumnIndex(TABLE_SUMMARY_ITEMS_NAME[i])));
							}else if(field.getType()==long.class){
								field.set(bookinfo, cursor2.getLong(cursor2.getColumnIndex(TABLE_SUMMARY_ITEMS_NAME[i])));
							}
							
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				}
				if(cursor2!=null){
					cursor2.close();	
				}
				BooksList.add(bookinfo);
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if(cursor!=null){
			cursor.close();	
		}

		return BooksList;
		
	}
	
	public static Map<String, ArrayList<BookInfo>> SortBookInfoListByType(ArrayList<BookInfo> Booklist){
		Map<String, ArrayList<BookInfo>> map=new HashMap<String, ArrayList<BookInfo>>();
		if(Booklist==null||Booklist.size()<=0) return map;
		for(int i=0,count=Booklist.size();i<count;i++){
			String type=Booklist.get(i).type;
			if(map.containsKey(type)){
				ArrayList<BookInfo> list=map.get(type);
				if(list==null){
					list=new ArrayList<BookInfo>();
					map.put(type, list);
				}
				list.add(Booklist.get(i));
			}else{
				ArrayList<BookInfo> list2=new ArrayList<BookInfo>();
				list2.add(Booklist.get(i));
				map.put(type, list2);
			}
		}
		return map;

	}
	
	public static BookInfo getBookInfoByName(Context context,String selection){	
		BookInfo bookinfo = new BookInfo();
		if(context==null||selection==null) return null;
		Cursor cursor=null;
		try{
			cursor=context.getContentResolver().query(URI_BOOK, TABLE_BOOK_ITEMS_NAME,
					selection, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				for(int i=0;i<TABLE_BOOK_ITEMS_NAME.length;i++){
					try {
						Field field=BookInfo.class.getDeclaredField(TABLE_BOOK_ITEMS_NAME[i]);
						field.setAccessible(true);
						if(field.getType()==String.class){
							field.set(bookinfo, cursor.getString(cursor.getColumnIndex(TABLE_BOOK_ITEMS_NAME[i])));
						}else if(field.getType()==int.class){
							field.set(bookinfo, cursor.getInt(cursor.getColumnIndex(TABLE_BOOK_ITEMS_NAME[i])));
						}else if(field.getType()==long.class){
							field.set(bookinfo, cursor.getLong(cursor.getColumnIndex(TABLE_BOOK_ITEMS_NAME[i])));
						}
						
					} catch (Exception e) {					
						e.printStackTrace();
					}
				}				
			}			
			
			Cursor cursor2=context.getContentResolver().query(URI_SUMMARY,  TABLE_SUMMARY_ITEMS_NAME, "id="+"\""+bookinfo.id+"\"", null, null);
			if(cursor2 != null){
				cursor2.moveToFirst();
				for(int i=0;i<TABLE_SUMMARY_ITEMS_NAME.length;i++){
					try {
						Field field=BookInfo.class.getDeclaredField(TABLE_SUMMARY_ITEMS_NAME[i]);
						field.setAccessible(true);
						if(field.getType()==String.class){
							field.set(bookinfo, cursor2.getString(cursor2.getColumnIndex(TABLE_SUMMARY_ITEMS_NAME[i])));
						}else if(field.getType()==int.class){
							field.set(bookinfo, cursor2.getInt(cursor2.getColumnIndex(TABLE_SUMMARY_ITEMS_NAME[i])));
						}else if(field.getType()==long.class){
							field.set(bookinfo, cursor2.getLong(cursor2.getColumnIndex(TABLE_SUMMARY_ITEMS_NAME[i])));
						}
						
					} catch (Exception e) {						
						e.printStackTrace();
					}
				}			
			}
			if(cursor2!=null){
				cursor2.close();	
			}
		}catch (Exception e) {			
			e.printStackTrace();
		}		

		if (cursor != null) {
			cursor.close();
		}
		return bookinfo;		
	}

	
	
}
