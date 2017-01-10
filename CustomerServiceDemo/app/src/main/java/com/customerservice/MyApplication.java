package com.customerservice;

import android.app.Application;

import com.customerservice.receiver.CsBroadCastCenter;
import com.customerservice.utils.CsAppUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Bill on 2016/12/8.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initCS();


        //创建默认的ImageLoader配置参数
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
                .createDefault(this);

        //Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(configuration);
    }

    /**
     * 初始化客服
     */
    private void initCS(){
        CsAppUtils.init(getApplicationContext());
        CsBroadCastCenter.getInstance().init(getApplicationContext());
    }

}
