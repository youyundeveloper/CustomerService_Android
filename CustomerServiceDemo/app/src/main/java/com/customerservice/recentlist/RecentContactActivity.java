package com.customerservice.recentlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.customerservice.R;
import com.customerservice.chat.ChatActivity;
import com.customerservice.receiver.BroadCastCenter;
import com.customerservice.utils.AppUtils;

public class RecentContactActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView kfIdText;
    private TextView unreadNumText;
    private View itemLayout;

    private MyInnerReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_contact);
        initView();
        addListener();
        registerReceiver();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    @Override
    public void onClick(View v) {
        if (v == itemLayout) {
            unreadNumText.setVisibility(View.GONE);

            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        }
    }

    private void initData() {
        kfIdText.setText(AppUtils.CUSTOM_SERVICE_ID);

        showUnRead(AppUtils.unReadNum);
    }

    private void addListener() {
        itemLayout.setOnClickListener(this);
    }

    private void initView() {
        setToolBar();
        kfIdText = (TextView) findViewById(R.id.tv_id);
        unreadNumText = (TextView) findViewById(R.id.tv_unread_num);
        itemLayout = findViewById(R.id.rl_item_layout);
    }

    protected void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    private void showUnRead(int num) {
        if (num > 0) {
            unreadNumText.setVisibility(View.VISIBLE);
            unreadNumText.setText(String.valueOf(num));
        } else {
            unreadNumText.setText(String.valueOf(num));
            unreadNumText.setVisibility(View.GONE);
        }
    }

    /**
     * 注册本地广播
     */
    private void registerReceiver() {
        receiver = new MyInnerReceiver();
        BroadCastCenter.getInstance().registerReceiver(receiver, AppUtils.MSG_TYPE_RECV_UNREAD_NUM);
    }

    /**
     * 注销广播
     */
    private void unregisterReceiver() {
        if (receiver != null)
            BroadCastCenter.getInstance().unregisterReceiver(receiver);
    }

    class MyInnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppUtils.MSG_TYPE_RECV_UNREAD_NUM.equals(action)) {
                AppUtils.unReadNum = 0;
                int num = intent.getIntExtra(AppUtils.TYPE_MSG, 0);
                showUnRead(num);
            }
        }
    }

}
