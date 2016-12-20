package com.customerservice.chat;

import com.customerservice.chat.jsonmodel.ChatMsgEntity;
import com.customerservice.chat.model.FileEntity;

import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 */

public interface ChatView {

    /**
     * 设置数据
     * @param list
     */
    void refreshList(List<ChatMsgEntity> list);

    /**
     * 定位到某一行
     * @param position
     */
    void scrollToPosition(int position);

    /**
     * 清空输入框
     */
    void clearInputMsg();

    /**
     * 历史加载完成
     */
    void onCompleteLoad();

}
