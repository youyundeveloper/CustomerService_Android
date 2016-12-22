package com.customerservice.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.customerservice.utils.AppUtils;
import com.customerservice.utils.Log;
import com.customerservice.R;
import com.customerservice.chat.ChatActivity;
import com.ioyouyun.wchat.WeimiInstance;
import com.ioyouyun.wchat.data.AuthResultData;
import com.ioyouyun.wchat.message.WChatException;
import com.ioyouyun.wchat.util.DebugConfig;

/**
 * Created by Bill on 2016/12/8.
 */

public class LoginActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setToolBar();
        progressBar = (ProgressBar) findViewById(R.id.pb_login);
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

    public void handleLogin(View v) {
        login();
    }

    private void login() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AuthResultData authResultData;
                    if (AppUtils.isOnlinePlatform) {
                        authResultData = WeimiInstance.getInstance().registerApp(
                                AppUtils.mAppContext,
                                AppUtils.generateOpenUDID(LoginActivity.this),
                                AppUtils.CLIENT_ID,
                                AppUtils.SECRET,
                                60);
                    } else {
                        authResultData = WeimiInstance.getInstance().testRegisterApp(
                                AppUtils.mAppContext,
                                AppUtils.generateOpenUDID(LoginActivity.this),
                                AppUtils.CLIENT_ID_TEST,
                                AppUtils.SECRET_TEST,
                                60);
                    }
                    closeProgress();
                    if (authResultData.success) {
                        DebugConfig.DEBUG = true;
                        AppUtils.uid = WeimiInstance.getInstance().getUID();
                        Log.logD("登录成功：" + AppUtils.uid);
                        gotoActivity(ChatActivity.class);
                    } else {
                        Log.logD("登录失败");
                    }
                } catch (WChatException e) {
                    e.printStackTrace();
                    Log.logD("登录失败");
                    closeProgress();
                }
            }
        }).start();
    }

    private void closeProgress(){
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
        finish();
    }

}
