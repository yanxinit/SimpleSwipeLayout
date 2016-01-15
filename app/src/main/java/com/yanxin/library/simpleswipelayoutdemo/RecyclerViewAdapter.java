package com.yanxin.library.simpleswipelayoutdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yanxin.library.simpleswipelayout.SimpleSwipeLayout;
import com.yanxin.library.simpleswipelayout.SwipeItemMangerImpl;
import com.yanxin.library.simpleswipelayout.interfaces.SwipeAdapter;
import com.yanxin.library.simpleswipelayout.interfaces.SwipeItemManager;

/**
 * Created by YanXin on 2015/12/30.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter implements SwipeAdapter {

    private SwipeItemManager mSwipeItemManager = new SwipeItemMangerImpl(this);

    private Context mContext;

    public static final String[] adapterData = new String[]{"Alabama", "Alaska", "Arizona", "Arkansas", "California",
            "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii", "Idaho", "Illinois",
            "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine", "Maryland", "Massachusetts",
            "Michigan", "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada",
            "New Hampshire", "New Jersey", "New Mexico", "New York", "North Carolina", "North Dakota",
            "Ohio", "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota",
            "Tennessee", "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia",
            "Wisconsin", "Wyoming"};

    public RecyclerViewAdapter(Context context) {
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder viewHolder = (MyViewHolder) holder;
        viewHolder.bind(position, adapterData[position]);
    }

    @Override
    public int getItemCount() {
        return adapterData.length;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_layout;
    }

    @Override
    public void notifyDatasetChanged() {
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mContent;
        SimpleSwipeLayout mSimpleSwipeLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            mContent = (TextView) itemView.findViewById(R.id.content);
            mSimpleSwipeLayout = (SimpleSwipeLayout) itemView.findViewById(R.id.swipe_layout);
        }

        public void bind(final int position, String s) {
            mSwipeItemManager.bind(itemView, position);
            mContent.setText(s);
            mSimpleSwipeLayout.setOnMenuClickListener(new SimpleSwipeLayout.onMenuClickListener() {
                @Override
                public void onMenuClick(View view) {
                    switch (view.getId()) {
                        case R.id.menu1:
                            Toast.makeText(mContext, "点击收藏", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.menu2:
                            Toast.makeText(mContext, "点击删除", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.content:
                            Toast.makeText(mContext, "点击item", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }

    }

}
