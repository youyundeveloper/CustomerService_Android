package com.customerservice.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.customerservice.chat.jsonmodel.CsJsonParentEntity;
import com.customerservice.chat.jsonmodel.CsNoticeMsgEntity;
import com.customerservice.chat.jsonmodel.CsTextMsgEntity;
import com.customerservice.chat.model.CsChatEntity;
import com.customerservice.chat.model.CsFileEntity;
import com.customerservice.receiver.CsBroadCastCenter;
import com.customerservice.receiver.CsReceiveMsgRunnable;
import com.customerservice.utils.CsAppUtils;
import com.customerservice.utils.CsLog;
import com.ioyouyun.wchat.WeimiInstance;
import com.ioyouyun.wchat.message.ConvType;
import com.ioyouyun.wchat.message.FileMessage;
import com.ioyouyun.wchat.message.HistoryMessage;
import com.ioyouyun.wchat.message.NoticeType;
import com.ioyouyun.wchat.message.TextMessage;
import com.ioyouyun.wchat.message.WChatException;
import com.ioyouyun.wchat.protocol.MetaMessageType;
import com.ioyouyun.wchat.util.HttpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 */

public class CsChatPresenter {

    private Activity activity;
    private CsChatView csChatView;
    private MyInnerReceiver receiver;
    private Handler handler;

    private List<CsChatEntity> chatMsgEntityList = new ArrayList<>();

    public CsChatPresenter(CsChatView csChatView, Activity activity) {
        this.csChatView = csChatView;
        this.activity = activity;
        handler = new Handler(Looper.getMainLooper());
        registerReceiver();
    }

    public List<CsChatEntity> getChatList(){
        return chatMsgEntityList;
    }

    /**
     * 注册本地广播
     */
    private void registerReceiver() {
        receiver = new MyInnerReceiver();
        CsBroadCastCenter.getInstance().registerReceiver(receiver, CsAppUtils.MSG_TYPE_RECEIVE, CsAppUtils.MSG_TYPE_SEND_FILE_PRO, CsAppUtils.MSG_TYPE_DOWNLOAD_IMAGE_FINISH);
    }

    /**
     * 注销广播
     */
    private void unregisterReceiver() {
        if (receiver != null)
            CsBroadCastCenter.getInstance().unregisterReceiver(receiver);
    }

    public void onDestroy() {
        unregisterReceiver();
    }

    /**
     * 发送图片
     * @param filePath
     * @param fileName
     */
    public void sendImage(String filePath, String fileName){
        String msgId = WeimiInstance.getInstance().genLocalMsgId(CsAppUtils.CUSTOM_SERVICE_ID);

        String compressPath = CsAppUtils.compressImage(filePath, CsAppUtils.getChatImagePath(fileName));
        int compressFileLength = 0;
        File compressFile = new File(compressPath);
        if (compressFile.exists())
            compressFileLength = (int) compressFile.length();
        byte[] thumbnail = CsAppUtils.genSendImgThumbnail(compressPath);
        String thumbnailPath = "";
        if (thumbnail != null) {
            thumbnailPath = CsAppUtils.getThumbnailPath(CsAppUtils.uid, msgId);
            CsAppUtils.saveImg(thumbnail, thumbnailPath); //保存缩略图
        }

        CsChatEntity chatEntity = new CsChatEntity();
        CsFileEntity csFileEntity = new CsFileEntity();
        csFileEntity.fileLength = compressFileLength;
        csFileEntity.fileLocal = compressPath;
        csFileEntity.thumbnailPath = thumbnailPath;
        chatEntity.msgId = msgId;
        chatEntity.msgType = CsChatEntity.CHAT_TYPE_PEOPLE_SEND_IMAGE;
        chatEntity.time = System.currentTimeMillis();
        chatEntity.csFileEntity = csFileEntity;

        int sliceCount = 0;
        try {
            sliceCount = WeimiInstance.getInstance().sendFile(msgId, CsAppUtils.CUSTOM_SERVICE_ID, csFileEntity.fileLocal, fileName, MetaMessageType.image, null, ConvType.single, null, thumbnail, 600);
        } catch (WChatException e) {
            e.printStackTrace();
        }
        if (sliceCount > 0) {
            List<Integer> list = new LinkedList<>();
            for (int i = 1; i <= sliceCount; i++) {
                list.add(i);
            }
            CsReceiveMsgRunnable.fileSend.put(msgId, list);
            CsReceiveMsgRunnable.fileSendCount.put(msgId, sliceCount);

            refreshUI(chatEntity);
        }
    }

