package com.customerservice;

import android.app.Application;

import com.customerservice.receiver.CsBroadCastCenter;
import com.customerservice.utils.CsAppUtils;

/**
 * Created by Bill on 2016/12/8.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initCS();
    }

    /**
     * 初始化客服
     */
    private void initCS(){
        CsAppUtils.init(getApplicationContext());
        CsBroadCastCenter.getInstance().init(getApplicationContext());
    }

}
