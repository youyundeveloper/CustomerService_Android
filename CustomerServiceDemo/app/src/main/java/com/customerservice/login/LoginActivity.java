package com.customerservice.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.customerservice.R;
import com.customerservice.receiver.ReceiveMsgRunnable;
import com.customerservice.recentlist.RecentContactActivity;
import com.customerservice.utils.AppUtils;
import com.customerservice.utils.Log;
import com.ioyouyun.wchat.ServerType;
import com.ioyouyun.wchat.WeimiInstance;
import com.ioyouyun.wchat.data.AuthResultData;
import com.ioyouyun.wchat.message.HistoryMessage;
import com.ioyouyun.wchat.message.WChatException;
import com.ioyouyun.wchat.util.DebugConfig;
import com.ioyouyun.wchat.util.HttpCallback;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 */

public class LoginActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextInputEditText nickNameEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initData();
    }

    private void initData() {
        AppUtils.isOnlinePlatform = false;
        AppUtils.CUSTOM_SERVICE_ID = AppUtils.CUSTOM_SERVICE_FIXED_ID_TEST;

        String nickName = LoginSharedUtil.INSTANCE.getNickName(AppUtils.isOnlinePlatform);
        if (!TextUtils.isEmpty(nickName)) {
            nickNameEdit.setText(nickName);
        }
    }

    private void initView() {
        setToolBar();
        progressBar = (ProgressBar) findViewById(R.id.pb_login);
        nickNameEdit = (TextInputEditText) findViewById(R.id.tie_nickname);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rg_platform);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_online) {
                    AppUtils.isOnlinePlatform = true;
                    AppUtils.CUSTOM_SERVICE_ID = AppUtils.CUSTOM_SERVICE_FIXED_ID;
                } else if (checkedId == R.id.rb_test) {
                    AppUtils.isOnlinePlatform = false;
                    AppUtils.CUSTOM_SERVICE_ID = AppUtils.CUSTOM_SERVICE_FIXED_ID_TEST;
                }
                nickNameEdit.setText(LoginSharedUtil.INSTANCE.getNickName(AppUtils.isOnlinePlatform));
            }
        });
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
                    System.exit(0);
                }
            });
        }
    }

    public void handleLogin(View v) {
        login();
    }

    private void setNickName(String uid){
        String nickName = nickNameEdit.getText().toString();
        if (TextUtils.isEmpty(nickName)) {
            AppUtils.nickName = uid;
            LoginSharedUtil.INSTANCE.setNickName("", AppUtils.isOnlinePlatform);
        } else{
            AppUtils.nickName = nickName;
            LoginSharedUtil.INSTANCE.setNickName(nickName, AppUtils.isOnlinePlatform);
        }
    }

    /**
     * 登录成功后开始接收消息
     */
    private void startReveive() {
        ReceiveMsgRunnable runnable = new ReceiveMsgRunnable(AppUtils.mAppContext);
        Thread msgThread = new Thread(runnable);
        msgThread.start();
    }

    private void login() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String clientId;
                String sedret;
                try {
                    if (AppUtils.isOnlinePlatform) {
                        clientId = AppUtils.CLIENT_ID;
                        sedret = AppUtils.SECRET;
                        WeimiInstance.getInstance().initSDK(LoginActivity.this, "1.2", ServerType.Online, "cn", "weibo", clientId);
                    } else {
                        clientId = AppUtils.CLIENT_ID_TEST;
                        sedret = AppUtils.SECRET_TEST;
                        WeimiInstance.getInstance().initSDK(LoginActivity.this, "1.2", ServerType.Test, "cn", "weibo", clientId);
                    }
                    WeimiInstance.getInstance().registerUid(AppUtils.generateOpenUDID(LoginActivity.this),
                            clientId, sedret,
                            new HttpCallback() {
                                @Override
                                public void onResponse(String s) {
                                    try {
                                        JSONObject responseObject = new JSONObject(s);
                                        if (responseObject.has("result")) {
                                            JSONObject resultObject = responseObject.getJSONObject("result");
                                            Log.logD("获取token成功：" + resultObject.getString("uid"));
                                            if (resultObject.has("refresh_token") && resultObject.has("access_token")) {
                                                AuthResultData resultData = WeimiInstance.getInstance().oauthUser(resultObject.getString("access_token"), resultObject.getString("refresh_token"), false, 30);
                                                closeProgress();
                                                if (resultData.success) {
                                                    startReveive();

                                                    // 设置不sycn客服消息
                                                    WeimiInstance.getInstance().shieldSyncUserId(AppUtils.CUSTOM_SERVICE_ID);
                                                    // 获取未读消息数
                                                    WeimiInstance.getInstance().getUnread();

                                                    DebugConfig.DEBUG = true;
                                                    AppUtils.uid = WeimiInstance.getInstance().getUID();
                                                    setNickName(AppUtils.uid);
                                                    Log.logD("登录成功：" + AppUtils.uid);
                                                    gotoActivity(RecentContactActivity.class);
                                                } else {
                                                    Log.logD("登录失败");
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onResponseHistory(List<HistoryMessage> list) {

                                }

                                @Override
                                public void onError(Exception e) {

                                }
                            });
                } catch (WChatException e) {
                    e.printStackTrace();
                    Log.logD("登录失败");
                    closeProgress();
                }
            }
        }).start();
    }

    private void showUid(final String uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AppUtils.toastMessage(uid);
            }
        });
    }

    private void closeProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void gotoActivity(Class cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

}
