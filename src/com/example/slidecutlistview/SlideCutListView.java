package com.example.slidecutlistview;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

/**
 * 
 * @author andywuchuanlong
 * 
 */
public class SlideCutListView extends ListView implements OnScrollListener{
	/**
	 * ��ǰ������ListView��position
	 */
	private int slidePosition;
	/**
	 * ��ָ����X������
	 */
	private int downY;
	/**
	 * ��ָ����Y������
	 */
	private int downX;
	/**
	 * ��Ļ���
	 */
	private int screenWidth;
	/**
	 * ListView��item
	 */
	private View itemView;
	/**
	 * ������
	 */
	private Scroller scroller;
	private static final int SNAP_VELOCITY = 600;
	/**
	 * �ٶ�׷�ٶ���
	 */
	private VelocityTracker velocityTracker;
	/**
	 * �Ƿ���Ӧ������Ĭ��Ϊ����Ӧ
	 */
	private boolean isSlide = false;
	private boolean isFresh = false;
	/**
	 * ��Ϊ���û���������С����
	 */
	private int mTouchSlop;
	/**
	 *  �Ƴ�item��Ļص��ӿ�
	 */
	private RemoveListener mRemoveListener;
	/**
	 * ����ָʾitem������Ļ�ķ���,�����������,��һ��ö��ֵ�����
	 */
	private RemoveDirection removeDirection;

	// ����ɾ�������ö��ֵ
	public enum RemoveDirection {
		RIGHT, LEFT;
	}


	public SlideCutListView(Context context) {
		this(context, null);
	}

