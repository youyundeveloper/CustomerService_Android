package com.customerservice;

import android.app.Application;

import com.customerservice.receiver.BroadCastCenter;
import com.customerservice.utils.AppUtils;

/**
 * Created by Bill on 2016/12/8.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.init(getApplicationContext());
        BroadCastCenter.getInstance().init(getApplicationContext());
    }
}
