package com.kuaxue.app.bookstore;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.greenlemonmobile.app.ebook.MyMd5;
import com.kuaxue.account.Account;
import com.kuaxue.app.bookstore.BookCacheData.DownloadBookInfo;
import com.kuaxue.app.bookstore.BooksDBOnInternet.BookInfo;
import com.kuaxue.download.Download;
import com.kuaxue.download.Download.OnDownloadListener;
import com.kuaxue.ebookstore.core.NetRequest;

public class DownLoadService extends Service {

	public static final String ACTION_DELETE_DOANLOAD_EBOOK="com.kuaxue.app.bookstore.delete.download.ebook";
	public static final String ACTION_DOANLOAD_EBOOK="com.kuaxue.app.bookstore.download.ebook";
	public static final String ACTION_PAUSE_DOANLOAD_EBOOK="com.kuaxue.app.bookstore.pause.download.ebook";
	public static final String ACTION_DOANLOAD_EBOOK_COVER="com.kuaxue.app.bookstore.download.ebook.cover";
	
	public static final String ACTION_DOANLOAD_EBOOK_UPDATE_COVER_RECIEVER="com.kuaxue.app.bookstore.updatecover";
	public static final String ACTION_DOANLOAD_EBOOK_RECIEVER="com.kuaxue.app.bookstore.download.ebook";
	
	public static final String ACTION_DOANLOAD_EBOOK_PROGRSS="com.kuaxue.app.bookstore.download.progress";
	public static final String ACTION_DOANLOAD_EBOOK_REMOVE="com.kuaxue.app.bookstore.download.remove";
	
	private ServiceConnection mConnection;
	private Account mAccount;
	private static ArrayList<EbookDownload> mEbookDownloadList = new ArrayList<DownLoadService.EbookDownload>();
	private static ArrayList<EbookDownload> mEbookDownQueneList = new ArrayList<DownLoadService.EbookDownload>();
	private ArrayList<BookInfo> mDowndLoadBooksCoverList;
	private final static int MSG_PROGRESS=1;
	private final static int MSG_FINISH=2;
	private final static int MSG_EXCEPTION=3;
	private final static int MSG_PAUSE=4;
	private final static int MSG_DELETE=5;
	
	private final static int MSG_ADDDOWN = 6;
	
	private final static int MAX_DOWN = 5;
	private final static byte[] mLock = new byte[0];
	
	private void StartSyncDataAndAccountService(){
		Intent intent = new Intent("com.kuaxue.syncdata.SyncDataService");
		startService(intent);
		intent = new Intent("com.kuaxue.account.AccountService");
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
		
	private void StopSyncDataAndAccountService(){
		unbindService(mConnection);
	}
	
	private class EbookDownload extends Download{
		DownloadBookInfo mDownloadInfo;
		public boolean isStop = false;

		public EbookDownload(DownloadBookInfo downloadInfo,Account account, String path, 
				String filename,OnDownloadListener listener) {
			super(account,  new File(filename), path,listener);
			mDownloadInfo=downloadInfo;		
		}		
	}
	
	public abstract class EbookOnDownloadListener implements OnDownloadListener{		
		private EbookDownload mEbookDownload=null;
		private long lastProgress = 0;
			
		public long getLastProgress() {
			return lastProgress;
		}

		public void setLastProgress(long lastProgress) {
			this.lastProgress = lastProgress;
		}

		void SetEbookOnDownload(EbookDownload ebookDownload){
			mEbookDownload=ebookDownload;
		}
		
		EbookDownload GetEbookOnDownload(){
			return mEbookDownload;
		}			
	}
	
	@Override
	public void onCreate() {		
		super.onCreate();
		
		mConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				mAccount = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mAccount = Account.Stub.asInterface(service);
				if(mAccount!=null){
					NetRequest.setAccount(mAccount);
				}
			}
		};
		
//		mEbookDownloadList=new ArrayList<DownLoadService.EbookDownload>();
		mDowndLoadBooksCoverList=new ArrayList<BooksDBOnInternet.BookInfo>();
		
