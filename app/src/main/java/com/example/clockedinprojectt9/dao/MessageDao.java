package com.example.clockedinprojectt9.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.clockedinprojectt9.models.Message;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert
    long insert(Message message);

    @Delete
    void delete(Message message);

    // Selects message history between two users and sorts by time sent
    @Query("SELECT * FROM messages WHERE " +
           "(sender_id = :user1Id AND receiver_id = :user2Id) OR " +
           "(sender_id = :user2Id AND receiver_id = :user1Id) " +
           "ORDER BY timestamp ASC")
    LiveData<List<Message>> getChatHistory(long user1Id, long user2Id);

    @Query("SELECT * FROM messages WHERE event_id = :eventId ORDER BY timestamp ASC")
    LiveData<List<Message>> getGroupChatHistory(long eventId);

    @Query("SELECT * FROM messages WHERE (sender_id = :userId OR receiver_id = :userId OR event_id IN (SELECT event_id FROM rsvps WHERE user_id = :userId)) ORDER BY timestamp DESC")
    LiveData<List<Message>> getAllRelevantMessages(long userId);

}
