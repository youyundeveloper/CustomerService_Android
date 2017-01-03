package com.customerservice.login;

import android.content.Context;
import android.content.SharedPreferences;

import com.customerservice.utils.AppUtils;

/**
 * Created by Bill on 2016/12/30.
 * <p>
 * 单例 存储用户昵称
 */

public enum LoginSharedUtil {

    INSTANCE;

    private SharedPreferences sharedPreferences;
    public static final String NAME = "kefu_login_preference";
    public static final String KEY_NICKNAME = "key_nickname";
    public static final String KEY_NICKNAME_TEST = "key_nickname_test";

    private SharedPreferences initShare() {
        if (sharedPreferences == null)
            sharedPreferences = AppUtils.mAppContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    public void setNickName(String nickName, boolean isOnline) {
        SharedPreferences.Editor editor = initShare().edit();
        if (isOnline)
            editor.putString(KEY_NICKNAME, nickName);
        else
            editor.putString(KEY_NICKNAME_TEST, nickName);
        editor.commit();
    }

    public String getNickName(boolean isOnline) {
        if (isOnline)
            return initShare().getString(KEY_NICKNAME, "");
        else
            return initShare().getString(KEY_NICKNAME_TEST, "");
    }
}
