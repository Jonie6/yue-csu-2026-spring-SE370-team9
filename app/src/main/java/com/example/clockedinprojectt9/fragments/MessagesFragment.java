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
import com.example.clockedinprojectt9.utils.SessionManager;

public class MessagesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = new SessionManager(requireContext());
        long currentUserId = sessionManager.getUserId();

        if (currentUserId == -1) {
            return;
        }

        RecyclerView recyclerView = view.findViewById(R.id.chatListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ChatListAdapter adapter = new ChatListAdapter(user -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("receiver_id", user.getUserId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Observe friends to automatically create the chat list
        AppDataBase.getDatabase(requireContext()).friendshipDao().getFriends(currentUserId).observe(getViewLifecycleOwner(), friends -> {
            if (friends != null) {
                adapter.setUsers(friends);
            }
        });
    }
}
