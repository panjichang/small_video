package com.pan.simplepicture.adapter;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.util.Insertable;
import com.nhaarman.listviewanimations.util.Swappable;
import com.pan.simplepicture.R;
import com.pan.simplepicture.activity.BaseActivity;
import com.pan.simplepicture.annotations.NonNull;
import com.pan.simplepicture.holder.BaseHolder;
import com.pan.simplepicture.manager.ThreadManager;

public class BaseListAdapter<T> extends BaseAdapter implements Swappable,
		Insertable<T> {
	private BaseActivity mActivity;
	private List<T> mItems;
	private Class<? extends BaseHolder<T>> clazz;
	private boolean mHasMore;
	private boolean mIsLoading;
	private boolean mLoadError;
	private View mLoadingView;
	private View mErrorView;
	private View mEmptyView;
	private OnLoadMoreListener<T> listener;

	public BaseListAdapter(BaseActivity mActivity, List<T> list,
			Class<? extends BaseHolder<T>> clazz) {
		this.mActivity = mActivity;
		this.mItems = list;
		mHasMore = canLoadMore();
		this.clazz = clazz;
	}

	@Override
	public int getCount() {
		if (mItems != null) {
			return mItems.size() + 1;
		}
		return 0;
	}

	public void setOnLoadMoreListener(OnLoadMoreListener<T> listener) {
		mHasMore = true;
		this.listener = listener;
	}

	@Override
	public T getItem(int arg0) {
		// TODO Auto-generated method stub
		return mItems.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int count = getCount();
		if ((position >= count - 1) && mHasMore) {
			loadMore();
		}
		View view = null;
		if (getItemViewType(position) != 0) {
			view = getItemView(position, convertView, parent);
		} else {
			view = getMoreView(position, convertView, parent);
		}
		return view;
	}

	private View getItemView(int position, View view, ViewGroup parent) {
		BaseHolder<T> holder = null;
		if (view == null) {
			try {
				Constructor<? extends BaseHolder<T>> constructor = clazz
						.getConstructor(BaseActivity.class, mItems
								.get(position).getClass());
				holder = constructor.newInstance(mActivity,
						mItems.get(position));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			holder = (BaseHolder<T>) view.getTag();
			holder.setData(mItems.get(position));
		}
		return holder.getRootView();
	}

	protected View getMoreView(int position, View convertView, ViewGroup parent) {
		View view;
		if (mHasMore) {
			if (mIsLoading && !mLoadError) {
				if (mLoadingView == null) {
					mLoadingView = mActivity
							.inflate(R.layout.list_more_loading);
				}
				view = mLoadingView;
			} else {
				if (mErrorView == null) {
					mErrorView = mActivity.inflate(R.layout.list_more_error);
				}
				view = mErrorView;
			}
		} else {
			if (mEmptyView == null) {
				mEmptyView = new View(mActivity);
			}
			view = mEmptyView;
		}
		return view;
	}

	public void loadMore() {
		if (!mIsLoading && !mLoadError) {
			mIsLoading = true;
		} else {
			return;
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				int startIndex = 0;
				if (mItems != null) {
					startIndex = mItems.size();
				}
				List<T> moreDatas = onLoadMore(startIndex);
				if (moreDatas != null) {
					mItems.addAll(moreDatas);
					if (moreDatas.size() < 10) {
						mHasMore = false;
					}
				} else {
					mLoadError = true;
				}
				mActivity.post(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
						mIsLoading = false;
					}
				});
			}
		};
		ThreadManager.getLongPool().execute(runnable);
	}

	protected boolean canLoadMore() {
		return false;
	}

	protected List<T> onLoadMore(int starIndex) {
		if (listener != null) {
			return listener.OnLoadMore(starIndex);
		}
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return super.getViewTypeCount() + 1;// 加1是为了最后加载更多的布局
	}

	@Override
	public int getItemViewType(int position) {
		if (position == getCount() - 1) {
			return 0;// 加载更多的布局
		} else {
			return 1;// 普通item的布局
		}
	}

	public interface OnLoadMoreListener<T> {
		List<T> OnLoadMore(int starIndex);
	}

	/**
	 * Returns the items.
	 */
	@NonNull
	public List<T> getItems() {
		return mItems;
	}

	/**
	 * Appends the specified element to the end of the {@code List}.
	 * 
	 * @param object
	 *            the object to add.
	 * 
	 * @return always true.
	 */
	public boolean add(@NonNull final T object) {
		boolean result = mItems.add(object);
		notifyDataSetChanged();
		return result;
	}

	@Override
	public void add(final int index, @NonNull final T item) {
		mItems.add(index, item);
		notifyDataSetChanged();
	}

	/**
	 * Adds the objects in the specified collection to the end of this List. The
	 * objects are added in the order in which they are returned from the
	 * collection's iterator.
	 * 
	 * @param collection
	 *            the collection of objects.
	 * 
	 * @return {@code true} if this {@code List} is modified, {@code false}
	 *         otherwise.
	 */
	public boolean addAll(@NonNull final Collection<? extends T> collection) {
		boolean result = mItems.addAll(collection);
		notifyDataSetChanged();
		return result;
	}

	public boolean contains(final T object) {
		return mItems.contains(object);
	}

	public void clear() {
		mItems.clear();
		notifyDataSetChanged();
	}

	public boolean remove(@NonNull final Object object) {
		boolean result = mItems.remove(object);
		notifyDataSetChanged();
		return result;
	}

	@NonNull
	public T remove(final int location) {
		T result = mItems.remove(location);
		notifyDataSetChanged();
		return result;
	}

	@Override
	public void swapItems(final int positionOne, final int positionTwo) {
		T firstItem = mItems.set(positionOne, getItem(positionTwo));
		notifyDataSetChanged();
		mItems.set(positionTwo, firstItem);
	}

	public void propagateNotifyDataSetChanged(
			@NonNull final BaseAdapter slavedAdapter) {
		mDataSetChangedSlavedAdapter = slavedAdapter;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		if (mDataSetChangedSlavedAdapter != null) {
			mDataSetChangedSlavedAdapter.notifyDataSetChanged();
		}
	}

	private BaseAdapter mDataSetChangedSlavedAdapter;
}
