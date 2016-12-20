package com.customerservice.receiver;

import android.content.Context;
import android.content.Intent;

import com.customerservice.AppUtils;
import com.customerservice.Log;
import com.customerservice.chat.jsonmodel.ChatMsgEntity;
import com.customerservice.chat.jsonmodel.NoticeMsgEntity;
import com.customerservice.chat.jsonmodel.TextMsgEntity;
import com.customerservice.chat.model.FileEntity;
import com.ioyouyun.wchat.message.FileMessage;
import com.ioyouyun.wchat.message.NoticeType;
import com.ioyouyun.wchat.message.NotifyCenter;
import com.ioyouyun.wchat.message.TextMessage;
import com.ioyouyun.wchat.message.WeimiNotice;
import com.ioyouyun.wchat.protocol.MetaMessageType;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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
            Log.logD("消息类型:" + type);

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
            }

        }
    }

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
            intent.setAction(AppUtils.MSG_TYPE_SEND_FILE_PRO);
        } else if (2 == action) {
            // 接收
            intent.setAction(AppUtils.MSG_TYPE_DOWNLOAD_FILE_PRO);
        }
        intent.putExtra(AppUtils.FILE_FILEID, fileId);
        intent.putExtra(AppUtils.FILE_PROGRESS, progress);
        intent.setPackage(context.getPackageName());
        BroadCastCenter.getInstance().broadcast(intent);
    }

    private void fileMessageMethod(WeimiNotice weimiNotice   ) {
        Log.logD("收到一条文件消息");
        FileMessage fileMessage = (FileMessage) weimiNotice.getObject();
        if (MetaMessageType.image == fileMessage.type) {
            receiveImage(fileMessage);
        }
    }

    private void receiveImage(FileMessage fileMessage) {
        String thumbnailPath = "";
        if (null != fileMessage.thumbData) {
            thumbnailPath = AppUtils.getThumbnailPath(fileMessage.fromuid, fileMessage.msgId);
            AppUtils.saveImg(fileMessage.thumbData, thumbnailPath); //保存缩略图
        }
        FileEntity fileEntity = new FileEntity();
        fileEntity.fileId = fileMessage.fileId;
        fileEntity.fileLength = fileMessage.fileLength;
        fileEntity.pieceSize = fileMessage.pieceSize;
        fileEntity.thumbnailPath = thumbnailPath;
        fileEntity.msgType = ChatMsgEntity.CHAT_TYPE_ROBOT_IMAGE;
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
        setBroadCast(AppUtils.MSG_TYPE_RECEIVE, fileEntity);
    }

    private void mixedTextMessageMethod(WeimiNotice weimiNotice) {
        TextMessage textMessage = (TextMessage) weimiNotice.getObject();
        Log.logD("收到一条富文本消息：" + textMessage.text);
    }

    private void textMessageMethod(WeimiNotice weimiNotice) {
        TextMessage textMessage = (TextMessage) weimiNotice.getObject();
        Log.logD("收到一条文本消息：" + textMessage.text);
        ChatMsgEntity entity = AppUtils.parseRobotMsg(textMessage.text);
        if (entity != null) {
            entity.time = textMessage.time;
            String padding = new String(textMessage.padding);
            Log.logD("额外消息：" + padding);
            if (AppUtils.isJSONObject(padding)) {
                try {
                    JSONObject object = new JSONObject(padding);
                    if (object != null) {
                        entity.nickName = object.optString(AppUtils.NICK_NAME);
                        entity.headUrl = object.optString(AppUtils.HEAD_URL);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            setBroadCast(AppUtils.MSG_TYPE_RECEIVE, entity);
        }
    }

    private void setBroadCast(String action, ChatMsgEntity entity) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(AppUtils.TYPE_MSG, entity);
        intent.setPackage(context.getPackageName());
        BroadCastCenter.getInstance().broadcast(intent);
    }

}
