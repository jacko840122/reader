package com.kuaxue.app.bookstore;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.greenlemonmobile.app.ebook.MyMd5;
import com.greenlemonmobile.app.ebook.R;
import com.greenlemonmobile.app.utils.FileUtil;
import com.kuaxue.app.bookstore.BookCacheData.DownloadBookInfo;
import com.kuaxue.app.bookstore.BookDownManagerActivity.Helper;
import com.kuaxue.app.bookstore.BooksDBOnInternet.BookInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

public class DownManagerAdpter extends BaseAdapter {
	private final Context mContext;
	private final LayoutInflater mInflater;
	private List<Helper> mhHelpers = null;
	private boolean isDelete = false;

	public DownManagerAdpter(Context context,List<Helper> mhHelpers) {	
		this.mContext = context;
		this.mhHelpers = mhHelpers;		
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mhHelpers.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mhHelpers.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.adapter_downmanager, null);
			
			holder.mCoverView = (ImageView) convertView.findViewById(R.id.book_cover2);
			holder.mTitleView = (TextView) convertView.findViewById(R.id.book_title2);
			holder.mAuthorView = (TextView) convertView.findViewById(R.id.book_author);			
			holder.mSizeView = (TextView) convertView.findViewById(R.id.book_size);
			holder.mDownCountView = (TextView)convertView.findViewById(R.id.download_count);
			
			holder.mDeleteBtn = (ImageButton)convertView.findViewById(R.id.btn_del);
			holder.mDownload_progressBar = (ProgressBar)convertView.findViewById(R.id.Download_progressBar);
			holder.mDownload_progress_TextView = (TextView) convertView.findViewById(R.id.Download_progress_Text);					
			
