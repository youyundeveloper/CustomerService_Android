package com.customerservice.login;

import android.content.Context;
import android.content.SharedPreferences;

import com.customerservice.utils.AppUtils;

/**
 * Created by Bill on 2016/12/30.
 *
 * 单例 存储用户昵称
 */

public enum LoginSharedUtil {

    INSTANCE;

    private SharedPreferences sharedPreferences;
    public static final String NAME = "kefu_login_preference";
    public static final String KEY_NICKNAME = "key_nickname";

    private SharedPreferences initShare() {
        if (sharedPreferences == null)
            sharedPreferences = AppUtils.mAppContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sharedPreferences;
    }

    public void setNickName(String nickName) {
        SharedPreferences.Editor editor = initShare().edit();
        editor.putString(KEY_NICKNAME, nickName);
        editor.commit();
    }

    public String getNickName() {
        return initShare().getString(KEY_NICKNAME, "");
    }
}
