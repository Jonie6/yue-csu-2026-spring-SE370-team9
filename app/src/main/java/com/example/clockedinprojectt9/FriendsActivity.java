package com.example.clockedinprojectt9;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clockedinprojectt9.adapters.FriendAdapter;
import com.example.clockedinprojectt9.adapters.PendingRequestAdapter;
import com.example.clockedinprojectt9.db.AppDataBase;
import com.example.clockedinprojectt9.models.Friendship;
import com.example.clockedinprojectt9.models.User;
import com.example.clockedinprojectt9.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FriendsActivity extends AppCompatActivity {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friends);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AppDataBase db = AppDataBase.getDatabase(this);
        SessionManager sessionManager = new SessionManager(this);
        long currentUserId = sessionManager.getUserId();

        // 2. Add Friend by Username logic
        EditText friendIdInput = findViewById(R.id.friendIdInput);
        friendIdInput.setHint("Enter Username");
        friendIdInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        Button sendRequestButton = findViewById(R.id.sendRequestButton);

        sendRequestButton.setOnClickListener(v -> {
            String targetUsername = friendIdInput.getText().toString().trim();
            if (!targetUsername.isEmpty()) {
                executorService.execute(() -> {
                    // Look up user by username
                    User targetUser = db.userDao().getUserByUsername(targetUsername);
                    
                    if (targetUser == null) {
                        runOnUiThread(() -> Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    long targetUserId = targetUser.getUserId();

                    if (targetUserId == currentUserId) {
                        runOnUiThread(() -> Toast.makeText(this, "You cannot add yourself", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    
                    // Check if already friends or request pending
                    Friendship existing = db.friendshipDao().getFriendship(currentUserId, targetUserId);
                    if (existing != null) {
                        runOnUiThread(() -> Toast.makeText(this, "Already friends or request pending", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    // Create friendship with current user as SENDER
                    Friendship request = new Friendship(currentUserId, targetUserId, currentUserId, "PENDING", System.currentTimeMillis());
                    db.friendshipDao().sendFriendRequest(request);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Request Sent to " + targetUsername + "!", Toast.LENGTH_SHORT).show();
                        friendIdInput.setText("");
                    });
                });
            }
        });

        // 3. Pending Requests List (Incoming only)
        RecyclerView pendingRecyclerView = findViewById(R.id.pendingRecyclerView);
        pendingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
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

            @Override
            public void onCancel(User receiver) {
                executorService.execute(() -> {
                    Friendship f = db.friendshipDao().getFriendship(currentUserId, receiver.getUserId());
                    if (f != null) {
                        db.friendshipDao().removeFriendship(f);
                    }
                });
            }
        });
        pendingRecyclerView.setAdapter(pendingAdapter);

        // 4. Friends List
        RecyclerView friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        FriendAdapter friendsAdapter = new FriendAdapter(new FriendAdapter.OnFriendClickListener() {
            @Override
            public void onRemoveClick(User friend) {
                executorService.execute(() -> {
                    Friendship f = db.friendshipDao().getFriendship(currentUserId, friend.getUserId());
                    if (f != null) {
                        db.friendshipDao().removeFriendship(f);
                        // Remove RSVPs to "Friends Only" events since they are no longer friends
                        db.rsvpDao().deleteFriendsOnlyRsvps(currentUserId, friend.getUserId());
                        db.rsvpDao().deleteFriendsOnlyRsvps(friend.getUserId(), currentUserId);

                        runOnUiThread(() -> Toast.makeText(FriendsActivity.this, "Friend removed", Toast.LENGTH_SHORT).show());
                    }
                });
            }

            @Override
            public void onItemClick(User friend) {
                android.content.Intent intent = new android.content.Intent(FriendsActivity.this, ChatActivity.class);
                intent.putExtra("receiver_id", friend.getUserId());
                startActivity(intent);
            }
        });
        friendsRecyclerView.setAdapter(friendsAdapter);

        // 5. Observe Database Data
        if (currentUserId != -1) {
            // Observe actual friends
            db.friendshipDao().getFriends(currentUserId).observe(this, friends -> {
                if (friends != null) friendsAdapter.setFriends(friends);
            });

            // Observe ONLY incoming pending requests (where I am NOT the sender)
            db.friendshipDao().getIncomingRequestUsers(currentUserId).observe(this, requests -> {
                if (requests != null) pendingAdapter.setIncomingRequests(requests);
            });
        }
    }
}
