package com.customerservice.chat.jsonmodel;

import java.io.Serializable;

/**
 * Created by Bill on 2016/12/8.
 * link消息类型
 */

public class LinkMsgEntity extends JsonParentEntity implements Serializable {

    public String url;
    public String content;

    public LinkMsgEntity() {
        type = "link";
    }

}
