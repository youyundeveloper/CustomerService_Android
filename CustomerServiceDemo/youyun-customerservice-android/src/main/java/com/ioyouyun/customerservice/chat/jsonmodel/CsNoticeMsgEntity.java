package com.ioyouyun.customerservice.chat.jsonmodel;

import java.io.Serializable;

/**
 * Created by Bill on 2016/12/16.
 * text消息类型,但是属于通知消息
 */

public class CsNoticeMsgEntity extends CsJsonParentEntity implements Serializable {

    public String content;

    public CsNoticeMsgEntity() {
        type = "text";
    }
}
