package com.customerservice.chat.jsonmodel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 */

public class CardMsgEntity extends ChatMsgEntity implements Serializable {

    public List<ChatMsgEntity> content;

    public CardMsgEntity() {
        type = "card";
        msgType = CHAT_TYPE_ROBOT_TEXT;
    }

}
