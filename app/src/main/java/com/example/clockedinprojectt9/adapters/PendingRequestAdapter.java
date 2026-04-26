package com.example.clockedinprojectt9.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clockedinprojectt9.R;
import com.example.clockedinprojectt9.models.User;

import java.util.ArrayList;
import java.util.List;

public class PendingRequestAdapter extends RecyclerView.Adapter<PendingRequestAdapter.PendingViewHolder> {

    private List<User> requestUsers = new ArrayList<>();
    private OnRequestListener listener;

    public interface OnRequestListener {
        void onAccept(User sender);
        void onDecline(User sender);
    }

    public PendingRequestAdapter(OnRequestListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pending_request, parent, false);
        return new PendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingViewHolder holder, int position) {
        User sender = requestUsers.get(position);
        holder.pendingText.setText("Request from: " + sender.getUsername());
        
        holder.acceptBtn.setOnClickListener(v -> listener.onAccept(sender));
        holder.declineBtn.setOnClickListener(v -> listener.onDecline(sender));
    }

    @Override
    public int getItemCount() {
        return requestUsers.size();
    }

    public void setRequests(List<User> users) {
        this.requestUsers = users;
        notifyDataSetChanged();
    }

    class PendingViewHolder extends RecyclerView.ViewHolder {
        TextView pendingText;
        Button acceptBtn, declineBtn;

        public PendingViewHolder(@NonNull View itemView) {
            super(itemView);
            pendingText = itemView.findViewById(R.id.pendingFriendText);
            acceptBtn = itemView.findViewById(R.id.acceptButton);
            declineBtn = itemView.findViewById(R.id.declineButton);
        }
    }
}
