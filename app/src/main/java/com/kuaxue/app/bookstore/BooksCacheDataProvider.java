package com.kuaxue.app.bookstore;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

public class BooksCacheDataProvider extends ContentProvider{
	private  BooksCacheDataOpenHelper     mBooksCacheDataOpenHelper;
	private  UriMatcher		    	uriMatcher;
	
	private static final int		URI_DOWNLOAD_BOOKS_INFO			= 1;
	private static final int		URI_DOWNLOAD_BOOKS_INFO_ID			= 2;
	
	private static final String AUTHORITY 			= "com.kuaxue.app.bookstore.db";	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mBooksCacheDataOpenHelper.getWritableDatabase();
		int num = 0;
		
		int what = uriMatcher.match(uri);
        if ( what==URI_DOWNLOAD_BOOKS_INFO_ID) {
            StringBuilder sb = new StringBuilder();
            if (selection != null && selection.length() > 0) {
                sb.append("( ");
                sb.append(selection);
                sb.append(" ) AND ");
            }
            String id = uri.getPathSegments().get(1);
            sb.append("id = ");
            sb.append(id);
            selection = sb.toString();
        }
		switch (what) {
			case URI_DOWNLOAD_BOOKS_INFO:
				num = db.delete(BooksCacheDataOpenHelper.TABLE_DOWNLOAD_BOOKS_INFO, selection, selectionArgs);
				break;
				
			case URI_DOWNLOAD_BOOKS_INFO_ID:
				num = db.delete(BooksCacheDataOpenHelper.TABLE_DOWNLOAD_BOOKS_INFO, selection, selectionArgs);
				break;	
			default:
				break;
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return num;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}
	
	
    /**
     * Concatenates two SQL WHERE clauses, handling empty or null values.
     */
    private String concatenateWhere(String a, String b) {
        if (TextUtils.isEmpty(a)) {
            return b;
        }
        if (TextUtils.isEmpty(b)) {
            return a;
        }

        return "(" + a + ") AND (" + b + ")";
    }

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		SQLiteDatabase db = mBooksCacheDataOpenHelper.getWritableDatabase();
		long rowId = -1;
		
		int what = uriMatcher.match(uri);
        if (what!= URI_DOWNLOAD_BOOKS_INFO) {
            // Can only insert into to main URI.
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        rowId = db.insert(BooksCacheDataOpenHelper.TABLE_DOWNLOAD_BOOKS_INFO, null, values);

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            Uri noteUri = Uri.parse(BookCacheData.URI_DOWNLOAD_BOOKS_INFO + "/" + Long.toString(rowId));
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);

	}

	@Override
	public boolean onCreate() {
		mBooksCacheDataOpenHelper = new BooksCacheDataOpenHelper(getContext());
		
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "download", URI_DOWNLOAD_BOOKS_INFO);
		uriMatcher.addURI(AUTHORITY, "download/#", URI_DOWNLOAD_BOOKS_INFO_ID);

		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		SQLiteDatabase db = mBooksCacheDataOpenHelper.getReadableDatabase();
		
		int what = uriMatcher.match(uri);
        String whereClause = null;
        if ( what==URI_DOWNLOAD_BOOKS_INFO_ID){
            whereClause = "id = " + uri.getPathSegments().get(1);
        }
		switch (what) {
		
			case URI_DOWNLOAD_BOOKS_INFO:
			case URI_DOWNLOAD_BOOKS_INFO_ID:	
				cursor = db.query(BooksCacheDataOpenHelper.TABLE_DOWNLOAD_BOOKS_INFO, projection, concatenateWhere(selection,whereClause), selectionArgs, null, null, sortOrder);
				break;	
				
			default:
				break;
		}
		
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mBooksCacheDataOpenHelper.getWritableDatabase();
		int num = 0;
		
		int what = uriMatcher.match(uri);
        String whereClause = null;
        if (what==URI_DOWNLOAD_BOOKS_INFO_ID){
            whereClause = "id = " + uri.getPathSegments().get(1);
        }
		switch (what) {		
			case URI_DOWNLOAD_BOOKS_INFO:
			case URI_DOWNLOAD_BOOKS_INFO_ID:	
				num = db.update(BooksCacheDataOpenHelper.TABLE_DOWNLOAD_BOOKS_INFO, values, concatenateWhere(selection,whereClause), selectionArgs);
				break;	
				
			default:
				break;
		}
	
		getContext().getContentResolver().notifyChange(uri, null);
		return num;
	}

	
	@Override
	public void shutdown() {
		mBooksCacheDataOpenHelper.close();
		super.shutdown();
	}


	private class BooksCacheDataOpenHelper extends SQLiteOpenHelper{
		private static final String   DBNAME  					= "BooksCacheData.db";
		private static final int   	  VERSION_1					= 1;
		
		private static final String    TABLE_DOWNLOAD_BOOKS_INFO= "download_books_info";
	
		
		public BooksCacheDataOpenHelper(Context context) {
			super(context, DBNAME, null, VERSION_1);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			//建立备注Table

	
			String sql = "CREATE TABLE  IF NOT EXISTS "+ TABLE_DOWNLOAD_BOOKS_INFO + "("
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "book_id TEXT,"
                    + "download_status INTEGER,"
                    + "download_size INTEGER,"
                    + "file_length INTEGER,"
                    + "Url TEXT,"     
                    + "path TEXT);";
			db.execSQL(sql);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	
	}
}
