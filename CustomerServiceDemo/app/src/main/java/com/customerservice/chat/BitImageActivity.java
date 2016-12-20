package com.customerservice.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.customerservice.AppUtils;
import com.customerservice.Log;
import com.customerservice.R;
import com.customerservice.chat.model.FileEntity;
import com.customerservice.receiver.BroadCastCenter;
import com.ioyouyun.wchat.WeimiInstance;
import com.ioyouyun.wchat.message.WChatException;

public class BitImageActivity extends AppCompatActivity {

    private final static String FILE_ENTITY = "file_entity";
    private final static String CHAT_POSITION = "chat_position";

    private ImageView imageView;
    private ProgressBar progressBar;
    private TextView progressText;

    private MyInnerReceiver receiver;

    private FileEntity fileEntity;
    private int position;

    private String downloadPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bit_image);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressText = (TextView) findViewById(R.id.tv_progress);
        imageView = (ImageView) findViewById(R.id.iv_bigimg);
        registerReceiver();

        getIntentExtra();

        showImg(fileEntity);
    }

    private void setChatPicInfo(String imgPath) {
        fileEntity.fileLocal = imgPath;
        notifyChatList(fileEntity);
    }

    private void notifyChatList(FileEntity fileEntity) {
        Intent intent = new Intent();
        intent.setAction(AppUtils.MSG_TYPE_DOWNLOAD_IMAGE_FINISH);
        intent.putExtra(AppUtils.MSG_TYPE_POSITION, position);
        intent.putExtra(AppUtils.TYPE_MSG, fileEntity);
        intent.setPackage(getPackageName());
        BroadCastCenter.getInstance().broadcast(intent);
    }

    private void loadLocalImage(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(bitmap);
    }

    private void showImg(FileEntity fileEntity) {
        if (TextUtils.isEmpty(fileEntity.fileLocal)) {
            Log.logD("下载大图");
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            progressText.setText("0");
            downLoadImg(fileEntity);
        } else {
            Log.logD("大图已存在");
            loadLocalImage(fileEntity.fileLocal);
        }
    }

    private void downLoadImg(FileEntity fileEntity) {
        downloadPath = AppUtils.getChatImagePath(fileEntity.fileId) + ".png";
        try {
            WeimiInstance.getInstance().downloadFile(fileEntity.fileId, downloadPath, fileEntity.fileLength, null, fileEntity.pieceSize, 60);
        } catch (WChatException e) {
            e.printStackTrace();
        }
    }

    public static void startActivity(Context activity, FileEntity fileEntity, int position) {
        Intent intent = new Intent(activity, BitImageActivity.class);
        intent.putExtra(FILE_ENTITY, fileEntity);
        intent.putExtra(CHAT_POSITION, position);
        activity.startActivity(intent);
    }

    private void getIntentExtra() {
        Intent intent = getIntent();
        if (intent != null) {
            fileEntity = (FileEntity) intent.getSerializableExtra(FILE_ENTITY);
            position = intent.getIntExtra(CHAT_POSITION, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
    }

    /**
     * 注册本地广播
     */
    private void registerReceiver() {
        receiver = new MyInnerReceiver();
        BroadCastCenter.getInstance().registerReceiver(receiver, AppUtils.MSG_TYPE_DOWNLOAD_FILE_PRO);
    }

    /**
     * 注销广播
     */
    private void unregisterReceiver() {
        if (receiver != null)
            BroadCastCenter.getInstance().unregisterReceiver(receiver);
    }

    class MyInnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AppUtils.MSG_TYPE_DOWNLOAD_FILE_PRO.equals(action)) {
                String fileId = intent.getStringExtra(AppUtils.FILE_FILEID);
                int progress = intent.getIntExtra(AppUtils.FILE_PROGRESS, 0);
                String filePath = AppUtils.getChatImagePath(fileId) + ".png";
                Log.logD("progress:" + progress);
                progressText.setText("" + progress);
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                    progressText.setVisibility(View.GONE);
                    // 下载完成
                    Log.logD("下载完成");
                    if (filePath.equals(downloadPath)) {
                        setChatPicInfo(filePath);
                        loadLocalImage(filePath);
                    }
                }
            }

        }
    }

}
