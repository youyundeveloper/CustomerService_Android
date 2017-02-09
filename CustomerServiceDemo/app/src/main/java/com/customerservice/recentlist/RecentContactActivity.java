package com.customerservice.recentlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.customerservice.R;
import com.customerservice.chat.CsChatActivity;
import com.customerservice.receiver.CsBroadCastCenter;
import com.customerservice.utils.CsAppUtils;
import com.customerservice.utils.CsLog;
import com.ioyouyun.wchat.WeimiInstance;

public class RecentContactActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView kfIdText;
    private TextView unreadNumText;
    private View itemLayout;
    private ProgressBar progressBar;

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

    private void logout() {
        CsLog.logD("logout");

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean result = WeimiInstance.getInstance().logout();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (result) {
                            finish();
                        } else {
                            CsAppUtils.toastMessage("退出登录失败");
                        }
                    }
                });
            }
        }).start();

    }

    @Override
    public void onClick(View v) {
        if (v == itemLayout) {
            unreadNumText.setVisibility(View.GONE);

            String fromData = "queryId=4030507255933456&type=1";
            CsChatActivity.startActivity(this, fromData);
        }
    }

    private void initData() {
        kfIdText.setText(CsAppUtils.CUSTOM_SERVICE_ID);

        showUnRead(CsAppUtils.unReadNum);
    }

    private void addListener() {
        itemLayout.setOnClickListener(this);
    }

    private void initView() {
        setToolBar();
        kfIdText = (TextView) findViewById(R.id.tv_id);
        unreadNumText = (TextView) findViewById(R.id.tv_unread_num);
        itemLayout = findViewById(R.id.rl_item_layout);
        progressBar = (ProgressBar) findViewById(R.id.pb_logout);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        progressBar.setVisibility(View.VISIBLE);
        logout();
    }

    protected void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.cs_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    back();
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
        CsBroadCastCenter.getInstance().registerReceiver(receiver, CsAppUtils.MSG_TYPE_RECV_UNREAD_NUM);
    }

    /**
     * 注销广播
     */
    private void unregisterReceiver() {
        if (receiver != null)
            CsBroadCastCenter.getInstance().unregisterReceiver(receiver);
    }

    class MyInnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CsAppUtils.MSG_TYPE_RECV_UNREAD_NUM.equals(action)) {
                CsAppUtils.unReadNum = 0;
                int num = intent.getIntExtra(CsAppUtils.TYPE_MSG, 0);
                showUnRead(num);
            }
        }
    }

}
