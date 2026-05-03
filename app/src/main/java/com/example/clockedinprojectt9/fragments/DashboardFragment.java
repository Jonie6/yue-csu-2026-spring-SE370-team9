package com.example.clockedinprojectt9.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clockedinprojectt9.adapters.EventAdapter;
import com.example.clockedinprojectt9.databinding.FragmentDashboardBinding;
import com.example.clockedinprojectt9.db.AppDataBase;
import com.example.clockedinprojectt9.models.Event;
import com.example.clockedinprojectt9.models.RSVP;
import com.example.clockedinprojectt9.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment implements EventAdapter.OnEventClickListener {

    private FragmentDashboardBinding binding;
    private AppDataBase db;
    private SessionManager sessionManager;
    private EventAdapter activityAdapter;
    private List<Long> attendingEventIds = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDataBase.getDatabase(requireContext());
        sessionManager = new SessionManager(requireContext());

        binding.activityFeedRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        activityAdapter = new EventAdapter(this);
        activityAdapter.setCurrentUserId(sessionManager.getUserId());
        binding.activityFeedRecycler.setAdapter(activityAdapter);

        loadNextUp();
        loadActivityFeed();
    }

    private void loadNextUp() {
        long userId = sessionManager.getUserId();
        long currentTime = System.currentTimeMillis();
        db.eventDao().getAttendingEvents(userId, currentTime).observe(getViewLifecycleOwner(), events -> {
            if (events != null && !events.isEmpty()) {
                Event nextEvent = events.get(0);
                binding.nextUpTitle.setText(nextEvent.getTitle());
                
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
                String timeStr = sdf.format(new Date(nextEvent.getStartTime()));
                binding.nextUpTime.setText(timeStr);
                
                binding.nextUpLocation.setText(nextEvent.getLocation());
            } else {
                binding.nextUpTitle.setText("No upcoming events");
                binding.nextUpTime.setText("Check the Discover tab to find events");
                binding.nextUpLocation.setText("");
            }
        });
    }

    private void loadActivityFeed() {
        long currentUserId = sessionManager.getUserId();

        db.rsvpDao().getRsvpsForUser(currentUserId).observe(getViewLifecycleOwner(), rsvps -> {
            attendingEventIds.clear();
            if (rsvps != null) {
                for (RSVP r : rsvps) {
                    attendingEventIds.add(r.getEventId());
                }
            }
            refreshFeed();
        });

        db.eventDao().getUpcomingEvents(System.currentTimeMillis()).observe(getViewLifecycleOwner(), events -> {
            allActivityEvents = events != null ? events : new ArrayList<>();
            refreshFeed();
        });
    }

    private List<Event> allActivityEvents = new ArrayList<>();

    private void refreshFeed() {
        activityAdapter.setEvents(allActivityEvents, attendingEventIds);
    }

    @Override
    public void onActionClick(Event event) {
        long userId = sessionManager.getUserId();
        long now = System.currentTimeMillis();

        executorService.execute(() -> {
            if (attendingEventIds.contains(event.getEventId())) {
                db.rsvpDao().deleteRsvp(userId, event.getEventId());
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "unRSVP'd from " + event.getTitle(), Toast.LENGTH_SHORT).show();
                    com.example.clockedinprojectt9.utils.NotificationUtils.showNotification(requireContext(), 
                        "Group Chat Removed", "You've been removed from the " + event.getTitle() + " group chat.");
                });
            } else {
                RSVP rsvp = new RSVP(event.getEventId(), userId, "Interested", now, now);
                db.rsvpDao().insert(rsvp);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "RSVP'd to " + event.getTitle(), Toast.LENGTH_SHORT).show();
                    com.example.clockedinprojectt9.utils.NotificationUtils.showNotification(requireContext(), 
                        "Added to Group Chat", "You've been added to the " + event.getTitle() + " group chat!");
                });
            }
        });
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
