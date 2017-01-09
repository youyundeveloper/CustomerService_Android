package com.customerservice.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.NinePatchDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.customerservice.R;
import com.customerservice.chat.imagemask.CsMaskView;
import com.customerservice.chat.jsonmodel.CsActionMsgEntity;
import com.customerservice.chat.jsonmodel.CsCardMsgEntity;
import com.customerservice.chat.jsonmodel.CsJsonParentEntity;
import com.customerservice.chat.jsonmodel.CsLinkMsgEntity;
import com.customerservice.chat.jsonmodel.CsNoticeMsgEntity;
import com.customerservice.chat.jsonmodel.CsTextMsgEntity;
import com.customerservice.chat.model.CsChatEntity;
import com.customerservice.chat.model.CsFileEntity;
import com.customerservice.utils.CsAppUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 */

public class CsChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final int screenWidth1p3 = CsAppUtils.mScreenWidth / 3;
    public final int screenWidth1p4 = CsAppUtils.mScreenWidth / 4;

    private OnItemClickListener onItemClickListener;
    private List<CsChatEntity> chatList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;
    private SimpleDateFormat sdf;

    public CsChatAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
    }

    public void setChatList(List<CsChatEntity> list) {
        this.chatList.clear();
        chatList.addAll(list);
        this.notifyDataSetChanged();
    }

    public CsChatEntity getItem(int position){
        return chatList.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == CsChatEntity.CHAT_TYPE_PEOPLE_SEND_TEXT) {
            View view = inflater.inflate(R.layout.cs_layout_chat_text_people, parent, false);
            PeopleTextHolder viewHolder = new PeopleTextHolder(view);
            return viewHolder;
        } else if(viewType == CsChatEntity.CHAT_TYPE_ROBOT_TEXT){
            View mView = LayoutInflater.from(context).inflate(R.layout.cs_layout_chat_text_robot, parent, false);
            RobotTextHolder viewHolder = new RobotTextHolder(mView);
            return viewHolder;
        }else if(viewType == CsChatEntity.CHAT_TYPE_PEOPLE_SEND_IMAGE){
            View mView = LayoutInflater.from(context).inflate(R.layout.cs_layout_chat_image_people, parent, false);
            PeopleImageHolder viewHolder = new PeopleImageHolder(mView);
            return viewHolder;
        }else if(viewType == CsChatEntity.CHAT_TYPE_ROBOT_IMAGE){
            View mView = LayoutInflater.from(context).inflate(R.layout.cs_layout_chat_image_robot, parent, false);
            RobotImageHolder viewHolder = new RobotImageHolder(mView);
            return viewHolder;
        }else if(viewType == CsChatEntity.CHAT_TYPE_NOTICE){
            View mView = LayoutInflater.from(context).inflate(R.layout.cs_layout_chat_notice, parent, false);
            NoticeHolder noticeHolder = new NoticeHolder(mView);
            return noticeHolder;
        }
        return null;

    }

    private void showHead(final ImageView imageView, Object resource){
        Glide.with(context)
                .load(resource)
                .asBitmap()
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(new BitmapImageViewTarget(imageView){
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    private boolean isFirstNotCard = false; //
    private SpannableStringBuilder builder = new SpannableStringBuilder();
    private void spannable(CsJsonParentEntity entity){
        if (entity instanceof CsTextMsgEntity) {
            CsTextMsgEntity csTextMsgEntity = (CsTextMsgEntity) entity;
            int perLength = builder.length();
            builder.append(csTextMsgEntity.content);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#000000"));
            builder.setSpan(colorSpan, perLength, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append("\n");

            isFirstNotCard = true;
        } else if (entity instanceof CsLinkMsgEntity) {
            CsLinkMsgEntity linkMsgEntity = (CsLinkMsgEntity) entity;
            int perLength = builder.length();
            builder.append(linkMsgEntity.content);
            ClickableSpan clickableSpan = new CsClickableSpan(entity, context);
            builder.setSpan(clickableSpan, perLength, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append("\n");

            isFirstNotCard = true;
        } else if (entity instanceof CsActionMsgEntity) {
            CsActionMsgEntity csActionMsgEntity = (CsActionMsgEntity) entity;
            int perLength = builder.length();
            builder.append(csActionMsgEntity.content);
            ClickableSpan clickableSpan = new CsClickableSpan(entity, context);
            builder.setSpan(clickableSpan, perLength, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append("\n");

            isFirstNotCard = true;
        } else if (entity instanceof CsCardMsgEntity) {
            CsCardMsgEntity cardMsgEntity = (CsCardMsgEntity) entity;
            List<CsJsonParentEntity> list = cardMsgEntity.content;
            if(isFirstNotCard)
                builder.append("\n");
            for (int i = 0; i < list.size(); i++){
                spannable(list.get(i));
            }

        }

    }

    private void showDatas(CsChatEntity entity, RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof PeopleTextHolder) {
            final PeopleTextHolder peopleTextHolder = (PeopleTextHolder) holder;
            showHead(peopleTextHolder.avatarImage, CsAppUtils.headUrl);
            if (entity.isShowTime) {
                peopleTextHolder.dataText.setVisibility(View.VISIBLE);
                peopleTextHolder.dataText.setText(sdf.format(new Date(entity.time)));
            } else {
                peopleTextHolder.dataText.setVisibility(View.GONE);
            }
            CsTextMsgEntity csTextMsgEntity = (CsTextMsgEntity) entity.csJsonParentEntity;
            peopleTextHolder.contentText.setText(csTextMsgEntity.content);
        } else if (holder instanceof RobotTextHolder) {
            final RobotTextHolder robotTextHolder = (RobotTextHolder) holder;
            if(entity.headUrl != null && entity.headUrl.startsWith("http")) {
                showHead(robotTextHolder.avatarImage, entity.headUrl);
            } else
                showHead(robotTextHolder.avatarImage, R.mipmap.ic_launcher);
            if (entity.isShowTime) {
                robotTextHolder.dataText.setVisibility(View.VISIBLE);
                robotTextHolder.dataText.setText(sdf.format(new Date(entity.time)));
            } else {
                robotTextHolder.dataText.setVisibility(View.GONE);
            }

            isFirstNotCard = false;
            builder.clear();
            spannable(entity.csJsonParentEntity);
            if(builder.length() > 0)
                builder.delete(builder.toString().lastIndexOf("\n"), builder.length());
            robotTextHolder.contentText.setText(builder);
            robotTextHolder.contentText.setMovementMethod(LinkMovementMethod.getInstance());
        } else if (holder instanceof PeopleImageHolder) {
            final PeopleImageHolder peopleImageHolder = (PeopleImageHolder) holder;
            showHead(peopleImageHolder.avatarImage, CsAppUtils.headUrl);
            final CsFileEntity csFileEntity = entity.csFileEntity;
            Bitmap bitmap = BitmapFactory.decodeFile(csFileEntity.thumbnailPath);
            CsMaskView imgView = new CsMaskView(context, bitmap,
                    (NinePatchDrawable) context.getResources().getDrawable(R.drawable.cs_chat_img_right_mask),
                    screenWidth1p3, screenWidth1p3,
                    screenWidth1p4, screenWidth1p4);
            peopleImageHolder.imgParent.removeAllViews();
            peopleImageHolder.imgParent.addView(imgView);
            ViewGroup.LayoutParams layoutParams = imgView.getLayoutParams();
            layoutParams.height = imgView.getMaskViewSize() != null ?
                    imgView.getMaskViewSize().viewHeight : layoutParams.height;
            layoutParams.width = imgView.getMaskViewSize() != null ?
                    imgView.getMaskViewSize().viewWidth : layoutParams.width;

            if(entity.fileProgress == -1){
                peopleImageHolder.progressText.setVisibility(View.GONE);
            }else{
                peopleImageHolder.progressText.setVisibility(View.VISIBLE);
                peopleImageHolder.progressText.setText(String.valueOf(entity.fileProgress));
            }

            if (entity.isShowTime) {
                peopleImageHolder.dataText.setVisibility(View.VISIBLE);
                peopleImageHolder.dataText.setText(sdf.format(new Date(entity.time)));
            } else {
                peopleImageHolder.dataText.setVisibility(View.GONE);
            }

            peopleImageHolder.imageLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CsBitImageActivity.startActivity(context, csFileEntity, position);
                }
            });
        }else if(holder instanceof RobotImageHolder){
            final RobotImageHolder robotImageHolder = (RobotImageHolder) holder;
            if(entity.headUrl != null && entity.headUrl.startsWith("http")) {
                showHead(robotImageHolder.avatarImage, entity.headUrl);
            } else
                showHead(robotImageHolder.avatarImage, R.mipmap.ic_launcher);
            final CsFileEntity csFileEntity = entity.csFileEntity;
            Bitmap bitmap = BitmapFactory.decodeFile(csFileEntity.thumbnailPath);
            CsMaskView imgView = new CsMaskView(context, bitmap,
                    (NinePatchDrawable) context.getResources().getDrawable(R.drawable.cs_chat_img_left_mask),
                    screenWidth1p3, screenWidth1p3,
                    screenWidth1p4, screenWidth1p4);
            robotImageHolder.imgParent.removeAllViews();
            robotImageHolder.imgParent.addView(imgView);
            ViewGroup.LayoutParams layoutParams = imgView.getLayoutParams();
            layoutParams.height = imgView.getMaskViewSize() != null ?
                    imgView.getMaskViewSize().viewHeight : layoutParams.height;
            layoutParams.width = imgView.getMaskViewSize() != null ?
                    imgView.getMaskViewSize().viewWidth : layoutParams.width;

            if (entity.isShowTime) {
                robotImageHolder.dataText.setVisibility(View.VISIBLE);
                robotImageHolder.dataText.setText(sdf.format(new Date(entity.time)));
            } else {
                robotImageHolder.dataText.setVisibility(View.GONE);
            }

            robotImageHolder.imgParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CsBitImageActivity.startActivity(context, csFileEntity, position);
                }
            });
        }else if (holder instanceof NoticeHolder) {
            final NoticeHolder noticeHolder = (NoticeHolder) holder;
            CsNoticeMsgEntity noticeMsgEntity = (CsNoticeMsgEntity) entity.csJsonParentEntity;
            noticeHolder.noticeMsgText.setText(noticeMsgEntity.content);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final CsChatEntity entity = chatList.get(position);
        showDatas(entity, holder, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    int position = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.itemView, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    /**
     * Item点击事件
     * @param onItemClickListener
     *
     * @deprecated 这里貌似用不上了
     */
    public void setOnItemClickLitener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 通知消息
     */
    class NoticeHolder extends RecyclerView.ViewHolder {

        TextView noticeMsgText;

        public NoticeHolder(View itemView) {
            super(itemView);
            noticeMsgText = (TextView) itemView.findViewById(R.id.tv_notice_msg);

        }

    }

    /**
     * 接收图片消息
     */
    class RobotImageHolder extends RecyclerView.ViewHolder {

        TextView dataText;
        ImageView avatarImage;
        ViewGroup imgParent;

        public RobotImageHolder(View itemView) {
            super(itemView);
            dataText = (TextView) itemView.findViewById(R.id.tv_send_time);
            avatarImage = (ImageView) itemView.findViewById(R.id.iv_user_head);
            imgParent = (ViewGroup) itemView.findViewById(R.id.img_parent);
        }
    }

    /**
     * 发送图片消息
     */
    class PeopleImageHolder extends RecyclerView.ViewHolder {

        TextView dataText;
        ImageView avatarImage;
        ViewGroup imgParent;
        View imageLayout;
        TextView progressText;

        public PeopleImageHolder(View itemView) {
            super(itemView);
            dataText = (TextView) itemView.findViewById(R.id.tv_send_time);
            avatarImage = (ImageView) itemView.findViewById(R.id.iv_user_head);
            imgParent = (ViewGroup) itemView.findViewById(R.id.img_parent);
            imageLayout = itemView.findViewById(R.id.image_layout);
            progressText = (TextView) itemView.findViewById(R.id.tv_progress);
        }
    }

    /**
     * 提问的文本消息
     */
    class PeopleTextHolder extends RecyclerView.ViewHolder {

        TextView dataText;
        ImageView avatarImage;
        TextView contentText;

        public PeopleTextHolder(View itemView) {
            super(itemView);
            dataText = (TextView) itemView.findViewById(R.id.tv_send_time);
            avatarImage = (ImageView) itemView.findViewById(R.id.iv_user_head);
            contentText = (TextView) itemView.findViewById(R.id.tv_chat_content);
        }
    }

    /**
     * 机器人的文本消息
     */
    class RobotTextHolder extends RecyclerView.ViewHolder {

        TextView dataText;
        ImageView avatarImage;
        TextView contentText;

        public RobotTextHolder(View itemView) {
            super(itemView);
            dataText = (TextView) itemView.findViewById(R.id.tv_send_time);
            avatarImage = (ImageView) itemView.findViewById(R.id.iv_user_head);
            contentText = (TextView) itemView.findViewById(R.id.tv_chat_content);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).msgType;
    }
}
