package com.kuaxue.app.bookstore;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import org.ebookdroid.CodecType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.greenlemonmobile.app.ebook.MyMd5;
import com.greenlemonmobile.app.ebook.R;
import com.greenlemonmobile.app.ebook.books.reader.EpubContext;
import com.greenlemonmobile.app.ebook.books.reader.EpubReaderActivity;
import com.greenlemonmobile.app.ebook.entity.Bookmark;
import com.greenlemonmobile.app.ebook.entity.FileInfo;
import com.greenlemonmobile.app.ebook.entity.LocalBook;
import com.greenlemonmobile.app.utils.FileUtil;
import com.greenlemonmobile.app.utils.Md5Encrypt;
import com.kuaxue.app.bookstore.BookCacheData.DownloadBookInfo;
import com.kuaxue.app.bookstore.BooksDBOnInternet.BookInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SearchBooksListAdpter extends BaseAdapter {
	private final Context mContext;
	private final ArrayList<BookInfo> mBookInfoList;
	private final LayoutInflater mInflater;

	public SearchBooksListAdpter(Context context,ArrayList<BookInfo> BookInfoList) {
		mContext = context;
		mBookInfoList = BookInfoList;
		mInflater = LayoutInflater.from(mContext);		
	}

	@Override
	public int getCount() {
		return mBookInfoList.size();
	}

	@Override
	public Object getItem(int position) {
		return mBookInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position,View convertView,ViewGroup parent) {
		BookInfo bookInfo;
		BaseViewHolder holder = null;

		bookInfo = mBookInfoList.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.book_info, null);
            holder = new BaseViewHolder();
            convertView.setTag(holder);
      
            holder.IV_book_cover = (ImageView) convertView.findViewById(R.id.book_cover2);
            holder.TV_Book_title = (TextView) convertView.findViewById(R.id.book_title2);
            holder.TV_book_size = (TextView) convertView.findViewById(R.id.book_size);
            holder.TV_download_count = (TextView) convertView
					.findViewById(R.id.download_count);
            holder.Book_author = (TextView) convertView.findViewById(R.id.book_author2);
            holder.TV_comment_ratingBar = (RatingBar) convertView
					.findViewById(R.id.comment_ratingBar);
            holder.TV_comment_count = (TextView) convertView
					.findViewById(R.id.comment_count);
            holder.Download_or_read_btn = (Button) convertView
					.findViewById(R.id.download_or_read_btn);
            holder.Book_info_item=(ViewGroup) convertView
					.findViewById(R.id.book_info_item);
            holder.downloading_progressContainer = (ViewGroup) convertView
					.findViewById(R.id.downloading_progress);
            holder.Download_progressBar = (ProgressBar) convertView
					.findViewById(R.id.Download_progressBar);
            holder.Download_progress_TextView = (TextView) convertView
					.findViewById(R.id.Download_progress_Text);
            
		}else {
            holder = (BaseViewHolder) convertView.getTag();
            if(holder.watcher!=null){
            	holder.watcher.onDestroy();
            }
        }
        holder.Book_info_item.setTag(bookInfo);
        
        holder.watcher=new UpdateDownloadOrReadStatusWatcher(bookInfo, holder.Download_or_read_btn, holder.Download_progressBar, holder.downloading_progressContainer, holder.Download_progress_TextView);
		holder.Book_info_item.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext,
						BookDetailsActivity.class);
				intent.putExtra("BookInfo", (Serializable) v.getTag());
				mContext.startActivity(intent);
			}
		});


		if (bookInfo != null) {
			final String filePath = BookCacheData.CACHEDATA_DIR
					+ File.separator + MyMd5.getMd5String(bookInfo.getImage()) + ".img";

			ImageLoader.getInstance().displayImage(Uri.fromFile(new File(filePath)).toString(),holder.IV_book_cover);

			holder.TV_comment_ratingBar.setIsIndicator(true);

			holder.TV_Book_title.setText(bookInfo.name != null ? bookInfo.name : "");
			holder.TV_book_size.setText("大小:"
					+ (bookInfo.length != null ? FileUtil
							.convertStorage(Integer.valueOf(bookInfo.length))
							: ""));
			holder.TV_download_count.setText("下载次数:"
					+ (bookInfo.downloadTimes != null ? bookInfo.downloadTimes
							: ""));
			{
				try {
					if (bookInfo.star != null && bookInfo.commentTimes != null) {
						int max = 10;
						int len = Integer.valueOf(bookInfo.star)
								/ Integer.valueOf(bookInfo.commentTimes);
						if (len > max)
							len = max;
						holder.TV_comment_ratingBar.setRating((float) len / 2);
					} else {
						holder.TV_comment_ratingBar.setRating(0);
					}
				} catch (Exception e) {
					holder.TV_comment_ratingBar.setRating(0);
				}
			}

			holder.TV_comment_count.setText("("
					+ (bookInfo.commentTimes != null ? bookInfo.commentTimes
							: "0") + ")");

			holder.Book_author
					.setText("作者:"
							+ ((bookInfo.author != null && !bookInfo.author
									.isEmpty()) ? bookInfo.author : "无"));
			
			
			holder.watcher.UpdateDownloadOrReadStatus();
		}

		return convertView;
	}
	
    private class BaseViewHolder {
		Button Download_or_read_btn;

		ImageView IV_book_cover;
		TextView TV_Book_title;
		TextView TV_book_size;
		TextView TV_download_count;
		TextView Book_author;
		RatingBar TV_comment_ratingBar;
		TextView TV_comment_count;
		ProgressBar Download_progressBar;
		ViewGroup downloading_progressContainer;
		ViewGroup Book_info_item;
		TextView Download_progress_TextView;  
		UpdateDownloadOrReadStatusWatcher watcher;
    }

	private class UpdateDownloadOrReadStatusWatcher {

		private boolean bIsRegisterContentObserver = false;
		private boolean bIsFirst = true;
		final private BookInfo mBookInfo;
		final private Button mDownload_or_read_btn;
		final private ProgressBar mDownload_progressBar;
		final private ViewGroup mDownloading_progressContainer;
		final private TextView mDownload_progress_TextView;
		

		
		public UpdateDownloadOrReadStatusWatcher(BookInfo bookInfo,Button download_or_read_btn,ProgressBar download_progressBar,ViewGroup downloading_progressContainer,TextView download_progress_TextView){
			mBookInfo=bookInfo;
			mDownload_or_read_btn=download_or_read_btn;
			mDownload_progressBar=download_progressBar;
			mDownloading_progressContainer=downloading_progressContainer;
			mDownload_progress_TextView=download_progress_TextView;
		}
		
		private final ContentObserver mDownLoadStatusObserver = new ContentObserver(
				new Handler()) {
			@Override
			public void onChange(boolean selfChange) {

				UpdateDownloadOrReadStatus();
			}

			@Override
			public void onChange(boolean selfChange, Uri uri) {
				UpdateDownloadOrReadStatus();
			}
		};

		private void Inter2ReadBook(final File file) {
			final Uri data = Uri.fromFile(file);
			CodecType codecType = CodecType.getByUri(data.toString());
			LocalBook book = LocalBook.getLocalBook(mContext, file.toString());
			if (book == null) {
				FileInfo fileInfo = FileUtil.GetFileInfo(file.toString());
				if (fileInfo != null) {
					LocalBook.importLocalBook((Activity) mContext, fileInfo);
					book = LocalBook.getLocalBook(mContext, file.toString());
				}
			}
			
			if(codecType==null){
				Toast.makeText(mContext, "不支持文件格式！", Toast.LENGTH_LONG).show();
				return;
			}

			if (codecType.getContextClass().getSimpleName()
					.equals(EpubContext.class.getSimpleName())) {
				Intent intent = new Intent(mContext, EpubReaderActivity.class);
				if (book != null)
					intent.putExtra("BookID", book.id);
				mContext.startActivity(intent);
			} else {

				int currentPage = 0;
				Bookmark bookmark = Bookmark.getBookmark(mContext,
						Md5Encrypt.md5(file.toString()));
				if (bookmark != null)
					currentPage = (int) bookmark.current_page;
				if (currentPage < 0)
					currentPage = 0;
				final Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.fromFile(file));
				intent.setClass(mContext,
						org.ebookdroid.ui.viewer.ViewerActivity.class);
				intent.putExtra("persistent", "false");
				intent.putExtra("nightMode", "false");
				intent.putExtra("pageIndex", Integer.toString((currentPage>0)?(currentPage-1):0));
				if (book != null)
					intent.putExtra("BookID", book.id);
				mContext.startActivity(intent);
			}
		}

		private void StartDownLoadBook(Integer id) {			
			Intent intent = new Intent(mContext, DownLoadService.class);
			intent.setAction(DownLoadService.ACTION_DOANLOAD_EBOOK);
			intent.putExtra("BookInfo", (Serializable) mBookInfo);
			mContext.startService(intent);
			if (bIsRegisterContentObserver) {
				mContext.getContentResolver().unregisterContentObserver(
						mDownLoadStatusObserver);
			}
			Uri ObserverUri = BookCacheData.URI_DOWNLOAD_BOOKS_INFO;
			if (id != null) {
				Uri.withAppendedPath(BookCacheData.URI_DOWNLOAD_BOOKS_INFO,
						id.toString());
			}
			mContext.getContentResolver().registerContentObserver(ObserverUri,
					true, mDownLoadStatusObserver);
			bIsRegisterContentObserver = true;
		}
		
		
		private void PauseDownLoadBook(Integer id) {			
			Intent intent = new Intent(mContext, DownLoadService.class);
			intent.setAction(DownLoadService.ACTION_PAUSE_DOANLOAD_EBOOK);
			intent.putExtra("BookInfo", (Serializable) mBookInfo);
			mContext.startService(intent);
			if (bIsRegisterContentObserver) {
				mContext.getContentResolver().unregisterContentObserver(
						mDownLoadStatusObserver);
			}
			Uri ObserverUri = BookCacheData.URI_DOWNLOAD_BOOKS_INFO;
			if (id != null) {
				Uri.withAppendedPath(BookCacheData.URI_DOWNLOAD_BOOKS_INFO,
						id.toString());
			}
			mContext.getContentResolver().registerContentObserver(ObserverUri,
					true, mDownLoadStatusObserver);
			bIsRegisterContentObserver = true;
		}

		private void UpdateDownloadOrReadStatus() {			
			int start = mBookInfo.path.lastIndexOf('/');
			String Dir = "/mnt/internal_sd/跨学书城";
			String Filepath = Dir + File.separator
					+ mBookInfo.path.substring(start >= 0 ? (start + 1) : 0);
			final File file = new File(Filepath);
			mDownload_or_read_btn.setVisibility(View.VISIBLE);
			mDownloading_progressContainer.setVisibility(View.INVISIBLE);
			if (file.exists()) {
				mDownload_or_read_btn.setText(R.string.read);
				mDownloading_progressContainer.setVisibility(View.GONE);
				mDownload_or_read_btn.setClickable(true);
				mDownload_or_read_btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Inter2ReadBook(file);
					}
				});

			} else {
				final DownloadBookInfo downloadBookInfo = BookCacheData
						.getDownloadBookInfo(mContext,
								mBookInfo.id);
				if (downloadBookInfo == null) {
					mDownload_or_read_btn.setText(R.string.download);
					mDownload_or_read_btn.setVisibility(View.VISIBLE);
					mDownloading_progressContainer.setVisibility(View.GONE);
					mDownload_or_read_btn.setClickable(true);
					mDownload_or_read_btn
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									StartDownLoadBook(null);
								}
							});
				} else {
					if (downloadBookInfo.download_status == BookCacheData.DOWNLOAD_STATUS_DOWNLOADING) {
						mDownload_or_read_btn.setText(R.string.downloading);
						mDownloading_progressContainer.setVisibility(View.VISIBLE);
						mDownload_progressBar.setVisibility(View.VISIBLE);
						mDownload_progress_TextView.setVisibility(View.VISIBLE);
						mDownload_or_read_btn.setClickable(false);
						mDownload_or_read_btn.setVisibility(View.INVISIBLE);
						mDownload_progressBar
								.setMax(downloadBookInfo.file_length);
						mDownload_progressBar
								.setProgress(downloadBookInfo.download_size);
						
						float percent=0;
						if(downloadBookInfo.file_length==0){
							percent=0;
						}
						else if(downloadBookInfo.download_size>=downloadBookInfo.file_length){
							percent=100;
						}else{
							percent=((float)((float)downloadBookInfo.download_size/(float)downloadBookInfo.file_length)*100);
						}	
//						mDownload_progress_TextView.setText(String.format("%1$d%%", percent));
						mDownload_progress_TextView.setText(String.format("%.1f%%", percent));
						if (bIsFirst) {
							bIsFirst = false;
							StartDownLoadBook(downloadBookInfo.id);
						}
						
						mDownload_progress_TextView.setClickable(true);
						mDownload_progress_TextView
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										PauseDownLoadBook(downloadBookInfo.id);
									}
								});

						Log.i("SearchBooksActivity", mBookInfo.name
								+ "**file_length="
								+ downloadBookInfo.file_length + "**Download="
								+ downloadBookInfo.download_size);
					} else if (downloadBookInfo.download_status == BookCacheData.DOWNLOAD_STATUS_ERROR) {
						if (bIsFirst) {
							bIsFirst = false;
							mDownload_or_read_btn.setText(R.string.download);
						} else {
//							mDownload_or_read_btn
//									.setText(R.string.download_error_and_retry);
//							mDownload_or_read_btn.setTextColor(Color.RED);
							mDownload_or_read_btn.setText(R.string.download);
							
						}

						//mDownload_progressBar.setVisibility(View.GONE);
						mDownloading_progressContainer.setVisibility(View.INVISIBLE);
						mDownload_or_read_btn.setClickable(true);
						mDownload_or_read_btn
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										StartDownLoadBook(downloadBookInfo.id);
									}
								});

					} else if (downloadBookInfo.download_status == BookCacheData.DOWNLOAD_STATUS_FINISH
							&& file.exists()) {
						mDownload_or_read_btn.setText(R.string.read);
						//mDownload_progressBar.setVisibility(View.GONE);
						mDownloading_progressContainer.setVisibility(View.INVISIBLE);
						mDownload_or_read_btn.setClickable(true);
						mDownload_or_read_btn
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										Inter2ReadBook(file);
									}
								});
					} else {
						if(downloadBookInfo.download_status == BookCacheData.DOWNLOAD_STATUS_PAUSE){
							mDownload_or_read_btn.setText(R.string.continue_download);
		
						}else{
							mDownload_or_read_btn.setText(R.string.download);
						}

						//mDownload_progressBar.setVisibility(View.GONE);
						mDownloading_progressContainer.setVisibility(View.INVISIBLE);
						mDownload_or_read_btn.setClickable(true);
						mDownload_or_read_btn
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										// TODO Auto-generated method stub
										StartDownLoadBook(downloadBookInfo.id);
									}
								});
					}
				}
			}
		}
		
		@Override
		protected void finalize(){
			if (bIsRegisterContentObserver) {
				mContext.getContentResolver().unregisterContentObserver(
						mDownLoadStatusObserver);
				bIsRegisterContentObserver = false;
			}
		}
		
		protected void onDestroy() {
			// TODO Auto-generated method stub
			if (bIsRegisterContentObserver) {
				mContext.getContentResolver().unregisterContentObserver(
						mDownLoadStatusObserver);
				bIsRegisterContentObserver = false;
			}
		}
	}

}
