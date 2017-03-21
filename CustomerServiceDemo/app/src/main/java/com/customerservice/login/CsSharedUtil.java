package com.customerservice.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by Bill on 2016/12/30.
 * <p>
 * 单例 sharedPreferences 封装
 */

public class CsSharedUtil {

    private static SharedPreferences sharedPreferences;
    public static final String NAME = "customerservice_preference";
    public static final String KEY_NICKNAME = "key_nickname";
    public static final String KEY_NICKNAME_TEST = "key_nickname_test";
    public static final String KEY_UNREAD_NUM = "key_unread_num";

    private static volatile CsSharedUtil instance;

    private CsSharedUtil(Context context) {
        sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static CsSharedUtil getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (CsSharedUtil.class) {
                if (instance == null) {
                    instance = new CsSharedUtil(context);
                }
            }
        }
        return instance;
    }

    public void setNickName(String nickName, boolean isOnline) {
        if (sharedPreferences == null)
            return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isOnline)
            editor.putString(KEY_NICKNAME, nickName);
        else
            editor.putString(KEY_NICKNAME_TEST, nickName);
        editor.commit();
    }

    public String getNickName(boolean isOnline) {
        if (sharedPreferences == null)
            return "";
        if (isOnline)
            return sharedPreferences.getString(KEY_NICKNAME, "");
        else
            return sharedPreferences.getString(KEY_NICKNAME_TEST, "");
    }

    public void setUnreadNum(int unreadNum) {
        if (sharedPreferences == null)
            return;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_UNREAD_NUM, unreadNum);
        editor.commit();
    }

    public int getUnreadNum() {
        if (sharedPreferences == null)
            return 0;
        return sharedPreferences.getInt(KEY_UNREAD_NUM, 0);
    }

}
