package com.example.clockedinprojectt9.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.clockedinprojectt9.models.Event;

import java.util.List;

@Dao
public interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Event event);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);

    @Query("SELECT * FROM events WHERE event_id = :id")
    Event getEventById(long id);

    @Query("SELECT * FROM events ORDER BY start_time ASC")
    LiveData<List<Event>> getAllEvents();

    @Query("SELECT * FROM events WHERE creator_user_id = :userId")
    LiveData<List<Event>> getEventsByCreator(long userId);

    @Query("SELECT * FROM events WHERE is_canceled = 0 AND end_time > :currentTime ORDER BY start_time ASC")
    LiveData<List<Event>> getUpcomingEvents(long currentTime);

    @Query("SELECT e.* FROM events e INNER JOIN rsvps r ON e.event_id = r.event_id WHERE r.user_id = :userId AND e.end_time > :currentTime ORDER BY e.start_time ASC")
    LiveData<List<Event>> getAttendingEvents(long userId, long currentTime);
}
