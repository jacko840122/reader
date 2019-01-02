package com.greenlemonmobile.app.ebook.adapter;

import java.io.File;
import java.util.ArrayList;

import org.ebookdroid.CodecType;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.greenlemonmobile.app.ebook.LibraryActivity.ViewType;
import com.greenlemonmobile.app.ebook.R;
import com.greenlemonmobile.app.ebook.iBooksReaderApp;
import com.greenlemonmobile.app.ebook.books.reader.EpubContext;
import com.greenlemonmobile.app.ebook.entity.Bookmark;
import com.greenlemonmobile.app.ebook.entity.LocalBook;
import com.greenlemonmobile.app.utils.ImageBuffer;
import com.greenlemonmobile.app.utils.Md5Encrypt;

public class BookinfosGridAdapter extends BaseAdapter {
    
    private final Context mContext;
    private final ArrayList<LocalBook> mLocalBooks;
    private final LayoutInflater mInflater;
    private final ViewType mItemType;
    private OnDeleteListener mListener;
    private boolean deleteMode = false;
    
    public boolean isDeleteMode() {
		return deleteMode;
	}

	public void setDeleteMode(boolean deleteMode) {
		this.deleteMode = deleteMode;
	}

	public BookinfosGridAdapter(Context context, ViewType type, ArrayList<LocalBook> books) {
        mContext = context;
        mLocalBooks = books;
        mInflater = LayoutInflater.from(context);
        mItemType = type;
    }

    @Override
    public int getCount() {
        return mLocalBooks.size()+1;
    }

    @Override
    public Object getItem(int position) {
    	if(position>=mLocalBooks.size()){
    		return null;
    	}else{
    		return mLocalBooks.get(position);
    	}
        
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        BaseViewHolder holder = null;
        
        if(position>=mLocalBooks.size()){
        	convertView =mInflater.inflate(R.layout.add_book_button, null);
        	ImageView imageview=(ImageView) convertView.findViewById(R.id.imageview_add_book);
    		imageview.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
	            	if(iBooksReaderApp.onlyTabHost!=null){
	            		iBooksReaderApp.onlyTabHost.setCurrentTab(1);
	            	}
				}
			});
 
        	return convertView;
        }
        if (convertView == null||convertView.getId()==R.id.add_book_item) {
            switch (mItemType) {
                case BIG_THUMB:
                    convertView = mInflater.inflate(R.layout.library_bookinfos_big, null);
                    break;
                case MEDIUM_THUMB:
                    convertView = mInflater.inflate(R.layout.library_bookinfos_medium, null);
                    break;
                case SMALL_THUMB:
                    convertView = mInflater.inflate(R.layout.library_bookinfos_small, null);
                    break;
                case BOOK_SHELF:
                	convertView = mInflater.inflate(R.layout.library_bookinfos_shelf, null);
                	break;
            }
            holder = new BaseViewHolder();
            convertView.setTag(holder);
            
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.format = (ImageView) convertView.findViewById(R.id.format);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.readProgress = (TextView) convertView.findViewById(R.id.read_progress);
            holder.position = position;
        } else {
            holder = (BaseViewHolder) convertView.getTag();
        }
        
        final LocalBook book = mLocalBooks.get(position);
        holder.title.setText(book.title);
        holder.icon.setImageBitmap(null);
             	
    	final File file = new File(book.file);
        final Uri data = Uri.fromFile(file);
        CodecType codecType = CodecType.getByUri(data.toString());

        String Str2="未读";
    	if (codecType.getContextClass().getSimpleName().equals(EpubContext.class.getSimpleName())) {
    		Str2="未读";
    	} else {

            int currentPage = 0;
            Bookmark bookmark = Bookmark.getBookmark(mContext, Md5Encrypt.md5(book.file));
            if (bookmark != null)
                currentPage = (int) bookmark.current_page;
            if (currentPage < 0)
                currentPage = 0;
            if(bookmark != null&&bookmark.total_page>0){
            	Str2=new String("已读："+bookmark.current_page+"/"+bookmark.total_page+"页");
            }else{
            	Str2=new String("未读");
            }
    	}
        
        holder.readProgress.setText(Str2);
        holder.icon.setImageResource(BaseViewHolder.getFormatCoverDrawable(book.file));
        holder.format.setImageResource(BaseViewHolder.getFormatDrawable(book.file));
        Bitmap bitmap = ImageBuffer.getBitmap(mContext, (mItemType == ViewType.DETAILS) ? book.detail_image : book.list_image);
        if (bitmap != null)
        	holder.icon.setImageBitmap(bitmap);
        
        
//        // Using an AsyncTask to load the slow images in a background thread
//        new AsyncTask<BaseViewHolder, Void, Bitmap>() {
//            private BaseViewHolder v;
//
//            @Override
//            protected Bitmap doInBackground(BaseViewHolder... params) {
//                v = params[0];
//                return ImageBuffer.getBitmap(mContext, (mItemType == ViewType.DETAILS) ? book.detail_image : book.list_image);
//            }
//
//            @Override
//            protected void onPostExecute(Bitmap result) {
//                super.onPostExecute(result);
//                v.icon.setVisibility(View.VISIBLE);
//                if (result != null)
//                	v.icon.setImageBitmap(result);
//            }
//        }.execute(holder);
        
        ImageView view = (ImageView) convertView.findViewById(R.id.delete);
        if(deleteMode){
        	view.setVisibility(View.VISIBLE);
        	view.setOnClickListener(new OnClickListener() {	
				@Override
				public void onClick(View v) {
					mListener.onDelete(position);				
				}
			});
        }
        else{
        	view.setVisibility(View.GONE);
        }
        return convertView;
    }
    
    public OnDeleteListener getmListener() {
		return mListener;
	}

	public void setmListener(OnDeleteListener mListener) {
		this.mListener = mListener;
	}

	public interface OnDeleteListener{
    	public void onDelete(int index);
    }

    public static class BaseViewHolder {
        public ImageView icon;
        public ImageView format;
        public TextView title;
        public TextView readProgress;
        public int position;
        
        public static int getFormatDrawable(String fileName) {
        	final File file = new File(fileName);
            final Uri data = Uri.fromFile(file);
            CodecType codecType = CodecType.getByUri(data.toString());
            
            if (codecType != null) {
                switch (codecType) {
                case EPUB:
                	return R.drawable.format_epub;
                case TXT:
                	return R.drawable.format_txt;
                case PDF:
                	return R.drawable.format_pdf;
                case DJVU:
                	return R.drawable.format_djvu;
                case XPS:
                	return R.drawable.format_xps;
                case CBZ:
                	break;
                case CBR:
                	break;
                case FB2:
                	break;
                }
            }
        	
            return R.drawable.format_default;
        }
        
        
        
        public static int getFormatCoverDrawable(String fileName) {
        	final File file = new File(fileName);
            final Uri data = Uri.fromFile(file);
            CodecType codecType = CodecType.getByUri(data.toString());
            
            if (codecType != null) {
                switch (codecType) {
                case EPUB:
                	return R.drawable.cover_epub;
                case TXT:
                	return R.drawable.cover_txt;
                case PDF:
                	return R.drawable.cover_pdf;
                case DJVU:
                	return R.drawable.cover_djvu;
                case XPS:
                	return R.drawable.cover_xps;
                case CBZ:
                	break;
                case CBR:
                	break;
                case FB2:
                	break;
                }
            }
        	
            return R.drawable.cover_txt;
        }
    }
}
