package com.ioyouyun.customerservice.chat.jsonmodel;

import java.io.Serializable;

/**
 * Created by Bill on 2016/12/8.
 * text消息类型
 */

public class CsTextMsgEntity extends CsJsonParentEntity implements Serializable {

    public String content;

    public CsTextMsgEntity() {
        type = "text";
    }
}
