package com.customerservice.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.customerservice.utils.CsAppUtils;
import com.customerservice.utils.CsLog;
import com.customerservice.chat.jsonmodel.CsActionMsgEntity;
import com.customerservice.chat.jsonmodel.CsJsonParentEntity;
import com.customerservice.chat.jsonmodel.CsLinkMsgEntity;
import com.ioyouyun.wchat.WeimiInstance;
import com.ioyouyun.wchat.message.ConvType;
import com.ioyouyun.wchat.message.WChatException;

/**
 * Created by Bill on 2016/12/9.
 */

public class CsClickableSpan extends ClickableSpan {

    CsJsonParentEntity entity;
    Context context;

    public CsClickableSpan(CsJsonParentEntity entity, Context context) {
        super();
        this.entity = entity;
        this.context = context;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        if (entity instanceof CsActionMsgEntity) {
            ds.setColor(Color.parseColor("#0496fe"));
            ds.setUnderlineText(false);
        } else if (entity instanceof CsLinkMsgEntity) {
            ds.setColor(Color.parseColor("#0496fe"));
            ds.setUnderlineText(true);
        }
    }

    @Override
    public void onClick(View view) {
        if (entity instanceof CsLinkMsgEntity) {
            CsLinkMsgEntity linkMsgEntity = (CsLinkMsgEntity) entity;
            linkClick(linkMsgEntity);
        } else if (entity instanceof CsActionMsgEntity) {
            ((TextView)view).setHighlightColor(Color.parseColor("#a0a0a0"));
            CsActionMsgEntity csActionMsgEntity = (CsActionMsgEntity) entity;
            actionClick(csActionMsgEntity);
        }
    }

    private void actionClick(CsActionMsgEntity entity) {
        String msgId = WeimiInstance.getInstance().genLocalMsgId(CsAppUtils.CUSTOM_SERVICE_ID);
        String text = CsAppUtils.encapsulateClickMsg(entity);
        CsLog.logD("action:" + text);
        boolean result = false;
        try {
            result = WeimiInstance.getInstance().sendMixedText(msgId, CsAppUtils.CUSTOM_SERVICE_ID, text, ConvType.single, null, 60);
        } catch (WChatException e) {
            e.printStackTrace();
        }
        if (!result)
            CsAppUtils.toastMessage("点击失败了！");

    }

    private void linkClick(CsLinkMsgEntity entity){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(entity.url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
