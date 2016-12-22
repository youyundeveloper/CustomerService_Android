package com.customerservice.chat.jsonmodel;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 * card消息类型
 */

public class CardMsgEntity extends JsonParentEntity implements Serializable {

    public List<JsonParentEntity> content;

    public CardMsgEntity() {
        type = "card";
    }

}
