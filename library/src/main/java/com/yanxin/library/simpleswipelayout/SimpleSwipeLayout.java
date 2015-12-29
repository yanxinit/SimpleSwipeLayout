package com.yanxin.library.simpleswipelayout;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleSwipeLayout extends FrameLayout {

    public enum Status {
        Middle,
        Open,
        Close
    }

    public interface onMenuClickListener {
        void onMenuClick(View view);
    }

    private int mDragDistance;
    private List<View> mMenuItemViewList;
    private View mContentView;

    private ViewDragHelper mDragHelper;

    private onMenuClickListener mOnMenuClickListener;

    private int mValidDistance;

    private Map<View, Rect> mViewInitLocationMap;

    public void setOnMenuClickListener(onMenuClickListener onMenuClickListener) {
        mOnMenuClickListener = onMenuClickListener;
    }

    private ViewDragHelper.Callback mDragHelperCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (child != mContentView) {
                mDragHelper.captureChildView(mContentView, pointerId);
                return false;
            }
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int minLeft = getPaddingLeft() - mDragDistance;
            int maxLeft = getPaddingLeft();
            int resultLeft = Math.min(maxLeft, Math.max(left, minLeft));
            if (child == mContentView) {
                return resultLeft;
            }
            return left;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mDragDistance;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            float minSpeed = mDragHelper.getMinVelocity();
            float currentX = releasedChild.getX();
            float distanceX = Math.abs(getPaddingLeft() - currentX);
            if (distanceX >= mDragDistance / 2 || xvel < -minSpeed) {
                smoothOpen();
            } else if (distanceX < mDragDistance / 2 || xvel > minSpeed) {
                smoothClose();
            }
            invalidate();
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == mContentView) {
                adjustMenuItemViews(left, dx);
            }
            invalidate();
        }
    };

    private void adjustMenuItemViews(int left, int dx) {
        int distanceX = left - getPaddingLeft();
        int result = distanceX / mMenuItemViewList.size();
        int remainder = distanceX % mMenuItemViewList.size();
        int unit = 0;
        if (remainder != 0)
            unit = remainder / Math.abs(remainder);
        for (int i = 0; i < mMenuItemViewList.size(); i++) {
            View view = mMenuItemViewList.get(i);
            if (i == 0) {
                view.offsetLeftAndRight(dx);
                continue;
            }
            int offset = result * (mMenuItemViewList.size() - i) + unit;
            remainder -= unit;
//            view.setX(view.getX() + ((float) dx) / mMenuItemViewList.size() * (mMenuItemViewList.size() - i));
            Rect rect = mViewInitLocationMap.get(view);
            view.layout(rect.left + offset, rect.top, rect.right + offset, rect.bottom);
        }
    }

    private void smoothClose() {
        mDragHelper.smoothSlideViewTo(mContentView, getPaddingLeft(), getPaddingTop());
    }

    private void smoothOpen() {
        mDragHelper.smoothSlideViewTo(mContentView, getPaddingLeft() - mDragDistance, getPaddingTop());
    }

    public SimpleSwipeLayout(Context context) {
        this(context, null);
    }

    public SimpleSwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDragHelper = ViewDragHelper.create(this, mDragHelperCallback);
        mValidDistance = mDragHelper.getTouchSlop();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        initData();
        startLayout();
    }

    private void startLayout() {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        if (mContentView != null)
            mContentView.layout(left, top, left + mContentView.getMeasuredWidth(), top + mContentView.getMeasuredHeight());
        if (mMenuItemViewList == null)
            return;
        mViewInitLocationMap = new HashMap<>();
        for (View view : mMenuItemViewList) {
            view.layout(mContentView.getRight(), mContentView.getTop(), mContentView.getRight() + view.getMeasuredWidth(), mContentView.getTop() + view.getMeasuredHeight());
            Rect rect = new Rect();
            view.getHitRect(rect);
            mViewInitLocationMap.put(view, rect);
        }
    }

    private void initData() {
        if (mMenuItemViewList != null) {
            mDragDistance = 0;
            for (View view : mMenuItemViewList) {
                mDragDistance += view.getMeasuredWidth();
            }
        }
    }

    float sX;
    float sY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                try {
                    mDragHelper.processTouchEvent(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sX = event.getX();
                sY = event.getY();
                return false;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - sX;
                if (Math.abs(dx) > mValidDistance && checkIsDrag(event)) {
                    return true;
                }
                return false;
        }
        return false;
    }

    private boolean checkIsDrag(MotionEvent ev) {
        float dX = ev.getX() - sX;
        float dY = ev.getY() - sY;
        float angle = Math.abs(dY / dX);
        angle = (float) Math.toDegrees(Math.atan(angle));
        if (angle < 70)
            return true;
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - sX;
                if (Math.abs(dx) > mValidDistance && checkIsDrag(event)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                try {
                    mDragHelper.processTouchEvent(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return true;
    }

    private boolean isInContentView(MotionEvent ev) {
        Rect rect = new Rect();
        mContentView.getHitRect(rect);
        if (rect.contains((int) ev.getX(), (int) ev.getY()))
            return true;
        return false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initMenuItemViews();
        mContentView = getContentView();
        setupMenuClick();
    }

    private void initMenuItemViews() {
        if (getChildCount() <= 1)
            return;
        mMenuItemViewList = new ArrayList<>();
        for (int i = 1; i < getChildCount(); i++) {
            mMenuItemViewList.add(getChildAt(i));
        }
    }

    private void setupMenuClick() {
        for (View view : mMenuItemViewList) {
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnMenuClickListener != null)
                        mOnMenuClickListener.onMenuClick(v);
                }
            });
        }
    }

    private View getContentView() {
        if (getChildCount() == 0)
            return null;
        return getChildAt(0);
    }

    private Status getCurrentStatus() {
        if (mContentView == null) {
            return Status.Close;
        }
        int left = mContentView.getLeft();
        if (left == getPaddingLeft()) {
            return Status.Close;
        }
        if (left == getPaddingLeft() - mDragDistance)
            return Status.Open;
        return Status.Middle;
    }

}
