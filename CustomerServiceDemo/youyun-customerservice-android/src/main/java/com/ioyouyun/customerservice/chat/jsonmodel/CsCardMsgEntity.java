package com.ioyouyun.customerservice.chat.jsonmodel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 * card消息类型
 */

public class CsCardMsgEntity extends CsJsonParentEntity implements Serializable {

    public List<CsJsonParentEntity> content;

    public CsCardMsgEntity() {
        type = "card";
    }

}
