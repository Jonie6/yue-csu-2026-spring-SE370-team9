package com.example.clockedinprojectt9.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clockedinprojectt9.databinding.ItemEventBinding;
import com.example.clockedinprojectt9.models.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private List<Long> attendingEventIds = new ArrayList<>();
    private final OnEventClickListener listener;
    private String actionButtonText = "RSVP";
    private long currentUserId = -1;

    public interface OnEventClickListener {
        void onActionClick(Event event);
        void onMapClick(Event event);
        void onCancelClick(Event event);
    }

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    public void setActionButtonText(String text) {
        this.actionButtonText = text;
    }

    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }

    public void setEvents(List<Event> events, List<Long> attendingEventIds) {
        this.events = events;
        this.attendingEventIds = attendingEventIds != null ? attendingEventIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventBinding binding = ItemEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        String text = actionButtonText;
        if (actionButtonText.equals("RSVP") && attendingEventIds.contains(event.getEventId())) {
            text = "Un-RSVP";
        }
        holder.bind(event, listener, text, currentUserId);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventBinding binding;

        public EventViewHolder(ItemEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Event event, OnEventClickListener listener, String actionText, long currentUserId) {
            binding.txtEventTitle.setText(event.getTitle());
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
            binding.txtEventTime.setText(sdf.format(new Date(event.getStartTime())));
            
            binding.txtEventLocation.setText(event.getLocation());
            
            binding.btnRsvp.setText(actionText);
            binding.btnRsvp.setOnClickListener(v -> listener.onActionClick(event));
            binding.btnViewMap.setOnClickListener(v -> listener.onMapClick(event));

            if (event.getCreatorUserId() == currentUserId) {
                binding.btnCancelEvent.setVisibility(android.view.View.VISIBLE);
                binding.btnCancelEvent.setOnClickListener(v -> listener.onCancelClick(event));
            } else {
                binding.btnCancelEvent.setVisibility(android.view.View.GONE);
            }
        }
    }
}
