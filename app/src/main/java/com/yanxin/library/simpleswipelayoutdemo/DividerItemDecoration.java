package com.yanxin.library.simpleswipelayoutdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
    public static final int DEFAULT_VALUE = -1;
    private static final int DEFAULT_DIVIDER_HEIGHT = 1;
    private int mOrientation;
    private int padding;
    private int startpadding;
    private int endpadding;
    private int dividerHeight;
    private int dividerColor;
    private Context mContext;
    private Paint mPaddingPaint;
    private Paint mDividerPaint;

    public DividerItemDecoration(Context context) {
        this(context, VERTICAL_LIST);
    }

    public DividerItemDecoration(Context context, int orientation) {
        this(context, orientation, -1);
    }

    public DividerItemDecoration(Context context, int orientation, int padding) {
        this(context, orientation, padding, -1);
    }

    public DividerItemDecoration(Context context, int orientation, int padding, int dividerHeight) {
        setOrientation(orientation);
        mContext = context;

        init();
        if (padding != -1) this.padding = padding;
        updatePaddint();
        if (dividerHeight != -1) this.dividerHeight = dividerHeight;
    }

    public DividerItemDecoration(Context context, int orientation, int startpadding, int endpadding, int dividerHeight) {
        setOrientation(orientation);
        mContext = context;

        init();
        if (startpadding != -1) this.startpadding = startpadding;
        if (endpadding != -1) this.endpadding = endpadding;
        if (dividerHeight != -1) this.dividerHeight = dividerHeight;
    }

    public void setDividerColor(int color) {
        dividerColor = color;
        mDividerPaint.setColor(dividerColor);
    }

    private void updatePaddint() {
        startpadding = padding;
        endpadding = padding;
    }

    private void init() {
        padding = mContext.getResources().getDimensionPixelSize(R.dimen.list_horizontal_margin);
        updatePaddint();
        dividerHeight = DEFAULT_DIVIDER_HEIGHT;
        dividerColor = mContext.getResources().getColor(R.color.divider_color);

        mPaddingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaddingPaint.setColor(mContext.getResources().getColor(android.R.color.white));
        mPaddingPaint.setStyle(Paint.Style.FILL);

        mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDividerPaint.setColor(dividerColor);
        mDividerPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            if (shouldShowDivider(child, parent)) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin +
                        Math.round(ViewCompat.getTranslationY(child));
                final int bottom = top + dividerHeight;
                if (!isHideBottomDivider(getPosition()) || i != getPosition()) {
                    c.drawRect(left, top, left + startpadding, bottom, mPaddingPaint);
                    c.drawRect(right - endpadding, top, right, bottom, mPaddingPaint);
                    c.drawRect(left + startpadding, top, right - endpadding, bottom, mDividerPaint);
                }
                if (i == 0 && isShowFirstTopDivider()) {
                    int firstTop = child.getTop() - params.topMargin;
                    int firstBottom = firstTop + dividerHeight;
                    c.drawRect(left, firstTop, left + startpadding, firstBottom, mPaddingPaint);
                    c.drawRect(right - endpadding, firstTop, right, firstBottom, mPaddingPaint);
                    c.drawRect(left + startpadding, firstTop, right - endpadding, firstBottom, mDividerPaint);
                }
            }
        }
    }

    public boolean isHideBottomDivider(int position) {
        return false;
    }

    public int getPosition() {
        return 0;
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            if (shouldShowDivider(child, parent)) {
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getRight() + params.rightMargin +
                        Math.round(ViewCompat.getTranslationX(child));
                final int right = left + dividerHeight;

                c.drawRect(left, top, right, top + startpadding, mPaddingPaint);
                c.drawRect(left, bottom - endpadding, right, bottom, mPaddingPaint);
                c.drawRect(left, top + startpadding, right, bottom - endpadding, mDividerPaint);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mOrientation == VERTICAL_LIST) {
            if (shouldShowDivider(view, parent)) {
                outRect.set(0, 0, 0, dividerHeight);
            } else {
                outRect.set(0, 0, 0, 0);
            }
        } else {
            if (shouldShowDivider(view, parent)) {
                outRect.set(0, 0, dividerHeight, 0);
            } else {
                outRect.set(0, 0, 0, 0);
            }
        }
    }

    protected boolean shouldShowDivider(View view, RecyclerView parent) {
        return parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1;
    }

    protected int getDividerHeight() {
        return dividerHeight;
    }

    protected int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    protected boolean isShowFirstTopDivider() {
        return false;
    }
}
