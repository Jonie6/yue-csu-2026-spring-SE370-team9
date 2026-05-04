package com.example.clockedinprojectt9.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clockedinprojectt9.ChatActivity;
import com.example.clockedinprojectt9.R;
import com.example.clockedinprojectt9.adapters.ChatListAdapter;
import com.example.clockedinprojectt9.db.AppDataBase;
import com.example.clockedinprojectt9.models.ChatSummary;
import com.example.clockedinprojectt9.models.Event;
import com.example.clockedinprojectt9.models.Message;
import com.example.clockedinprojectt9.models.User;
import com.example.clockedinprojectt9.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesFragment extends Fragment {

    private ChatListAdapter adapter;
    private AppDataBase db;
    private long currentUserId;

    private List<User> friends = new ArrayList<>();
    private List<Event> attendingEvents = new ArrayList<>();
    private List<Message> allMessages = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDataBase.getDatabase(requireContext());
        SessionManager sessionManager = new SessionManager(requireContext());
        currentUserId = sessionManager.getUserId();

        if (currentUserId == -1) {
            return;
        }

        RecyclerView recyclerView = view.findViewById(R.id.chatListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ChatListAdapter(new ChatListAdapter.OnChatClickListener() {
            @Override
            public void onUserChatClick(User user) {
                Intent intent = new Intent(requireContext(), ChatActivity.class);
                intent.putExtra("receiver_id", user.getUserId());
                startActivity(intent);
            }

            @Override
            public void onEventChatClick(Event event) {
                Intent intent = new Intent(requireContext(), ChatActivity.class);
                intent.putExtra("event_id", event.getEventId());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        observeData();
    }

    private void observeData() {
        db.friendshipDao().getFriends(currentUserId).observe(getViewLifecycleOwner(), friends -> {
            this.friends = friends != null ? friends : new ArrayList<>();
            updateChatList();
        });

        db.eventDao().getParticipatingEvents(currentUserId, 0).observe(getViewLifecycleOwner(), events -> {
            this.attendingEvents = events != null ? events : new ArrayList<>();
            updateChatList();
        });

        db.messageDao().getAllRelevantMessages(currentUserId).observe(getViewLifecycleOwner(), messages -> {
            this.allMessages = messages != null ? messages : new ArrayList<>();
            updateChatList();
        });
    }

    private void updateChatList() {
        Map<String, ChatSummary> chatMap = new HashMap<>();

        // 1. Add all friends (default state if no messages)
        for (User friend : friends) {
            String key = "user_" + friend.getUserId();
            chatMap.put(key, new ChatSummary(friend, "Start a conversation", 0));
        }

        // 2. Add all attending events
        for (Event event : attendingEvents) {
            String key = "event_" + event.getEventId();
            chatMap.put(key, new ChatSummary(event, "Group chat for " + event.getTitle(), 0));
        }

        // 3. Update with actual latest messages
        for (Message msg : allMessages) {
            String key = null;
            Object target = null;

            if (msg.getEventId() != null && msg.getEventId() != 0) {
                key = "event_" + msg.getEventId();
                for (Event e : attendingEvents) {
                    if (e.getEventId() == msg.getEventId()) {
                        target = e;
                        break;
                    }
                }
            } else {
                long peerId = msg.getSenderId() == currentUserId ? (msg.getReceiverId() != null ? msg.getReceiverId() : 0) : msg.getSenderId();
                if (peerId != 0) {
                    key = "user_" + peerId;
                    for (User u : friends) {
                        if (u.getUserId() == peerId) {
                            target = u;
                            break;
                        }
                    }
                }
            }

            if (key != null && target != null) {
                ChatSummary existing = chatMap.get(key);
                if (existing == null || existing.getLastTimestamp() == 0) {
                    chatMap.put(key, new ChatSummary(target, msg.getContent(), msg.getTimestamp()));
                }
            }
        }

        List<ChatSummary> sortedList = new ArrayList<>(chatMap.values());
        Collections.sort(sortedList, (a, b) -> Long.compare(b.getLastTimestamp(), a.getLastTimestamp()));

        adapter.setSummaries(sortedList);
    }
}
