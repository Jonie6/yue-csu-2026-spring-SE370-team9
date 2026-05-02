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

}
