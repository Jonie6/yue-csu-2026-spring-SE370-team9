package com.example.clockedinprojectt9.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clockedinprojectt9.ChatActivity;
import com.example.clockedinprojectt9.R;
import com.example.clockedinprojectt9.adapters.FriendAdapter;
import com.example.clockedinprojectt9.adapters.PendingRequestAdapter;
import com.example.clockedinprojectt9.db.AppDataBase;
import com.example.clockedinprojectt9.models.Friendship;
import com.example.clockedinprojectt9.models.User;
import com.example.clockedinprojectt9.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FriendsFragment extends Fragment {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppDataBase db = AppDataBase.getDatabase(requireContext());
        SessionManager sessionManager = new SessionManager(requireContext());
        long currentUserId = sessionManager.getUserId();

        // 1. Add Friend by Username logic
        EditText friendIdInput = view.findViewById(R.id.friendIdInput);
        friendIdInput.setHint("Enter Username");
        friendIdInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        Button sendRequestButton = view.findViewById(R.id.sendRequestButton);

        sendRequestButton.setOnClickListener(v -> {
            String targetUsername = friendIdInput.getText().toString().trim();
            if (!targetUsername.isEmpty()) {
                executorService.execute(() -> {
                    // Look up user by username
                    User targetUser = db.userDao().getUserByUsername(targetUsername);
                    
                    if (targetUser == null) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    long targetUserId = targetUser.getUserId();

                    if (targetUserId == currentUserId) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "You cannot add yourself", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    
                    // Check if already friends or request pending
                    Friendship existing = db.friendshipDao().getFriendship(currentUserId, targetUserId);
                    if (existing != null) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Already friends or request pending", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    // Create friendship with current user as SENDER
                    Friendship request = new Friendship(currentUserId, targetUserId, currentUserId, "PENDING", System.currentTimeMillis());
                    db.friendshipDao().sendFriendRequest(request);
                    
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Request Sent to " + targetUsername + "!", Toast.LENGTH_SHORT).show();
                        friendIdInput.setText("");
                    });
                });
            }
        });

        // 3. Pending Requests List (Incoming only)
        RecyclerView pendingRecyclerView = view.findViewById(R.id.pendingRecyclerView);
        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        PendingRequestAdapter pendingAdapter = new PendingRequestAdapter(new PendingRequestAdapter.OnRequestListener() {
            @Override
            public void onAccept(User sender) {
                executorService.execute(() -> {
                    long u1 = Math.min(currentUserId, sender.getUserId());
                    long u2 = Math.max(currentUserId, sender.getUserId());
                    db.friendshipDao().acceptFriendRequest(u1, u2);
                });
            }

            @Override
            public void onDecline(User sender) {
                executorService.execute(() -> {
                    Friendship f = db.friendshipDao().getFriendship(currentUserId, sender.getUserId());
                    if (f != null) {
                        db.friendshipDao().removeFriendship(f);
                    }
                });
            }
        });
        pendingRecyclerView.setAdapter(pendingAdapter);

        // 4. Friends List
        RecyclerView friendsRecyclerView = view.findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        FriendAdapter friendsAdapter = new FriendAdapter(new FriendAdapter.OnFriendClickListener() {
            @Override
            public void onRemoveClick(User friend) {
                executorService.execute(() -> {
                    Friendship f = db.friendshipDao().getFriendship(currentUserId, friend.getUserId());
                    if (f != null) {
                        db.friendshipDao().removeFriendship(f);
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Friend removed", Toast.LENGTH_SHORT).show());
                    }
                });
            }

            @Override
            public void onItemClick(User friend) {
                android.content.Intent intent = new android.content.Intent(requireContext(), ChatActivity.class);
                intent.putExtra("receiver_id", friend.getUserId());
                startActivity(intent);
            }
        });
        friendsRecyclerView.setAdapter(friendsAdapter);

        // 5. Observe Database Data
        if (currentUserId != -1) {
            // Observe actual friends
            db.friendshipDao().getFriends(currentUserId).observe(getViewLifecycleOwner(), friends -> {
                if (friends != null) friendsAdapter.setFriends(friends);
            });

            // Observe ONLY incoming pending requests (where I am NOT the sender)
            db.friendshipDao().getIncomingRequestUsers(currentUserId).observe(getViewLifecycleOwner(), requests -> {
                if (requests != null) pendingAdapter.setRequests(requests);
            });
        }
    }
}
