package com.customerservice.chat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.customerservice.R;
import com.customerservice.chat.model.CsChatEntity;
import com.customerservice.utils.CsAppUtils;
import com.ioyouyun.wchat.WeimiInstance;

import java.io.File;
import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 */

public class CsChatActivity extends Activity implements CsChatView, View.OnClickListener {

    public static final int REQUEST_CODE_CAMERA = 1001;
    public static final int REQUEST_CODE_LOCAL = 1002;

    private InputMethodManager manager;

    private View backBtn;
    private TextView titleText;
    private View photoGalleryBtn; // 相册
    private View takePhotoBtn; // 拍照
    private EditText chatMsgEdit;
    private Button sendBtn;
    private Button moreBtn;
    private View moreLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private CsChatAdapter csChatAdapter;

    private CsChatPresenter presenter;
    private boolean isShowMore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cs_activity_chat);
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        initView();
        initData();
        addListener();

        connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        back();
    }

    @Override
    public void onBackPressed() {
        back();
        super.onBackPressed();
    }

    private void back() {
        disconnect();
        presenter.onDestroy();
    }

    /**
     * 进入房间连接客服
     */
    private void connect() {
        // 取消设置不sycn客服消息
        WeimiInstance.getInstance().cancleShieldSyncUserId(CsAppUtils.CUSTOM_SERVICE_ID);
        presenter.sendMixedText(1);
    }

    /**
     * 离开房间调用告诉客服已断开
     */
    private void disconnect() {
        presenter.sendMixedText(2);
        // 设置不sycn客服消息
        WeimiInstance.getInstance().shieldSyncUserId(CsAppUtils.CUSTOM_SERVICE_ID);
    }

    /**
     * 隐藏软件盘
     */
    private void hideSoftInput(View view) {
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void startActivity(Activity activity){
        Intent intent = new Intent(activity, CsChatActivity.class);
        activity.startActivity(intent);
    }

    private void initData() {
        titleText.setText(getResources().getString(R.string.cs_online_service));

        presenter = new CsChatPresenter(this, this);

        recyclerView.setMotionEventSplittingEnabled(false);
        recyclerView.setLayoutManager(linearLayoutManager = new LinearLayoutManager(this));
        csChatAdapter = new CsChatAdapter(this);
        recyclerView.setAdapter(csChatAdapter);
//        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimaryDark)
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.getHistory();
            }
        });
    }

    private void addListener() {
        chatMsgEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    moreBtn.setVisibility(View.GONE);
                    sendBtn.setVisibility(View.VISIBLE);
                } else {
                    moreBtn.setVisibility(View.VISIBLE);
                    sendBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        chatMsgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowMore) {
                    isShowMore = false;
                    moreLayout.setVisibility(View.GONE);
                }
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                closeBoard();
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
        moreBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        takePhotoBtn.setOnClickListener(this);
        photoGalleryBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
    }

    private void closeBoard() {
        if (isSoftInputShown()) {
            hideSoftInput(chatMsgEdit);
        }
        if (isShowMore) {
            isShowMore = false;
            moreLayout.setVisibility(View.GONE);
        }
    }

    private void initView() {
        titleText = (TextView) findViewById(R.id.tv_title);
        backBtn = findViewById(R.id.iv_left);
        chatMsgEdit = (EditText) findViewById(R.id.et_send);
        sendBtn = (Button) findViewById(R.id.btn_send);
        moreBtn = (Button) findViewById(R.id.btn_more);
        moreLayout = findViewById(R.id.ll_more);
        recyclerView = (RecyclerView) findViewById(R.id.rv_chat);
        photoGalleryBtn = findViewById(R.id.tv_picture);
        takePhotoBtn = findViewById(R.id.tv_take_photo);
    }

    /**
     * 相册
     */
    private void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }

    /**
     * 拍照
     */
    private File cameraFile;
    private void selectPicFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        long ts = System.currentTimeMillis();
        String mImagePath = CsAppUtils.getCameraPath() + ts + ".jpg";
        cameraFile = new File(mImagePath);
        Uri uri = Uri.fromFile(cameraFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    /**
     * 选择相册图片后处理
     *
     * @param uri
     */
    private String sendLocalImage(Uri uri) {
        String picturePath = "";
        if (uri != null) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                picturePath = cursor.getString(columnIndex);
                cursor.close();
                cursor = null;
            } else {
                File file = new File(uri.getPath());
                if (file.exists()) {
                    picturePath = file.getAbsolutePath();
                }
            }
            if (picturePath == null || picturePath.equals("null") || "".equals(picturePath)) {
                CsAppUtils.toastMessage("找不到图片");
                return null;
            }
        }
        return picturePath;
    }

    private void sendImage(String filePath) {
        presenter.sendImage(filePath, filePath.substring(filePath.lastIndexOf("/") + 1));
    }

    @Override
    public void onClick(View view) {
        if (view == moreBtn) {
            if (isShowMore) {
                isShowMore = false;
                moreLayout.setVisibility(View.GONE);
            } else {
                if (isSoftInputShown()) {
                    hideSoftInput(chatMsgEdit);
                }
                isShowMore = true;
                moreLayout.setVisibility(View.VISIBLE);
            }
        } else if (view == sendBtn) {
            String text = chatMsgEdit.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                presenter.sendText(text);
            }
        } else if (view == photoGalleryBtn) {
            selectPicFromLocal();
        } else if (view == takePhotoBtn) {
            CsAppUtils.requestPermission(this, REQUEST_CODE_CAMERA, callback, Manifest.permission.CAMERA);
        } else if (view == backBtn) {
            onBackPressed();
        }
    }

    CsAppUtils.PermissionCallback callback = new CsAppUtils.PermissionCallback() {
        @Override
        public void onComplete(int requestCode) {
            if(REQUEST_CODE_CAMERA == requestCode){
                selectPicFromCamera();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_CAMERA){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectPicFromCamera();
            } else{
                CsAppUtils.toastMessage("Permission Denied");
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                if (cameraFile != null && cameraFile.exists()) {
                    String path = cameraFile.getAbsolutePath();
                    sendImage(path);
                }
            } else if (requestCode == REQUEST_CODE_LOCAL) {
                if (data != null) {
                    String path = sendLocalImage(data.getData());
                    if (!TextUtils.isEmpty(path))
                        sendImage(path);
                }
            }
        }
    }

    @Override
    public void refreshList(List<CsChatEntity> list) {
        csChatAdapter.setChatList(list);
    }

    @Override
    public void scrollToPosition(int position) {
        linearLayoutManager.scrollToPositionWithOffset(position, 0);
    }

    @Override
    public void clearInputMsg() {
        chatMsgEdit.setText("");
    }

    @Override
    public void onCompleteLoad() {
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * 是否显示软件盘
     *
     * @return
     */
    private boolean isSoftInputShown() {
        return getSupportSoftInputHeight() != 0;
    }

    /**
     * 获取软件盘的高度
     *
     * @return
     */
    private int getSupportSoftInputHeight() {
        Rect r = new Rect();
        /**
         * decorView是window中的最顶层view，可以从window中通过getDecorView获取到decorView。
         * 通过decorView获取到程序显示的区域，包括标题栏，但不包括状态栏。
         */
        getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        //获取屏幕的高度
        int screenHeight = getWindow().getDecorView().getRootView().getHeight();
        //计算软件盘的高度
        int softInputHeight = screenHeight - r.bottom;

        /**
         * 某些Android版本下，没有显示软键盘时减出来的高度总是144，而不是零，
         * 这是因为高度是包括了虚拟按键栏的(例如华为系列)，所以在API Level高于20时，
         * 我们需要减去底部虚拟按键栏的高度（如果有的话）
         */
        if (Build.VERSION.SDK_INT >= 20) {
            // When SDK Level >= 20 (Android L), the softInputHeight will contain the height of softButtonsBar (if has)
            softInputHeight = softInputHeight - getSoftButtonsBarHeight();
        }
        return softInputHeight;
    }

    /**
     * 底部虚拟按键栏的高度
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int getSoftButtonsBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        //这个方法获取可能不是真实屏幕的高度
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        //获取当前屏幕的真实高度
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

}
