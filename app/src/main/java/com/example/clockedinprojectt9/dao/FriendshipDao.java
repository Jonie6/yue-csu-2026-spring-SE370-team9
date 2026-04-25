package com.example.clockedinprojectt9.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.clockedinprojectt9.models.Friendship;
import com.example.clockedinprojectt9.models.User;
import java.util.List;

@Dao
public interface FriendshipDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void sendFriendRequest(Friendship friendship);

    @Query("UPDATE friendships SET status = 'ACCEPTED' WHERE user_id_1 = :u1 AND user_id_2 = :u2")
    void acceptFriendRequest(long u1, long u2);

    @Delete
    void removeFriendship(Friendship friendship);

    // This query finds all Users who are friends with the given userId
    @Query("SELECT * FROM users WHERE user_id IN (" +
            "SELECT user_id_2 FROM friendships WHERE user_id_1 = :userId AND status = 'ACCEPTED' " +
            "UNION " +
            "SELECT user_id_1 FROM friendships WHERE user_id_2 = :userId AND status = 'ACCEPTED')")
    LiveData<List<User>> getFriends(long userId);

    @Query("SELECT * FROM friendships WHERE (user_id_1 = :userId OR user_id_2 = :userId) AND status = 'PENDING'")
    LiveData<List<Friendship>> getPendingRequests(long userId);
}