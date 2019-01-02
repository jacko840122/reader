package com.kuaxue.app.bookstore;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ebookdroid.CodecType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.common.http.BaseResult;
import com.common.http.listener.NetListener;
import com.greenlemonmobile.app.ebook.MyMd5;
import com.greenlemonmobile.app.ebook.R;
import com.greenlemonmobile.app.ebook.books.reader.EpubContext;
import com.greenlemonmobile.app.ebook.books.reader.EpubReaderActivity;
import com.greenlemonmobile.app.ebook.entity.Bookmark;
import com.greenlemonmobile.app.ebook.entity.FileInfo;
import com.greenlemonmobile.app.ebook.entity.LocalBook;
import com.greenlemonmobile.app.utils.FileUtil;
import com.greenlemonmobile.app.utils.Md5Encrypt;
import com.greenlemonmobile.app.utils.WindowsStyle;
import com.kuaxue.app.bookstore.BookCacheData.DownloadBookInfo;
import com.kuaxue.app.bookstore.BooksDBOnInternet.BookInfo;
import com.kuaxue.ebookstore.core.NetRequest;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BookDetailsActivity extends BaseActivity implements OnClickListener {

	private BookInfo mBookInfo;

	private Button mDownload_or_read_btn;
	private Button mback_btn;
	private ImageView mIV_book_cover;
	private TextView mBook_author;
	private TextView mTV_Book_title;
	private TextView mTV_book_size;
	private TextView mTV_download_count;
	private RatingBar mTV_comment_ratingBar;
	private TextView mTV_comment_count;
	
	private TextView mTV_introduction;
	private TextView mTV_comment_count2;
	private TextView mTV_load_more;
	private Button   mTV_comment_btn;
	private ListView mLV_comment;

	private ProgressBar mDownload_progressBar;
	
	private ViewGroup  mDownloading_progressContainer;
	private TextView mDownload_progress_TextView;  
	
	private int mGetCommentsCount=0;
	private int mGetCommentsStart=0;
	private ArrayList<Comment> CommentList=new ArrayList<Comment>();
	private static int COMMENT_COUNT_ONE_TIME=30;

	private boolean bIsFirst = true;
	private boolean bIsRegisterContentObserver = false;
	public static int REQUEST_COMMENT=0x01;
	private CommentsListAdpter mCommentsListAdpter;
	
	

	private NetListener mNetListener =new NetListener() {
		
		@Override
		public void onResult(BaseResult result) {
			// TODO Auto-generated method stub
			if(result==null||result.errorCode!=200||result.object==null){
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mTV_load_more.setText(R.string.fail_and_load_more_tip);
						mTV_load_more.setClickable(true);
					}
					
				});
			}else{
				JSONObject jsonObject=result.object;
				final List<Comment> comments = new ArrayList<Comment>();
				if (jsonObject.has("comments")) {
					try {
						JSONArray mArray = jsonObject
								.getJSONArray("comments");
						if (mArray != null) {
							for (int i = 0; i < mArray.length(); i++) {
								Comment mComment = new Comment();
								mComment.setJson(mArray.getJSONObject(i));
								comments.add(mComment);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(comments.size()<=0){
							mTV_load_more.setText(R.string.no_more_tip);
							mTV_load_more.setClickable(true);
						}else{
							mTV_load_more.setText(R.string.load_more_tip);
							mTV_load_more.setClickable(true);
							CommentList.addAll(comments);
							mGetCommentsCount=CommentList.size();
							mGetCommentsStart=mGetCommentsCount;
							mCommentsListAdpter.notifyDataSetChanged();
							mTV_comment_count2
							.setText("("
									+ CommentList.size()+"条)");
						}

					}
				});


			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_details_main);
		WindowsStyle.setStatus(this,R.color.title_bar_background);

		mBookInfo = (BookInfo) getIntent().getSerializableExtra("BookInfo");
		InitView();
		NetRequest.getComments(mBookInfo.id, mGetCommentsStart, COMMENT_COUNT_ONE_TIME, mNetListener, this);
		mCommentsListAdpter=new CommentsListAdpter(this, CommentList);
		mLV_comment.setAdapter(mCommentsListAdpter);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (bIsRegisterContentObserver) {
			getContentResolver().unregisterContentObserver(
					mDownLoadStatusObserver);
			bIsRegisterContentObserver = false;
		}
	}

	private void InitView() {

		mDownload_or_read_btn = (Button) findViewById(R.id.download_or_read_btn);
		mback_btn = (Button) findViewById(R.id.back_btn);
		mIV_book_cover = (ImageView) findViewById(R.id.book_cover2);
		mTV_Book_title = (TextView) findViewById(R.id.book_title2);
		mTV_book_size = (TextView) findViewById(R.id.book_size);
		mBook_author = (TextView) findViewById(R.id.book_author);
		mTV_download_count = (TextView) findViewById(R.id.download_count);
		mTV_comment_ratingBar = (RatingBar) findViewById(R.id.comment_ratingBar);
		mTV_comment_count = (TextView) findViewById(R.id.comment_count);
		mDownload_progressBar = (ProgressBar) findViewById(R.id.Download_progressBar);
		
		mTV_introduction = (TextView) findViewById(R.id.textView_introduction);
		mTV_comment_count2 = (TextView) findViewById(R.id.textView_comment_count);
		mTV_comment_btn = (Button) findViewById(R.id.button_comment);
		mTV_load_more= (TextView) findViewById(R.id.load_more);
		mLV_comment = (ListView) findViewById(R.id.listView_comment);
        mDownloading_progressContainer = (ViewGroup)findViewById(R.id.downloading_progress);
        mDownload_progress_TextView = (TextView)findViewById(R.id.Download_progress_Text);

		mback_btn.setOnClickListener(this);
		mTV_comment_btn.setOnClickListener(this);
		mTV_load_more.setOnClickListener(this);

		UpdateDownloadOrReadStatus();

		final String filePath = BookCacheData.CACHEDATA_DIR + File.separator
				+ MyMd5.getMd5String(mBookInfo.getImage()) + ".img";

		ImageLoader.getInstance().displayImage(
				Uri.fromFile(new File(filePath)).toString(), mIV_book_cover);
		mTV_comment_ratingBar.setIsIndicator(true);

		mTV_introduction.setText(mBookInfo.summary != null ? mBookInfo.summary : "");
		mTV_Book_title.setText(mBookInfo.name != null ? mBookInfo.name : "");
		mTV_book_size.setText("大小:"
				+ (mBookInfo.length != null ? FileUtil.convertStorage(Integer
						.valueOf(mBookInfo.length)) : ""));
		mTV_download_count.setText("下载次数:"
				+ (mBookInfo.downloadTimes != null ? mBookInfo.downloadTimes
						: ""));
		{
			try {
				if (mBookInfo.star != null && mBookInfo.commentTimes != null) {
					int max = 10;
					int len = Integer.valueOf(mBookInfo.star)
							/ Integer.valueOf(mBookInfo.commentTimes);
					if (len > max)
						len = max;
					mTV_comment_ratingBar.setRating((float) len / 2);
				} else {
					mTV_comment_ratingBar.setRating(0);
				}
			} catch (Exception e) {
				mTV_comment_ratingBar.setRating(0);
			}
		}

		mBook_author
		.setText("作者:"
				+ ((mBookInfo.author != null && !mBookInfo.author
						.isEmpty()) ? mBookInfo.author : "无"));
		
		mTV_comment_count
				.setText("("
						+ (mBookInfo.commentTimes != null ? mBookInfo.commentTimes
								: "0")+")");
		
		mTV_comment_count2
		.setText("("
				+ (mBookInfo.commentTimes != null ? mBookInfo.commentTimes
						: "0")+"条)");
	}

	private void Inter2ReadBook(final File file) {
		final Uri data = Uri.fromFile(file);
		CodecType codecType = CodecType.getByUri(data.toString());
		LocalBook book = LocalBook.getLocalBook(BookDetailsActivity.this,
				file.toString());
		if (book == null) {
			FileInfo fileInfo = FileUtil.GetFileInfo(file.toString());
			if (fileInfo != null) {
				LocalBook.importLocalBook(BookDetailsActivity.this, fileInfo);
				book = LocalBook.getLocalBook(BookDetailsActivity.this,
						file.toString());
			}
		}
		if(codecType==null){
			Toast.makeText(BookDetailsActivity.this, "不支持文件格式！", Toast.LENGTH_LONG).show();
			return;
		}

		if (codecType.getContextClass().getSimpleName()
				.equals(EpubContext.class.getSimpleName())) {
			Intent intent = new Intent(BookDetailsActivity.this,
					EpubReaderActivity.class);
			if (book != null)
				intent.putExtra("BookID", book.id);
			startActivity(intent);
		} else {

			int currentPage = 0;
			Bookmark bookmark = Bookmark.getBookmark(BookDetailsActivity.this,
					Md5Encrypt.md5(file.toString()));
			if (bookmark != null)
				currentPage = (int) bookmark.current_page;
			if (currentPage < 0)
				currentPage = 0;
			final Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.fromFile(file));
			intent.setClass(BookDetailsActivity.this,
					org.ebookdroid.ui.viewer.ViewerActivity.class);
			intent.putExtra("persistent", "false");
			intent.putExtra("nightMode", "false");
			intent.putExtra("pageIndex", Integer.toString((currentPage>0)?(currentPage-1):0));
			if (book != null)
				intent.putExtra("BookID", book.id);
			startActivity(intent);
		}
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

	private void StartDownLoadBook(Integer id) {
		Intent intent = new Intent(BookDetailsActivity.this,
				DownLoadService.class);
		intent.setAction(DownLoadService.ACTION_DOANLOAD_EBOOK);
		intent.putExtra("BookInfo", (Serializable)mBookInfo);
		startService(intent);
		if (bIsRegisterContentObserver) {
			getContentResolver().unregisterContentObserver(
					mDownLoadStatusObserver);
		}
		Uri ObserverUri = BookCacheData.URI_DOWNLOAD_BOOKS_INFO;
		if (id != null) {
			Uri.withAppendedPath(BookCacheData.URI_DOWNLOAD_BOOKS_INFO,
					id.toString());
		}
		getContentResolver().registerContentObserver(ObserverUri, true,
				mDownLoadStatusObserver);
		bIsRegisterContentObserver = true;
	}
	
	
	private void PauseDownLoadBook(Integer id) {
		Intent intent = new Intent(BookDetailsActivity.this,
				DownLoadService.class);
		intent.setAction(DownLoadService.ACTION_PAUSE_DOANLOAD_EBOOK);
		intent.putExtra("BookInfo", (Serializable)mBookInfo);
		startService(intent);
		if (bIsRegisterContentObserver) {
			getContentResolver().unregisterContentObserver(
					mDownLoadStatusObserver);
		}
		Uri ObserverUri = BookCacheData.URI_DOWNLOAD_BOOKS_INFO;
		if (id != null) {
			Uri.withAppendedPath(BookCacheData.URI_DOWNLOAD_BOOKS_INFO,
					id.toString());
		}
		getContentResolver().registerContentObserver(ObserverUri, true,
				mDownLoadStatusObserver);
		bIsRegisterContentObserver = true;
	}

	private void UpdateDownloadOrReadStatus() {

		int start = mBookInfo.path.lastIndexOf('/');
		String Dir = "/mnt/internal_sd/跨学书城";
		String Filepath = Dir + File.separator
				+ mBookInfo.path.substring(start >= 0 ? (start + 1) : 0);
		final File file = new File(Filepath);
		mDownload_or_read_btn.setVisibility(View.VISIBLE);
		mDownloading_progressContainer.setVisibility(View.VISIBLE);
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
					.getDownloadBookInfo(BookDetailsActivity.this,
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
					
					int percent=0;
					if(downloadBookInfo.file_length==0){
						percent=0;
					}
					else if(downloadBookInfo.download_size>=downloadBookInfo.file_length){
						percent=100;
					}else{
						percent=(int)((float)((float)downloadBookInfo.download_size/(float)downloadBookInfo.file_length)*100);
					}	
					mDownload_progress_TextView.setText(String.format("%1$d%%", percent));

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
//						mDownload_or_read_btn
//								.setText(R.string.download_error_and_retry);
//						mDownload_or_read_btn.setTextColor(Color.RED);
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.back_btn:
			finish();
			break;
		
		case R.id.button_comment:
			{
				Intent intent=new Intent(this, CommentActivity.class);
				intent.putExtra("BookInfo", (Serializable)mBookInfo);
				startActivityForResult(intent, REQUEST_COMMENT);
			}
			break;

		case R.id.load_more:
			{
				mTV_load_more.setText(R.string.loading_more_tip);
				mTV_load_more.setClickable(false);
				NetRequest.getComments(mBookInfo.id, mGetCommentsStart, COMMENT_COUNT_ONE_TIME, mNetListener, this);
				
			}	
			break;
			
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==REQUEST_COMMENT&&data!=null){
			boolean bUpLoadComment =data.getBooleanExtra("UpLoadComment", false);
			if(bUpLoadComment){
				mGetCommentsStart=0;
				mGetCommentsCount=0;
				CommentList.clear();
				NetRequest.getComments(mBookInfo.id, mGetCommentsStart, COMMENT_COUNT_ONE_TIME, mNetListener, this);
			}
		}
	}
	
	

}
