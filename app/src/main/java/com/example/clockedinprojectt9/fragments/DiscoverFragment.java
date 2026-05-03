package com.example.clockedinprojectt9.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clockedinprojectt9.adapters.EventAdapter;
import com.example.clockedinprojectt9.databinding.FragmentDiscoverBinding;
import com.example.clockedinprojectt9.db.AppDataBase;
import com.example.clockedinprojectt9.models.Event;
import com.example.clockedinprojectt9.models.RSVP;
import com.example.clockedinprojectt9.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscoverFragment extends Fragment implements EventAdapter.OnEventClickListener {

    private FragmentDiscoverBinding binding;
    private EventAdapter adapter;
    private AppDataBase db;
    private List<Event> allEvents = new ArrayList<>();
    private List<Long> attendingEventIds = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDataBase.getDatabase(requireContext());
        adapter = new EventAdapter(this);
        adapter.setCurrentUserId(new SessionManager(requireContext()).getUserId());
        binding.discoverRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.discoverRecycler.setAdapter(adapter);

        loadEvents();
        setupSearch();
    }

    private void loadEvents() {
        long currentUserId = new SessionManager(requireContext()).getUserId();
        
        // Observe RSVPs first or concurrently
        db.rsvpDao().getRsvpsForUser(currentUserId).observe(getViewLifecycleOwner(), rsvps -> {
            attendingEventIds.clear();
            if (rsvps != null) {
                for (RSVP r : rsvps) {
                    attendingEventIds.add(r.getEventId());
                }
            }
            updateAdapter();
        });

        db.eventDao().getUpcomingEvents(System.currentTimeMillis()).observe(getViewLifecycleOwner(), events -> {
            if (events != null) {
                List<Event> othersEvents = new ArrayList<>();
                for (Event e : events) {
                    if (e.getCreatorUserId() != currentUserId) {
                        othersEvents.add(e);
                    }
                }
                allEvents = othersEvents;
                updateAdapter();
            }
        });
    }

    private void updateAdapter() {
        adapter.setEvents(allEvents, attendingEventIds);
    }

    private void setupSearch() {
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterEvents(String query) {
        List<Event> filteredList = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                event.getLocation().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(event);
            }
        }
        adapter.setEvents(filteredList, attendingEventIds);
    }

    @Override
    public void onActionClick(Event event) {
        long userId = new SessionManager(requireContext()).getUserId();
        long now = System.currentTimeMillis();

        executorService.execute(() -> {
            if (attendingEventIds.contains(event.getEventId())) {
                // Undo RSVP
                db.rsvpDao().deleteRsvp(userId, event.getEventId());
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(requireContext(), "RSVP removed for " + event.getTitle(), Toast.LENGTH_SHORT).show()
                );
            } else {
                // RSVP
                RSVP rsvp = new RSVP(event.getEventId(), userId, "Interested", now, now);
                db.rsvpDao().insert(rsvp);
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(requireContext(), "RSVP'd to " + event.getTitle(), Toast.LENGTH_SHORT).show()
                );
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
