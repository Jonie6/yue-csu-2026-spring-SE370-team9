package com.example.clockedinprojectt9.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clockedinprojectt9.R;
import com.example.clockedinprojectt9.models.User;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private List<User> friends = new ArrayList<>();
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onRemoveClick(User friend);
        void onItemClick(User friend);
    }

    public FriendAdapter(OnFriendClickListener listener) {
        this.listener = listener;
    }

    public FriendAdapter() {
        // Default constructor if needed
    }
    
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User currentFriend = friends.get(position);
        holder.nameTextView.setText(currentFriend.getDisplayName());
        
        // Set a default profile picture using a sample avatar resource
        holder.profileImageView.setImageResource(R.drawable.ic_account_box);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentFriend);
            }
        });

        holder.menuButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.friend_item_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_remove_friend) {
                    if (listener != null) {
                        listener.onRemoveClick(currentFriend);
                    }
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
        notifyDataSetChanged();
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private ImageView profileImageView;
        private ImageButton menuButton;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.friendNameText);
            profileImageView = itemView.findViewById(R.id.imageView);
            menuButton = itemView.findViewById(R.id.menuButtonFriends);
        }
    }
}
