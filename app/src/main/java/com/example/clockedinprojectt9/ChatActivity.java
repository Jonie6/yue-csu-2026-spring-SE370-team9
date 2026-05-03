package com.example.clockedinprojectt9;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clockedinprojectt9.adapters.MessageAdapter;
import com.example.clockedinprojectt9.db.AppDataBase;
import com.example.clockedinprojectt9.models.Message;
import com.example.clockedinprojectt9.models.User;
import com.example.clockedinprojectt9.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private MessageAdapter messageAdapter;
    private long currentUserId;
    private long receiverId;
    private long eventId;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AppDataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = AppDataBase.getDatabase(this);

        // Get IDs from Session
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId();
        
        receiverId = getIntent().getLongExtra("receiver_id", -1);
        eventId = getIntent().getLongExtra("event_id", 0);

        if (receiverId == -1 && eventId == 0) {
            Toast.makeText(this, "Error: Chat target not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup UI
        TextView chatPartnerName = findViewById(R.id.chatPartnerName);
        Button backButton = findViewById(R.id.backButton);
        RecyclerView recyclerView = findViewById(R.id.chatRecyclerView);
        EditText messageInput = findViewById(R.id.messageInput);
        Button sendButton = findViewById(R.id.sendButton);

        backButton.setOnClickListener(v -> finish());

        // Setup RecyclerView
        messageAdapter = new MessageAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); 
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);

        if (eventId != 0) {
            // Group Chat
            messageAdapter.setGroupChat(true);
            executorService.execute(() -> {
                com.example.clockedinprojectt9.models.Event event = db.eventDao().getEventById(eventId);
                if (event != null) {
                    runOnUiThread(() -> chatPartnerName.setText(event.getTitle() + " (Group)"));
                }
                
                // Load all users to show names in group chat
                java.util.List<User> allUsers = db.userDao().getAllUsersList();
                java.util.Map<Long, String> nameMap = new java.util.HashMap<>();
                for (User u : allUsers) {
                    nameMap.put(u.getUserId(), u.getDisplayName());
                }
                runOnUiThread(() -> messageAdapter.setUserNames(nameMap));
            });

            db.messageDao().getGroupChatHistory(eventId).observe(this, messages -> {
                messageAdapter.setMessages(messages);
                if (messages != null && !messages.isEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            });
        } else {
            // Private Chat
            executorService.execute(() -> {
                User receiver = db.userDao().getUserById(receiverId);
                if (receiver != null) {
                    runOnUiThread(() -> chatPartnerName.setText(receiver.getDisplayName()));
                }
            });

            db.messageDao().getChatHistory(currentUserId, receiverId).observe(this, messages -> {
                messageAdapter.setMessages(messages);
                if (messages != null && !messages.isEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            });
        }

        // Send Message Logic
        sendButton.setOnClickListener(v -> {
            String content = messageInput.getText().toString().trim();
            if (!content.isEmpty()) {
                long timestamp = System.currentTimeMillis();
                Long targetReceiverId = eventId != 0 ? null : receiverId;
                Long targetEventId = eventId != 0 ? eventId : null;
                Message message = new Message(currentUserId, targetReceiverId, targetEventId, content, timestamp);
                executorService.execute(() -> {
                    db.messageDao().insert(message);
                });
                messageInput.setText("");
            }
        });
    }
}
