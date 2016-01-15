package com.yanxin.library.simpleswipelayout.interfaces;

import android.view.View;

import com.yanxin.library.simpleswipelayout.SimpleSwipeLayout;

import java.util.List;

public interface SwipeItemManager {

    void openItem(int position);

    void closeItem(int position);

    void closeAllExcept(SimpleSwipeLayout layout);

    void closeAllItems();

    List<Integer> getOpenItems();

    List<SimpleSwipeLayout> getOpenLayouts();

    void removeShownLayouts(SimpleSwipeLayout layout);

    boolean isOpen(int position);

    Mode getMode();

    void setMode(Mode mode);

    void bind(View view, int position);

    enum Mode {
        Single, Multiple
    }

}
