package com.customerservice.recentlist;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.customerservice.R;
import com.customerservice.login.CsSharedUtil;
import com.ioyouyun.customerservice.CsManager;
import com.ioyouyun.customerservice.utils.CsAppUtils;
import com.ioyouyun.customerservice.utils.CsLog;

import org.json.JSONException;
import org.json.JSONObject;

public class RecentContactActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView kfIdText;
    private TextView unreadNumText;
    private View itemLayout;
    private ProgressBar progressBar;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_contact);
        initView();
        addListener();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void logout() {
        CsLog.logD("logout");

        CsManager.getInstance().logout(new CsManager.LogoutCallback() {
            @Override
            public void success() {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            finish();
                        }
                    });
                }
            }

            @Override
            public void failure() {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            CsAppUtils.toastMessage("退出登录失败");
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v == itemLayout) {
            JSONObject jsonObject = new JSONObject();
            String fromData = "";

//            // 进入客服界面 库拍格式
//            try {
//                jsonObject.put("kp_source", "queryId=4030507255933456&type=1");
//                fromData = jsonObject.toString();
//            } catch (JSONException e) {
//            }

            // 进入客服界面 乐居格式
            try {
                jsonObject.put("building", "A小区");
                jsonObject.put("city_id", "010");
                jsonObject.put("city_name", "北京");
                jsonObject.put("level", 2);
                jsonObject.put("platform", 1);
                fromData = jsonObject.toString();
            } catch (JSONException e) {
            }
            CsManager.getInstance().gotoCsChatActivity(this, fromData);

            showUnRead(0);
            CsSharedUtil.getInstance(RecentContactActivity.this).setUnreadNum(0);
        }
    }

    private void initData() {
        kfIdText.setText(CsAppUtils.CUSTOM_SERVICE_ID);

        showUnRead(CsSharedUtil.getInstance(this).getUnreadNum());
    }

    private void addListener() {
        itemLayout.setOnClickListener(this);
        CsManager.getInstance().addUnreadNumListener(new CsManager.UnreadNumListener() {
            @Override
            public void onUnreadNum(final int unreadNum) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showUnRead(unreadNum);
                            CsSharedUtil.getInstance(RecentContactActivity.this).setUnreadNum(unreadNum);
                        }
                    });
                }
            }
        });
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
        if (unreadNumText == null)
            return;
        unreadNumText.setText(String.valueOf(num));
        if (num > 0) {
            unreadNumText.setVisibility(View.VISIBLE);
        } else {
            unreadNumText.setVisibility(View.GONE);
        }
    }

}
