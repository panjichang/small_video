package com.pan.simplepicture.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.EdgeEffectCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pan.simplepicture.R;
import com.pan.simplepicture.activity.BaseActivity;

public class PagerTab extends ViewGroup {

	private ViewPager mViewPager;
	private PageListener mPageListener = new PageListener();//用于注册给ViewPager监听状态和滚动
	private OnPageChangeListener mDelegatePageListener;//用于通知外界ViewPager的状态和滚动
	private BaseActivity mActivity;

	private int mDividerPadding = 12;// 分割线上下的padding
	private int mDividerWidth = 1;// 分割线的宽度
	private int mDividerColor = 0x00000000;//分割线颜色
	private Paint mDividerPaint;//分割线的画笔

	private int mIndicatorHeight = 3;//指示器的高度
	private int mIndicatorWidth;//指示器的宽度，是动态的随着tab的宽度变化
	private int mIndicatorLeft;//指示器的距离左边的距离
	private int mIndicatorColor = 0xffffffff;//指示器颜色
	private Paint mIndicatorPaint; //指示器的画笔

	private int mContentWidth;//记录自身内容的宽度
	private int mContentHeight;//记录自身内容的高度

	private int mTabPadding = 20;// tab左右的内边距
	private int mTabTextSize = 18; //tab文字大小
	private int mTabBackgroundResId = R.drawable.bg_tab_text;// tab背景资源
	private int mTabTextColorResId = R.color.tab_text_color; //tab文字颜色
	private int mTabCount;//tab的个数

	private int mCurrentPosition = 0;//当前光标所处的tab，规则是以光标的最左端所在的item的position
	private float mCurrentOffsetPixels;//光标左边距离当前光标所处的tab的左边距离
	private int mSelectedPosition = 0; //当前被选中的tab，用于记录手指点击tab的position

	private boolean mIsBeingDragged = false;//是否处于拖动中
	private float mLastMotionX;//上一次手指触摸的x坐标
	private VelocityTracker mVelocityTracker;//用于记录速度的帮助类
	private int mMinimumVelocity;//系统默认的最小满足fling的速度
	private int mMaximumVelocity;//系统默认最大的fling速度
	private int mTouchSlop;//系统默认满足滑动的最小位移

	private ScrollerCompat mScroller;//处理滚动的帮助者
	private int mLastScrollX;//记录上一次滚动的x位置，这是用于处理overScroll，实际位置可能会受到限制

	private int mMaxScrollX = 0;// 控件最大可滚动的距离
	private int mSplitScrollX = 0;// 根据item的个数，计算出每移动一个item控件需要移动的距离

	private EdgeEffectCompat mLeftEdge;//处理overScroll的反馈效果
	private EdgeEffectCompat mRightEdge;

	public PagerTab(Context context) {
		this(context, null);
	}