	public SlideCutListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideCutListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		screenWidth = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		scroller = new Scroller(context);
		// ��ȡϵͳ��Ϊ�û���������С���� ��
		// Distance in pixels a touch can wander before we think the user is scrolling
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		initWithContext(getContext());
	}
	
	/**
	 * ���û���ɾ���Ļص��ӿ�
	 * @param removeListener
	 */
	public void setRemoveListener(RemoveListener removeListener) {
		this.mRemoveListener = removeListener;
	}

	int mStartX;
	int mStartY;
	/**
	 * �ַ��¼�����Ҫ�������жϵ�������Ǹ�item, �Լ�ͨ��postDelayed��������Ӧ���һ����¼�
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			addVelocityTracker(event);
			// ����scroller������û�н���������ֱ�ӷ���
			if (!scroller.isFinished()) {
				return super.dispatchTouchEvent(event);
			}
			mStartX = downX = (int) event.getX();
			mStartY = downY = (int) event.getY();
			// Maps a point to a position in the list.
			slidePosition = pointToPosition(downX, downY);
			// ��Ч��position, �����κδ���
			if (slidePosition == AdapterView.INVALID_POSITION) {
				return super.dispatchTouchEvent(event);
			}
			// ��ȡ���ǵ����item view
			itemView = getChildAt(slidePosition - getFirstVisiblePosition());
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			float x = Math.abs(event.getX() - downX);
			float y = Math.abs(event.getY() - downY);
			if (Math.abs(getXScrollVelocity()) > SNAP_VELOCITY
					|| (x > mTouchSlop && y < mTouchSlop)) {
					// ��Ӧ����
					isSlide = true;
			}else {
				x = Math.abs(event.getX() - mStartX);
				y = Math.abs(event.getY() - mStartY);
				if ((y-x)>mTouchSlop && y>=mTouchSlop){
					Log.e("slide","fresh--");
					isFresh = true;
				}
			}
			break;
		}
		case MotionEvent.ACTION_UP:
			// �ͷ��ٶȸ�����
			recycleVelocityTracker();
			break;
		}

		return super.dispatchTouchEvent(event);
	}
	
	/**
	 * ���������϶�ListView item���߼�
	 * �����ƶ�����
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isSlide && slidePosition != AdapterView.INVALID_POSITION) {
			// ��Ҫ��Ӧ����
			addVelocityTracker(ev);
			final int action = ev.getAction();
			int x = (int) ev.getX();
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				int deltaX = downX - x;
				downX = x;
				// ��ָ�϶�itemView����, deltaX����0���������С��0���ҹ�
				itemView.scrollBy(deltaX, 0);
				break;
			case MotionEvent.ACTION_UP:
				int velocityX = getXScrollVelocity();
				if (velocityX > SNAP_VELOCITY) {
					scrollRight();
				} else if (velocityX < -SNAP_VELOCITY) {
					scrollLeft();
				} else {
					scrollByDistanceX();
				}

				recycleVelocityTracker();
				// ��ָ�뿪��ʱ��Ͳ���Ӧ���ҹ���
				isSlide = false;
				break;
			}
			return true; // �϶���ʱ��ListView������
		}else if (isFresh){
			
			if (mLastY == -1) {
				mLastY = ev.getRawY();
			}

			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastY = ev.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				final float deltaY = ev.getRawY() - mLastY;
				mLastY = ev.getRawY();
				if (getFirstVisiblePosition() == 0
						&& (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
					// the first item is showing, header has shown or pull down.
					updateHeaderHeight(deltaY / OFFSET_RADIO);
					invokeOnScrolling();
				} else if (getLastVisiblePosition() == mTotalItemCount - 1
						&& (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
					// last item, already pulled up or want to pull up.
					updateFooterHeight(-deltaY / OFFSET_RADIO);
				}
				break;
			default:
//				isFresh = false;
				mLastY = -1; // reset
				if (getFirstVisiblePosition() == 0) {
					// invoke refresh
					if (mEnablePullRefresh
							&& mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
						mPullRefreshing = true;
						mHeaderView.setState(SlideCutListViewHeader.STATE_REFRESHING);
						if (mListViewListener != null) {
							mListViewListener.onRefresh();
						}
					}
					Log.e("slide", "resetHeaderHeight");
					resetHeaderHeight();
				}
				if (getLastVisiblePosition() == mTotalItemCount - 1) {
					// invoke load more.
					if (mEnablePullLoad
							&& mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
						startLoadMore();
					}
					Log.e("slide", "resetFooterHeight");
					resetFooterHeight();
				}
				break;
			}
		}
		//����ֱ�ӽ���ListView������onTouchEvent�¼�
		return super.onTouchEvent(ev);
	}
	/**
	 * ���һ�����getScrollX()���ص������Ե�ľ��룬������View���ԵΪԭ�㵽��ʼ�����ľ��룬�������ұ߻���Ϊ��ֵ
	 */
	private void scrollRight() {
		removeDirection = RemoveDirection.RIGHT;
		final int delta = (screenWidth + itemView.getScrollX());
		// ����startScroll����������һЩ�����Ĳ�����������computeScroll()�����е���scrollTo������item
		scroller.startScroll(itemView.getScrollX(), 0, -delta, 0,Math.abs(delta));
		postInvalidate(); // ˢ��itemView
	}

	/**
	 * ���󻬶���������������֪�����󻬶�Ϊ��ֵ
	 */
	private void scrollLeft() {
		removeDirection = RemoveDirection.LEFT;
		final int delta = (screenWidth - itemView.getScrollX());
		// ����startScroll����������һЩ�����Ĳ�����������computeScroll()�����е���scrollTo������item
		scroller.startScroll(itemView.getScrollX(), 0, delta, 0,Math.abs(delta));
		postInvalidate(); // ˢ��itemView
	}

	/**
	 * ������ָ����itemView�ľ������ж��ǹ�������ʼλ�û�������������ҹ���
	 */
	private void scrollByDistanceX() {
		// �����������ľ��������Ļ������֮һ��������ɾ��
		if (itemView.getScrollX() >= screenWidth / 3) {
			scrollLeft();
		} else if (itemView.getScrollX() <= -screenWidth / 3) {
			scrollRight();
		} else {
			// ���ص�ԭʼλ��,Ϊ��͵����������ֱ�ӵ���scrollTo����
			itemView.scrollTo(0, 0);
		}

	}



	@Override
	public void computeScroll() {
		// ����startScroll��ʱ��scroller.computeScrollOffset()����true��
		if (isFresh){
			Log.e("slide", isFresh+"");
			if (mScroller.computeScrollOffset()) {
				if (mScrollBack == SCROLLBACK_HEADER) {
					mHeaderView.setVisiableHeight(mScroller.getCurrY());
				} else {
					mFooterView.setBottomMargin(mScroller.getCurrY());
				}
				postInvalidate();
				invokeOnScrolling();
			}else{
				isFresh = false;
			}
		}else{
			if (scroller.computeScrollOffset()) {
				// ��ListView item���ݵ�ǰ�Ĺ���ƫ�������й���
				itemView.scrollTo(scroller.getCurrX(), scroller.getCurrY());
				postInvalidate();
				// ��������������ʱ����ûص��ӿ�
				if (scroller.isFinished()) {
					if (mRemoveListener == null) {
						throw new NullPointerException("RemoveListener is null, we should called setRemoveListener()");
					}
					itemView.scrollTo(0, 0);
					mRemoveListener.removeItem(removeDirection, slidePosition);
				}
			}
		}
	}

	/**
	 * ����û����ٶȸ�����
	 * 
	 * @param event
	 */
	private void addVelocityTracker(MotionEvent event) {
		if (velocityTracker == null) {
			/**
			 * Retrieve a new VelocityTracker object to watch the velocity of a motion. 
			 * Be sure to call recycle when done. 
			 */
			velocityTracker = VelocityTracker.obtain();
		}
		velocityTracker.addMovement(event);
	}

	/**
	 * �Ƴ��û��ٶȸ�����
	 */
	private void recycleVelocityTracker() {
		if (velocityTracker != null) {
			velocityTracker.recycle();
			velocityTracker = null;
		}
	}

	/**
	 * ��ȡX����Ļ����ٶ�,����0���һ�������֮����
	 * @return
	 */
	private int getXScrollVelocity() {
		velocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) velocityTracker.getXVelocity();
		return velocity;
	}

	/**
	 * 
	 * ��ListView item������Ļ���ص�����ӿ�
	 * ������Ҫ�ڻص�����removeItem()���Ƴ���Item,Ȼ��ˢ��ListView
	 *
	 */
	public interface RemoveListener {
		public void removeItem(RemoveDirection direction, int position);
	}

	/*************************  XlistView  ***********************************/
	
	private float mLastY = -1; // save event y
	private Scroller mScroller; // used for scroll back
	private OnScrollListener mScrollListener; // user's scroll listener

	// the interface to trigger refresh and load more.
	private IXListViewListener mListViewListener;

	// -- header view
	private SlideCutListViewHeader mHeaderView;
	// header view content, use it to calculate the Header's height. And hide it
	// when disable pull refresh.
	private RelativeLayout mHeaderViewContent;
	private TextView mHeaderTimeView;
	private int mHeaderViewHeight; // header view's height
	private boolean mEnablePullRefresh = true;
	private boolean mPullRefreshing = false; // is refreashing.

	// -- footer view
	private SlideCutListViewFooter mFooterView;
	private boolean mEnablePullLoad;
	private boolean mPullLoading;
	private boolean mIsFooterReady = false;

	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;

	// for mScroller, scroll back from header or footer.
	private int mScrollBack;
	private final static int SCROLLBACK_HEADER = 0;
	private final static int SCROLLBACK_FOOTER = 1;

	private final static int SCROLL_DURATION = 400; // scroll back duration
	private final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >= 50px
														// at bottom, trigger
														// load more.
	private final static float OFFSET_RADIO = 1.8f; // support iOS like pull
													// feature.
	
	private void initWithContext(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());
		// XListView need the scroll event, and it will dispatch the event to
		// user's listener (as a proxy).
		super.setOnScrollListener(this);

		// init header view
		mHeaderView = new SlideCutListViewHeader(context);
		mHeaderViewContent = (RelativeLayout) mHeaderView.findViewById(R.id.xlistview_header_content);
		mHeaderTimeView = (TextView) mHeaderView.findViewById(R.id.xlistview_header_time);
		addHeaderView(mHeaderView);

		// init footer view
		mFooterView = new SlideCutListViewFooter(context);

		// init header height
		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						mHeaderViewHeight = mHeaderViewContent.getHeight();
						getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				});
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// make sure XListViewFooter is the last footer view, and only add once.
		if (mIsFooterReady == false) {
			mIsFooterReady = true;
			addFooterView(mFooterView);
		}
		super.setAdapter(adapter);
	}

	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderViewContent.setVisibility(View.INVISIBLE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * enable or disable pull up load more feature.
	 * 
	 * @param enable
	 */
	public void setPullLoadEnable(boolean enable) {
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
		} else {
			mPullLoading = false;
			mFooterView.show();
			mFooterView.setState(SlideCutListViewFooter.STATE_NORMAL);
			// both "pull up" and "click" will invoke load more.
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startLoadMore();
				}
			});
		}
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			isFresh = true;
			resetHeaderHeight();
		}
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(SlideCutListViewFooter.STATE_NORMAL);
		}
	}

	/**
	 * set last refresh time
	 * 
	 * @param time
	 */
	public void setRefreshTime(String time) {
		mHeaderTimeView.setText(time);
	}

	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnXScrollListener) {
			OnXScrollListener l = (OnXScrollListener) mScrollListener;
			l.onXScrolling(this);
		}
	}

	private void updateHeaderHeight(float delta) {
		mHeaderView.setVisiableHeight((int) delta
				+ mHeaderView.getVisiableHeight());
		if (mEnablePullRefresh && !mPullRefreshing) { // δ����ˢ��״̬�����¼�ͷ
			if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
				mHeaderView.setState(SlideCutListViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(SlideCutListViewHeader.STATE_NORMAL);
			}
		}
		setSelection(0); // scroll to top each time
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0) // not visible.
			return;
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int finalHeight = 0; // default: scroll back to dismiss header.
		// is refreshing, just scroll back to show all the header.
		if (mPullRefreshing && height > mHeaderViewHeight) {
			finalHeight = mHeaderViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		Log.e("slide", (finalHeight - height)+"--"+height+"--"+isFresh);
		mScroller.startScroll(0, height, 0, finalHeight - height,
				SCROLL_DURATION);
		// trigger computeScroll
		invalidate();
	}

	private void updateFooterHeight(float delta) {
		int height = mFooterView.getBottomMargin() + (int) delta;
		if (mEnablePullLoad && !mPullLoading) {
			if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load
													// more.
				mFooterView.setState(SlideCutListViewFooter.STATE_READY);
			} else {
				mFooterView.setState(SlideCutListViewFooter.STATE_NORMAL);
			}
		}
		mFooterView.setBottomMargin(height);

		// setSelection(mTotalItemCount - 1); // scroll to bottom
	}

	private void resetFooterHeight() {
		int bottomMargin = mFooterView.getBottomMargin();
		if (bottomMargin > 0) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin,
					SCROLL_DURATION);
			invalidate();
		}
	}

	private void startLoadMore() {
		mPullLoading = true;
		mFooterView.setState(SlideCutListViewFooter.STATE_LOADING);
		if (mListViewListener != null) {
			mListViewListener.onLoadMore();
		}
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// send to user's listener
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}

	public void setXListViewListener(IXListViewListener l) {
		mListViewListener = l;
	}

	/**
	 * you can listen ListView.OnScrollListener or this one. it will invoke
	 * onXScrolling when header/footer scroll back.
	 */
	public interface OnXScrollListener extends OnScrollListener {
		public void onXScrolling(View view);
	}

	/**
	 * implements this interface to get refresh/load more event.
	 */
	public interface IXListViewListener {
		public void onRefresh();

		public void onLoadMore();
	}

}
