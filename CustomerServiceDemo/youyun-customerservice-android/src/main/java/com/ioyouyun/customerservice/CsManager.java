package com.ioyouyun.customerservice;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ioyouyun.customerservice.chat.CsChatActivity;
import com.ioyouyun.customerservice.receiver.CsBroadCastCenter;
import com.ioyouyun.customerservice.receiver.CsReceiveMsgRunnable;
import com.ioyouyun.customerservice.utils.CsAppUtils;
import com.ioyouyun.customerservice.utils.CsLog;
import com.ioyouyun.wchat.ServerType;
import com.ioyouyun.wchat.WeimiInstance;
import com.ioyouyun.wchat.data.AuthResultData;
import com.ioyouyun.wchat.message.HistoryMessage;
import com.ioyouyun.wchat.message.WChatException;
import com.ioyouyun.wchat.util.DebugConfig;
import com.ioyouyun.wchat.util.HttpCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 客服sdk customer service manager
 * Created by luis on 2017/3/9.
 */

public class CsManager {

    private static volatile CsManager instance;

    private Context context;
    private List<UnreadNumListener> unreadNumListeners = new ArrayList<>();
    private UnreadNumReceiver unreadNumReceiver;

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    private CsManager() {
    }

    public static CsManager getInstance() {
        if (instance == null) {
            synchronized (CsManager.class) {
                if (instance == null) {
                    instance = new CsManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化CsMagager
     *
     * @param context          上下文
     * @param clientId
     * @param secret
     * @param isOnlinePlatform 是否线上平台
     * @return
     */
    public CsManager init(Context context, String clientId, String secret, boolean isOnlinePlatform) throws WChatException {
        this.context = context.getApplicationContext();

        CsAppUtils.init(this.context);
        CsBroadCastCenter.getInstance().init(this.context);

        CsAppUtils.CLIENT_ID = clientId;
        CsAppUtils.SECRET = secret;

        CsAppUtils.isOnlinePlatform = isOnlinePlatform;

        if (isOnlinePlatform) {
            WeimiInstance.getInstance().initSDK(this.context, "1.2", ServerType.Online, "cn", "weibo", clientId);
        } else {
            WeimiInstance.getInstance().initSDK(this.context, "1.2", ServerType.Test, "cn", "weibo", clientId);
        }

        return this;
    }

    /**
     * 客服消息未读数的监听
     *
     * @return
     */
    public CsManager addUnreadNumListener(UnreadNumListener unreadNumListener) {
        this.unreadNumListeners.add(unreadNumListener);
        return this;
    }

    public CsManager removeUnreadNumListener(UnreadNumListener unreadNumListener) {
        this.unreadNumListeners.remove(unreadNumListener);
        return this;
    }

    public CsManager clearUnreadNumListeners() {
        this.unreadNumListeners.clear();
        return this;
    }

    /**
     * 设置客服号uid
     *
     * @param customerServiceId
     * @return
     */
    public CsManager setCustomerServiceId(String customerServiceId) {
        CsAppUtils.CUSTOM_SERVICE_ID = customerServiceId;
        return this;
    }

    /**
     * 设置用户Uid
     * @param uid
     * @return
     */
    public CsManager setUid(String uid) {
        CsAppUtils.uid = uid;
        return this;
    }

    /**
     * 设置用户昵称
     * @param nickName
     * @return
     */
    public CsManager setNickName(String nickName) {
        CsAppUtils.nickName = nickName;
        return this;
    }

    /**
     * 设置用户头像
     * @param avatar
     * @return
     */
    public CsManager setAvatar(String avatar) {
        CsAppUtils.headUrl = avatar;
        return this;
    }

    /**
     * 用户登录
     */
    public void login(final LoginCallback loginCallback) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    WeimiInstance.getInstance().registerUid(CsAppUtils.generateOpenUDID(context),
                            CsAppUtils.CLIENT_ID, CsAppUtils.SECRET,
                            new HttpCallback() {
                                @Override
                                public void onResponse(String s) {
                                    try {
                                        JSONObject responseObject = new JSONObject(s);
                                        if (responseObject.has("result")) {
                                            JSONObject resultObject = responseObject.getJSONObject("result");
                                            CsLog.logD("获取token成功：" + resultObject.getString("uid"));
                                            if (resultObject.has("refresh_token") && resultObject.has("access_token")) {
                                                AuthResultData resultData = WeimiInstance.getInstance().oauthUser(resultObject.getString("access_token"), resultObject.getString("refresh_token"), false, 30);
                                                if (resultData.success) {
                                                    startReceive();
                                                    registerUnreadNumReceiver();

                                                    // 设置不sycn客服消息
                                                    WeimiInstance.getInstance().shieldSyncUserId(CsAppUtils.CUSTOM_SERVICE_ID);
                                                    // 获取未读消息数
                                                    WeimiInstance.getInstance().getUnread();

                                                    DebugConfig.DEBUG = true;
                                                    CsAppUtils.uid = WeimiInstance.getInstance().getUID();
                                                    CsLog.logD("登录成功：" + CsAppUtils.uid);
                                                    if (loginCallback != null) {
                                                        loginCallback.success(responseObject.optString("result", ""));
                                                    }
                                                } else {
                                                    CsLog.logD("登录失败");
                                                    if (loginCallback != null) {
                                                        loginCallback.failure(new WChatException("登录失败", WChatException.AuthFailed));
                                                    }
                                                }
                                            } else {
                                                if (loginCallback != null) {
                                                    loginCallback.failure(new WChatException("获取token失败", WChatException.AuthFailed));
                                                }
                                            }
                                        } else {
                                            if (loginCallback != null) {
                                                loginCallback.failure(new WChatException("获取token失败", WChatException.AuthFailed));
                                            }
                                        }
                                    } catch (Exception e) {
                                        if (loginCallback != null) {
                                            loginCallback.failure(new WChatException(e));
                                        }
                                    }
                                }

                                @Override
                                public void onResponseHistory(List<HistoryMessage> list) {

                                }

                                @Override
                                public void onError(Exception e) {
                                    if (loginCallback != null) {
                                        loginCallback.failure(new WChatException(e));
                                    }
                                }
                            });
                } catch (WChatException e) {
                    if (loginCallback != null) {
                        loginCallback.failure(new WChatException(e));
                    }
                }
            }
        });
    }

    /**
     * 用户退出
     */
    public void logout(final LogoutCallback callback) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                boolean result = WeimiInstance.getInstance().logout();
                if (callback != null) {
                    if (result) {
                        callback.success();
                    } else {
                        callback.failure();
                    }
                }
                unregisterUnreadNumReceiver();
            }
        });
    }

    /**
     * 跳转到客服聊天界面
     *
     * @param context
     * @param fromData
     */
    public void gotoCsChatActivity(Activity context, String fromData) {
        CsChatActivity.startActivity(context, fromData);
    }