	public PagerTab(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerTab(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (context instanceof BaseActivity) {
			mActivity = (BaseActivity) context;
		}
		init();
		initPaint();
	}

	/** 初始化一些常量 */
	private void init() {
		//把一个值从dip转换成px
		mIndicatorHeight = mActivity.dip2px(mIndicatorHeight);
		mDividerPadding = mActivity.dip2px(mDividerPadding);
		mTabPadding = mActivity.dip2px(mTabPadding);
		mDividerWidth = mActivity.dip2px(mDividerWidth);
		mTabTextSize = mActivity.dip2px(mTabTextSize);
		//创建一个scroller
		mScroller = ScrollerCompat.create(mActivity);
		//获取一个系统关于View的常量配置类
		final ViewConfiguration configuration = ViewConfiguration.get(mActivity);
		//获取滑动的最小距离
		mTouchSlop = configuration.getScaledTouchSlop();
		//获取fling的最小速度
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		//获取fling的最大速度
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

		mLeftEdge = new EdgeEffectCompat(mActivity);
		mRightEdge = new EdgeEffectCompat(mActivity);
	}

	/** 初始化笔 */
	private void initPaint() {
		mIndicatorPaint = new Paint();
		mIndicatorPaint.setAntiAlias(true);
		mIndicatorPaint.setStyle(Paint.Style.FILL);
		mIndicatorPaint.setColor(mIndicatorColor);

		mDividerPaint = new Paint();
		mDividerPaint.setAntiAlias(true);
		mDividerPaint.setStrokeWidth(mDividerWidth);
		mDividerPaint.setColor(mDividerColor);
	}

	/** 设置ViewPager */
	public void setViewPager(ViewPager viewPager) {
		if (viewPager == null || viewPager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager is null or ViewPager does not have adapter instance.");
		}
		mViewPager = viewPager;
		onViewPagerChanged();
	}

	private void onViewPagerChanged() {
		mViewPager.setOnPageChangeListener(mPageListener);//给ViewPager设置监听
		mTabCount = mViewPager.getAdapter().getCount();//有多少个tab需要看ViewPager有多少个页面
		for (int i = 0; i < mTabCount; i++) {
			if (mViewPager.getAdapter() instanceof IconTabProvider) {//如果想要使用icon作为tab，则需要adapter实现IconTabProvider接口
				addIconTab(i, ((IconTabProvider) mViewPager.getAdapter()).getPageIconResId(i));
			} else {
				addTextTab(i, mViewPager.getAdapter().getPageTitle(i).toString());
			}
		}
		ViewTreeObserver viewTreeObserver = getViewTreeObserver();
		if (viewTreeObserver != null) {//监听第一个的全局layout事件，来设置当前的mCurrentPosition，显示对应的tab
			viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);//只需要监听一次，之后通过listener回调即可
					mCurrentPosition = mViewPager.getCurrentItem();
					if (mDelegatePageListener != null) {
						mDelegatePageListener.onPageSelected(mCurrentPosition);
					}
				}
			});
		}
	}

	/** 设置监听，因为Tab会监听ViewPager的状态，所以不要给ViewPager设置监听了，设置给Tab，由Tab转发 */
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mDelegatePageListener = listener;
	}

	/** 添加文字tab */
	private void addTextTab(final int position, String title) {
		TextView tab = new TextView(mActivity);
		tab.setText(title);
		tab.setGravity(Gravity.CENTER);
		tab.setSingleLine();
		tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
		tab.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
		tab.setTextColor(mActivity.getColorStateList(mTabTextColorResId));
		tab.setBackgroundDrawable(mActivity.getDrawable(mTabBackgroundResId));
		tab.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		addTab(position, tab);
	}

	/** 添加图片icon */
	private void addIconTab(final int position, int resId) {
		ImageButton tab = new ImageButton(mActivity);
		tab.setImageResource(resId);
		tab.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		addTab(position, tab);
	}

	private void addTab(final int position, View tab) {
		tab.setFocusable(true);
		//设置tab的点击事件，当tab被点击时候切换pager的页面
		tab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(position);
			}
		});
		tab.setPadding(mTabPadding, 0, mTabPadding, 0);
		addView(tab, position);
	}

	/** 测量时的回调 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 获取控件自身的宽高,模式
		int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
		int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom() - getPaddingBottom();
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		int totalWidth = 0;
		int highest = 0;
		int goneChildCount = 0;
		for (int i = 0; i < mTabCount; i++) {
			final View child = getChildAt(i);
			if (child == null || child.getVisibility() == View.GONE) {
				goneChildCount--;
				continue;
			}
			int childWidthMeasureSpec;
			int childHeightMeasureSpec;

			LayoutParams childLayoutParams = child.getLayoutParams();
			if (childLayoutParams == null) {
				childLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			}

			if (childLayoutParams.width == LayoutParams.MATCH_PARENT) {
				childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
			} else if (childLayoutParams.width == LayoutParams.WRAP_CONTENT) {
				childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST);
			} else {
				childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childLayoutParams.width, MeasureSpec.EXACTLY);
			}

			if (childLayoutParams.height == LayoutParams.MATCH_PARENT) {
				childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
			} else if (childLayoutParams.height == LayoutParams.WRAP_CONTENT) {
				childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);
			} else {
				childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childLayoutParams.height, MeasureSpec.EXACTLY);
			}

			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

			int childWidth = child.getMeasuredWidth();
			int childHeight = child.getMeasuredHeight();

			totalWidth += childWidth;
			highest = highest < childHeight ? childHeight : highest;
		}

		if (totalWidth <= widthSize) {//如果子Tab的总宽度小于PagerTab，则采用平分模式
			int splitWidth = (int) (widthSize / (mTabCount - goneChildCount + 0.0f) + 0.5f);
			for (int i = 0; i < mTabCount; i++) {
				final View child = getChildAt(i);
				if (child == null || child.getVisibility() == View.GONE) {
					continue;
				}
				int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(splitWidth, MeasureSpec.EXACTLY);
				int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY);
				child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			}
			mMaxScrollX = 0;
			mSplitScrollX = 0;
		} else {//如果所有子View大于控件的宽度
			mMaxScrollX = totalWidth - widthSize;
			mSplitScrollX = (int) (mMaxScrollX / (mTabCount - goneChildCount - 1.0f) + 0.5f);
		}

		if (widthMode == MeasureSpec.EXACTLY) {
			mContentWidth = widthSize;
		} else {
			mContentWidth = totalWidth;
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			mContentHeight = heightSize;
		} else {
			mContentHeight = highest;
		}

		int measureWidth = mContentWidth + getPaddingLeft() + getPaddingRight();
		int measureHeight = mContentHeight + getPaddingTop() + getPaddingBottom();
		setMeasuredDimension(measureWidth, measureHeight);
	}

	/** 布局时的回调 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {//这里简化了，没有考虑margin的情况
		if (changed) {
			int height = b - t;//控件供子View显示的高度
			int left = l;
			for (int i = 0; i < mTabCount; i++) {
				final View child = getChildAt(i);
				if (child == null || child.getVisibility() == View.GONE) {
					continue;
				}
				int top = (int) ((height - child.getMeasuredHeight()) / 2.0f + 0.5f);//如果控件比tab要高，则居中显示
				int right = left + child.getMeasuredWidth();
				child.layout(left, top, right, top + child.getMeasuredHeight());//摆放tab
				left = right;//因为是水平摆放的，所以为下一个准备left值
			}
		}
	}

	/** 绘制时的回调 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		final int height = getHeight();
		//画指示器
		canvas.drawRect(mIndicatorLeft, height - mIndicatorHeight, mIndicatorLeft + mIndicatorWidth, height, mIndicatorPaint);

		// 画分割线
		for (int i = 0; i < mTabCount - 1; i++) {//分割线的个数比tab的个数少一个
			final View child = getChildAt(i);
			if (child == null || child.getVisibility() == View.GONE) {
				continue;
			}
			if (child != null) {
				canvas.drawLine(child.getRight(), mDividerPadding, child.getRight(), mContentHeight - mDividerPadding, mDividerPaint);
			}
		}
		// 因为overScroll效果是一个持续效果，所以需要持续画
		boolean needsInvalidate = false;
		if (!mLeftEdge.isFinished()) {//如果效果没停止
			final int restoreCount = canvas.save();//先保存当前画布
			final int heightEdge = getHeight() - getPaddingTop() - getPaddingBottom();
			final int widthEdge = getWidth();
			canvas.rotate(270);
			canvas.translate(-heightEdge + getPaddingTop(), 0);
			mLeftEdge.setSize(heightEdge, widthEdge);
			needsInvalidate |= mLeftEdge.draw(canvas);
			canvas.restoreToCount(restoreCount);
		}
		if (!mRightEdge.isFinished()) {
			final int restoreCount = canvas.save();
			final int widthEdge = getWidth();
			final int heightEdge = getHeight() - getPaddingTop() - getPaddingBottom();
			canvas.rotate(90);
			canvas.translate(-getPaddingTop(), -(widthEdge + mMaxScrollX));
			mRightEdge.setSize(heightEdge, widthEdge);
			needsInvalidate |= mRightEdge.draw(canvas);
			canvas.restoreToCount(restoreCount);
		}
		if (needsInvalidate) {
			postInvalidate();
		}
	}

	/** 触摸事件是否拦截的方法 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if (mIsBeingDragged && action == MotionEvent.ACTION_MOVE) {//当已经处于拖动，并且当前事件是MOVE，直接消费掉
			return true;
		}
		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				final float x = ev.getX();
				mLastMotionX = x; //记录住当前的x坐标
				mIsBeingDragged = !mScroller.isFinished();//如果按下的时候还在滚动，则把状态处于拖动状态
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				final float x = ev.getX();
				final int xDiff = (int) Math.abs(x - mLastMotionX);//计算两次的差值
				if (xDiff > mTouchSlop) {//如果大于最小移动的距离，则把状态改变为拖动状态
					mIsBeingDragged = true;
					mLastMotionX = x;
					ViewParent parent = getParent();//并请求父View不要再拦截自己触摸事件，交给自己处理
					if (parent != null) {
						parent.requestDisallowInterceptTouchEvent(true);
					}
				}
				break;
			}
			case MotionEvent.ACTION_CANCEL://当手指离开或者触摸事件取消的时候，把拖动状态取消掉
			case MotionEvent.ACTION_UP:
				mIsBeingDragged = false;
				break;
		}
		return mIsBeingDragged;//如果是拖动状态，则拦截事件，交给自己的onTouch处理
	}

	/** 触摸事件的处理方法 */
	public boolean onTouchEvent(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);
		final int action = ev.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN: {//如果是down事件，记录住当前的x坐标
				final float x = ev.getX();
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
				mLastMotionX = x;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				final float x = ev.getX();
				final float deltaX = x - mLastMotionX;
				if (!mIsBeingDragged) {//如果还没有处于拖动，则判断两次的差值是否大于最小拖动的距离
					if (Math.abs(deltaX) > mTouchSlop) {
						mIsBeingDragged = true;
					}
				}
				if (mIsBeingDragged) {//如果处于拖动状态，记录住x坐标
					mLastMotionX = x;
					onMove(deltaX);
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				if (mIsBeingDragged) {
					final VelocityTracker velocityTracker = mVelocityTracker;
					//先对速度进行一个调整，第一个参数是时间单位，1000毫秒，第二个参数是最大速度。
					velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
					float velocity = velocityTracker.getXVelocity();//获取水平方向上的速度
					onUp(velocity);
				}
			}
			case MotionEvent.ACTION_CANCEL: {
				mIsBeingDragged = false;
				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
				break;
			}
		}
		return true;
	}

	private void onMove(float x) {
		if (mMaxScrollX <= 0) {
			if (mViewPager.isFakeDragging() || mViewPager.beginFakeDrag()) {
				mViewPager.fakeDragBy(x);
			}
		} else {
			int scrollByX = -(int) (x + 0.5);
			if (getScrollX() + scrollByX < 0) {
				scrollByX = 0 - getScrollX();
				mLeftEdge.onPull(Math.abs(x) / getWidth());
			}
			if (getScrollX() + scrollByX > mMaxScrollX) {
				scrollByX = mMaxScrollX - getScrollX();
				mRightEdge.onPull(Math.abs(x) / getWidth());
			}
			scrollBy(scrollByX, 0);
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	private void onUp(float velocity) {
		if (mMaxScrollX <= 0) {
			if (mViewPager.isFakeDragging()) mViewPager.endFakeDrag();
		} else {
			if (Math.abs(velocity) <= mMinimumVelocity) {
				return;
			}
			mScroller.fling(getScrollX(), 0, -(int) (velocity + 0.5), 0, 0, mMaxScrollX, 0, 0, 270, 0);
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			int oldX = mLastScrollX;
			mLastScrollX = mScroller.getCurrX();
			if (mLastScrollX < 0 && oldX >= 0) {
				mLeftEdge.onAbsorb((int) mScroller.getCurrVelocity());
			} else if (mLastScrollX > mMaxScrollX && oldX <= mMaxScrollX) {
				mRightEdge.onAbsorb((int) mScroller.getCurrVelocity());
			}
			int x = mLastScrollX;
			if (mLastScrollX < 0) {
				x = 0;
			} else if (mLastScrollX > mMaxScrollX) {
				x = mMaxScrollX;
			}
			scrollTo(x, 0);
		}
		ViewCompat.postInvalidateOnAnimation(this);
	}

	/** 检测mIndicatorOffset的合法性，并计算出其他有关tab的属性值 */
	private void checkAndcalculate() {
		// 如果指示器起始位置比第一个tab的起始位置还要小，纠正为第一个tab的起始位置，指示器宽度就是第一个tab的宽度
		final View firstTab = getChildAt(0);
		if (mIndicatorLeft < firstTab.getLeft()) {
			mIndicatorLeft = firstTab.getLeft();
			mIndicatorWidth = firstTab.getWidth();
		}
		// 如果指示器起始位置比最后一个tab的起始位置还要大，纠正为最后一个tab的起始位置，指示器宽度就是最后一个tab的宽度
		View lastTab = getChildAt(mTabCount - 1);
		if (mIndicatorLeft > lastTab.getLeft()) {
			mIndicatorLeft = lastTab.getLeft();
			mIndicatorWidth = lastTab.getWidth();
		}
		// 通过指示器的起始位置计算出当前处于第几个position，并且计算出已经偏移了多少，偏移量是以当前所处的tab的宽度的百分比
		for (int i = 0; i < mTabCount; i++) {
			View tab = getChildAt(i);
			if (mIndicatorLeft < tab.getLeft()) {
				mCurrentPosition = i - 1;
				View currentTab = getChildAt(mCurrentPosition);
				mCurrentOffsetPixels = (mIndicatorLeft - currentTab.getLeft()) / (currentTab.getWidth() + 0.0f);
				break;
			}
		}
	}

	/** 滚动到指定的child */
	public void scrollSelf(int position, float offset) {
		if (position >= mTabCount) {
			return;
		}
		final View tab = getChildAt(position);
		mIndicatorLeft = (int) (tab.getLeft() + tab.getWidth() * offset + 0.5);
		int rightPosition = position + 1;
		if (offset > 0 && rightPosition < mTabCount) {
			View rightTab = getChildAt(rightPosition);
			mIndicatorWidth = (int) (tab.getWidth() * (1 - offset) + rightTab.getWidth() * offset + 0.5);
		} else {
			mIndicatorWidth = tab.getWidth();
		}
		checkAndcalculate();

		int newScrollX = position * mSplitScrollX + (int) (offset * mSplitScrollX + 0.5);
		if (newScrollX < 0) {
			newScrollX = 0;
		}
		if (newScrollX > mMaxScrollX) {
			newScrollX = mMaxScrollX;
		}
		//scrollTo(newScrollX, 0);//滑动
		int duration = 100;
		if (mSelectedPosition != -1) {
			duration = (Math.abs(mSelectedPosition - position)) * 100;
		}
		mScroller.startScroll(getScrollX(), 0, (newScrollX - getScrollX()), 0, duration);
		ViewCompat.postInvalidateOnAnimation(this);
	}

	/** 选中指定位置的Tab */
	private void selectTab(int position) {
		for (int i = 0; i < mTabCount; i++) {
			View tab = getChildAt(i);
			if (tab != null) {
				tab.setSelected(position == i);
			}
		}
	}

	/** ViewPager的OnPageChangeListener实现类，因为我们需要在PagerTab中获取PagerView的监听，以便可以调整tab */
	private class PageListener implements OnPageChangeListener {
		@Override
		public void onPageScrolled(int position, float positionOffset, final int positionOffsetPixels) {
			//根据VierPager的偏移值来滚动tab
			scrollSelf(position, positionOffset);
			if (mDelegatePageListener != null) {//这个是提供给外部的
				mDelegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (state == ViewPager.SCROLL_STATE_IDLE) {
				mSelectedPosition = -1;
			}
			if (mDelegatePageListener != null) {
				mDelegatePageListener.onPageScrollStateChanged(state);
			}
		}

		@Override
		public void onPageSelected(int position) {
			mSelectedPosition = position;
			selectTab(position);
			if (mDelegatePageListener != null) {
				mDelegatePageListener.onPageSelected(position);
			}
		}
	}

	/** 如果指示器希望是图片，则继承该接口 */
	public interface IconTabProvider {
		public int getPageIconResId(int position);
		public int getPageSelectedIconResId();
	}
}
