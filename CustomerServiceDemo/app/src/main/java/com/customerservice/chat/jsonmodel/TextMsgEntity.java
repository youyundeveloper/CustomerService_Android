package com.customerservice.chat.jsonmodel;

import java.io.Serializable;

/**
 * Created by Bill on 2016/12/8.
 */

public class TextMsgEntity extends ChatMsgEntity implements Serializable {

    public String content;

    public TextMsgEntity() {
        type = "text";
        msgType = CHAT_TYPE_ROBOT_TEXT;
    }
}
