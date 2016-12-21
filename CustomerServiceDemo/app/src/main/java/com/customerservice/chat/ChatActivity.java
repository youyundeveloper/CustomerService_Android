package com.customerservice.chat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.customerservice.AppUtils;
import com.customerservice.R;
import com.customerservice.chat.jsonmodel.ChatMsgEntity;

import java.io.File;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements ChatView, View.OnClickListener {

    public static final int REQUEST_CODE_CAMERA = 1001;
    public static final int REQUEST_CODE_LOCAL = 1002;

    private InputMethodManager manager;

    private View photoGalleryBtn; // 相册
    private View takePhotoBtn; // 拍照
    private EditText chatMsgEdit;
    private Button sendBtn;
    private Button moreBtn;
    private View moreLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ChatAdapter chatAdapter;

    private ChatPresenter presenter;
    private boolean isShowMore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        setToolBar();
        initView();
        initData();
        addListener();

        connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_id_user_id) {
            AppUtils.toastMessage(AppUtils.uid);
            return true;
        } else if (id == R.id.menu_id_connect_service) {
            connect();
            return true;
        } else if (id == R.id.menu_id_disconnect_service) {
            disconnect();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void connect(){
        presenter.sendMixedText(1);
    }

    private void disconnect(){
        presenter.sendMixedText(2);
    }

    /**
     * 隐藏软件盘
     */
    private void hideSoftInput(View view) {
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void initData() {
        presenter = new ChatPresenter(this, this);

        recyclerView.setMotionEventSplittingEnabled(false);
        recyclerView.setLayoutManager(linearLayoutManager = new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this);
        recyclerView.setAdapter(chatAdapter);
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
        chatMsgEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isShowMore) {
                    isShowMore = false;
                    moreLayout.setVisibility(View.GONE);
                }
                return false;
            }
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isShowMore) {
                    isShowMore = false;
                    moreLayout.setVisibility(View.GONE);
                }
                return false;
            }
        });
        moreBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        takePhotoBtn.setOnClickListener(this);
        photoGalleryBtn.setOnClickListener(this);
    }

    private void initView() {
        chatMsgEdit = (EditText) findViewById(R.id.et_send);
        sendBtn = (Button) findViewById(R.id.btn_send);
        moreBtn = (Button) findViewById(R.id.btn_more);
        moreLayout = findViewById(R.id.ll_more);
        recyclerView = (RecyclerView) findViewById(R.id.rv_chat);
        photoGalleryBtn = findViewById(R.id.tv_picture);
        takePhotoBtn = findViewById(R.id.tv_take_photo);
    }

    protected void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
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
        String mImagePath = AppUtils.getCameraPath() + ts + ".jpg";
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
                AppUtils.toastMessage("找不到图片");
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
                hideSoftInput(chatMsgEdit);
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
            selectPicFromCamera();
        }
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
    public void refreshList(List<ChatMsgEntity> list) {
        chatAdapter.setChatList(list);
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
}
