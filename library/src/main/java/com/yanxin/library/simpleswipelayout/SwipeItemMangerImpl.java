package com.yanxin.library.simpleswipelayout;

import android.view.View;

import com.yanxin.library.simpleswipelayout.interfaces.SwipeAdapter;
import com.yanxin.library.simpleswipelayout.interfaces.SwipeItemManager;
import com.yanxin.library.simpleswipelayout.interfaces.SwipeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * SwipeItemMangerImpl is a helper class to help all the adapters to maintain open status.
 */
public class SwipeItemMangerImpl implements SwipeItemManager {

    private static final int INVALID_POSITION = -1;

    private int mOpenPosition = INVALID_POSITION;
    private Mode mMode = Mode.Single;
    private Set<Integer> mOpenPositions = new HashSet<>();
    private Set<SimpleSwipeLayout> mShownLayouts = new HashSet<>();

    private SwipeAdapter mSwipeAdapter;

    public SwipeItemMangerImpl(SwipeAdapter swipeAdapter) {
        if (swipeAdapter == null)
            throw new IllegalArgumentException("SwipeAdapter can not be null");
        mSwipeAdapter = swipeAdapter;
    }

    public Mode getMode() {
        return mMode;
    }

    public void setMode(Mode mode) {
        mMode = mode;
        mOpenPositions.clear();
        mShownLayouts.clear();
        mOpenPosition = INVALID_POSITION;
    }

    @Override
    public void bind(View view, int position) {
        int resId = mSwipeAdapter.getSwipeLayoutResourceId(position);
        SimpleSwipeLayout swipeLayout = (SimpleSwipeLayout) view.findViewById(resId);
        if (swipeLayout == null)
            throw new IllegalStateException("can not find SimpleSwipeLayout in target view");
        if (swipeLayout.getTag(resId) == null) {
            OnLayoutListener onLayoutListener = new OnLayoutListener(position);
            SwipeMemory swipeMemory = new SwipeMemory(position);
            swipeLayout.addSwipeListener(swipeMemory);
            swipeLayout.addOnLayoutChangeListener(onLayoutListener);
            swipeLayout.setTag(resId, new ValueBox(position, swipeMemory, onLayoutListener));
            mShownLayouts.add(swipeLayout);
        } else {
            ValueBox valueBox = (ValueBox) swipeLayout.getTag(resId);
            valueBox.swipeMemory.setPosition(position);
            valueBox.onLayoutListener.setPosition(position);
            valueBox.position = position;
        }
    }

    @Override
    public void openItem(int position) {
        if (mMode == Mode.Multiple) {
            if (!mOpenPositions.contains(position))
                mOpenPositions.add(position);
        } else {
            mOpenPosition = position;
        }
        mSwipeAdapter.notifyDatasetChanged();
    }

    @Override
    public void closeItem(int position) {
        if (mMode == Mode.Multiple) {
            mOpenPositions.remove(position);
        } else {
            if (mOpenPosition == position)
                mOpenPosition = INVALID_POSITION;
        }
        mSwipeAdapter.notifyDatasetChanged();
    }

    @Override
    public void closeAllExcept(SimpleSwipeLayout layout) {
        for (SimpleSwipeLayout s : mShownLayouts) {
            if (s != layout)
                s.smoothClose();
        }
    }

    @Override
    public void closeAllItems() {
        if (mMode == Mode.Multiple) {
            mOpenPositions.clear();
        } else {
            mOpenPosition = INVALID_POSITION;
        }
        for (SimpleSwipeLayout s : mShownLayouts) {
            s.smoothClose();
        }
    }

    @Override
    public void removeShownLayouts(SimpleSwipeLayout layout) {
        mShownLayouts.remove(layout);
    }

    @Override
    public List<Integer> getOpenItems() {
        if (mMode == Mode.Multiple) {
            return new ArrayList<>(mOpenPositions);
        } else {
            return Collections.singletonList(mOpenPosition);
        }
    }

    @Override
    public List<SimpleSwipeLayout> getOpenLayouts() {
        return new ArrayList<>(mShownLayouts);
    }

    @Override
    public boolean isOpen(int position) {
        if (mMode == Mode.Multiple) {
            return mOpenPositions.contains(position);
        } else {
            return mOpenPosition == position;
        }
    }

    class ValueBox {
        OnLayoutListener onLayoutListener;
        SwipeMemory swipeMemory;
        int position;

        ValueBox(int position, SwipeMemory swipeMemory, OnLayoutListener onLayoutListener) {
            this.swipeMemory = swipeMemory;
            this.onLayoutListener = onLayoutListener;
            this.position = position;
        }
    }

    class OnLayoutListener implements View.OnLayoutChangeListener {

        private int position;

        OnLayoutListener(int position) {
            this.position = position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (isOpen(position)) {
                ((SimpleSwipeLayout) v).smoothOpen();
            } else {
                ((SimpleSwipeLayout) v).smoothClose();
            }
        }
    }

    class SwipeMemory implements SwipeListener {

        private int position;

        SwipeMemory(int position) {
            this.position = position;
        }

        @Override
        public void onClose(SimpleSwipeLayout layout) {
            if (mMode == Mode.Multiple) {
                mOpenPositions.remove(position);
            } else {
                mOpenPosition = INVALID_POSITION;
            }
        }

        @Override
        public void onUpdate(SimpleSwipeLayout layout, int leftOffset, int topOffset) {

        }

        @Override
        public void onHandRelease(SimpleSwipeLayout layout, float xvel, float yvel) {

        }

        @Override
        public void onStartOpen(SimpleSwipeLayout layout) {
            if (mMode == Mode.Single) {
                closeAllExcept(layout);
            }
        }

        @Override
        public void onOpen(SimpleSwipeLayout layout) {
            if (mMode == Mode.Multiple)
                mOpenPositions.add(position);
            else {
                closeAllExcept(layout);
                mOpenPosition = position;
            }
        }

        @Override
        public void onStartClose(SimpleSwipeLayout layout) {

        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

}
