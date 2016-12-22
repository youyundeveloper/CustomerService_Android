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
import com.customerservice.chat.model.ChatEntity;
import com.customerservice.utils.AppUtils;
import com.customerservice.R;
import com.customerservice.chat.imagemask.MaskView;
import com.customerservice.chat.jsonmodel.ActionMsgEntity;
import com.customerservice.chat.jsonmodel.CardMsgEntity;
import com.customerservice.chat.jsonmodel.JsonParentEntity;
import com.customerservice.chat.jsonmodel.LinkMsgEntity;
import com.customerservice.chat.jsonmodel.NoticeMsgEntity;
import com.customerservice.chat.jsonmodel.TextMsgEntity;
import com.customerservice.chat.model.FileEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final int screenWidth1p3 = AppUtils.mScreenWidth / 3;
    public final int screenWidth1p4 = AppUtils.mScreenWidth / 4;

    private OnItemClickListener onItemClickListener;
    private List<ChatEntity> chatList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;
    private SimpleDateFormat sdf;

    public ChatAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
    }

    public void setChatList(List<ChatEntity> list) {
        this.chatList.clear();
        chatList.addAll(list);
        this.notifyDataSetChanged();
    }

    public ChatEntity getItem(int position){
        return chatList.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChatEntity.CHAT_TYPE_PEOPLE_SEND_TEXT) {
            View view = inflater.inflate(R.layout.layout_chat_text_people, parent, false);
            PeopleTextHolder viewHolder = new PeopleTextHolder(view);
            return viewHolder;
        } else if(viewType == ChatEntity.CHAT_TYPE_ROBOT_TEXT){
            View mView = LayoutInflater.from(context).inflate(R.layout.layout_chat_text_robot, parent, false);
            RobotTextHolder viewHolder = new RobotTextHolder(mView);
            return viewHolder;
        }else if(viewType == ChatEntity.CHAT_TYPE_PEOPLE_SEND_IMAGE){
            View mView = LayoutInflater.from(context).inflate(R.layout.layout_chat_image_people, parent, false);
            PeopleImageHolder viewHolder = new PeopleImageHolder(mView);
            return viewHolder;
        }else if(viewType == ChatEntity.CHAT_TYPE_ROBOT_IMAGE){
            View mView = LayoutInflater.from(context).inflate(R.layout.layout_chat_image_robot, parent, false);
            PeopleImageHolder viewHolder = new PeopleImageHolder(mView);
            return viewHolder;
        }else if(viewType == ChatEntity.CHAT_TYPE_NOTICE){
            View mView = LayoutInflater.from(context).inflate(R.layout.layout_chat_notice, parent, false);
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
    private void spannable(JsonParentEntity entity){
        if (entity instanceof TextMsgEntity) {
            TextMsgEntity textMsgEntity = (TextMsgEntity) entity;
            int perLength = builder.length();
            builder.append(textMsgEntity.content);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#000000"));
            builder.setSpan(colorSpan, perLength, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append("\n");

            isFirstNotCard = true;
        } else if (entity instanceof LinkMsgEntity) {
            LinkMsgEntity linkMsgEntity = (LinkMsgEntity) entity;
            int perLength = builder.length();
            builder.append(linkMsgEntity.content);
            ClickableSpan clickableSpan = new MyClickableSpan(entity, context);
            builder.setSpan(clickableSpan, perLength, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append("\n");

            isFirstNotCard = true;
        } else if (entity instanceof ActionMsgEntity) {
            ActionMsgEntity actionMsgEntity = (ActionMsgEntity) entity;
            int perLength = builder.length();
            builder.append(actionMsgEntity.content);
            ClickableSpan clickableSpan = new MyClickableSpan(entity, context);
            builder.setSpan(clickableSpan, perLength, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            builder.append("\n");

            isFirstNotCard = true;
        } else if (entity instanceof CardMsgEntity) {
            CardMsgEntity cardMsgEntity = (CardMsgEntity) entity;
            List<JsonParentEntity> list = cardMsgEntity.content;
            if(isFirstNotCard)
                builder.append("\n");
            for (int i = 0; i < list.size(); i++){
                spannable(list.get(i));
            }

        }

    }

    private void showDatas(ChatEntity entity, RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof PeopleTextHolder) {
            final PeopleTextHolder peopleTextHolder = (PeopleTextHolder) holder;
            showHead(peopleTextHolder.avatarImage, R.drawable.you);
            if (entity.isShowTime) {
                peopleTextHolder.dataText.setVisibility(View.VISIBLE);
                peopleTextHolder.dataText.setText(sdf.format(new Date(entity.time)));
            } else {
                peopleTextHolder.dataText.setVisibility(View.GONE);
            }
            TextMsgEntity textMsgEntity = (TextMsgEntity) entity.jsonParentEntity;
            peopleTextHolder.contentText.setText(textMsgEntity.content);
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
            spannable(entity.jsonParentEntity);
            if(builder.length() > 0)
                builder.delete(builder.toString().lastIndexOf("\n"), builder.length());
            robotTextHolder.contentText.setText(builder);
            robotTextHolder.contentText.setMovementMethod(LinkMovementMethod.getInstance());
        } else if (holder instanceof PeopleImageHolder) {
            final PeopleImageHolder peopleImageHolder = (PeopleImageHolder) holder;
            NinePatchDrawable ninePatchDrawable;
            if(entity.msgType == ChatEntity.CHAT_TYPE_ROBOT_IMAGE){
                if(entity.headUrl != null && entity.headUrl.startsWith("http")) {
                    showHead(peopleImageHolder.avatarImage, entity.headUrl);
                } else
                    showHead(peopleImageHolder.avatarImage, R.mipmap.ic_launcher);
                ninePatchDrawable = (NinePatchDrawable) context.getResources().getDrawable(R.drawable.chat_img_left_mask);
            }else{
                showHead(peopleImageHolder.avatarImage, R.drawable.you);
                ninePatchDrawable = (NinePatchDrawable) context.getResources().getDrawable(R.drawable.chat_img_right_mask);
            }
            final FileEntity fileEntity = entity.fileEntity;
            Bitmap bitmap = BitmapFactory.decodeFile(fileEntity.thumbnailPath);
            MaskView imgView = new MaskView(context, bitmap, ninePatchDrawable,
                    screenWidth1p3, screenWidth1p3,
                    screenWidth1p4, screenWidth1p4);
            peopleImageHolder.imgParent.removeAllViews();
            peopleImageHolder.imgParent.addView(imgView);
            ViewGroup.LayoutParams layoutParams = imgView.getLayoutParams();
            layoutParams.height = imgView.getMaskViewSize() != null ?
                    imgView.getMaskViewSize().viewHeight : layoutParams.height;
            layoutParams.width = imgView.getMaskViewSize() != null ?
                    imgView.getMaskViewSize().viewWidth : layoutParams.width;

            if (entity.isShowTime) {
                peopleImageHolder.dataText.setVisibility(View.VISIBLE);
                peopleImageHolder.dataText.setText(sdf.format(new Date(entity.time)));
            } else {
                peopleImageHolder.dataText.setVisibility(View.GONE);
            }
            peopleImageHolder.imgParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BitImageActivity.startActivity(context, fileEntity, position);
                }
            });
        }else if (holder instanceof NoticeHolder) {
            final NoticeHolder noticeHolder = (NoticeHolder) holder;
            NoticeMsgEntity noticeMsgEntity = (NoticeMsgEntity) entity.jsonParentEntity;
            noticeHolder.noticeMsgText.setText(noticeMsgEntity.content);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ChatEntity entity = chatList.get(position);
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
     * 图片消息
     */
    class PeopleImageHolder extends RecyclerView.ViewHolder {

        TextView dataText;
        ImageView avatarImage;
        ViewGroup imgParent;

        public PeopleImageHolder(View itemView) {
            super(itemView);
            dataText = (TextView) itemView.findViewById(R.id.tv_send_time);
            avatarImage = (ImageView) itemView.findViewById(R.id.iv_user_head);
            imgParent = (ViewGroup) itemView.findViewById(R.id.img_parent);
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
