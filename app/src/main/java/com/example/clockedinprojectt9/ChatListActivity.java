package com.example.clockedinprojectt9;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clockedinprojectt9.adapters.ChatListAdapter;
import com.example.clockedinprojectt9.db.AppDataBase;
import com.example.clockedinprojectt9.utils.SessionManager;

public class ChatListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        SessionManager sessionManager = new SessionManager(this);
        long currentUserId = sessionManager.getUserId();

        if (currentUserId == -1) {
            finish();
            return;
        }

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.chatListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ChatListAdapter adapter = new ChatListAdapter(user -> {
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("receiver_id", user.getUserId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Observe friends to automatically create the chat list
        AppDataBase.getDatabase(this).friendshipDao().getFriends(currentUserId).observe(this, friends -> {
            if (friends != null) {
                adapter.setUsers(friends);
            }
        });
    }
}
