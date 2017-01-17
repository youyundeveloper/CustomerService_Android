package com.customerservice.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.customerservice.R;
import com.customerservice.chat.model.CsFileEntity;
import com.customerservice.receiver.CsBroadCastCenter;
import com.customerservice.utils.CsAppUtils;
import com.customerservice.utils.CsLog;
import com.ioyouyun.wchat.WeimiInstance;
import com.ioyouyun.wchat.message.WChatException;

/**
 * Created by Bill on 2016/12/8.
 * 查看大图
 */

public class CsBigImageActivity extends Activity {

    private final static String FILE_ENTITY = "file_entity";
    private final static String CHAT_POSITION = "chat_position";

    private ImageView imageView;
    private ProgressBar progressBar;
    private TextView progressText;

    private MyInnerReceiver receiver;

    private CsFileEntity csFileEntity;
    private int position;

    private String downloadPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cs_activity_bit_image);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressText = (TextView) findViewById(R.id.tv_progress);
        imageView = (ImageView) findViewById(R.id.iv_bigimg);
        registerReceiver();

        getIntentExtra();

        showImg(csFileEntity);
    }

    private void setChatPicInfo(String imgPath) {
        csFileEntity.fileLocal = imgPath;
        notifyChatList(csFileEntity);
    }

    private void notifyChatList(CsFileEntity csFileEntity) {
        Intent intent = new Intent();
        intent.setAction(CsAppUtils.MSG_TYPE_DOWNLOAD_IMAGE_FINISH);
        intent.putExtra(CsAppUtils.MSG_TYPE_POSITION, position);
        intent.putExtra(CsAppUtils.TYPE_MSG, csFileEntity);
        intent.setPackage(getPackageName());
        CsBroadCastCenter.getInstance().broadcast(intent);
    }

    private void loadLocalImage(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        imageView.setImageBitmap(bitmap);
    }

    private void showImg(CsFileEntity csFileEntity) {
        if (TextUtils.isEmpty(csFileEntity.fileLocal)) {
            CsLog.logD("下载大图");
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            progressText.setText("0");
            downLoadImg(csFileEntity);
        } else {
            CsLog.logD("大图已存在");
            loadLocalImage(csFileEntity.fileLocal);
        }
    }

    private void downLoadImg(CsFileEntity csFileEntity) {
        downloadPath = CsAppUtils.getChatImagePath(csFileEntity.fileId) + ".png";
        try {
            CsLog.logD("fileId:" + csFileEntity.fileId);
            CsLog.logD("fileLength:" + csFileEntity.fileLength);
            CsLog.logD("pieceSize:" + csFileEntity.pieceSize);

            WeimiInstance.getInstance().downloadFile(csFileEntity.fileId, downloadPath, csFileEntity.fileLength, null, csFileEntity.pieceSize, 60);

//            WeimiInstance.getInstance().downloadAllFile(csFileEntity.fileId, downloadPath, 60);
        } catch (WChatException e) {
            e.printStackTrace();
        }
    }

    public static void startActivity(Context activity, CsFileEntity csFileEntity, int position) {
        Intent intent = new Intent(activity, CsBigImageActivity.class);
        intent.putExtra(FILE_ENTITY, csFileEntity);
        intent.putExtra(CHAT_POSITION, position);
        activity.startActivity(intent);
    }

    private void getIntentExtra() {
        Intent intent = getIntent();
        if (intent != null) {
            csFileEntity = (CsFileEntity) intent.getSerializableExtra(FILE_ENTITY);
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
        CsBroadCastCenter.getInstance().registerReceiver(receiver, CsAppUtils.MSG_TYPE_DOWNLOAD_FILE_PRO);
    }

    /**
     * 注销广播
     */
    private void unregisterReceiver() {
        if (receiver != null)
            CsBroadCastCenter.getInstance().unregisterReceiver(receiver);
    }

    class MyInnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CsAppUtils.MSG_TYPE_DOWNLOAD_FILE_PRO.equals(action)) {
                String fileId = intent.getStringExtra(CsAppUtils.FILE_FILEID);
                int progress = intent.getIntExtra(CsAppUtils.FILE_PROGRESS, 0);
                String filePath = CsAppUtils.getChatImagePath(fileId) + ".png";
                CsLog.logD("progress:" + progress);
                progressText.setText("" + progress);
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                    progressText.setVisibility(View.GONE);
                    // 下载完成
                    CsLog.logD("下载完成");
                    if (filePath.equals(downloadPath)) {
                        setChatPicInfo(filePath);
                        loadLocalImage(filePath);
                    }
                }
            }

        }
    }

}
