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
        // Retrieve ID of person you are chatting with
        receiverId = getIntent().getLongExtra("receiver_id", -1);

        //Checks if user exists
        if (receiverId == -1) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
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

        // Load receiver's name (Background thread for DB method -> UiThread for update)
        executorService.execute(() -> {
            User receiver = db.userDao().getUserById(receiverId);
            if (receiver != null) {
                runOnUiThread(() -> chatPartnerName.setText(receiver.getDisplayName()));
            }
        });

        // Setup RecyclerView
        messageAdapter = new MessageAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Show latest messages at the bottom
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(messageAdapter);

        // Setup Database Observation
        db.messageDao().getChatHistory(currentUserId, receiverId).observe(this, messages -> {
            messageAdapter.setMessages(messages);
            // Auto-scroll to bottom when new messages arrive
            if (messages != null && !messages.isEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        });

        // Send Message Logic
        sendButton.setOnClickListener(v -> {
            String content = messageInput.getText().toString().trim();
            if (!content.isEmpty()) {
                long timestamp = System.currentTimeMillis();
                Message message = new Message(currentUserId, receiverId, content, timestamp);
                executorService.execute(() -> {
                    db.messageDao().insert(message);
                    
                });
                messageInput.setText("");
            }
        });
    }
}
