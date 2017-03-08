package com.ioyouyun.customerservice.chat.jsonmodel;

import java.io.Serializable;

/**
 * Created by Bill on 2016/12/8.
 * link消息类型
 */

public class CsLinkMsgEntity extends CsJsonParentEntity implements Serializable {

    public String url;
    public String content;

    public CsLinkMsgEntity() {
        type = "link";
    }

}
