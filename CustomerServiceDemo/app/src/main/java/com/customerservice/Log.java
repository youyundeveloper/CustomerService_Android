package com.customerservice;

/**
 * Created by Bill on 2016/12/8.
 */

public class Log {

    private static final String TAG = "my_tag";
    private static boolean isShowLog = true;

    public static void logD(String msg){
        if(isShowLog)
            android.util.Log.v(TAG, msg);
    }

}
