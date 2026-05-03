package com.example.clockedinprojectt9.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clockedinprojectt9.R;
import com.example.clockedinprojectt9.models.ChatSummary;
import com.example.clockedinprojectt9.models.Event;
import com.example.clockedinprojectt9.models.User;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<ChatSummary> summaries = new ArrayList<>();
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onUserChatClick(User user);
        void onEventChatClick(Event event);
    }

    public ChatListAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_user, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatSummary summary = summaries.get(position);
        Object target = summary.getTarget();
        
        holder.lastMessageText.setText(summary.getLastMessage());

        if (target instanceof User) {
            User user = (User) target;
            holder.nameText.setText(user.getDisplayName());
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onUserChatClick(user);
            });
        } else if (target instanceof Event) {
            Event event = (Event) target;
            holder.nameText.setText(event.getTitle() + " (Group)");
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onEventChatClick(event);
            });
        }
    }

    @Override
    public int getItemCount() {
        return summaries.size();
    }

    public void setSummaries(List<ChatSummary> summaries) {
        this.summaries = summaries;
        notifyDataSetChanged();
    }

    // Keep these for compatibility if needed, but we'll use setSummaries
    public void setData(List<User> users, List<Event> events) {
    }

    public void setUsers(List<User> users) {
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView lastMessageText;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.chatUserName);
            lastMessageText = itemView.findViewById(R.id.chatLastMessage);
        }
    }
}