//    public void gotoCsChatActivity(Activity context, String fromData, ) {
//        CsChatActivity.startActivity(context, fromData);
//    }

    public interface UnreadNumListener {
        void onUnreadNum(int unreadNum);
    }

    public interface LoginCallback {
        void success(String result);

        void failure(Exception e);
    }

    public interface LogoutCallback {
        void success();

        void failure();
    }

    private void startReceive() {
        CsReceiveMsgRunnable runnable = new CsReceiveMsgRunnable(this.context);
        Thread msgThread = new Thread(runnable);
        msgThread.start();
    }

    private void registerUnreadNumReceiver() {
        unreadNumReceiver = new UnreadNumReceiver();
        CsBroadCastCenter.getInstance().registerReceiver(unreadNumReceiver, CsAppUtils.MSG_TYPE_RECV_UNREAD_NUM);
    }

    private void unregisterUnreadNumReceiver() {
        if (unreadNumReceiver != null)
            CsBroadCastCenter.getInstance().unregisterReceiver(unreadNumReceiver);
    }

    class UnreadNumReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CsAppUtils.MSG_TYPE_RECV_UNREAD_NUM.equals(action)) {
                int num = intent.getIntExtra(CsAppUtils.TYPE_MSG, 0);
                if (unreadNumListeners != null) {
                    for (UnreadNumListener unreadNumListener : unreadNumListeners) {
                        unreadNumListener.onUnreadNum(num);
                    }
                }
            }
        }
    }

}
