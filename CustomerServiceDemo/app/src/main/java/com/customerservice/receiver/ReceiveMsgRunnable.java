package com.customerservice.receiver;

import android.content.Context;
import android.content.Intent;

import com.customerservice.chat.jsonmodel.CsJsonParentEntity;
import com.customerservice.chat.jsonmodel.CsNoticeMsgEntity;
import com.customerservice.chat.model.CsChatEntity;
import com.customerservice.chat.model.CsFileEntity;
import com.customerservice.utils.CsAppUtils;
import com.customerservice.utils.CsLog;
import com.ioyouyun.wchat.data.UnreadData;
import com.ioyouyun.wchat.message.FileMessage;
import com.ioyouyun.wchat.message.NoticeType;
import com.ioyouyun.wchat.message.NotifyCenter;
import com.ioyouyun.wchat.message.TextMessage;
import com.ioyouyun.wchat.message.WeimiNotice;
import com.ioyouyun.wchat.protocol.MetaMessageType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Bill on 2016/12/8.
 */

public class ReceiveMsgRunnable implements Runnable {

    public static Map<String, List<Integer>> fileSend = new ConcurrentHashMap<>();
    public static Map<String, Integer> fileSendCount = new ConcurrentHashMap<>();

    private Context context;

    public ReceiveMsgRunnable(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void run() {
        WeimiNotice weimiNotice = null;
        while (true) {
            try {
                weimiNotice = (WeimiNotice) NotifyCenter.clientNotifyChannel.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            NoticeType type = weimiNotice.getNoticeType();
            CsLog.logD("消息类型:" + type);

            if (NoticeType.textmessage == type) {
                textMessageMethod(weimiNotice);
            } else if (NoticeType.mixedtextmessage == type) {
                mixedTextMessageMethod(weimiNotice);
            } else if (NoticeType.filemessage == type) {
                fileMessageMethod(weimiNotice);
            } else if (NoticeType.downloadfile == type) {
                downloadMethod(weimiNotice);
            } else if (NoticeType.sendfile == type) {
                sendfileMethod(weimiNotice);
            } else if (NoticeType.recvUnreadNum == type){
                recvUnreadNumMethod(weimiNotice);
            }

        }
    }

    /**
     * 单聊未读消息数
     * @param weimiNotice
     */
    private void recvUnreadNumMethod(WeimiNotice weimiNotice) {
        Map<String, UnreadData> map = (Map<String, UnreadData>) weimiNotice.getObject();
        if(map != null) {
            Iterator<Map.Entry<String, UnreadData>> entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, UnreadData> entry = entries.next();
                CsLog.logD("Key = " + entry.getKey() + ", Value = " + entry.getValue().num);
                if(CsAppUtils.CUSTOM_SERVICE_ID.equals(entry.getKey())){
                    int num = entry.getValue().num;
                    CsAppUtils.unReadNum = num;
                    Intent intent = new Intent();
                    intent.setAction(CsAppUtils.MSG_TYPE_RECV_UNREAD_NUM);
                    intent.putExtra(CsAppUtils.TYPE_MSG, num);
                    intent.setPackage(context.getPackageName());
                    CsBroadCastCenter.getInstance().broadcast(intent);
                }
            }
        }
    }

    /**
     * 发送文件进度回调
     * @param weimiNotice
     */
    private void sendfileMethod(WeimiNotice weimiNotice) {
        List<Integer> unUploadSliceList = (List<Integer>) weimiNotice.getObject();
        String msgId = weimiNotice.getWithtag();
        if (unUploadSliceList.isEmpty()) {
            fileSend.remove(msgId);
            fileSendCount.remove(msgId);
            transportProgress(1, msgId, 100);
            return;
        }

        // 如果收到缺少分片的回执，但只本地已经清理掉fileSendCount的缓存，可认定是很旧的回执，可以丢掉
        if (!fileSendCount.containsKey(msgId)) {
            return;
        }
        List<Integer> list = fileSend.get(msgId);
        List<Integer> newList = new LinkedList<>();
        for (int i : unUploadSliceList) { // 排重
            // 如果包含在旧的list中，说明之前就是还未到达的分片
            // 如果不包含在旧的list中，说明之前已经确认到达，但是这个包来得迟了，所以应该去掉重复的包，忽略即可
            if (list.contains(Integer.valueOf(i)) && Integer.valueOf(i) <= Integer.valueOf(fileSendCount.get(msgId))) {
                newList.add(Integer.valueOf(i));
            }
        }
        fileSend.put(msgId, newList);
        double sliceCount = (double) fileSendCount.get(msgId);
        int listSize = newList.size();
        if (0 == listSize) {
            fileSend.remove(msgId);
            fileSendCount.remove(msgId);
            transportProgress(1, msgId, 100);
            return;
        } else {
            double completed = (sliceCount - listSize) / sliceCount;
            int progress = (int) (completed * 100);
            transportProgress(1, msgId, progress);
        }
    }

    private void downloadMethod(WeimiNotice weimiNotice) {
        FileMessage fileMessage = (FileMessage) weimiNotice.getObject();
        String fileId = weimiNotice.getWithtag();
        double completed = (fileMessage.hasReveive.size() / (double) fileMessage.limit);
        int progress = (int) (completed * 100);
        transportProgress(2, fileId, progress);
    }

    /**
     * 进度条进度
     *
     * @param action   上传：1，下载：2
     * @param fileId   文件ID
     * @param progress 当前大小
     */
    private void transportProgress(int action, String fileId, int progress) {
        Intent intent = new Intent();
        if (1 == action) {
            // 发送
            intent.setAction(CsAppUtils.MSG_TYPE_SEND_FILE_PRO);
        } else if (2 == action) {
            // 接收
            intent.setAction(CsAppUtils.MSG_TYPE_DOWNLOAD_FILE_PRO);
        }
        intent.putExtra(CsAppUtils.FILE_FILEID, fileId);
        intent.putExtra(CsAppUtils.FILE_PROGRESS, progress);
        intent.setPackage(context.getPackageName());
        CsBroadCastCenter.getInstance().broadcast(intent);
    }

    /**
     * 接收文件
     * @param weimiNotice
     */
    private void fileMessageMethod(WeimiNotice weimiNotice) {
        CsLog.logD("收到一条文件消息");
        FileMessage fileMessage = (FileMessage) weimiNotice.getObject();
        if (MetaMessageType.image == fileMessage.type) {
            String fromId = fileMessage.fromuid;
            if (CsAppUtils.CUSTOM_SERVICE_ID.equals(fromId)) {
                receiveImage(fileMessage);
            }else{
                CsLog.logD("收到" + fromId + "发来的文件消息");
            }

        }
    }

    /**
     * 接收图片
     * @param fileMessage
     */
    private void receiveImage(FileMessage fileMessage) {
        String thumbnailPath = "";
        if (null != fileMessage.thumbData) {
            thumbnailPath = CsAppUtils.getThumbnailPath(fileMessage.fromuid, fileMessage.msgId);
            CsAppUtils.saveImg(fileMessage.thumbData, thumbnailPath); //保存缩略图
        }
        CsChatEntity chatEntity = new CsChatEntity();
        CsFileEntity csFileEntity = new CsFileEntity();
        csFileEntity.fileId = fileMessage.fileId;
        csFileEntity.fileLength = fileMessage.fileLength;
        csFileEntity.pieceSize = fileMessage.pieceSize;
        csFileEntity.thumbnailPath = thumbnailPath;
        chatEntity.msgId = fileMessage.msgId;
        chatEntity.msgType = CsChatEntity.CHAT_TYPE_ROBOT_IMAGE;
        chatEntity.time = fileMessage.time;
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
        setBroadCast(CsAppUtils.MSG_TYPE_RECEIVE, chatEntity);
    }

    /**
     * 富文本消息
     * @param weimiNotice
     */
    private void mixedTextMessageMethod(WeimiNotice weimiNotice) {
        TextMessage textMessage = (TextMessage) weimiNotice.getObject();
        CsLog.logD("收到一条富文本消息：" + textMessage.text);
    }

    /**
     * 文本消息
     * @param weimiNotice
     */
    private void textMessageMethod(WeimiNotice weimiNotice) {
        TextMessage textMessage = (TextMessage) weimiNotice.getObject();
        String fromId = textMessage.fromuid;
        // 判断是不是客服下发的消息，其他人发的消息这里不做处理
        if(CsAppUtils.CUSTOM_SERVICE_ID.equals(fromId)){
            CsLog.logD("收到一条文本消息：" + textMessage.text);
            CsJsonParentEntity entity = CsAppUtils.parseRobotMsg(textMessage.text);
            if (entity != null) {
                CsChatEntity chatEntity = new CsChatEntity();
                chatEntity.msgId = textMessage.msgId;
                chatEntity.msgType = CsChatEntity.CHAT_TYPE_ROBOT_TEXT;
                if(entity instanceof CsNoticeMsgEntity){
                    chatEntity.msgType = CsChatEntity.CHAT_TYPE_NOTICE;
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
                chatEntity.time = textMessage.time;
                chatEntity.csJsonParentEntity = entity;

                setBroadCast(CsAppUtils.MSG_TYPE_RECEIVE, chatEntity);
            }
        } else {
            CsLog.logD("收到" + fromId + "发来的文本消息");
        }

    }

    /**
     * 发送消息
     * @param action
     * @param entity
     */
    private void setBroadCast(String action, CsChatEntity entity) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(CsAppUtils.TYPE_MSG, entity);
        intent.setPackage(context.getPackageName());
        CsBroadCastCenter.getInstance().broadcast(intent);
    }

}
