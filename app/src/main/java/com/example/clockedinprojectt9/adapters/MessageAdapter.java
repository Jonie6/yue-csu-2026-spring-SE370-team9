package com.example.clockedinprojectt9.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clockedinprojectt9.R;
import com.example.clockedinprojectt9.models.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<Message> messages = new ArrayList<>();
    private final long currentUserId;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private boolean isGroupChat = false;
    private java.util.Map<Long, String> userNames = new java.util.HashMap<>();

    public MessageAdapter(long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setGroupChat(boolean groupChat) {
        isGroupChat = groupChat;
    }

    public void setUserNames(java.util.Map<Long, String> userNames) {
        this.userNames = userNames;
        notifyDataSetChanged();
    }

    // Determines if message is type_sent or type_recieved by comparing userId
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getSenderId() == currentUserId) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    // Picks UI element based on type
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    // Bind message data to UI
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        String timeString = timeFormat.format(new Date(message.getTimestamp()));

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message, timeString);
        } else {
            String senderName = userNames.containsKey(message.getSenderId()) ? userNames.get(message.getSenderId()) : "User " + message.getSenderId();
            ((ReceivedMessageViewHolder) holder).bind(message, timeString, isGroupChat, senderName);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView contentText;
        private final TextView timestampText;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            contentText = itemView.findViewById(R.id.textMessageContent);
            timestampText = itemView.findViewById(R.id.textMessageTimestamp);
        }

        public void bind(Message message, String time) {
            contentText.setText(message.getContent());
            timestampText.setText(time);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView contentText;
        private final TextView timestampText;
        private final TextView senderNameText;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            contentText = itemView.findViewById(R.id.textMessageContent);
            timestampText = itemView.findViewById(R.id.textMessageTimestamp);
            senderNameText = itemView.findViewById(R.id.textMessageSender);
        }

        public void bind(Message message, String time, boolean isGroup, String senderName) {
            contentText.setText(message.getContent());
            timestampText.setText(time);
            if (isGroup && senderNameText != null) {
                senderNameText.setVisibility(View.VISIBLE);
                senderNameText.setText(senderName);
            } else if (senderNameText != null) {
                senderNameText.setVisibility(View.GONE);
            }
        }
    }
}
