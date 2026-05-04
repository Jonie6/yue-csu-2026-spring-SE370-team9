package com.example.clockedinprojectt9.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.clockedinprojectt9.models.RSVP;

import java.util.List;

@Dao
public interface RSVPDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(RSVP rsvp);

    @Update
    void update(RSVP rsvp);

    @Delete
    void delete(RSVP rsvp);

    @Query("SELECT * FROM rsvps WHERE rsvp_id = :id")
    RSVP getById(long id);

    @Query("SELECT * FROM rsvps WHERE event_id = :eventId")
    LiveData<List<RSVP>> getRsvpsForEvent(long eventId);

    @Query("SELECT * FROM rsvps WHERE user_id = :userId")
    LiveData<List<RSVP>> getRsvpsForUser(long userId);

    @Query("SELECT * FROM rsvps WHERE event_id = :eventId AND user_id = :userId LIMIT 1")
    RSVP getRsvpForUserAndEvent(long userId, long eventId);

    @Query("DELETE FROM rsvps WHERE event_id = :eventId AND user_id = :userId")
    void deleteRsvp(long userId, long eventId);

    @Query("DELETE FROM rsvps WHERE user_id = :userId AND event_id IN (SELECT event_id FROM events WHERE creator_user_id = :creatorId AND visibility = 'Friends Only')")
    void deleteFriendsOnlyRsvps(long userId, long creatorId);
}
