package com.customerservice.chat.jsonmodel;

import java.io.Serializable;

/**
 * Created by Bill on 2016/12/8.
 */

public class LinkMsgEntity extends ChatMsgEntity implements Serializable {

    public String url;
    public String content;

    public LinkMsgEntity() {
        type = "link";
        msgType = CHAT_TYPE_ROBOT_TEXT;
    }

}
