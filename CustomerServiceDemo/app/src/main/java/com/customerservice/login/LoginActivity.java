package com.customerservice.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import com.customerservice.R;
import com.customerservice.recentlist.RecentContactActivity;
import com.ioyouyun.customerservice.CsManager;
import com.ioyouyun.customerservice.utils.CsAppUtils;
import com.ioyouyun.customerservice.utils.CsLog;
import com.ioyouyun.wchat.message.WChatException;

/**
 * Created by Bill on 2016/12/8.
 */

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_SD = 1001;

    private boolean isOnlinePlatform;

    private static String CLIENT_ID = "1-20525-4ab3a7c3ddb665945d0074f51e979ef0-andriod";
    private static String SECRET = "6f3efde9fb49a76ff6bfb257f74f4d5b";
    private static String CLIENT_ID_TEST = "1-20142-2e563db99a8ca41df48973b0c43ea50a-andriod";
    private static String SECRET_TEST = "ace518dab1fde58eacb126df6521d34c";

    private static final String CUSTOM_SERVICE_FIXED_ID = "584612"; // 正式客服id  // 584612
    private static final String CUSTOM_SERVICE_FIXED_ID_TEST = "743849"; // 测试客服id  // 549341

    private ProgressBar progressBar;
    private TextInputEditText nickNameEdit;
    private String nickName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        initData();

        // 申请SD卡权限
        CsAppUtils.requestPermission(this, REQUEST_CODE_WRITE_SD, callback, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_SD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestWriteSdSuccess();
            } else {
                CsAppUtils.toastMessage("Permission Denied");
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    CsAppUtils.PermissionCallback callback = new CsAppUtils.PermissionCallback() {
        @Override
        public void onComplete(int requestCode) {
            if (REQUEST_CODE_WRITE_SD == requestCode) {
                requestWriteSdSuccess();
            }
        }
    };

    private void requestWriteSdSuccess() {
        CsLog.logD("声请权限成功");
    }

    private void initData() {
        String nickName = CsSharedUtil.getInstance(LoginActivity.this).getNickName(CsAppUtils.isOnlinePlatform);
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
                    isOnlinePlatform = true;
                } else if (checkedId == R.id.rb_test) {
                    isOnlinePlatform = false;
                }
                nickNameEdit.setText(CsSharedUtil.getInstance(LoginActivity.this).getNickName(CsAppUtils.isOnlinePlatform));
            }
        });
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
                    System.exit(0);
                }
            });
        }
    }

    public void handleLogin(View v) {
        String clientId, secret, customerServiceId;
        if (isOnlinePlatform) {
            clientId = CLIENT_ID;
            secret = SECRET;
            customerServiceId = CUSTOM_SERVICE_FIXED_ID;
        } else {
            clientId = CLIENT_ID_TEST;
            secret = SECRET_TEST;
            customerServiceId = CUSTOM_SERVICE_FIXED_ID_TEST;
        }
        setNickName();
        try {
            CsManager csManager = CsManager.getInstance().init(this, clientId, secret, isOnlinePlatform).setCustomerServiceId(customerServiceId);
            csManager.setUid("10086").setNickName(nickName).setAvatar("http://www.qqzhi.com/uploadpic/2014-10-07/133603351.jpg");
            csManager.login(new CsManager.LoginCallback() {
                @Override
                public void success(String result) {
                    closeProgress();
                    gotoActivity(RecentContactActivity.class);
                }

                @Override
                public void failure(Exception e) {
                    closeProgress();
                    CsLog.logD("登录失败");
                }
            });
        } catch (WChatException e) {
            e.printStackTrace();
        }
    }

    private void setNickName() {
        String nickName = nickNameEdit.getText().toString();
        if (TextUtils.isEmpty(nickName.trim())) {
            this.nickName = "";
            CsSharedUtil.getInstance(LoginActivity.this).setNickName("", CsAppUtils.isOnlinePlatform);
        } else {
            this.nickName = nickName;
            CsSharedUtil.getInstance(LoginActivity.this).setNickName(nickName, CsAppUtils.isOnlinePlatform);
        }
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
