package com.ioyouyun.customerservice.chat.jsonmodel;

import java.io.Serializable;

/**
 * Created by Bill on 2016/12/8.
 * <p>
 * 客服发来的消息是json格式的，这里新建一个父类，每一种消息类型都继承此类，
 * 这样只需要在解析json的时候知道消息类型其他时候根据entity instanceof xxx就好了
 */

public class CsJsonParentEntity implements Serializable {

    public String type; // 接收到json的类型

}
