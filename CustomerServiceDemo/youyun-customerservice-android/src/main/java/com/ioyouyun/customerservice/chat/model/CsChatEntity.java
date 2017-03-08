package com.ioyouyun.customerservice.chat.model;

import com.ioyouyun.customerservice.chat.jsonmodel.CsJsonParentEntity;

import java.io.Serializable;

/**
 * Created by Bill on 2016/12/22.
 */

public class CsChatEntity implements Serializable {

    public static final int CHAT_TYPE_PEOPLE_SEND_TEXT = 0; // 发送普通文本
    public static final int CHAT_TYPE_ROBOT_TEXT = 1; // 接收机器人的text消息
    public static final int CHAT_TYPE_PEOPLE_SEND_IMAGE = 2; // 发送图片
    public static final int CHAT_TYPE_ROBOT_IMAGE = 3; // 接收图片
    public static final int CHAT_TYPE_NOTICE = 4; // 通知

    public int msgType; // 消息类型，决定在adapter中怎样展示
    public String msgId; // 消息id
    public long time; // 消息时间
    public boolean isShowTime; // 是否显示时间标签
    public CsJsonParentEntity csJsonParentEntity; // 客服消息
    public CsFileEntity csFileEntity; // 文件消息
    public String nickName; // 昵称
    public String headUrl; // 头像uil
    public int fileProgress = -1; // 文件进度
}