		StartSyncDataAndAccountService();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		StopSyncDataAndAccountService();
	}
		
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public static EbookDownload GetDownloadInDownloadList(String book_id){
		synchronized (mLock) {
			if(mEbookDownloadList!=null && !mEbookDownloadList.isEmpty()){
				for (EbookDownload mEbookDownload : mEbookDownloadList) {
					if(mEbookDownload.mDownloadInfo.book_id.equals(book_id)){
						return mEbookDownload;
					}
				}				
			}
			
			if(mEbookDownQueneList!= null && !mEbookDownQueneList.isEmpty()){
				for (EbookDownload mEbookDownload : mEbookDownQueneList) {
					if(mEbookDownload.mDownloadInfo.book_id.equals(book_id)){
						return mEbookDownload;
					}
				}				
			}			
			
			return null;
		}
	}
	
	private boolean IsInDownloadList(String book_id){
		synchronized (mDowndLoadBooksCoverList) {
			if(mDowndLoadBooksCoverList==null||mDowndLoadBooksCoverList.isEmpty()) return false;
			for(int i=0,count=mDowndLoadBooksCoverList.size();i<count;i++){
				BookInfo bookInfo=mDowndLoadBooksCoverList.get(i);
				if(bookInfo.id.equals(book_id)){
					return true;
				}
			}
			return false;
		}
	}
	
	private boolean coverIsExist(String bookImage){
		String path = BookCacheData.CACHEDATA_DIR + File.separator + MyMd5.getMd5String(bookImage) +".img";;
		return new File(path).exists();		
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		if(intent==null||intent.getAction()==null) return;
		super.onStart(intent, startId);
		String action = intent.getAction();
		if(ACTION_DOANLOAD_EBOOK.equals(action)||ACTION_PAUSE_DOANLOAD_EBOOK.equals(action)
				|| ACTION_DELETE_DOANLOAD_EBOOK.equals(action)){			
			BookInfo mBookInfo = (BookInfo)intent.getSerializableExtra("BookInfo");
			if (mBookInfo != null) {
				Message msg = new Message();
				msg.obj = mBookInfo;
				if (ACTION_DELETE_DOANLOAD_EBOOK.equals(action)) {
					msg.what = MSG_DELETE;					
				}else if(ACTION_PAUSE_DOANLOAD_EBOOK.equals(action)){	
					msg.what = MSG_PAUSE;					
				}else{
					msg.what = MSG_ADDDOWN;
				}
				mHandler.sendMessage(msg);
			}					
		}else if(ACTION_DOANLOAD_EBOOK_COVER.equals(intent.getAction())){
			final ArrayList<BookInfo> bookinfoS=intent.getParcelableArrayListExtra("BookInfos");
			if(bookinfoS==null) return;
			new Thread(new Runnable() {
				
				@Override
				public void run() {				
					int cn=3;
					while(mAccount==null&&cn>0){
						
						StartSyncDataAndAccountService();
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {							
							e.printStackTrace();
						}
						--cn;
					}
					if(mAccount==null) return;
					for(BookInfo bookinfo:bookinfoS){			
							if(!IsInDownloadList(bookinfo.id) && !coverIsExist(bookinfo.image)){								
								while(mDowndLoadBooksCoverList.size()>5);
									
								synchronized (mDowndLoadBooksCoverList) {
									mDowndLoadBooksCoverList.add(bookinfo);
								}
								DownLoadCover(bookinfo);
							}
						}
				}
			}).start();				
		}		
	}	
	
	private void DownLoadCover(BookInfo bookinfo){
		final String fileDir=BookCacheData.CACHEDATA_DIR;
		final String filePath=fileDir+File.separator+MyMd5.getMd5String(bookinfo.getImage())+".img";
		final String TempfilePath=filePath+".tmp";
		File Dir =new File(fileDir);
		final File file =new File(filePath);
		
		try {
			if(!Dir.exists()){
				Dir.mkdirs();
			}
			
			final BookInfo bookinfo2=bookinfo;
			final OnDownloadListener listener=new OnDownloadListener() {
				
				@Override
				public void progress(long postion, long length) {

				}
				
				@Override
				public void onFinish(String md5) {
					File TmpFile=new File(TempfilePath);
					file.delete();
					TmpFile.renameTo(file);
					Intent intent=new Intent(ACTION_DOANLOAD_EBOOK_UPDATE_COVER_RECIEVER);
					intent.putExtra("bookinfo", (Serializable)bookinfo2);
					intent.putExtra("filePath", filePath);
					DownLoadService.this.sendBroadcast(intent);		
					synchronized (mDowndLoadBooksCoverList) {
						mDowndLoadBooksCoverList.remove(bookinfo2);
					}										
				}

				@Override
				public void onException() {
					new File(TempfilePath).delete();
					synchronized (mDowndLoadBooksCoverList) {
						mDowndLoadBooksCoverList.remove(bookinfo2);
					}
				}
			};
			Download download=new Download(mAccount, new File(TempfilePath), bookinfo2.image,listener);
			download.start();			
		} catch (Exception e) {
			e.printStackTrace();
			synchronized (mDowndLoadBooksCoverList) {
					if(mDowndLoadBooksCoverList.contains(bookinfo)){
					mDowndLoadBooksCoverList.remove(bookinfo);
				}
			}
		}
	}	
	
	private void addDownLoad(BookInfo bookInfo){
		synchronized (mLock) {
			for (EbookDownload mEbookDownload:mEbookDownloadList) {
				if (mEbookDownload.mDownloadInfo.book_id.equals(bookInfo.id)) {
					return;
				}
			}
			
			for (EbookDownload mEbookDownload:mEbookDownQueneList) {
				if (mEbookDownload.mDownloadInfo.book_id.equals(bookInfo.id)) {
					return;
				}
			}
			
			String Dir="/mnt/internal_sd/跨学书城";
			File FileDir=new File(Dir);
			if(!FileDir.exists()) FileDir.mkdirs();
			
			DownloadBookInfo downloadInfo = BookCacheData.getDownloadBookInfo(this, bookInfo.id);
			if (downloadInfo == null) {
				downloadInfo =new DownloadBookInfo();
				int start=bookInfo.path.lastIndexOf('/');				
				String Filepath=Dir+File.separator+bookInfo.path.substring(start>=0?(start+1):0);
				downloadInfo.path=Filepath+".tmp";
				downloadInfo.Url=bookInfo.path;
				downloadInfo.book_id=bookInfo.id;
				downloadInfo.download_size=-1;
				downloadInfo.download_status=BookCacheData.DOWNLOAD_STATUS_DOWNLOADING;
				downloadInfo.file_length=-1;								
			}
			downloadInfo.download_status=BookCacheData.DOWNLOAD_STATUS_DOWNLOADING;
			BookCacheData.UpdateDownloadBookInfo(DownLoadService.this,downloadInfo);
				
			EbookOnDownloadListener listen = new EbookOnDownloadListener() {

				@Override
				public void progress(long postion, long length) {
					EbookDownload bookDownload = GetEbookOnDownload();
					if (bookDownload != null && !bookDownload.isStop) {
						long time = System.currentTimeMillis();
						if (time - getLastProgress() >= 1000
								|| postion == length) {
							DownloadBookInfo downloadInfo = bookDownload.mDownloadInfo;
							downloadInfo.download_status = BookCacheData.DOWNLOAD_STATUS_DOWNLOADING;
							downloadInfo.file_length = (int) length;
							downloadInfo.download_size = (int) postion;
							BookCacheData.UpdateDownloadBookInfo(
									DownLoadService.this, downloadInfo);

							Intent intent = new Intent(
									ACTION_DOANLOAD_EBOOK_PROGRSS);
							intent.putExtra("downLoadInfo", downloadInfo);
							sendBroadcast(intent);
							setLastProgress(time);
						}						
					}		
				}

				@Override
				public void onFinish(String md5) {
					Message msg = new Message();
					msg.what = MSG_FINISH;
					msg.obj = GetEbookOnDownload();
					mHandler.sendMessage(msg);
				}

				@Override
				public void onException() {
					Message msg = new Message();
					msg.what = MSG_EXCEPTION;
					msg.obj = GetEbookOnDownload();
					mHandler.sendMessage(msg);
				}
			};
			EbookDownload mEbookDownload = new EbookDownload(downloadInfo,
					mAccount, downloadInfo.Url, downloadInfo.path, listen);
			listen.SetEbookOnDownload(mEbookDownload);

			mEbookDownQueneList.add(mEbookDownload);
		}		
	}
	
	private void updateDownLoad(){
		synchronized (mLock) {			
			while (mEbookDownloadList.size() < MAX_DOWN && !mEbookDownQueneList.isEmpty()) {
				EbookDownload mDownload = mEbookDownQueneList.remove(0);
				mEbookDownloadList.add(mDownload);
				mDownload.start();
			}
		}
	}
	
	private void removeDownLoad(String bookId){
		synchronized (mLock) {
			EbookDownload mTmpDownload = null;
			for (EbookDownload mDownload:mEbookDownloadList) {
				if (mDownload.mDownloadInfo.book_id.equals(bookId)) {
					mTmpDownload = mDownload;
					break;
				}
			}
			
			if (mTmpDownload != null) {
				mEbookDownloadList.remove(mTmpDownload);
			}
			
			mTmpDownload = null;
			for (EbookDownload mDownload:mEbookDownQueneList) {
				if (mDownload.mDownloadInfo.book_id.equals(bookId)) {
					mTmpDownload = mDownload;
					break;
				}
			}
			
			if (mTmpDownload != null) {
				mEbookDownQueneList.remove(mTmpDownload);
			}
		}
	}
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			EbookDownload bookDownload = null;
			switch (msg.what) {
			case MSG_FINISH:									
				bookDownload=(EbookDownload) msg.obj;
				if(bookDownload != null){
					File TmpFile=new File(bookDownload.mDownloadInfo.path);
					String Filepath = bookDownload.mDownloadInfo.path.replace(".tmp", "");
					TmpFile.renameTo(new File(Filepath));
					
					DownloadBookInfo downloadInfo=bookDownload.mDownloadInfo;
					downloadInfo.download_status=BookCacheData.DOWNLOAD_STATUS_FINISH;
					BookCacheData.UpdateDownloadBookInfo(DownLoadService.this,downloadInfo);
					
					Intent intent=new Intent(ACTION_DOANLOAD_EBOOK_RECIEVER);
					intent.putExtra("filePath", Filepath);
					intent.putExtra("downLoadInfo", downloadInfo);
					DownLoadService.this.sendBroadcast(intent);	
					
					removeDownLoad(downloadInfo.book_id);
					updateDownLoad();
				}
				break;
				
			case MSG_EXCEPTION:	
				bookDownload=(EbookDownload) msg.obj;										
				if(bookDownload != null){					
					DownloadBookInfo downloadInfo=bookDownload.mDownloadInfo;
					downloadInfo.download_status=BookCacheData.DOWNLOAD_STATUS_ERROR;
					BookCacheData.UpdateDownloadBookInfo(DownLoadService.this,downloadInfo);	
					
					Intent intent=new Intent(ACTION_DOANLOAD_EBOOK_REMOVE);												
					intent.putExtra("downLoadInfo", downloadInfo);
					DownLoadService.this.sendBroadcast(intent);	
					
					removeDownLoad(downloadInfo.book_id);
					updateDownLoad();					
				}
				break;	
				
			case MSG_PAUSE:{
				BookInfo mBookInfo = (BookInfo)msg.obj;				
				bookDownload = GetDownloadInDownloadList(mBookInfo.id);										
				if(bookDownload != null){					
					bookDownload.stop();
					bookDownload.isStop = true;
					DownloadBookInfo downloadInfo=bookDownload.mDownloadInfo;
					downloadInfo.download_status=BookCacheData.DOWNLOAD_STATUS_PAUSE;
					BookCacheData.UpdateDownloadBookInfo(DownLoadService.this,downloadInfo);
						
					removeDownLoad(downloadInfo.book_id);
					updateDownLoad();
				}
				break;
			}
				
			case MSG_DELETE:
				BookInfo mBookInfo = (BookInfo)msg.obj;				
				bookDownload = GetDownloadInDownloadList(mBookInfo.id);											
				if(bookDownload!=null){					
					bookDownload.stop();
					bookDownload.isStop = true;
					DownloadBookInfo downloadInfo=bookDownload.mDownloadInfo;												
					BookCacheData.DeleteDownloadBookInfo(DownLoadService.this,downloadInfo.book_id);
					File TmpFile=new File(bookDownload.mDownloadInfo.path);
					TmpFile.delete();
					
					removeDownLoad(downloadInfo.book_id);
					updateDownLoad();
				}											
				break;
				
			case MSG_ADDDOWN:
				addDownLoad((BookInfo)msg.obj);
				updateDownLoad();
				break;

			default:
				break;
			}
		
			super.handleMessage(msg);
		}				
	};
}
