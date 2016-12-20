package com.customerservice.chat.jsonmodel;

import java.io.Serializable;

/**
 * Created by Bill on 2016/12/16.
 */

public class NoticeMsgEntity extends ChatMsgEntity implements Serializable {

    public String content;

    public NoticeMsgEntity() {
        type = "text";
        msgType = CHAT_TYPE_NOTICE;
    }
}
