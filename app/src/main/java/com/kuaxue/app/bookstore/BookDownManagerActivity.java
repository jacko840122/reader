package com.kuaxue.app.bookstore;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.greenlemonmobile.app.ebook.R;
import com.greenlemonmobile.app.utils.WindowsStyle;
import com.kuaxue.app.bookstore.BookCacheData.DownloadBookInfo;
import com.kuaxue.app.bookstore.BooksDBOnInternet.BookInfo;

public class BookDownManagerActivity extends BaseActivity{
	private ListView mListView = null;
	private ImageView mDeleteView = null;
	private ImageView mFinishView = null;
	private DownManagerAdpter managerAdpter = null;
	
	private IntentFilter mFilter = new IntentFilter();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_downmanager_main);
		WindowsStyle.setStatus(this,R.color.title_bar_background);	
		InitView();
		mFilter.addAction(DownLoadService.ACTION_DOANLOAD_EBOOK_RECIEVER);
		mFilter.addAction(DownLoadService.ACTION_DOANLOAD_EBOOK_PROGRSS);
		mFilter.addAction(DownLoadService.ACTION_DOANLOAD_EBOOK_REMOVE);		
	}
	
	@Override
	protected void onResume() {		
		registerReceiver(mBroadcastReceiver, mFilter);
		super.onResume();
	}


	@Override
	protected void onPause() {
		unregisterReceiver(mBroadcastReceiver);
		super.onPause();
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(DownLoadService.ACTION_DOANLOAD_EBOOK_RECIEVER)
					|| action.equals(DownLoadService.ACTION_DOANLOAD_EBOOK_REMOVE)) {
				if (managerAdpter != null) {
					managerAdpter.removeDownLoadBook(
							(DownloadBookInfo)intent.getSerializableExtra("downLoadInfo"));						
				}
			}else if(action.equals(DownLoadService.ACTION_DOANLOAD_EBOOK_PROGRSS)){
				if (managerAdpter != null) {
					managerAdpter.updateProgress(
							(DownloadBookInfo)intent.getSerializableExtra("downLoadInfo"));
				}				
			}			
		}		
	};

	private void InitView() {
		Button mback_btn = (Button) findViewById(R.id.back_btn);
		mback_btn.setOnClickListener(mClickListener);	
		mListView = (ListView)findViewById(R.id.listview);
		
		mDeleteView = (ImageView)findViewById(R.id.btn_delete);
		mDeleteView.setOnClickListener(mClickListener);
		
		mFinishView = (ImageView)findViewById(R.id.btn_finish);
		mFinishView.setVisibility(View.GONE);
		mFinishView.setOnClickListener(mClickListener);
		
		List<Helper> mHelpers = initAdapterHelpers();
		if (mHelpers != null && mHelpers.size() > 0) {
			managerAdpter = new DownManagerAdpter(this, mHelpers);
			mListView.setAdapter(managerAdpter);
		}		
	}
	
	private List<Helper> initAdapterHelpers(){
		List<Helper> mHelpers = new ArrayList<Helper>();
		List<DownloadBookInfo> mDownloadBookInfos = BookCacheData.getDownloadBookInfos(this);
		if(mDownloadBookInfos != null){
			for (DownloadBookInfo downloadBookInfo : mDownloadBookInfos) {
				if (downloadBookInfo.download_status == BookCacheData.DOWNLOAD_STATUS_DOWNLOADING
						|| downloadBookInfo.download_status == BookCacheData.DOWNLOAD_STATUS_PAUSE) {
					BookInfo mBookInfo = BooksDBOnInternet.getBookInfoByName(this, "id == '" + downloadBookInfo.book_id + "'");
					if (mBookInfo != null) {
						Helper mHelper = new Helper();
						mHelper.mBookInfo = mBookInfo;
						mHelper.mDownloadBookInfo = downloadBookInfo;
						mHelpers.add(mHelper);
						
						if (null != DownLoadService.GetDownloadInDownloadList(downloadBookInfo.book_id)){
							downloadBookInfo.download_status = BookCacheData.DOWNLOAD_STATUS_DOWNLOADING;
						}else{
							downloadBookInfo.download_status = BookCacheData.DOWNLOAD_STATUS_PAUSE;
						}
					}
				}				
			}			
		}
		return mHelpers;
	}
	
	private OnClickListener mClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back_btn:
				finish();
				break;
			case R.id.btn_delete:
				if (managerAdpter != null && managerAdpter.getCount() > 0) {
					managerAdpter.setDelete(true);
					managerAdpter.notifyDataSetChanged();
					mDeleteView.setVisibility(View.GONE);
					mFinishView.setVisibility(View.VISIBLE);
				}else{
					Toast mToast = Toast.makeText(getApplicationContext(),
							getString(R.string.nobook_downing),Toast.LENGTH_SHORT);		 
					mToast.show();	
				}
				break;
				
			case R.id.btn_finish:
				if (managerAdpter != null) {
					managerAdpter.setDelete(false);
					managerAdpter.notifyDataSetChanged();
					mDeleteView.setVisibility(View.VISIBLE);
					mFinishView.setVisibility(View.GONE);
				}
				break;

			default:
				break;
			}			
		}
	};
	
	class Helper{
		DownloadBookInfo mDownloadBookInfo;
		BookInfo mBookInfo;
	}
}
