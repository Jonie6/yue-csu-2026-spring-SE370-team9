package com.example.clockedinprojectt9.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "rsvps",
        foreignKeys = {
                @ForeignKey(
                        entity = Event.class,
                        parentColumns = "event_id",
                        childColumns = "event_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("event_id"),
                @Index("user_id"),
                @Index(value = {"event_id", "user_id"}, unique = true)
        }
)
public class RSVP {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rsvp_id")
    private long rsvpId;

    @ColumnInfo(name = "event_id")
    private long eventId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public RSVP(long eventId, long userId, String status, long createdAt, long updatedAt) {
        this.eventId = eventId;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getRsvpId() {
        return rsvpId;
    }

    public void setRsvpId(long rsvpId) {
        this.rsvpId = rsvpId;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
