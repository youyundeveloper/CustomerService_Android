package com.customerservice.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.customerservice.AppUtils;
import com.customerservice.R;
import com.customerservice.TimeUtil;
import com.customerservice.chat.jsonmodel.ActionMsgEntity;
import com.customerservice.chat.jsonmodel.CardMsgEntity;
import com.customerservice.chat.jsonmodel.ChatMsgEntity;
import com.customerservice.chat.jsonmodel.LinkMsgEntity;
import com.customerservice.chat.jsonmodel.NoticeMsgEntity;
import com.customerservice.chat.jsonmodel.TextMsgEntity;
import com.customerservice.chat.model.FileEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bill on 2016/12/8.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private OnItemClickListener onItemClickListener;
    private List<ChatMsgEntity> chatList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;
    private SimpleDateFormat sdf;

    public ChatAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public void setChatList(List<ChatMsgEntity> list) {
        this.chatList.clear();
        chatList.addAll(list);
        this.notifyDataSetChanged();
    }

    public ChatMsgEntity getItem(int position){
        return chatList.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChatMsgEntity.CHAT_TYPE_PEOPLE_SEND_TEXT) {
            View view = inflater.inflate(R.layout.layout_chat_text_people, parent, false);
            PeopleTextHolder viewHolder = new PeopleTextHolder(view);
            return viewHolder;
        } else if(viewType == ChatMsgEntity.CHAT_TYPE_ROBOT_TEXT){
            View mView = LayoutInflater.from(context).inflate(R.layout.layout_chat_text_robot, parent, false);
            RobotTextHolder viewHolder = new RobotTextHolder(mView);
            return viewHolder;
        }else if(viewType == ChatMsgEntity.CHAT_TYPE_PEOPLE_SEND_IMAGE){
            View mView = LayoutInflater.from(context).inflate(R.layout.layout_chat_image_people, parent, false);
            PeopleImageHolder viewHolder = new PeopleImageHolder(mView);
            return viewHolder;
        }else if(viewType == ChatMsgEntity.CHAT_TYPE_ROBOT_IMAGE){
            View mView = LayoutInflater.from(context).inflate(R.layout.layout_chat_image_robot, parent, false);
            PeopleImageHolder viewHolder = new PeopleImageHolder(mView);
            return viewHolder;
        }else if(viewType == ChatMsgEntity.CHAT_TYPE_NOTICE){
            View mView = LayoutInflater.from(context).inflate(R.layout.layout_chat_notice, parent, false);
            NoticeHolder noticeHolder = new NoticeHolder(mView);
            return noticeHolder;
        }
        return null;

    }

    private void text(ChatMsgEntity entity, String text, TextView textView){
        SpannableString textString = new SpannableString(text);
        ClickableSpan textSpan = new MyClickableSpan(entity, context);
        textString.setSpan(textSpan, 0, textString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.append(textString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private boolean isFirstNotCard = false; //
    private void showText(ChatMsgEntity entity, RobotTextHolder robotTextHolder){
        if (entity instanceof TextMsgEntity) {
            TextMsgEntity textMsgEntity = (TextMsgEntity) entity;
            text(textMsgEntity, textMsgEntity.content, robotTextHolder.contentText);
            isFirstNotCard = true;
        } else if (entity instanceof LinkMsgEntity) {
            LinkMsgEntity linkMsgEntity = (LinkMsgEntity) entity;
            text(linkMsgEntity, linkMsgEntity.content, robotTextHolder.contentText);
            isFirstNotCard = true;
        } else if (entity instanceof ActionMsgEntity) {
            ActionMsgEntity actionMsgEntity = (ActionMsgEntity) entity;
            text(actionMsgEntity, actionMsgEntity.content, robotTextHolder.contentText);
            isFirstNotCard = true;
        } else if (entity instanceof CardMsgEntity) {
            CardMsgEntity cardMsgEntity = (CardMsgEntity) entity;
            List<ChatMsgEntity> list = cardMsgEntity.content;
            if(isFirstNotCard)
                robotTextHolder.contentText.append("\n");
            for (int i = 0; i < list.size(); i++){
                showText(list.get(i), robotTextHolder);

                if(i < list.size() - 1){
                    robotTextHolder.contentText.append("\n");
                }
            }

        }
    }

    private void showDatas(ChatMsgEntity entity, RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof PeopleTextHolder) {
            final PeopleTextHolder peopleTextHolder = (PeopleTextHolder) holder;
//            Glide.with(context).load(R.drawable.sd).asGif().into(peopleTextHolder.avatarImage);
//            Glide.with(context).load(R.drawable.you).into(peopleTextHolder.avatarImage);
            Glide.with(context)
                    .load(R.drawable.you)
                    .asBitmap()
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(new BitmapImageViewTarget(peopleTextHolder.avatarImage){
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            peopleTextHolder.avatarImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
            /*String nickName = AppUtils.uid;
            String end = nickName.substring(nickName.length() - 1, nickName.length());
            TextDrawable drawable = TextDrawable.builder().buildRound(AppUtils.uid, Color.parseColor("#cb5372"));
            peopleTextHolder.avatarImage.setImageDrawable(drawable);*/
//            peopleTextHolder.dataText.setText(sdf.format(new Date(entity.time)));
            if (entity.isShowTime) {
                peopleTextHolder.dataText.setVisibility(View.VISIBLE);
                peopleTextHolder.dataText.setText(TimeUtil.format2(entity.time));
            } else {
                peopleTextHolder.dataText.setVisibility(View.GONE);
            }
            TextMsgEntity textMsgEntity = (TextMsgEntity) entity;
            peopleTextHolder.contentText.setText(textMsgEntity.content);
        } else if (holder instanceof RobotTextHolder) {
            final RobotTextHolder robotTextHolder = (RobotTextHolder) holder;
            if(entity.headUrl != null && entity.headUrl.startsWith("http")) {
                Glide.with(context)
                        .load(entity.headUrl)
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(new BitmapImageViewTarget(robotTextHolder.avatarImage){
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                robotTextHolder.avatarImage.setImageDrawable(circularBitmapDrawable);
                            }
                        });
            } else
                Glide.with(context).load(R.mipmap.ic_launcher).into(robotTextHolder.avatarImage);
//            robotTextHolder.dataText.setText(sdf.format(new Date(entity.time)));
            if (entity.isShowTime) {
                robotTextHolder.dataText.setVisibility(View.VISIBLE);
                robotTextHolder.dataText.setText(TimeUtil.format2(entity.time));
            } else {
                robotTextHolder.dataText.setVisibility(View.GONE);
            }

            robotTextHolder.contentText.setText("");
            isFirstNotCard = false;
            showText(entity, robotTextHolder);
        } else if (holder instanceof PeopleImageHolder) {
            final PeopleImageHolder peopleImageHolder = (PeopleImageHolder) holder;
            if(entity.msgType == ChatMsgEntity.CHAT_TYPE_ROBOT_IMAGE){
                if(entity.headUrl != null && entity.headUrl.startsWith("http")){
                    Glide.with(context)
                            .load(entity.headUrl)
                            .asBitmap()
                            .centerCrop()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .into(new BitmapImageViewTarget(peopleImageHolder.avatarImage){
                                @Override
                                protected void setResource(Bitmap resource) {
                                    RoundedBitmapDrawable circularBitmapDrawable =
                                            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                    circularBitmapDrawable.setCircular(true);
                                    peopleImageHolder.avatarImage.setImageDrawable(circularBitmapDrawable);
                                }
                            });
                } else
                    Glide.with(context).load(R.mipmap.ic_launcher).into(peopleImageHolder.avatarImage);
            }else{
                Glide.with(context)
                        .load(R.drawable.xjs)
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .into(new BitmapImageViewTarget(peopleImageHolder.avatarImage){
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                peopleImageHolder.avatarImage.setImageDrawable(circularBitmapDrawable);
                            }
                        });
            }
//            peopleImageHolder.dataText.setText(sdf.format(new Date(entity.time)));
            if (entity.isShowTime) {
                peopleImageHolder.dataText.setVisibility(View.VISIBLE);
                peopleImageHolder.dataText.setText(TimeUtil.format2(entity.time));
            } else {
                peopleImageHolder.dataText.setVisibility(View.GONE);
            }
            final FileEntity fileEntity = (FileEntity) entity;
            Bitmap bitmap = BitmapFactory.decodeFile(fileEntity.thumbnailPath);
            peopleImageHolder.contentImage.setImageBitmap(bitmap);

            peopleImageHolder.contentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BitImageActivity.startActivity(context, fileEntity, position);
                }
            });
        }else if (holder instanceof NoticeHolder) {
            final NoticeHolder noticeHolder = (NoticeHolder) holder;
            NoticeMsgEntity noticeMsgEntity = (NoticeMsgEntity) entity;
            noticeHolder.noticeMsgText.setText(noticeMsgEntity.content);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ChatMsgEntity entity = chatList.get(position);
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
        ImageView contentImage;

        public PeopleImageHolder(View itemView) {
            super(itemView);
            dataText = (TextView) itemView.findViewById(R.id.tv_send_time);
            avatarImage = (ImageView) itemView.findViewById(R.id.iv_user_head);
            contentImage = (ImageView) itemView.findViewById(R.id.iv_chat_content);
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
