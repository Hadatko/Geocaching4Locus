package com.arcao.geocaching4locus.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.adapter.BookmarkListRecyclerAdapter;
import com.arcao.geocaching4locus.task.BookmarkListRetrieveTask;
import com.arcao.geocaching4locus.widget.decorator.SpacesItemDecoration;
import java.lang.ref.WeakReference;
import java.util.List;

public class BookmarkListFragment extends Fragment implements BookmarkListRetrieveTask.TaskListener {
	public interface ListListener {
		void onTitleChanged(String title);
		void onBookmarkListSelected(BookmarkList bookmarkList, boolean selectAll);
	}

	@Bind(R.id.list) RecyclerView recyclerView;
	@Bind(R.id.progressContainer) View progressContainer;
	@Bind(R.id.listContainer) View listContainer;
	@Bind(R.id.textEmpty) TextView textEmpty;

	private WeakReference<ListListener> mListListenerRef;
	private BookmarkListRecyclerAdapter	adapter = new BookmarkListRecyclerAdapter();
	private BookmarkListRetrieveTask mTask;


	public static BookmarkListFragment newInstance() {
		Bundle args = new Bundle();

		BookmarkListFragment fragment = new BookmarkListFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListListenerRef = new WeakReference<>((ListListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ListListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);

		if (adapter.getItemCount() == 0) {
			mTask = new BookmarkListRetrieveTask(getActivity(), this);
			mTask.execute();
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_bookmark_list, container, false);
		ButterKnife.bind(this, v);

		prepareRecyclerView();

		ListListener listener = mListListenerRef.get();
		if (listener != null) {
			listener.onTitleChanged("");
		}

		return  v;
	}

	private void prepareRecyclerView() {
		adapter.setOnItemClickListener(new BookmarkListRecyclerAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(BookmarkList bookmarkList, boolean selectAll) {
				ListListener listener = mListListenerRef.get();
				if (listener != null)
					listener.onBookmarkListSelected(bookmarkList, selectAll);
			}
		});

		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		recyclerView.addItemDecoration(new SpacesItemDecoration((int)getResources().getDimension(R.dimen.cardview_space)));

		setEmptyText("");
		setListShown(adapter.getItemCount() > 0);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mTask != null) {
			mTask.cancel(true);
			mTask = null;
		}
	}

	@Override
	public void onTaskFinished(List<BookmarkList> bookmarkLists) {
		adapter.setBookmarkLists(bookmarkLists);
		setListShown(true);
	}

	@Override
	public void onTaskFailed(Intent errorIntent) {
		getActivity().startActivity(errorIntent);
		getActivity().finish();
	}


	private void setListShown(boolean visible) {
		if (visible) {
			progressContainer.setVisibility(View.GONE);
			listContainer.setVisibility(View.VISIBLE);
		} else {
			progressContainer.setVisibility(View.VISIBLE);
			listContainer.setVisibility(View.GONE);
		}
		textEmpty.setVisibility(adapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
	}

	private void setEmptyText(CharSequence text) {
		textEmpty.setText(text);
	}

}