    /**
     * 发送文本消息
     * @param text
     */
    public void sendText(String text) {
        String msgId = WeimiInstance.getInstance().genLocalMsgId(CsAppUtils.CUSTOM_SERVICE_ID);
        String sendMsg = CsAppUtils.encapsulateTextMsg(text);
//        String sendMsg = CsAppUtils.encapsulateTest();
        boolean result = false;
        try {
            result = WeimiInstance.getInstance().sendText(msgId, CsAppUtils.CUSTOM_SERVICE_ID, sendMsg, ConvType.single, null, 60);
        } catch (WChatException e) {
            e.printStackTrace();
        }
        if(result){
            CsChatEntity chatEntity = new CsChatEntity();
            CsTextMsgEntity entity = new CsTextMsgEntity();
            entity.content = text;
            chatEntity.msgId = msgId;
            chatEntity.msgType = CsChatEntity.CHAT_TYPE_PEOPLE_SEND_TEXT;
            chatEntity.time = System.currentTimeMillis();
            chatEntity.csJsonParentEntity = entity;

            refreshUI(chatEntity);
        }
    }

    /**
     * @param type 1:enter 2:leave
     * 发送富文本消息
     */
    public void sendMixedText(int type) {
        String msgId = WeimiInstance.getInstance().genLocalMsgId(CsAppUtils.CUSTOM_SERVICE_ID);
        try {
            byte[] padding = null;
            if(1 == type){
                padding = CsAppUtils.encapsulateExt().getBytes("utf-8");
            }
            CsLog.logD("ext:" + CsAppUtils.encapsulateExt());
            WeimiInstance.getInstance().sendMixedText(msgId, CsAppUtils.CUSTOM_SERVICE_ID, CsAppUtils.encapsulateEnterOrLeaveMsg(type), ConvType.single, padding, 60);
        } catch (WChatException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取历史记录
     */
    public void getHistory(){
        long time = -1;
        if (chatMsgEntityList.size() > 0) {
            time = chatMsgEntityList.get(0).time / 1000;
        }
        WeimiInstance.getInstance().shortGetHistoryByTime(null, CsAppUtils.CUSTOM_SERVICE_ID, time, 10, ConvType.single, new HttpCallback() {
            @Override
            public void onResponse(String s) {

            }

            @Override
            public void onResponseHistory(List<HistoryMessage> list) {
                refreshComplete();
                if (list == null || list.size() == 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            CsAppUtils.toastMessage("没有更多数据");
                        }
                    });
                    return;
                }
                for (HistoryMessage message : list) {
                    if (NoticeType.textmessage == message.type) {
                        TextMessage textMessage = (TextMessage) message.message;
                        receiveText(textMessage);
                    } else if (NoticeType.mixedtextmessage == message.type) {
                        TextMessage textMessage = (TextMessage) message.message;
                        CsLog.logD("历史中收到一条富文本消息：" + textMessage.text);
                    }else if (NoticeType.filemessage == message.type) {
                        FileMessage fileMessage = (FileMessage) message.message;
                        receiveFile(fileMessage);
                    }
                }
                historyRefreshAdapter();

            }

            @Override
            public void onError(Exception e) {
                refreshComplete();
            }
        }, 120);
    }

    private void receiveFile(FileMessage fileMessage) {
        if (MetaMessageType.image == fileMessage.type) {
            historyCount++;
            String thumbnailPath = "";
            if (null != fileMessage.thumbData) {
                thumbnailPath = CsAppUtils.getThumbnailPath(fileMessage.fromuid, fileMessage.msgId);
                CsAppUtils.saveImg(fileMessage.thumbData, thumbnailPath); //保存缩略图
            }
            CsChatEntity chatEntity = new CsChatEntity();
            int msgType = CsChatEntity.CHAT_TYPE_ROBOT_IMAGE;
            if (CsAppUtils.uid.equals(fileMessage.fromuid))
                msgType = CsChatEntity.CHAT_TYPE_PEOPLE_SEND_IMAGE;
            chatEntity.msgId = fileMessage.msgId;
            chatEntity.msgType = msgType;
            chatEntity.time = fileMessage.time;
            CsFileEntity csFileEntity = new CsFileEntity();
            csFileEntity.fileId = fileMessage.fileId;
            csFileEntity.fileLength = fileMessage.fileLength;
            csFileEntity.pieceSize = fileMessage.pieceSize;
            csFileEntity.thumbnailPath = thumbnailPath;
            chatEntity.csFileEntity = csFileEntity;
            String padding = new String(fileMessage.padding);
            CsLog.logD("额外消息：" + padding);
            if (CsAppUtils.isJSONObject(padding)) {
                try {
                    JSONObject object = new JSONObject(padding);
                    if (object != null) {
                        chatEntity.nickName = object.optString(CsAppUtils.NICK_NAME);
                        chatEntity.headUrl = object.optString(CsAppUtils.HEAD_URL);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            chatMsgEntityList.add(0, chatEntity);
            if (chatMsgEntityList.size() > 1) {
                if (chatMsgEntityList.get(1).time - chatMsgEntityList.get(0).time <= CsAppUtils.MSG_TIME_SEPARATE) {
                    chatMsgEntityList.get(1).isShowTime = false;
                }
            }

        }
    }

    private void receiveText(TextMessage textMessage) {
        historyCount++;

        CsChatEntity chatEntity = new CsChatEntity();
        CsJsonParentEntity entity = CsAppUtils.parseRobotMsg(textMessage.text);
        int msgType = CsChatEntity.CHAT_TYPE_ROBOT_TEXT;
        if (CsAppUtils.uid.equals(textMessage.fromuid))
            msgType = CsChatEntity.CHAT_TYPE_PEOPLE_SEND_TEXT;
        if (entity != null) {
            if(entity instanceof CsNoticeMsgEntity){
                msgType = CsChatEntity.CHAT_TYPE_NOTICE;
            } else {
                String padding = new String(textMessage.padding);
                CsLog.logD("额外消息：" + padding);
                if (CsAppUtils.isJSONObject(padding)) {
                    try {
                        JSONObject object = new JSONObject(padding);
                        if (object != null) {
                            chatEntity.nickName = object.optString(CsAppUtils.NICK_NAME);
                            chatEntity.headUrl = object.optString(CsAppUtils.HEAD_URL);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            chatEntity.msgId = textMessage.msgId;
            chatEntity.msgType = msgType;
            chatEntity.time = textMessage.time;
            chatEntity.csJsonParentEntity = entity;
            chatEntity.isShowTime = true;

            chatMsgEntityList.add(0, chatEntity);
            if (chatMsgEntityList.size() > 1) {
                if (chatMsgEntityList.get(1).time - chatMsgEntityList.get(0).time <= CsAppUtils.MSG_TIME_SEPARATE) {
                    chatMsgEntityList.get(1).isShowTime = false;
                }
            }

        }
    }

    /**
     * 刷新历史聊天列表
     */
    private void historyRefreshAdapter() {
        if (historyCount > 0) {
            final int count = historyCount;
            historyCount = 0;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    csChatView.refreshList(chatMsgEntityList);
                    csChatView.scrollToPosition(count);
                }
            });
        }
    }
    int historyCount = 0;

    /**
     * 取消下拉加载进度条
     */
    private void refreshComplete() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                csChatView.onCompleteLoad();
            }
        });
    }

    /**
     * 刷新UI,并计算时间显示规则
     * @param entity
     */
    private void refreshUI(CsChatEntity entity){
        int index = chatMsgEntityList.size();
        chatMsgEntityList.add(index, entity);
        int preIndex = index - 1;
        if (preIndex >= 0) {
            if (chatMsgEntityList.get(index).time - chatMsgEntityList.get(preIndex).time > CsAppUtils.MSG_TIME_SEPARATE) {
                chatMsgEntityList.get(index).isShowTime = true;
                chatMsgEntityList.set(index, entity);
            }
        } else {
            chatMsgEntityList.get(index).isShowTime = true;
            chatMsgEntityList.set(index, entity);
        }

        csChatView.refreshList(chatMsgEntityList);
        csChatView.scrollToPosition(chatMsgEntityList.size() - 1);
        csChatView.clearInputMsg();
    }

    class MyInnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CsAppUtils.MSG_TYPE_RECEIVE.equals(action)) {
                CsChatEntity entity = (CsChatEntity) intent.getSerializableExtra(CsAppUtils.TYPE_MSG);
                refreshUI(entity);
            }else if (CsAppUtils.MSG_TYPE_SEND_FILE_PRO.equals(action)) {
                String fileId = intent.getStringExtra(CsAppUtils.FILE_FILEID);
                int progress = intent.getIntExtra(CsAppUtils.FILE_PROGRESS, 0);
                CsLog.logD("发送进度：" + progress);
                for (int i = chatMsgEntityList.size() - 1; i >= 0; i--) {
                    if(chatMsgEntityList.get(i).msgId.equals(fileId)){
                        if(progress == 100){
                            chatMsgEntityList.get(i).fileProgress = -1;
                        } else chatMsgEntityList.get(i).fileProgress = progress;
                        break;
                    }
                }
                csChatView.refreshList(chatMsgEntityList);
            }else if (CsAppUtils.MSG_TYPE_DOWNLOAD_IMAGE_FINISH.equals(action)) {
                int position = intent.getIntExtra(CsAppUtils.MSG_TYPE_POSITION, 0);
                CsFileEntity entity = (CsFileEntity) intent.getSerializableExtra(CsAppUtils.TYPE_MSG);
                chatMsgEntityList.get(position).csFileEntity = entity;
                csChatView.refreshList(chatMsgEntityList);

                CsLog.logD("下载大图完成,更新数据");
            }
        }
    }

}
