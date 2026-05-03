package com.example.clockedinprojectt9.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clockedinprojectt9.CreateEventActivity;
import com.example.clockedinprojectt9.LoginActivity;
import com.example.clockedinprojectt9.adapters.EventAdapter;
import com.example.clockedinprojectt9.databinding.FragmentProfileBinding;
import com.example.clockedinprojectt9.db.AppDataBase;
import com.example.clockedinprojectt9.models.Event;
import com.example.clockedinprojectt9.models.User;
import com.example.clockedinprojectt9.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment implements EventAdapter.OnEventClickListener {

    private FragmentProfileBinding binding;
    private AppDataBase db;
    private SessionManager sessionManager;
    private EventAdapter myEventsAdapter;
    private EventAdapter attendingEventsAdapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDataBase.getDatabase(requireContext());
        sessionManager = new SessionManager(requireContext());

        binding.myEventsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        myEventsAdapter = new EventAdapter(this);
        myEventsAdapter.setCurrentUserId(sessionManager.getUserId());
        myEventsAdapter.setActionButtonText("Edit");
        binding.myEventsRecycler.setAdapter(myEventsAdapter);

        binding.attendingEventsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        attendingEventsAdapter = new EventAdapter(new EventAdapter.OnEventClickListener() {
            @Override
            public void onActionClick(Event event) {
                // unRSVP logic
                executorService.execute(() -> {
                    db.rsvpDao().deleteRsvp(sessionManager.getUserId(), event.getEventId());
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "unRSVP'd from " + event.getTitle(), Toast.LENGTH_SHORT).show();
                        com.example.clockedinprojectt9.utils.NotificationUtils.showNotification(requireContext(), 
                            "Group Chat Removed", "You've been removed from the " + event.getTitle() + " group chat.");
                    });
                });
            }

            @Override
            public void onMapClick(Event event) {
                ProfileFragment.this.onMapClick(event);
            }

            @Override
            public void onCancelClick(Event event) {
                // Not host of these events usually, but if they are, handle it
                ProfileFragment.this.onCancelClick(event);
            }
        });
        attendingEventsAdapter.setActionButtonText("unRSVP");
        binding.attendingEventsRecycler.setAdapter(attendingEventsAdapter);

        loadUserData();
        loadMyEvents();
        loadAttendingEvents();

        binding.btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }

    private void loadUserData() {
        long userId = sessionManager.getUserId();
        executorService.execute(() -> {
            User user = db.userDao().getUserById(userId);
            if (user != null) {
                requireActivity().runOnUiThread(() -> {
                    binding.profileUsername.setText(user.getUsername());
                    binding.profileEmail.setText(user.getEmail());
                });
            }
        });
    }

    private void loadMyEvents() {
        long userId = sessionManager.getUserId();
        db.eventDao().getEventsByCreator(userId).observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                myEventsAdapter.setEvents(events, null);
            }
        });
    }

    private void loadAttendingEvents() {
        long userId = sessionManager.getUserId();
        db.eventDao().getAttendingEvents(userId, 0).observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                attendingEventsAdapter.setEvents(events, null);
            }
        });
    }

    @Override
    public void onActionClick(Event event) {
        Intent intent = new Intent(requireContext(), CreateEventActivity.class);
        intent.putExtra(CreateEventActivity.EXTRA_EVENT_ID, event.getEventId());
        startActivity(intent);
    }

    @Override
    public void onMapClick(Event event) {
        String uri = "geo:0,0?q=" + android.net.Uri.encode(event.getLocation());
        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri)));
    }

    @Override
    public void onCancelClick(Event event) {
        executorService.execute(() -> {
            db.eventDao().delete(event);
            requireActivity().runOnUiThread(() -> 
                Toast.makeText(requireContext(), "Event canceled: " + event.getTitle(), Toast.LENGTH_SHORT).show()
            );
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
