package com.kuaxue.app.bookstore;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.greenlemonmobile.app.ebook.R;
import com.greenlemonmobile.app.utils.WindowsStyle;
import com.kuaxue.app.bookstore.BooksDBOnInternet.BookInfo;

public class SearchBookActivity extends BaseActivity implements
		OnClickListener, TextWatcher {

	private Button mBack_btn;
	private EditText mEditText;
	private Button mDelete_btn;
	private GridView mGridView;
	private View mNo_search_tip;
	private LinearLayout mSearch_input_container;
	private SearchBooksListAdpter mListAdapter;
	final private ArrayList<BookInfo> mAllBookInfoList = new ArrayList<BooksDBOnInternet.BookInfo>();
	final private ArrayList<BookInfo> mSearchBookInfoList = new ArrayList<BooksDBOnInternet.BookInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_book_main);
		WindowsStyle.setStatus(this, R.color.title_bar_background);
		InitView();

		ArrayList<BookInfo> list = getIntent().getParcelableArrayListExtra(
				"BookInfos");
		if (list == null)
			return;

		mAllBookInfoList.addAll(list);
		boolean bIsSearch = getIntent().getBooleanExtra("IsSearch", false);
		if (bIsSearch) {
			mGridView.setVisibility(View.INVISIBLE);
		} else {
			mSearchBookInfoList.addAll(mAllBookInfoList);
			mSearch_input_container.setVisibility(View.INVISIBLE);
			mBack_btn.setText("返回");
		}

	}

	private ArrayList<BookInfo> FindBooksList(String FindStr) {
		ArrayList<BookInfo> list = new ArrayList<BookInfo>();

		for (int i = 0, count = mAllBookInfoList.size(); i < count; i++) {
			BookInfo bookinfo = mAllBookInfoList.get(i);
			if (bookinfo.name.contains(FindStr)
					|| bookinfo.author.contains(FindStr)) {
				list.add(bookinfo);
			}
		}
		return list;
	}

	private void InitView() {
		mBack_btn = (Button) findViewById(R.id.back_btn);
		mEditText = (EditText) findViewById(R.id.search_input);
		mDelete_btn = (Button) findViewById(R.id.bt_delete);
		mGridView = (GridView) findViewById(R.id.GridView_Search);
		mNo_search_tip = findViewById(R.id.no_search_tip);
		mSearch_input_container = (LinearLayout) findViewById(R.id.search_input_container);

		mBack_btn.setOnClickListener(this);
		mDelete_btn.setOnClickListener(this);

		mListAdapter = new SearchBooksListAdpter(this, mSearchBookInfoList);
		mGridView.setAdapter(mListAdapter);

		mEditText.addTextChangedListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back_btn: {
			finish();
		}
			break;

		case R.id.bt_delete: {
			mEditText.setText("");
			mGridView.setVisibility(View.INVISIBLE);
			mNo_search_tip.setVisibility(View.INVISIBLE);
		}
			break;

		default:
			break;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		String FindStr = s.toString();
		if (FindStr == null || FindStr.isEmpty()) {
			mGridView.setVisibility(View.INVISIBLE);
			mNo_search_tip.setVisibility(View.INVISIBLE);
		} else {
			mSearchBookInfoList.clear();
			mSearchBookInfoList.addAll(FindBooksList(FindStr));
			if (mSearchBookInfoList.size() <= 0) {
				mGridView.setVisibility(View.INVISIBLE);
				mNo_search_tip.setVisibility(View.VISIBLE);
			} else {
				mGridView.setVisibility(View.VISIBLE);
				mNo_search_tip.setVisibility(View.INVISIBLE);
				mListAdapter.notifyDataSetChanged();
			}
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mListAdapter.notifyDataSetChanged();
	}

}
