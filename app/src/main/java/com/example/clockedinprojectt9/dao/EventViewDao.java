package com.example.clockedinprojectt9.dao;

import com.example.clockedinprojectt9.models.EventView;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface EventViewDao {

    @Transaction
    @Query("SELECT * FROM events")
    List<EventView> getAllEventViews();

    @Transaction
    @Query("SELECT * FROM events WHERE event_id = :eventId")
    EventView getEventViewById(long eventId);
}