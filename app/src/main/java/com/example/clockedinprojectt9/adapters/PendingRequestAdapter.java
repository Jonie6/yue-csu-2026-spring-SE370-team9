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

    private List<User> incomingRequests = new ArrayList<>();
    private List<User> outgoingRequests = new ArrayList<>();
    private OnRequestListener listener;

    public interface OnRequestListener {
        void onAccept(User sender);
        void onDecline(User sender);
        void onCancel(User receiver);
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
        if (position < incomingRequests.size()) {
            User sender = incomingRequests.get(position);
            holder.pendingText.setText("Received from: " + sender.getUsername());
            holder.acceptBtn.setVisibility(View.VISIBLE);
            holder.declineBtn.setText("Decline");
            holder.acceptBtn.setOnClickListener(v -> listener.onAccept(sender));
            holder.declineBtn.setOnClickListener(v -> listener.onDecline(sender));
        } else {
            User receiver = outgoingRequests.get(position - incomingRequests.size());
            holder.pendingText.setText("Sent to: " + receiver.getUsername());
            holder.acceptBtn.setVisibility(View.GONE);
            holder.declineBtn.setText("Cancel");
            holder.declineBtn.setOnClickListener(v -> listener.onCancel(receiver));
        }
    }

    @Override
    public int getItemCount() {
        return incomingRequests.size() + outgoingRequests.size();
    }

    public void setIncomingRequests(List<User> users) {
        this.incomingRequests = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOutgoingRequests(List<User> users) {
        this.outgoingRequests = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class PendingViewHolder extends RecyclerView.ViewHolder {
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
