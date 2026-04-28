package com.example.clockedinprojectt9.models;

import androidx.room.Embedded;
import androidx.room.Relation;

public class EventView {

    @Embedded
    private Event event;

    @Relation(
            parentColumn = "creator_user_id",
            entityColumn = "user_id"
    )
    private User creator;

    public EventView(Event event, User creator) {
        this.event = event;
        this.creator = creator;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }
}