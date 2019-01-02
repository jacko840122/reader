package com.kuaxue.app.bookstore;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.common.http.BaseResult;
import com.common.http.listener.NetListener;
import com.greenlemonmobile.app.ebook.R;
import com.greenlemonmobile.app.utils.WindowsStyle;
import com.kuaxue.app.bookstore.BooksDBOnInternet.BookInfo;
import com.kuaxue.center.util.CenterHelper;
import com.kuaxue.center.util.IRemoteCallBack;
import com.kuaxue.ebookstore.core.NetRequest;

public class CommentActivity extends BaseActivity implements OnClickListener {

	private RatingBar mCommet_ratingBar;
	private EditText mET_Input_comment;
	private Button mBack_btn3;
	private Button mSend_btn;
	private BookInfo mBookInfo;
	private String mUserName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment_main);
		WindowsStyle.setStatus(this,R.color.title_bar_background);
		mBookInfo = (BookInfo) getIntent().getSerializableExtra("BookInfo");
		InitView();
		CenterHelper centerHelper = new CenterHelper();
		centerHelper.getUserInfo(this, new IRemoteCallBack() {
			
			@Override
			public boolean result(Object obj) {
				try {
					JSONObject mJsonObject=new JSONObject((String)obj);
					if(mJsonObject!=null){
						mJsonObject=mJsonObject.getJSONObject("data").getJSONObject("user");
						mUserName=mJsonObject.getString("nickName");
						if(mUserName==null){
							mUserName=mJsonObject.getString("username");
						}
					}
					return true;
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					return false;
				}			
				
			}
		});
	}

	private void InitView() {

		mET_Input_comment = (EditText) findViewById(R.id.input_comment);
		mCommet_ratingBar = (RatingBar) findViewById(R.id.commet_ratingBar2);
		mBack_btn3 = (Button) findViewById(R.id.back_btn3);
		mSend_btn = (Button) findViewById(R.id.send_btn);

		mBack_btn3.setOnClickListener(this);
		mSend_btn.setOnClickListener(this);

	}

	private ProgressDialog mImportProgressDialog;

	private void showImportProgress(String msg) {
		mImportProgressDialog = new ProgressDialog(this);
		mImportProgressDialog.setMessage(msg);
		mImportProgressDialog.setIndeterminate(true);
		mImportProgressDialog.setCancelable(false);
		mImportProgressDialog.show();
	}

	private void closeImportProgress() {
		if (mImportProgressDialog != null) {
			mImportProgressDialog.dismiss();
			mImportProgressDialog = null;
		}
	}

	private void UpLoadComment() {

		if(mET_Input_comment.getText().toString().isEmpty()){
			Toast.makeText(CommentActivity.this, "请输入评论内容!", Toast.LENGTH_LONG).show();
			return;
		}
		showImportProgress("正在发表评论...");
		Comment comment=new Comment();
		comment.content=mET_Input_comment.getText().toString();
		comment.star=(int)(mCommet_ratingBar.getRating()*2);
		comment.target=mBookInfo.id;
		comment.userName=mUserName!=null?mUserName:"匿名";
		NetRequest.addComment(comment, new NetListener() {
			
			@Override
			public void onResult(BaseResult result) {
				// TODO Auto-generated method stub
				final BaseResult result2=result;
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						closeImportProgress();
						if(result2.errorCode!=200){
							Toast.makeText(CommentActivity.this, "上传失败!", Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(CommentActivity.this, "上传成功!", Toast.LENGTH_LONG).show();
							Intent intent=new Intent();
							intent.putExtra("UpLoadComment", true);
							setResult(BookDetailsActivity.REQUEST_COMMENT, intent);
							finish();
						}
						
					}

				});

			}
		}, CommentActivity.this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.back_btn3:
			finish();
			break;

		case R.id.send_btn:
			UpLoadComment();
			break;

		default:
			break;
		}
	}
}
