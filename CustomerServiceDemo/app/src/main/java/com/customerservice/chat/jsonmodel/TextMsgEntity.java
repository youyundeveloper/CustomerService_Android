package com.customerservice.chat.jsonmodel;

import java.io.Serializable;

/**
 * Created by Bill on 2016/12/8.
 * text消息类型
 */

public class TextMsgEntity extends JsonParentEntity implements Serializable {

    public String content;

    public TextMsgEntity() {
        type = "text";
    }
}
