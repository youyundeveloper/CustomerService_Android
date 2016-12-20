package com.customerservice.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.customerservice.AppUtils;
import com.customerservice.Log;
import com.customerservice.chat.jsonmodel.ChatMsgEntity;
import com.customerservice.chat.jsonmodel.TextMsgEntity;
import com.customerservice.chat.model.FileEntity;
import com.customerservice.receiver.BroadCastCenter;
import com.customerservice.receiver.ReceiveMsgRunnable;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 */

public class ChatPresenter {

    private Activity activity;
    private ChatView chatView;
    private MyInnerReceiver receiver;
    private Handler handler;

    private List<ChatMsgEntity> chatMsgEntityList = new ArrayList<>();

    public ChatPresenter(ChatView chatView, Activity activity) {
        this.chatView = chatView;
        this.activity = activity;
        handler = new Handler(Looper.getMainLooper());
        registerReceiver();
        ReceiveMsgRunnable runnable = new ReceiveMsgRunnable(activity);
        Thread msgThread = new Thread(runnable);
        msgThread.start();
    }

    /**
     * 注册本地广播
     */
    private void registerReceiver() {
        receiver = new MyInnerReceiver();
        BroadCastCenter.getInstance().registerReceiver(receiver, AppUtils.MSG_TYPE_RECEIVE, AppUtils.MSG_TYPE_SEND_FILE_PRO, AppUtils.MSG_TYPE_DOWNLOAD_IMAGE_FINISH);
    }

    /**
     * 注销广播
     */
    private void unregisterReceiver() {
        if (receiver != null)
            BroadCastCenter.getInstance().unregisterReceiver(receiver);
    }

    public void onDestroy() {
        unregisterReceiver();
    }

    public void sendImage(String filePath, String fileName){
        String msgId = WeimiInstance.getInstance().genLocalMsgId(AppUtils.CUSTOM_SERVICE_ID);

        int fileLength = 0;
        File file = new File(filePath);
        if (file.exists())
            fileLength = (int) file.length();

        String compressPath = AppUtils.compressImage(filePath, AppUtils.getChatImagePath(fileName));
        int compressFileLength = 0;
        File compressFile = new File(compressPath);
        if (compressFile.exists())
            compressFileLength = (int) compressFile.length();
        byte[] thumbnail = AppUtils.genSendImgThumbnail(compressPath);
        String thumbnailPath = "";
        if (thumbnail != null) {
            thumbnailPath = AppUtils.getThumbnailPath(AppUtils.uid, msgId);
            AppUtils.saveImg(thumbnail, thumbnailPath); //保存缩略图
        }
        int thumbnailFileLength = 0;
        File thumbnailFile = new File(thumbnailPath);
        if (thumbnailFile.exists())
            thumbnailFileLength = (int) thumbnailFile.length();

        FileEntity fileEntity = new FileEntity();
        fileEntity.fileLength = compressFileLength;
        fileEntity.fileLocal = compressPath;
        fileEntity.thumbnailPath = thumbnailPath;
        fileEntity.msgType = ChatMsgEntity.CHAT_TYPE_PEOPLE_SEND_IMAGE;
        fileEntity.time = System.currentTimeMillis();

        Log.logD("文件原图大小：" + fileLength + " |压缩后大小：" + compressFileLength + " |缩略图大小：" + thumbnailFileLength);

        int sliceCount = 0;
        try {
            sliceCount = WeimiInstance.getInstance().sendFile(msgId, AppUtils.CUSTOM_SERVICE_ID, fileEntity.fileLocal, fileName, MetaMessageType.image, null, ConvType.single, null, thumbnail, 600);
        } catch (WChatException e) {
            e.printStackTrace();
        }
        if (sliceCount > 0) {
            List<Integer> list = new LinkedList<>();
            for (int i = 1; i <= sliceCount; i++) {
                list.add(i);
            }
            ReceiveMsgRunnable.fileSend.put(msgId, list);
            ReceiveMsgRunnable.fileSendCount.put(msgId, sliceCount);

            refreshUI(fileEntity);
        }
    }

    /**
     * 发送文本消息
     * @param text
     */
    public void sendText(String text) {
        String msgId = WeimiInstance.getInstance().genLocalMsgId(AppUtils.CUSTOM_SERVICE_ID);
        String sendMsg = AppUtils.encapsulateTextMsg(text);
//        String sendMsg = AppUtils.encapsulateTest();
        boolean result = false;
        try {
            result = WeimiInstance.getInstance().sendText(msgId, AppUtils.CUSTOM_SERVICE_ID, sendMsg, ConvType.single, null, 60);
        } catch (WChatException e) {
            e.printStackTrace();
        }
        if(result){
            TextMsgEntity entity = new TextMsgEntity();
            entity.content = text;
            entity.msgType = ChatMsgEntity.CHAT_TYPE_PEOPLE_SEND_TEXT;
            entity.time = System.currentTimeMillis();
            refreshUI(entity);
        }
    }