			holder.mTV_comment_ratingBar = (RatingBar) convertView.findViewById(R.id.comment_ratingBar);
			holder.mTV_comment_count = (TextView)convertView.findViewById(R.id.comment_count);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}	
		
		Helper mHelper = (Helper)getItem(position);
		final BookInfo mBookInfo = mHelper.mBookInfo;
		final DownloadBookInfo mDownloadBookInfo = mHelper.mDownloadBookInfo;
		
		final String filePath = BookCacheData.CACHEDATA_DIR + File.separator
				+ MyMd5.getMd5String(mBookInfo.getImage()) + ".img";

		ImageLoader.getInstance().displayImage(
				Uri.fromFile(new File(filePath)).toString(), holder.mCoverView);
		
		holder.mTitleView.setText(mBookInfo.name != null ? mBookInfo.name : "");
		holder.mSizeView.setText("大小:"
				+ (mBookInfo.length != null ? FileUtil.convertStorage(Integer
						.valueOf(mBookInfo.length)) : ""));
		holder.mDownCountView.setText("下载次数:"
				+ (mBookInfo.downloadTimes != null ? mBookInfo.downloadTimes
						: ""));		

		holder.mAuthorView.setText("作者:"
				+ ((mBookInfo.author != null && !mBookInfo.author
						.isEmpty()) ? mBookInfo.author : "无"));		
		
		int status = mDownloadBookInfo.download_status;
		if (status == BookCacheData.DOWNLOAD_STATUS_DOWNLOADING) {
			holder.mDownload_progressBar.setVisibility(View.VISIBLE);
			holder.mDownload_progress_TextView.setVisibility(View.VISIBLE);
			
			holder.mDownload_progressBar.setMax(mDownloadBookInfo.file_length);
			holder.mDownload_progressBar.setProgress(mDownloadBookInfo.download_size);
			
			float percent=0;
			if(mDownloadBookInfo.file_length==0){
				percent=0;
			}else if(mDownloadBookInfo.download_size>=mDownloadBookInfo.file_length){
				percent=100;
			}else{
				percent=((float)((float)mDownloadBookInfo.download_size/(float)mDownloadBookInfo.file_length)*100);
			}	
			holder.mDownload_progress_TextView.setText(String.format("%.1f%%", percent));		
		}else if(status == BookCacheData.DOWNLOAD_STATUS_PAUSE){
			holder.mDownload_progress_TextView.setText(R.string.continue_download);
			holder.mDownload_progressBar.setProgress(0);
		}

		if (isDelete) {
			holder.mDeleteBtn.setVisibility(View.VISIBLE);			
		}else{
			holder.mDeleteBtn.setVisibility(View.GONE);
		}
		
		holder.mDeleteBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DeleteDownLoadBook(mBookInfo, mDownloadBookInfo);
			}
		});
		
		holder.mDownload_progress_TextView
		.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mDownloadBookInfo.download_status == BookCacheData.DOWNLOAD_STATUS_DOWNLOADING) {
					PauseDownLoadBook(mBookInfo,mDownloadBookInfo);					
				}else{
					StartDownLoadBook(mBookInfo,mDownloadBookInfo);
				}
				notifyDataSetChanged();
			}
		});
		
		holder.mTV_comment_ratingBar.setIsIndicator(true);
		try {
			if (mBookInfo.star != null && mBookInfo.commentTimes != null) {
				int max = 10;
				int len = Integer.valueOf(mBookInfo.star)
						/ Integer.valueOf(mBookInfo.commentTimes);
				if (len > max)
					len = max;
				holder.mTV_comment_ratingBar.setRating((float) len / 2);
			} else {
				holder.mTV_comment_ratingBar.setRating(0);
			}
		} catch (Exception e) {
			holder.mTV_comment_ratingBar.setRating(0);
		}
		
		holder.mTV_comment_count.setText("("
				+ (mBookInfo.commentTimes != null ? mBookInfo.commentTimes
						: "0")+")");

		return convertView;
	}
	
	private void StartDownLoadBook(BookInfo mBookInfo,DownloadBookInfo mBookInfo2) {
		Intent intent = new Intent(mContext,DownLoadService.class);
		intent.setAction(DownLoadService.ACTION_DOANLOAD_EBOOK);
		intent.putExtra("BookInfo", (Serializable)mBookInfo);
		mContext.startService(intent);
		mBookInfo2.download_status = BookCacheData.DOWNLOAD_STATUS_DOWNLOADING;
		notifyDataSetChanged();
	}
	
	private void PauseDownLoadBook(BookInfo mBookInfo,DownloadBookInfo mBookInfo2) {
		Intent intent = new Intent(mContext,DownLoadService.class);
		intent.setAction(DownLoadService.ACTION_PAUSE_DOANLOAD_EBOOK);
		intent.putExtra("BookInfo", (Serializable)mBookInfo);
		mContext.startService(intent);		
		
		mBookInfo2.download_status = BookCacheData.DOWNLOAD_STATUS_PAUSE;
		notifyDataSetChanged();
	}
	
	private void DeleteDownLoadBook(BookInfo mBookInfo,DownloadBookInfo mBookInfo2) {
		Intent intent = new Intent(mContext,DownLoadService.class);
		intent.setAction(DownLoadService.ACTION_DELETE_DOANLOAD_EBOOK);
		intent.putExtra("BookInfo", (Serializable)mBookInfo);
		mContext.startService(intent);		
		
		removeDownLoadBook(mBookInfo2);
	}
		
	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

	private long lastRefresh = 0;
	public void updateProgress(DownloadBookInfo mDownload){
		if (mhHelpers != null) {
			for (Helper mHelper:mhHelpers) {
				DownloadBookInfo mBookInfo = mHelper.mDownloadBookInfo;
				if (mBookInfo.book_id.equals(mDownload.book_id)) {
					mBookInfo.download_status = mDownload.download_status;
					mBookInfo.file_length = mDownload.file_length;
					mBookInfo.download_size = mDownload.download_size;
					
					if (System.currentTimeMillis() - lastRefresh >= 1500) {						
						notifyDataSetChanged();
						lastRefresh = System.currentTimeMillis();
					}
					break;
				}
			}
		}		
	}
	
	public void removeDownLoadBook(DownloadBookInfo mDownload) {
		if (mhHelpers != null) {
			for (int i = 0; i < mhHelpers.size(); i++) {
				DownloadBookInfo mBookInfo = mhHelpers.get(i).mDownloadBookInfo;
				if (mBookInfo.book_id.equals(mDownload.book_id)) {
					mhHelpers.remove(i);
					notifyDataSetChanged();
					break;
				}
			}			
		}			
	}

	private final class ViewHolder{
		ImageView mCoverView;
		TextView mTitleView;
		TextView mAuthorView;
		TextView mSizeView;
		TextView mDownCountView;	
		
		ImageButton mDeleteBtn;
		
		ProgressBar mDownload_progressBar;
		TextView mDownload_progress_TextView;
		
		RatingBar mTV_comment_ratingBar;
		TextView mTV_comment_count;		
	}	
}
