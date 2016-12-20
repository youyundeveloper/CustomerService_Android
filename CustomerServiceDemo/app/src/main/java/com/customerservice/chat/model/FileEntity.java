package com.customerservice.chat.model;

import com.customerservice.chat.jsonmodel.ChatMsgEntity;

import java.io.Serializable;


/**
 * Created by Bill on 2016/12/13.
 */

public class FileEntity extends ChatMsgEntity implements Serializable{

    public String fileId; // 文件id, 下发文件时下发, 下载原文件需要
    public int fileLength; // 文件大小
    public int pieceSize; // 分片id, 下发文件时下发
    public String fileLocal; // 文件本地路径

    public String thumbnailPath; // 缩略图路径

    public FileEntity() {

    }

}