    /**
     * @param type 1:enter 2:leave
     * 发送富文本消息
     */
    public void sendMixedText(int type) {
        String msgId = WeimiInstance.getInstance().genLocalMsgId(AppUtils.CUSTOM_SERVICE_ID);
        try {
            WeimiInstance.getInstance().sendMixedText(msgId, AppUtils.CUSTOM_SERVICE_ID, AppUtils.encapsulateEnterOrLeaveMsg(type), ConvType.single, null, 60);
        } catch (WChatException e) {
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
        WeimiInstance.getInstance().shortGetHistoryByTime(AppUtils.CUSTOM_SERVICE_ID, time, 20, ConvType.single, new HttpCallback() {
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
                            AppUtils.toastMessage("没有更多数据");
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
                        Log.logD("历史中收到一条富文本消息：" + textMessage.text);
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
        historyCount++;

        if (MetaMessageType.image == fileMessage.type) {
            String thumbnailPath = "";
            if (null != fileMessage.thumbData) {
                thumbnailPath = AppUtils.getThumbnailPath(fileMessage.fromuid, fileMessage.msgId);
                AppUtils.saveImg(fileMessage.thumbData, thumbnailPath); //保存缩略图
            }
            FileEntity fileEntity = new FileEntity();
            int msgType = ChatMsgEntity.CHAT_TYPE_ROBOT_IMAGE;
            if (AppUtils.uid.equals(fileMessage.fromuid))
                msgType = ChatMsgEntity.CHAT_TYPE_PEOPLE_SEND_IMAGE;
            fileEntity.msgType = msgType;
            fileEntity.fileId = fileMessage.fileId;
            fileEntity.fileLength = fileMessage.fileLength;
            fileEntity.pieceSize = fileMessage.pieceSize;
            fileEntity.thumbnailPath = thumbnailPath;
            fileEntity.time = fileMessage.time;
            String padding = new String(fileMessage.padding);
            Log.logD("额外消息：" + padding);
            try {
                JSONObject object = new JSONObject(padding);
                if(object != null){
                    fileEntity.nickName = object.optString(AppUtils.NICK_NAME);
                    fileEntity.headUrl = object.optString(AppUtils.HEAD_URL);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            chatMsgEntityList.add(0, fileEntity);
            if (chatMsgEntityList.size() > 1) {
                if (chatMsgEntityList.get(1).time - chatMsgEntityList.get(0).time <= AppUtils.MSG_TIME_SEPARATE) {
                    chatMsgEntityList.get(1).isShowTime = false;
                }
            }

        }
    }

    private void receiveText(TextMessage textMessage) {
        historyCount++;

        ChatMsgEntity entity = AppUtils.parseRobotMsg(textMessage.text);
        int msgType = ChatMsgEntity.CHAT_TYPE_ROBOT_TEXT;
        if (AppUtils.uid.equals(textMessage.fromuid))
            msgType = ChatMsgEntity.CHAT_TYPE_PEOPLE_SEND_TEXT;
        if (entity != null) {
            String padding = new String(textMessage.padding);
            Log.logD("额外消息：" + padding);
            try {
                JSONObject object = new JSONObject(padding);
                if(object != null){
                    entity.nickName = object.optString(AppUtils.NICK_NAME);
                    entity.headUrl = object.optString(AppUtils.HEAD_URL);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            entity.msgType = msgType;
            entity.time = textMessage.time;
            entity.isShowTime = true;

            chatMsgEntityList.add(0, entity);
            if (chatMsgEntityList.size() > 1) {
                if (chatMsgEntityList.get(1).time - chatMsgEntityList.get(0).time <= AppUtils.MSG_TIME_SEPARATE) {
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
                    chatView.refreshList(chatMsgEntityList);
                    chatView.scrollToPosition(count);
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
                chatView.onCompleteLoad();
            }
        });
    }

    private void refreshUI(ChatMsgEntity entity){
        int index = chatMsgEntityList.size();
        chatMsgEntityList.add(index, entity);
        int preIndex = index - 1;
        if (preIndex >= 0) {
            if (chatMsgEntityList.get(index).time - chatMsgEntityList.get(preIndex).time > AppUtils.MSG_TIME_SEPARATE) {
                chatMsgEntityList.get(index).isShowTime = true;
                chatMsgEntityList.set(index, entity);
            }
        } else {
            chatMsgEntityList.get(index).isShowTime = true;
            chatMsgEntityList.set(index, entity);
        }

        chatView.refreshList(chatMsgEntityList);
        chatView.scrollToPosition(chatMsgEntityList.size() - 1);
        chatView.clearInputMsg();
    }

    class MyInnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppUtils.MSG_TYPE_RECEIVE.equals(action)) {
                ChatMsgEntity entity = (ChatMsgEntity) intent.getSerializableExtra(AppUtils.TYPE_MSG);
                refreshUI(entity);
            }else if (AppUtils.MSG_TYPE_SEND_FILE_PRO.equals(action)) {
                String fileId = intent.getStringExtra(AppUtils.FILE_FILEID);
                int progress = intent.getIntExtra(AppUtils.FILE_PROGRESS, 0);
                Log.logD("发送进度：" + progress);
            }else if (AppUtils.MSG_TYPE_DOWNLOAD_IMAGE_FINISH.equals(action)) {
                int position = intent.getIntExtra(AppUtils.MSG_TYPE_POSITION, 0);
                FileEntity entity = (FileEntity) intent.getSerializableExtra(AppUtils.TYPE_MSG);
                chatMsgEntityList.set(position, entity);
                chatView.refreshList(chatMsgEntityList);

                Log.logD("下载大图完成,更新数据");
            }
        }
    }

}
