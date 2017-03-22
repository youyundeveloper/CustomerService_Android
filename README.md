# 游云客服Android SDK集成

[toc]
## 前期准备
在您阅读此文档之前，我们假定您已具备基础的 Android 应用开发经验，并能够理解相关基础概念。

### 1、注册开发者帐号
开发者在集成游云客服功能前，需前往 [游云官方网站](http://www.17youyun.com) 注册创建游云开发者帐号。

### 2、下载 SDK
您可以到游云官方网站[下载游云Android SDK和Demo](http://wiki.17youyun.com/index.php?title=Android#SDK_.E4.B8.8B.E8.BD.BD) 。下载包中分为如下两部分：

* CustomerService Demo - 游云客服Demo和相关库
    * app - 客服demo
    * youyun-customerservice-android - 客服library

### 3、创建应用
您要进行应用开发之前，需要先在游云开发者平台创建应用。如果您已经注册了游云开发者帐号，请前往 [游云开发者平台](http://www.17youyun.com) 创建应用。

您创建完应用后，首先需要了解的是 App ClientID / Secret，它们是游云 SDK 连接服务器所必须的标识，每一个 App 对应一套 App ClientID / Secret。

## 集成开发
### 1、添加module
将CustomerServiceDemo里的youyun-customerservice-android作为module添加到您的项目中

### 2、添加权限
在您项目的AndroidManifest.xm中添加以下权限
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
<uses-permission android:name="android.permission.CAMERA" />
```

### 3、添加相应组件
同样在您项目的AndroidManifest.xm中添加以下组件
```xml
<!-- 游云客服相关 -->
<!-- 客服聊天界面Activity -->
<activity
    android:name="com.ioyouyun.customerservice.chat.CsChatActivity"
    android:screenOrientation="portrait"
    android:windowSoftInputMode="adjustResize|stateHidden" />

<!-- 查看大图界面Activity -->
<activity
    android:name="com.ioyouyun.customerservice.chat.CsBigImageActivity"
    android:screenOrientation="portrait" />

<!-- 网络变化监听Receiver -->
<receiver android:name="com.ioyouyun.wchat.util.NetworkReceiver">
    <intent-filter android:priority="2147483647">
        <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
    </intent-filter>
</receiver>
```

## 接口文档
客服功能的相关接口都封装在com.ioyouyun.customerservice.CsManager中，可通过CsManager.getInstance()获取单例调用，其中大部分方法可链式调用，不能链式调用的就是不推荐链式调用的～

### 1、初始化SDK
```
/**
 * @param context          上下文
 * @param clientId         游云平台App ClientID
 * @param secret           游云平台Secret
 * @param isOnlinePlatform 是否线上平台
 * @return
 */
init(Context context, String clientId, String secret, boolean isOnlinePlatform)
```
### 2、设置客服号uid
须在登录login之前调用
```
setCustomerServiceId(String customerServiceId)
```
### 3、设置用户uid
须在进入客服聊天界面gotoCsChatActivity之前调用
```
setUid(String uid)
```
### 4、设置用户昵称
须在进入客服聊天界面gotoCsChatActivity之前调用
```
setNickName(String nickName)
```
### 5、设置用户头像
须在进入客服聊天界面gotoCsChatActivity之前调用
```
setAvatar(String avatar)
```
### 6、客服消息未读数的监听
```
/**
 * 注册客服消息未读数的监听
 * @return
 */
addUnreadNumListener(UnreadNumListener unreadNumListener)

/**
 * 移除客服消息未读数的监听
 * @return
 */
removeUnreadNumListener(UnreadNumListener unreadNumListener)

/**
 * 清空客服消息未读数的监听
 * @return
 */
clearUnreadNumListeners()
```
### 7、用户登录
```
login(LoginCallback callback)
```
### 8、用户退出登录
```
logout(LogoutCallback callback)
```
### 9、跳转到客服聊天界面
```
/** 
 * @param context
 * @param fromData json格式字符串，具体内容跟服务端协定
 */
gotoCsChatActivity(Activity context, String fromData)
```
