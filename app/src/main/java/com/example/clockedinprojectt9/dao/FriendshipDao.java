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

    @Query("SELECT * FROM users WHERE user_id IN (" +
            "SELECT user_id_2 FROM friendships WHERE user_id_1 = :userId AND status = 'ACCEPTED' " +
            "UNION " +
            "SELECT user_id_1 FROM friendships WHERE user_id_2 = :userId AND status = 'ACCEPTED')")
    LiveData<List<User>> getFriends(long userId);

    // Get users who sent a request to the current user
    @Query("SELECT * FROM users WHERE user_id IN (" +
            "SELECT sender_id FROM friendships WHERE " +
            "((user_id_1 = :userId AND user_id_1 != sender_id) OR " +
            "(user_id_2 = :userId AND user_id_2 != sender_id)) " +
            "AND status = 'PENDING')")
    LiveData<List<User>> getIncomingRequestUsers(long userId);

    // Get users to whom the current user sent a request
    @Query("SELECT * FROM users WHERE user_id IN (" +
            "SELECT CASE WHEN user_id_1 = :userId THEN user_id_2 ELSE user_id_1 END " +
            "FROM friendships WHERE sender_id = :userId AND status = 'PENDING')")
    LiveData<List<User>> getOutgoingRequestUsers(long userId);

    @Query("SELECT * FROM friendships WHERE (user_id_1 = :u1 AND user_id_2 = :u2) OR (user_id_1 = :u2 AND user_id_2 = :u1) LIMIT 1")
    Friendship getFriendship(long u1, long u2);
}
