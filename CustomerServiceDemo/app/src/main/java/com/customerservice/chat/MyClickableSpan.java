package com.customerservice.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.customerservice.AppUtils;
import com.customerservice.Log;
import com.customerservice.chat.jsonmodel.ActionMsgEntity;
import com.customerservice.chat.jsonmodel.ChatMsgEntity;
import com.customerservice.chat.jsonmodel.LinkMsgEntity;
import com.customerservice.chat.jsonmodel.TextMsgEntity;
import com.ioyouyun.wchat.WeimiInstance;
import com.ioyouyun.wchat.message.ConvType;
import com.ioyouyun.wchat.message.WChatException;

/**
 * Created by Bill on 2016/12/9.
 */

public class MyClickableSpan extends ClickableSpan {

    ChatMsgEntity entity;
    Context context;

    public MyClickableSpan(ChatMsgEntity entity, Context context) {
        super();
        this.entity = entity;
        this.context = context;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        if (entity instanceof TextMsgEntity) {
            ds.setColor(Color.parseColor("#000000"));
        } else if (entity instanceof ActionMsgEntity) {
            ds.setColor(Color.parseColor("#00ffff"));
        } else if (entity instanceof LinkMsgEntity) {
            ds.setColor(Color.parseColor("#00ffff"));
            ds.setUnderlineText(true);
        }
    }

    @Override
    public void onClick(View view) {
        if (entity instanceof LinkMsgEntity) {
            LinkMsgEntity linkMsgEntity = (LinkMsgEntity) entity;
            linkClick(linkMsgEntity);
        } else if (entity instanceof ActionMsgEntity) {
//            ((TextView)view).setHighlightColor(context.getResources().getColor(android.R.color.holo_red_light));
            ActionMsgEntity actionMsgEntity = (ActionMsgEntity) entity;
            actionClick(actionMsgEntity);
        }
    }

    private void actionClick(ActionMsgEntity entity) {
        String msgId = WeimiInstance.getInstance().genLocalMsgId(AppUtils.CUSTOM_SERVICE_ID);
        String text = AppUtils.encapsulateClickMsg(entity);
        Log.logD("action:" + text);
        boolean result = false;
        try {
            result = WeimiInstance.getInstance().sendMixedText(msgId, AppUtils.CUSTOM_SERVICE_ID, text, ConvType.single, null, 60);
        } catch (WChatException e) {
            e.printStackTrace();
        }
        if (!result)
            AppUtils.toastMessage("点击失败了！");

    }

    private void linkClick(LinkMsgEntity entity){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(entity.url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
