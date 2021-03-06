package com.yanxin.library.simpleswipelayout.interfaces;

import com.yanxin.library.simpleswipelayout.SimpleSwipeLayout;

public interface SwipeListener {

    void onStartOpen(SimpleSwipeLayout layout);

    void onOpen(SimpleSwipeLayout layout);

    void onStartClose(SimpleSwipeLayout layout);

    void onClose(SimpleSwipeLayout layout);

    void onUpdate(SimpleSwipeLayout layout, int leftOffset, int topOffset);

    void onHandRelease(SimpleSwipeLayout layout, float xVel, float yVel);

}
