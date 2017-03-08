package com.ioyouyun.customerservice.chat.jsonmodel;

import java.io.Serializable;

/**
 * Created by Bill on 2016/12/8.
 *
 * action消息类型
 */

public class CsActionMsgEntity extends CsJsonParentEntity implements Serializable {

    public String content;
    public String actionJson; // 存一个json object，点击时原样传给客服

    public CsActionMsgEntity() {
        type = "action";
    }

}
