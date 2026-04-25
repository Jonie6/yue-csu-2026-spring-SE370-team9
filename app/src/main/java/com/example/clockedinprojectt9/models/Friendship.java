package com.example.clockedinprojectt9.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "friendships",
        primaryKeys = {"user_id_1", "user_id_2"},
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "user_id_1", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "user_id_2", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("user_id_2")} // user_id_1 is already indexed as part of Primary Key
)

public class Friendship {
    @ColumnInfo(name = "user_id_1")
    public long userId1;

    @ColumnInfo(name = "user_id_2")
    public long userId2;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    public Friendship(long userId1, long userId2, String status, long createdAt) {
        //logic to ensure user 1 < user 2 to prevent 1,2 and 2,1 existing at once
        if (userId1 < userId2) {
            this.userId1 = userId1;
            this.userId2 = userId2;
        } else {
            this.userId1 = userId2;
            this.userId2 = userId1;
        }
        this.status = status;
        this.createdAt = createdAt;
    }
}


