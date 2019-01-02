package com.kuaxue.app.bookstore;

import java.util.ArrayList;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.greenlemonmobile.app.ebook.R;

public class CommentsListAdpter extends BaseAdapter{

    private final Context mContext;
    private final ArrayList<Comment> mCommentsList;
    private final LayoutInflater mInflater;
	
    
	public CommentsListAdpter(Context context,ArrayList<Comment> CommentsList){
		mContext=context;
		mCommentsList=CommentsList;
		mInflater = LayoutInflater.from(mContext);
	}
    
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mCommentsList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mCommentsList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TextView tv_user;
		TextView tv_time;
		TextView tv_content;
		RatingBar rb_comment;
		
		if(convertView==null){
			convertView=mInflater.inflate(R.layout.comment_item, null);		
		}
		tv_user=(TextView) convertView.findViewById(R.id.textView_user);
		tv_time=(TextView) convertView.findViewById(R.id.textView_time);
		tv_content=(TextView) convertView.findViewById(R.id.textView_content);
		rb_comment=(RatingBar) convertView.findViewById(R.id.comment_ratingBar);
		Comment comment=mCommentsList.get(position);
		tv_user.setText("用户昵称:"+((comment.userName!=null&&!comment.userName.isEmpty())?comment.userName:"匿名"));
		long dateTaken = comment.createTime; 
		String datetime=null;
		if (dateTaken != 0) {  
			datetime = DateFormat.format("yyyy年MM月dd日 kk:mm", dateTaken).toString();                    
		}  
		tv_time.setText("发表于"+(datetime!=null?datetime:""));
		tv_content.setText(comment.content!=null?comment.content:"");
		rb_comment.setRating((float)comment.star/2);
		return convertView;
	}

}
