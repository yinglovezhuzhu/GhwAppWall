/*
 * Copyright (C) 2014. The Android Open Source Project.
 *
 *         yinglovezhuzhu@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ghw.sdk.extend.widget.pullview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by yinglovezhuzhu@gmail.com
 */
public abstract class BasePullListView extends ListView implements IPullView, AbsListView.OnScrollListener {

    private PullFooterView mFooterView;

    protected int mFirstItemIndex;
    protected int mVisibleItemCount;
    protected int mTotalItemCount;

    protected int mVerticalScrollOffset = 0;
    protected int mVerticalScrollExtent = 0;
    protected int mVerticalScrollRange = 0;

    /**
     * Whether it can refresh.
     */
    protected boolean mEnablePullRefresh = false;
    /**
     * Whether it can load more data.
     */
    protected boolean mEnableLoadMore = false;
    /**
     * Is refreshing data
     */
    protected boolean mRefreshing = false;
    /**
     * Can be over scroll *
     */
    protected boolean mEnableOverScroll = false;

    protected LoadMode mLoadMode = LoadMode.PULL_TO_LOAD;

    protected int mState = IDEL;

    protected OnRefreshListener mRefreshListener;
    protected OnLoadMoreListener mLoadMoreListener;
    protected OnScrollListener mScrollListener;
    protected OnItemClickListener mItemClickListener;

    /**
     * Constructor
     *
     * @param context
     */
    public BasePullListView(Context context) {
        super(context);
        initView(context);
    }

    /**
     * Constructor
     *
     * @param context
     */
    public BasePullListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    /**
     * Constructor
     *
     * @param context
     */
    public BasePullListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mFirstItemIndex = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        mTotalItemCount = totalItemCount;
        mVerticalScrollOffset = computeVerticalScrollOffset();
        mVerticalScrollExtent = computeVerticalScrollExtent();
        mVerticalScrollRange = computeVerticalScrollRange();
        
