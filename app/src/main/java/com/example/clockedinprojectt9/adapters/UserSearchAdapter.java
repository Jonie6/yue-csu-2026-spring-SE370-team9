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

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private final OnUserClickListener listener;
    private final OnAddClickListener addClickListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public interface OnAddClickListener {
        void onAddClick(User user);
    }

    public UserSearchAdapter(OnUserClickListener listener, OnAddClickListener addClickListener) {
        this.listener = listener;
        this.addClickListener = addClickListener;
    }

    public void setUsers(List<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.usernameText.setText(user.getUsername());
        
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
        holder.sendRequestButton.setOnClickListener(v -> addClickListener.onAddClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        Button sendRequestButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            sendRequestButton = itemView.findViewById(R.id.sendRequestButton);
        }
    }
}
