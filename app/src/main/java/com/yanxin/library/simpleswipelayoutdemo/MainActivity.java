package com.yanxin.library.simpleswipelayoutdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yanxin.library.simpleswipelayout.SimpleSwipeLayout;

public class MainActivity extends AppCompatActivity implements SimpleSwipeLayout.onMenuClickListener {

    private SimpleSwipeLayout mSimpleSwipeLayout;

    private TextView mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContent = (TextView) findViewById(R.id.content);
        mSimpleSwipeLayout = (SimpleSwipeLayout) findViewById(R.id.swipe_layout);
        mSimpleSwipeLayout.setOnMenuClickListener(this);
        mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecyclerViewActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onMenuClick(View view) {
        switch (view.getId()) {
            case R.id.menu1:
                Toast.makeText(this, "menu1 is clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu2:
                Toast.makeText(this, "menu2 is clicked", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
