package com.example.clockedinprojectt9.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "messages",
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "sender_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "user_id",
                        childColumns = "receiver_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Event.class,
                        parentColumns = "event_id",
                        childColumns = "event_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("sender_id"),
                @Index("receiver_id"),
                @Index("event_id")
        }
)
public class Message {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "message_id")
    private long messageId;

    @ColumnInfo(name = "sender_id")
    private long senderId;

    @ColumnInfo(name = "receiver_id")
    private Long receiverId; // Null if it's a group message

    @ColumnInfo(name = "event_id")
    private Long eventId; // Null if not a group message

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    public Message(long senderId, Long receiverId, Long eventId, String content, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.eventId = eventId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
