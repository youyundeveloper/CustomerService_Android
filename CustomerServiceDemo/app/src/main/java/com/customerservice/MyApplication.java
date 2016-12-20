package com.customerservice;

import android.app.Application;

import com.customerservice.receiver.BroadCastCenter;

/**
 * Created by Bill on 2016/12/8.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.mAppContext = getApplicationContext();
        BroadCastCenter.getInstance().init(getApplicationContext());
    }
}
