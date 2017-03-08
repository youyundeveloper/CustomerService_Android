package com.ioyouyun.customerservice.chat;

import com.ioyouyun.customerservice.chat.model.CsChatEntity;

import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 */

public interface CsChatView {

    /**
     * 设置数据
     *
     * @param list
     */
    void refreshList(List<CsChatEntity> list);

    /**
     * 定位到某一行
     *
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
