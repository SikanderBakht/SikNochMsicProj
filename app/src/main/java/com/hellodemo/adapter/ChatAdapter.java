package com.hellodemo.adapter;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.hellodemo.ChatActivity;
import com.hellodemo.R;
import com.hellodemo.models.ChatScreen;
import com.hellodemo.models.Message;
import com.hellodemo.ui.MemphisTextView;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * Created by new user on 3/3/2018.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private String TAG = "HelloDemoChatAdapter";
    private static final int SELF_MESSAGE = 0;
    private static final int RECEIVED_MESSAGE = 1;
    static MemphisTextView prev_view;
    static MemphisTextView curr_view;
    static int click_count = 0;
    private ChatActivity mContext;
    private long mSelfUserId = 0;
    private ChatScreen mChatScreenModel;
    private boolean isGroup = false;

    public ChatAdapter(ChatActivity activity, long userId) {
        mContext = activity;
        mSelfUserId = userId;
    }

    /*public int getLatestReceivedMessageIndex() {
        int msgs_count = getItemCount() - 1;
        while(msgs_count >= 0 && getItemViewType(msgs_count) == RECEIVED_MESSAGE) {
            msgs_count --;
        }
        return msgs_count;
    }*/

    public int getLastSeenMessageIndex() {
        List<Message> messages = mChatScreenModel.getMessages();
        int msgs_index = messages.size() - 1;
        while(msgs_index >= 0) {
            if(getItemViewType(msgs_index) == SELF_MESSAGE && messages.get(msgs_index).getStatus() == 1) {
                return msgs_index;
            }
            msgs_index--;
        }
        return msgs_index;
    }
    
    @Override
    public int getItemViewType(int position) {
        if (mChatScreenModel.getMessages().get(position).getUserId() == mSelfUserId) {
            return SELF_MESSAGE;
        } else
            return RECEIVED_MESSAGE;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case RECEIVED_MESSAGE: {
                View view = LayoutInflater.from(mContext).inflate(R.layout.item_received_chat_message, parent, false);
                return new MessageViewHolder(view);
            }
            default: {
                View view = LayoutInflater.from(mContext).inflate(R.layout.item_self_chat_message, parent, false);
                return new MessageViewHolder(view);
            }
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        holder.bindView(position);
    }


    @Override
    public int getItemCount() {
        if (mChatScreenModel == null || mChatScreenModel.getMessages() == null || mChatScreenModel.getMessages().size() == 0)
            return 0;
        else
            return mChatScreenModel.getMessages().size();
    }



    public void setData(ChatScreen mChatScreenModel, boolean isGroup) {
        this.mChatScreenModel = mChatScreenModel;
        this.isGroup = isGroup;
        notifyDataSetChanged();
    }

    public void appendMessageToData(Message message) {
        mChatScreenModel.getMessages().add(message);
        notifyItemInserted(mChatScreenModel.getMessages().size() - 1);
        //notifyDataSetChanged();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
//        private final View itemView;
        private final TextView message_text;
        private final TextView message_timestamp;
        private final ImageView avatar_image;
        private final ImageView msg_seen;

        //private int latestSelfMessageIndex;
        private int lastSeenMessageIndex;



        MessageViewHolder(final View itemView) {
            super(itemView);
//            this.itemView = itemView;
            message_text = itemView.findViewById(R.id.message_text);
            message_timestamp = itemView.findViewById(R.id.timestamp_textview);
            avatar_image = itemView.findViewById(R.id.avatar_image);
            msg_seen = itemView.findViewById(R.id.msg_seen);

//            message_timestamp.setVisibility(View.INVISIBLE);

            message_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    click_count++;
                    if(click_count == 1) {
//                        curr_view = (MemphisTextView) view;
                        curr_view = (MemphisTextView) message_timestamp;
                    } else {
                        prev_view = curr_view;
                        curr_view = (MemphisTextView) message_timestamp;
                    }

                    if(message_timestamp.getVisibility() == View.VISIBLE) {
                        notifyDataSetChanged();
                    } else {
                        if(click_count % 2 == 1){
                            if(prev_view != null){
                                prev_view.setVisibility(View.INVISIBLE);
                            }
                            message_timestamp.setVisibility(View.VISIBLE);
                        } else {
                            if(prev_view.getVisibility() == View.VISIBLE) {
                                prev_view.setVisibility(View.INVISIBLE);
                            }
                            message_timestamp.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }

        void bindView(int position) {
            final Message message = mChatScreenModel.getMessages().get(position);
            //latestSelfMessageIndex = getLatestReceivedMessageIndex();
            lastSeenMessageIndex = getLastSeenMessageIndex();
            if (isGroup) {
                Picasso.with(mContext).load(message.getUser_avatar()).into(avatar_image);
            } else {
                if (getItemViewType() == SELF_MESSAGE) {
                    Picasso.with(mContext).load(mChatScreenModel.getSelfAvatar()).into(avatar_image);
                } else {
                    Picasso.with(mContext).load(mChatScreenModel.getUserAvatar()).into(avatar_image);
                }
            }

            if (getItemViewType() == SELF_MESSAGE) {

                View errorView = itemView.findViewById(R.id.error_view);
                View messageView = itemView.findViewById(R.id.message_view);

                if(message.getStatus() == 1 && position == lastSeenMessageIndex) {
                    msg_seen.setVisibility(View.VISIBLE);
                }
                else {
                    msg_seen.setVisibility(View.INVISIBLE);
                }

                messageView.setAlpha(1);
                errorView.setVisibility(View.GONE);

                switch (message.getDeliveryState()) {
                    case Message.MESSAGE_DELIVERY_SUCCESS:
                        break;
                    case Message.MESSAGE_DELIVERY_FAILED:
                        messageView.setAlpha(0.5f);
                        errorView.setVisibility(View.VISIBLE);
                        itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // first we need to remove the current message from the view...
                                mChatScreenModel.getMessages().remove(message);
                                notifyDataSetChanged();

                                // we then have to add new view and send message again...
                                mContext.sendMessage(message.getMessage());
                            }
                        });
                        break;
                    case Message.MESSAGE_DELIVERY_PENDING:
                        break;
                }
            }
            message_text.setText(message.getMessage());
            message_timestamp.setText(message.getDayDate());
            message_timestamp.setVisibility(View.INVISIBLE);
        }
    }
}