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

import com.yanxin.library.simpleswipelayout.interfaces.SwipeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleSwipeLayout extends FrameLayout {

    private int mDragDistance;
    private List<View> mMenuItemViewList;
    private View mContentView;

    private ViewDragHelper mDragHelper;
    private int mValidDistance;
    private Map<View, Rect> mViewInitLocationMap;

    private SwipeListener mSwipeListener;
    private onMenuClickListener mOnMenuClickListener;

    private Mode mMode = Mode.PullOut;

    public enum Status {
        Middle,
        Open,
        Close
    }

    public enum Mode {
        PullOut,
        LayDown,
        PullStack
    }

    public interface onMenuClickListener {
        void onMenuClick(View view);
    }

    public void setOnMenuClickListener(onMenuClickListener onMenuClickListener) {
        mOnMenuClickListener = onMenuClickListener;
    }

    public void setSwipeListener(SwipeListener swipeListener) {
        mSwipeListener = swipeListener;
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
            return resultLeft;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mDragDistance;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            processHandRelease(releasedChild, xvel);
            if (mSwipeListener != null)
                mSwipeListener.onHandRelease(SimpleSwipeLayout.this, xvel, yvel);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            switch (mMode) {
                case PullOut:
                    onPullOutViewPositionChanged(left, dx);
                    break;
                case PullStack:
                    onPullStackViewPositionChanged(left, dx);
                    break;
            }

            notifySwipeListener(left, top, dx, dy);
        }
    };

    private void onPullOutViewPositionChanged(int left, int dx) {
        for (View view : mMenuItemViewList) {
            Rect rect = mViewInitLocationMap.get(view);
            view.layout(rect.left + dx, rect.top, rect.right + dx, rect.bottom);
            calenderRect(view);
        }
        invalidate();
    }

    private void onPullStackViewPositionChanged(int left, int dx) {
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
            Rect rect = mViewInitLocationMap.get(view);
            view.layout(rect.left + offset, rect.top, rect.right + offset, rect.bottom);
        }
        invalidate();
    }

    private void notifySwipeListener(int left, int top, int dx, int dy) {
        if (mSwipeListener == null)
            return;
        if (dx > 0)
            mSwipeListener.onStartClose(this);
        if (dx < 0)
            mSwipeListener.onStartOpen(this);
        mSwipeListener.onUpdate(this, dx, dy);
        if (getCurrentStatus() == Status.Close)
            mSwipeListener.onClose(this);
        if (getCurrentStatus() == Status.Open)
            mSwipeListener.onOpen(this);
    }

    private void processHandRelease(View releasedChild, float xvel) {
        float minSpeed = mDragHelper.getMinVelocity();
        float currentX = releasedChild.getX();
        float distanceX = Math.abs(getPaddingLeft() - currentX);
        if (distanceX >= mDragDistance / 2 || xvel < -minSpeed) {
            smoothOpen();
        } else if (distanceX < mDragDistance / 2 || xvel > minSpeed) {
            smoothClose();
        }
    }

    public void smoothClose() {
        mDragHelper.smoothSlideViewTo(mContentView, getPaddingLeft(), getPaddingTop());
        invalidate();
    }

    public void smoothOpen() {
        mDragHelper.smoothSlideViewTo(mContentView, getPaddingLeft() - mDragDistance, getPaddingTop());
        invalidate();
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
        mViewInitLocationMap = new HashMap<>();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        calculateDragDistance();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutContentView();

        mViewInitLocationMap.clear();
        switch (mMode) {
            case LayDown:
                layoutLayDown();
                break;
            case PullStack:
                layoutPullStack();
                break;
            case PullOut:
                layoutPullOut();
                break;
        }
    }

    private void layoutContentView() {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        mContentView.layout(
                left,
                top,
                left + mContentView.getMeasuredWidth(),
                top + mContentView.getMeasuredHeight()
        );
        mContentView.bringToFront();
    }

    private void layoutPullOut() {
        View lastView = mContentView;
        for (View view : mMenuItemViewList) {
            view.layout(
                    lastView.getRight(),
                    lastView.getTop(),
                    lastView.getRight() + view.getMeasuredWidth(),
                    lastView.getTop() + view.getMeasuredHeight()
            );
            calenderRect(view);
            lastView = view;
        }
    }

    private void layoutLayDown() {
        View lastView = mContentView;
        for (int i = mMenuItemViewList.size() - 1; i >= 0; i--) {
            View view = mMenuItemViewList.get(i);
            if (lastView == mContentView)
                view.layout(
                        lastView.getRight() - view.getMeasuredWidth(),
                        lastView.getTop(),
                        lastView.getRight(),
                        lastView.getTop() + view.getMeasuredHeight()
                );
            else
                view.layout(
                        lastView.getLeft() - view.getMeasuredWidth(),
                        lastView.getTop(),
                        lastView.getLeft(),
                        lastView.getTop() + view.getMeasuredHeight()
                );
            calenderRect(view);
            lastView = view;
        }
    }

    private void calenderRect(View view) {
        Rect rect = new Rect();
        view.getHitRect(rect);
        mViewInitLocationMap.put(view, rect);
    }

    private void layoutPullStack() {
        for (View view : mMenuItemViewList) {
            view.layout(
                    mContentView.getRight(),
                    mContentView.getTop(),
                    mContentView.getRight() + view.getMeasuredWidth(),
                    mContentView.getTop() + view.getMeasuredHeight()
            );
            calenderRect(view);
        }
    }

    private void calculateDragDistance() {
        mDragDistance = 0;
        for (View view : mMenuItemViewList)
            mDragDistance += view.getMeasuredWidth();
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
        mContentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMenuClickListener != null)
                    mOnMenuClickListener.onMenuClick(v);
            }
        });
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
