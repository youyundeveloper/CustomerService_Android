package com.customerservice.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by Bill on 2016/12/30.
 * <p>
 * 单例 存储用户昵称
 */

public class LoginSharedUtil {

    private static SharedPreferences sharedPreferences;
    public static final String NAME = "kefu_login_preference";
    public static final String KEY_NICKNAME = "key_nickname";
    public static final String KEY_NICKNAME_TEST = "key_nickname_test";

    private static volatile LoginSharedUtil instance;

    private LoginSharedUtil(Context context) {
        sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static LoginSharedUtil getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (LoginSharedUtil.class) {
                if (instance == null) {
                    instance = new LoginSharedUtil(context);
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
}