        if (null != mScrollListener) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE
                && mVerticalScrollRange == mVerticalScrollOffset + mVerticalScrollExtent
                && mState == IDEL) {
            if (mEnableLoadMore && mLoadMode == LoadMode.AUTO_LOAD) {
                setSelection(mTotalItemCount);
                loadMore();
                mState = LOADING;
                updateFooterViewByState(0);
            }
        }
        if (null != mScrollListener) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    private int mStartY;
    private boolean mRecording = false;
    private boolean mIsBack = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = (int) event.getY();
                if (!mRecording) {
                    mRecording = mVerticalScrollRange == mVerticalScrollOffset + mVerticalScrollExtent;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int tempY = (int) event.getY();
                if (mVerticalScrollRange > mVerticalScrollExtent && mFirstItemIndex + mVisibleItemCount == mTotalItemCount) {
                	if (!mRecording) {
                		mRecording = true;
                		mStartY = tempY;
                	}
                	int moveY = tempY - mStartY;
                	int scrollY = Math.abs(moveY) / OFFSET_RATIO;
                	if (mState != LOADING
                			&& (mLoadMode == LoadMode.PULL_TO_LOAD && (mEnableLoadMore || mEnableOverScroll)
                			|| mLoadMode == LoadMode.AUTO_LOAD && !mEnableLoadMore && mEnableOverScroll)) {
                		//可以向上pull的条件是
                		//1.mState != LOADING，即非LOADING状态下
                		//2.mLoadMode == LoadMode.PULL_TO_LOAD时有更多数据可加载或者可以过度滑动（OverScroll）
                		// 或者mLoadMode == LoadMode.AUTO_LOAD时没有更多数据可加载但可以过度滑动（OverScroll）
                		
                		// Ensure that the process of setting padding, current position has always been at the footer,
                		// or if when the list exceeds the screen, then, when the push up, the list will scroll at the same time
                		switch (mState) {
                		case RELEASE_TO_LOAD: // release-to-load
                			if(mFirstItemIndex > 0) {
                				setSelection(mTotalItemCount);
                			}
                			// Slide down, header part was covered, but not all be covered(Pull down to cancel)
                			if (moveY < 0 && scrollY <= mFooterView.mViewHeight) {
                				mState = PULL_TO_LOAD;
                			} else if (moveY >= 0 && mFirstItemIndex > 0) { //Slide up(Pull up to make footer to show)
                				mState = IDEL;
                			}
                			updateFooterViewByState(scrollY - mFooterView.mViewHeight);
                			break;
                		case PULL_TO_LOAD:
                			if(mFirstItemIndex > 0) {
                				setSelection(mTotalItemCount);
                			}
                			// Pull up to the state can enter RELEASE_TO_REFRESH
                			if (scrollY > mFooterView.mViewHeight) {
                				mState = RELEASE_TO_LOAD;
                				mIsBack = true;
                			} else if (moveY >= 0) {
                				mState = IDEL;
                			}
                			updateFooterViewByState(scrollY - mFooterView.mViewHeight);
                			break;
                		case IDEL:
                			if (moveY < 0) {
                				mState = PULL_TO_LOAD;
                			}
                			updateFooterViewByState(-mFooterView.mViewHeight);
                			break;
                		default:
                			break;
                		}
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mFirstItemIndex + mVisibleItemCount >= mTotalItemCount) {
                    switch (mState) {
                        case IDEL:
                            //Do nothing.
                            break;
                        case PULL_TO_LOAD:
                            //Pull to load more data.
                            mState = IDEL;
                            updateFooterViewByState(-mFooterView.mViewHeight);
                            break;
                        case RELEASE_TO_LOAD:
                            if (mEnableLoadMore) {
                                //Release to load more data.
                                updateFooterViewByState(0);
                                loadMore();
                            } else {
                                mState = IDEL;
                                updateFooterViewByState(-mFooterView.mViewHeight);
                            }
                            break;
                        default:
                            break;
                    }
                }
                mRecording = false;
                mIsBack = false;
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        this.mScrollListener = l;
    }


    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
//        super.setOnItemClickListener(listener);
        this.mItemClickListener = listener;
    }

    /**
     * Can load more or not
     * @return
     */
    public boolean canLoadMore() {
        return mEnableLoadMore;
    }

    /**
     * Do load more operation.
     */
    protected void loadMore() {
        if (null == mLoadMoreListener || mState == LOADING) {
            return;
        }
        mRefreshing = false;
        mState = LOADING;
        mLoadMoreListener.onLoadMore();
        mFooterView.progress();
    }

    /**
     * Do refresh operation.
     */
    protected void refresh() {
        if (null == mRefreshListener || mState == LOADING) {
            return;
        }
        mRefreshing = true;
        mState = LOADING;
        mRefreshListener.onRefresh();
    }

    /**
     * Update footer view by state
     *
     * @param paddingBottom
     */
    private void updateFooterViewByState(int paddingBottom) {
        mFooterView.rotate(paddingBottom);
        switch (mState) {
            case RELEASE_TO_LOAD:
                break;
            case PULL_TO_LOAD:
                if (mIsBack) {
                    mIsBack = false;
                }
                break;
            case LOADING:
                break;
            case IDEL:
                break;
            default:
                break;
        }
        mFooterView.setVisibility(mEnableLoadMore ? View.VISIBLE : View.INVISIBLE);
        mFooterView.setPadding(0, 0, 0, paddingBottom);
    }

    /**
     * Refresh data complete
     */
    public void refreshCompleted() {
        mState = IDEL;
        mRefreshing = false;
        mRecording = false;
    }

    /**
     * Load more complete
     */
    public void loadMoreCompleted(boolean canLoadmore) {
        mState = IDEL;
        mRefreshing = false;
        mRecording = false;
        this.mEnableLoadMore = null != mLoadMoreListener && canLoadmore;
        updateFooterViewByState(-mFooterView.mViewHeight);
    }

    /**
     * Show loading view on foot<br>
     * <br><p>Use this method when header view was added on PullListView.
     *
     * @param text
     */
    public void onFootLoading(CharSequence text) {
        mState = LOADING;
        mFooterView.setPadding(0, 0, 0, 0);
        mFooterView.setArrowVisibility(View.GONE);
        mFooterView.setTitleVisibility(View.VISIBLE);
        mFooterView.setTitleText(text);
        mFooterView.setVisibility(View.VISIBLE);
    }

    /**
     * Show loading view on foot<br>
     * <br><p>Use this method when header view was added on PullListView.
     *
     * @param resId
     */
    public void onFootLoading(int resId) {
        mState = LOADING;
        mFooterView.setPadding(0, 0, 0, 0);
        mFooterView.setArrowVisibility(View.GONE);
        mFooterView.setTitleVisibility(View.VISIBLE);
        mFooterView.setTitleText(resId);
        mFooterView.setVisibility(View.VISIBLE);
    }

    /**
     * Sets listener to listen refresh action
     *
     * @param listener
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mRefreshListener = listener;
        mEnablePullRefresh = null != listener;
    }

    /**
     * Sets listener to listen load more action
     *
     * @param listener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mLoadMoreListener = listener;
        mEnableLoadMore = null != listener;
    }

    /**
     * Sets the mode to load more data.<br>
     * <p>can use value is {@link LoadMode#AUTO_LOAD}
     * and {@link LoadMode#PULL_TO_LOAD}<br>
     * default is {@link LoadMode#AUTO_LOAD}
     *
     * @param mode
     * @see {@link com.ghw.sdk.extend.widget.pullview.IPullView.LoadMode}
     */
    public void setLoadMode(LoadMode mode) {
        this.mLoadMode = mode;
    }

    /**
     * Sets the pull listview can over scroll or not.
     *
     * @param enable
     */
    public void setEnableOverScroll(boolean enable) {
        this.mEnableOverScroll = enable;
    }

    /**
     * Gets it is refreshing<br/>
     * </br><p/>If doing refresh operation, you need to use this method before {@link #refreshCompleted()}<br/>
     * to get it is refresh operation or not.
     *
     * @return
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    private void initView(Context context) {
        mFooterView = new PullFooterView(context);
        addFooterView(mFooterView, null, true);
        
        mState = IDEL;
        mRefreshing = false;
        mRecording = false;
        mEnableLoadMore = null != mLoadMoreListener && mEnableLoadMore;
        updateFooterViewByState(-mFooterView.mViewHeight);

        super.setOnScrollListener(this);
        super.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(null != mItemClickListener) {
                    mItemClickListener.onItemClick(parent, view, position - getHeaderViewsCount(), id);
                }
            }
        });
    }
}
