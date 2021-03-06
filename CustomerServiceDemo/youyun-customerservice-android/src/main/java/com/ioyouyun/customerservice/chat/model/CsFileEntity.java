package com.ioyouyun.customerservice.chat.model;

import java.io.Serializable;


/**
 * Created by Bill on 2016/12/13.
 * <p>
 * 文件消息实体类
 */

public class CsFileEntity implements Serializable {

    public String fileId; // 文件id, 下发文件时下发, 下载原文件需要
    public int fileLength; // 文件大小
    public int pieceSize; // 分片大小, 下发文件时下发
    public String fileLocal; // 文件本地路径

    public String thumbnailPath; // 缩略图路径

}
